const { logger } = require('../../winstonLogger');
const { sign, verify } = require('../../jwt');
const socketIo = require('socket.io');
const db = require('../../database');
const scylla = require('../../scylladb');
const { getUrlOfCDN } = require('../../WebUtils');

async function init(wsManager) {
    const cdnURL = await getUrlOfCDN();

    wsManager.getMessage(async (data) => {
        const author = data.userId;
        const message = data.message;

        if (message === "getRooms") {
            const roomsQuery = `SELECT * FROM rooms WHERE author = ?;`;
            const roomsResult = await scylla.client.execute(roomsQuery, [author], { prepare: true });

            const chatsQuery = `SELECT * FROM rooms WHERE to_user = ?;`;
            const chatsResult = await scylla.client.execute(chatsQuery, [author], { prepare: true });

            const result = [...chatsResult.rows, ...roomsResult.rows];

            const transformKeysToString = (obj) => {
                let transformedStr = '';
                for (const key in obj) {
                    if (obj.hasOwnProperty(key)) {
                        const newKey = key.replace(/_([a-z])/g, (match, letter) => letter.toUpperCase());
                        transformedStr += `${newKey}: ${obj[key]} | `;
                    }
                }
                return transformedStr.slice(0, -3);
            };

            const transformedResult = result.map(row => transformKeysToString(row)).join('\n');

            wsManager.sendMessageToUser(author, transformedResult);

            logger.info(`Kullanıcı ${author} dm odalarını talep etti.`);
        }

        if (message.startsWith("2->")) {
            const token = message.split("->")[1];

            const verifyID = (await verify(token));
            const questID = verifyID.payload.session;

            const quest = await db.getRecord('quests', { id: questID });

            const id = quest[0].user_id;
            const user = await db.getRecord("users", { id });

            const isHospital = await db.getRecord('hospitals', { author: id });

            var hospitalApply = 0;
            var hospitalName = "", hospitalApplyDate = "";

            if (isHospital.length > 0) {
                hospitalApply = 2;
            } else {
                const isHospitalApply = await db.getRecord('hospital_applies', { author: id });

                if (isHospitalApply.length > 0) {
                    hospitalApply = 1;

                    hospitalName = isHospitalApply[0].hospital_name;
                    hospitalApplyDate = isHospitalApply[0].created_at;
                }
            }

            if (user.length > 0) {
                const userA = {
                    id: (user[0].id).toString(),
                    name: user[0].name,
                    surname: user[0].surname,
                    pp: cdnURL + "/" + user[0].profile_picture,
                    username: user[0].username,
                    hospitalApply,
                    hospitalName,
                    hospitalApplyDate

                };

                wsManager.sendMessageToUser(author, "2->" + (JSON.stringify(userA)).toString());
            } else {
                wsManager.sendMessageToUser(author, "2");
            }
        }

        if (message.startsWith('3->')) {
            const token = message.split('->')[1];

            const verifyID = verify(token);
            const questID = verifyID.payload.session;

            const quest = await db.getRecord('quests', { id: questID });

            const toUserUid = message.split('|')[1];
            const timestamp = new Date(Date.now());
            const content = message.split('|')[2];

            const id = quest[0].user_id;
            const user = await db.getRecord("users", { id });
            const toUser = await db.getRecord("users", { id: toUserUid });

            if (!toUser.length > 0) {
                wsManager.sendMessageToUser(author, "2");
            } else {
                if (user.length > 0) {
                    wsManager.sendMessageToUser(toUserUid, `4->${id}|${Date.now()}|${content}`);
                } else {
                    wsManager.sendMessageToUser(author, "2");
                }
            }
        }

        if (message.startsWith('5->')) {
            const token = message.split('|')[0].replace('5->', '');
            const doctor_id = message.split('|')[1];
            const description = message.split('|')[2];

            const verifyID = verify(token);
            const questID = verifyID.payload.session;

            const quest = await db.getRecord('quests', { id: questID });
            const id = quest[0].user_id;

            const workingHours = {
                start: 9,   // Sabah 09:00
                end: 17,    // Akşam 17:00
                lunchStart: 12, // Öğle arası 12:00
                lunchEnd: 13,   // Öğle arası 13:00
                appointmentDuration: 1 // Randevu süresi (saat)
            };

            const unavailableDays = [0, 6];

            const existingAppointments = await db.getRecord('appointments', { doctor_id });

            const findAvailableSlot = () => {
                const now = new Date();
                let currentDate = new Date(now);
                currentDate.setDate(currentDate.getDate() + 2);
                currentDate.setHours(workingHours.start, 0, 0, 0);

                for (let day = 0; day < 30; day++) {
                    if (unavailableDays.includes(currentDate.getDay())) {
                        currentDate.setDate(currentDate.getDate() + 1);
                        currentDate.setHours(workingHours.start, 0, 0, 0);
                        continue;
                    }
                    for (let hour = workingHours.start; hour < workingHours.end; hour++) {
                        if (hour >= workingHours.lunchStart && hour < workingHours.lunchEnd) {
                            continue;
                        }

                        const slotStart = new Date(currentDate);
                        slotStart.setHours(hour, 0, 0, 0);

                        const slotEnd = new Date(slotStart);
                        slotEnd.setHours(hour + workingHours.appointmentDuration, 0, 0, 0);

                        const isSlotAvailable = !existingAppointments.some(app => {
                            const appDate = new Date(app.appointment_date.split('/').reverse().join('-'));
                            const appStart = new Date(appDate);
                            const [appHour, appMinute] = app.appointment_time.split(':').map(Number);
                            appStart.setHours(appHour, appMinute, 0, 0);

                            const appEnd = new Date(appStart);
                            appEnd.setHours(appHour + workingHours.appointmentDuration, 0, 0, 0);

                            return (slotStart < appEnd && slotEnd > appStart);
                        });

                        if (isSlotAvailable) {
                            return {
                                date: `${String(slotStart.getDate()).padStart(2, '0')}/${String(slotStart.getMonth() + 1).padStart(2, '0')}/${slotStart.getFullYear()}`,
                                time: `${String(slotStart.getHours()).padStart(2, '0')}:00`
                            };
                        }
                    }

                    currentDate.setDate(currentDate.getDate() + 1);
                    currentDate.setHours(workingHours.start, 0, 0, 0);
                }

                return null;
            };

            const availableSlot = findAvailableSlot();

            if (!availableSlot) {
                wsManager.sendMessageToUser(id, `5->${id}|${Date.now()}|0|{"error": "No available slots found"}`);
                return;
            }

            try {
                const apply_id = await db.createRecord('appointments', {
                    user_id: id,
                    created_at: new Date(Date.now()),
                    doctor_id,
                    appointment_date: availableSlot.date,
                    appointment_time: availableSlot.time,
                    description
                });

                const data = [{
                    isSuccessful: true,
                    appointment_date: availableSlot.date,
                    appointment_time: availableSlot.time,
                    appointment_id: apply_id
                }];

                wsManager.sendMessageToUser(id, `5->${id}|${Date.now()}|1|${JSON.stringify(data)}`);
            } catch (err) {
                console.log(err);
                wsManager.sendMessageToUser(id, `5->${id}|${Date.now()}|0|{"error": "Database error"}`);
            }
        }

        if (message.startsWith("6->")) {
            const m = message.replace("6->", "");

            const idPart = m.split("|")[0]; // request ID
            const token = m.split("|")[1];
            const table = m.split("|")[2];
            const m_data = m.split("|")[3];

            let data = {};
            const verifyID = verify(token);
            const questID = verifyID.payload.session;
            const quest = await db.getRecord("quests", { id: questID });
            const userId = quest[0].user_id;

            m_data.split(",").forEach(item => {
                const [col, content] = item.split("=");
                data[col] = content === "t-o-k-e-n" ? userId : content;
            });

            const c = await db.getRecord(table, data);

            wsManager.sendMessageToUser(userId, `6->|${idPart}|1|${JSON.stringify(c)}`);
        }

        if (message.startsWith('7->')) {
            // Veri ekleme
            const m = message.replace('7->', '');

            const token = m.split('|')[0];
            const table = m.split("|")[1];
            const m_data = m.split("|")[2];

            let data = {};

            m_data.split(',').forEach(item => {
                const [col, content] = item.split('=');
                data[col] = content;
            });

            const verifyID = verify(token);
            const questID = verifyID.payload.session;

            const quest = await db.getRecord('quests', { id: questID });
            const userId = quest[0].user_id;

            const id = await db.createRecord(table, data);

            if (id !== null) {
                wsManager.sendMessageToUser(userId, `7->${id}|${Date.now()}|1`);
            } else {
                wsManager.sendMessageToUser(userId, `7->${id}|${Date.now()}|0`);
            }
        }

        if (message.startsWith('8->')) {
            // Veri güncelleme
            const m = message.replace('8->', '');

            const token = m.split('|')[0];
            const table = m.split("|")[1];
            const key = m.split("|")[2];
            const m_data = m.split("|")[3];

            let data = {};

            m_data.split(',').forEach(item => {
                const [col, content] = item.split('=');
                data[col] = content;
            });

            const verifyID = verify(token);
            const questID = verifyID.payload.session;

            const quest = await db.getRecord('quests', { id: questID });
            const userId = quest[0].user_id;

            const id = await db.updateRecord(table, key, data);

            if (id !== null) {
                wsManager.sendMessageToUser(userId, `8->${id}|${Date.now()}|1`);
            } else {
                wsManager.sendMessageToUser(userId, `8->${id}|${Date.now()}|0`);
            }
        }

        if (message.startsWith('9->')) {
            // Veri silme
            const m = message.replace('9->', '');

            const token = m.split('|')[0];
            const table = m.split("|")[1];
            const key = m.split("|")[2];

            const verifyID = verify(token);
            const questID = verifyID.payload.session;

            const quest = await db.getRecord('quests', { id: questID });
            const userId = quest[0].user_id;

            const id = await db.deleteRecord(table, key);

            if (id !== null) {
                wsManager.sendMessageToUser(userId, `9->${id}|${Date.now()}|1`);
            } else {
                wsManager.sendMessageToUser(userId, `9->${id}|${Date.now()}|0`);
            }
        }
    });
}

module.exports = { init };