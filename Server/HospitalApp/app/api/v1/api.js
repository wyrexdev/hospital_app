const router = require('express').Router();

const db = require('../../../database');
const { isEmailDomainValid, getUsernameFromEmail, generateRandomPassword } = require('../../controllers/apiControllers');
const { hasNumber, hasSpecialCharacter, getIPAdress, checkLoginAttempts, incrementLoginAttempt, incrementRegisterAttempt, checkRegisterAttempts } = require('../../../Utils');
const { sign, verify } = require('../../../jwt');
const { verifyIdToken } = require('../../../google');

const multer = require('multer');
const path = require('path');

const mailUsers = require('../../../mail/users');
const mail = require('../../../mail/mail');
const verifyMail = require('../../../mail/verify');

const { hmacValue } = require('../../../hmac/hmac');

const fs = require('fs');

const { scanFileForVirus } = require('../../../ClamAV/clamav');

const tcVerify = require('../../../tc');

const { getUrlOfCDN } = require('../../../WebUtils');
const { rateLimitMiddleware } = require('../../../RateLimiter');

router.get('/', async (req, res) => {
    try {
        const start = Date.now();

        const duration = (Date.now() - start) + 1;

        res.json({ status: 'Active!', ms: duration + "ms" });
    } catch (err) {
        console.log(err);
    }
});


router.post('/login', async (req, res) => {
    const { email, password } = req.body;

    try {
        const ipConfig = getIPAdress(req);
        const ip = ipConfig.ipv4;

        if (!await checkLoginAttempts(ip)) {
            return res.json({ status: 429, message: 'Too many login attempts, try again later.' });
        }

        const start = Date.now();

        if (!email) {
            await incrementLoginAttempt(ip);

            return res.json({ status: 1002 });
        }

        if (!password) {
            return res.json({ status: 1003 });
        }

        const isValidEmail = await isEmailDomainValid(email);

        if (!isValidEmail) {
            await incrementLoginAttempt(ip);

            return res.json({ status: 1004 });
        }

        if ((password.length < 8)) {
            await incrementLoginAttempt(ip);

            return res.json({ status: 1005 });
        }

        if (!(hasSpecialCharacter(password))) {
            await incrementLoginAttempt(ip);

            return res.json({ status: 1006 });
        }

        if (!(hasNumber(password))) {
            await incrementLoginAttempt(ip);

            return res.json({ status: 1007 });
        }

        const userO = await db.getRecord('users', { email });

        if (!(userO.length > 0)) {
            await incrementLoginAttempt(ip);

            return res.json({ status: 1001 });
        }

        const duration = (Date.now() - start) + 1;

        if (userO !== null && userO !== undefined && userO.length > 0) {
            const userVerify = await verify(userO[0]?.password || "");

            if (userVerify.valid) {
                if (password === (userVerify.payload?.password || "")) {
                    await incrementLoginAttempt(ip, true);

                    await db.updateRecord('users', userO[0].id, { last_login_ip: ipConfig.ipv4 });

                    const requiredFields = ['tc', 'name', 'surname'];
                    let allCompleted = !requiredFields.some(field => !userO[0][field]);

                    allCompleted = allCompleted && (userO[0].insurance_type !== undefined) && (userO[0].gender !== undefined) &&
                        (userO[0].insurance_type >= 0) && (userO[0].gender >= 0);

                    const user = {};

                    const questID = await db.createRecord('quests', {
                        user_id: userO[0].id,
                        quest_time: new Date(Date.now()),
                        ip_adress: ip,
                        is_verify: 0
                    });

                    user.token = await sign({ session: questID || 0 });

                    const emailVerifyID = await db.createRecord('verify', {
                        date: new Date(Date.now()),
                        quest_id: questID,
                        ip,
                        expire: new Date(Date.now() + 60 * 60 * 1000),
                        is_used: false
                    });

                    await mail.sendMail(mailUsers.noreply, email, 'Hesabınızı Doğrulayın', verifyMail.getVerifyText(userO[0].username, emailVerifyID));

                    return res.json({ status: 200, user, verify: false, ms: duration + "ms" });
                } else {
                    await incrementLoginAttempt(ip);

                    return res.json({ status: 1011, ms: duration + "ms" });
                }
            } else {
                await incrementLoginAttempt(ip);

                return res.json({ status: 1013, ms: duration + "ms" });
            }
        } else {
            await incrementLoginAttempt(ip);

            return res.json({ status: 1001, ms: duration + "ms" });
        }
    } catch (err) {
        console.log(err);
        return res.json({ status: 502 });
    }
});

router.post('/register', async (req, res) => {
    const { email, username, password } = req.body;

    try {
        const ipConfig = getIPAdress(req);
        const ip = ipConfig.ipv4;

        if (!await checkRegisterAttempts(ip)) {
            return res.json({ status: 429, message: 'Too many register attempts, try again later.' });
        }

        const start = Date.now();

        if (!email) {
            return res.json({ status: 1002 });
        }

        if (!password) {
            return res.json({ status: 1003 });
        }

        if (!username) {
            return res.json({ status: 1008 });
        }

        const isValidEmail = await isEmailDomainValid(email);

        if (!isValidEmail) {
            incrementRegisterAttempt(ip);
            return res.json({ status: 1004 });
        }

        if ((password.length < 8)) {
            return res.json({ status: 1005 });
        }

        if (!(hasSpecialCharacter(password))) {
            return res.json({ status: 1006 });
        }

        if (!(hasNumber(password))) {
            return res.json({ status: 1007 });
        }

        const isUser = await db.getRecord('users', { email });
        const isUsername = await db.getRecord('users', { username });

        if (isUser.length > 0) {
            incrementRegisterAttempt(ip);
            return res.json({ status: 1009 });
        }

        if (isUsername.length > 0) {
            return res.json({ status: 1010 });
        }

        const today = new Date(Date.now());

        const id = await db.createRecord('users', {
            email,
            password: await sign({ password }),
            username,
            sign_date: today,
            last_login_ip: ip
        });

        const user = {};

        const questID = await db.createRecord('quests', {
            user_id: id,
            quest_time: new Date(Date.now()),
            ip_adress: ip,
            is_verify: 0
        });

        user.token = await sign({ session: questID || 0 });

        const duration = (Date.now() - start) + 1;

        const emailVerifyID = await db.createRecord('verify', {
            date: new Date(Date.now()),
            quest_id: questID,
            ip,
            expire: new Date(Date.now() + 60 * 60 * 1000),
            is_used: false
        });

        await mail.sendMail(mailUsers.noreply, email, 'Hesabınızı Doğrulayın', verifyMail.getVerifyText(username, emailVerifyID));

        incrementRegisterAttempt(ip);
        return res.json({ status: 200, user, verify: false, ms: duration + "ms" });
    } catch (err) {
        console.log(err);
    }
});

router.post('/user', async (req, res) => {
    const { id } = req.body;

    try {
        const start = Date.now();

        if (!id) return res.json({ status: 1014 });

        const verifyID = (await verify(id));

        if (!verifyID.valid) return res.json({ status: 1012 });

        const questID = verifyID.payload.session;

        const quest = await db.getRecord('quests', { id: questID });

        if (!quest.length > 0) return res.json({ status: 1012 });

        if (quest[0].is_verify === 0) return res.json({ status: 1017 });

        const userO = await db.getRecord('users', { id: quest[0].user_id });

        if (!(userO.length > 0)) return res.json({ status: 1001 });

        const passwordVerify = await verify(userO[0].password);

        const password = passwordVerify.valid ? await sign({ password: passwordVerify.payload.password }) : null;

        const requiredFields = ['tc', 'name', 'surname'];
        let allCompleted = !requiredFields.some(field => !userO[0][field]);

        allCompleted = allCompleted && (userO[0].insurance_type !== undefined) && (userO[0].gender !== undefined) &&
            (userO[0].insurance_type >= 0) && (userO[0].gender >= 0);

        const user = {
            username: userO[0].username,
            pp: await getUrlOfCDN() + "/" + userO[0].profile_picture,
            isActive: userO[0].isActive
        };

        const duration = (Date.now() - start) + 1;

        if (userO !== null && userO !== undefined && userO.length > 0) {
            return res.json({ status: 200, user, allCompleted, ms: duration + "ms" });
        } else {
            return res.json({ status: 1001, ms: duration + "ms" });
        }
    } catch (err) {
        console.log(err);
    }
});

router.post('/send-verify-mail', rateLimitMiddleware, async (req, res) => {
    const { id } = req.body;

    try {
        if (!id) return res.json({ status: 1014 });

        const ipConfig = getIPAdress(req);
        const ip = ipConfig.ipv4;

        const verifyID = (await verify(id));

        if (!verifyID.valid) return res.json({ status: 1012 });

        const questID = verifyID.payload.session;

        const quest = await db.getRecord('quests', { id: questID });
        const user = await db.getRecord('users', { id: quest[0].user_id });

        const emailVerifyID = await db.createRecord('verify', {
            date: new Date(Date.now()),
            quest_id: quest[0].id,
            ip,
            expire: new Date(Date.now() + 60 * 60 * 1000),
            is_used: false
        });

        await mail.sendMail(mailUsers.noreply, user[0].email, 'Hesabınızı Doğrulayın', verifyMail.getVerifyText(user[0].email, emailVerifyID));

        return res.json({ status: 200 });
    } catch (err) {
        console.log(err);
    }
});

const storage = multer.diskStorage({
    destination: async function (req, file, cb) {
        const jsonData = JSON.parse(req.body.json);

        const verifyID = (await verify(jsonData.id));

        if (!verifyID.valid) return res.json({ status: 1012 });

        const userID = verifyID.payload.session;

        const userFolder = `public/users/${userID}/`;
        fs.mkdirSync(userFolder, { recursive: true });
        cb(null, userFolder);
    },
    filename: function (req, file, cb) {
        const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
        const ext = path.extname(file.originalname);
        cb(null, "pp" + '-' + uniqueSuffix + ext);
    }
});

const upload = multer({ storage: storage, limits: { fileSize: 50 * 1024 * 1024 } });

router.post('/update', upload.single('pp'), async (req, res) => {
    try {
        const jsonData = JSON.parse(req.body.json);

        var { id, tc, gender, disability_status, allergies, insurance_type, chronic_diseases, name, surname, birthDate } = jsonData;

        const file = req.file;

        var ppUrl = null;

        if (file) {
            scanFileForVirus(file.path);

            ppUrl = file.path.replace(/public\//g, '');
        }

        const start = Date.now();

        if (!id) return res.json({ status: 1014 });

        const verifyID = (await verify(id)) || null;

        if (!verifyID.valid) return res.json({ status: 1012 });

        const questID = verifyID.payload.session;

        const quest = await db.getRecord('quests', { id: questID });

        const userID = quest[0].user_id;

        const userO = await db.getRecord('users', { id: userID });

        if (!(userO.length > 0)) return res.json({ status: 1001 });

        const passwordVerify = await verify(userO[0].password);

        const user = {
            username: userO[0].username,
            password: passwordVerify.valid ? await sign({ password: passwordVerify.payload.password }) : null,
            isActive: userO[0].isActive
        };

        const duration = (Date.now() - start) + 1;

        if (userO !== null && userO !== undefined && userO.length > 0) {
            const updateData = {};

            if (tc) {
                updateData.tc = await sign({ tc });
                updateData.user_hmac = await hmacValue(tc);

                const isHmacUser = await db.getRecord('users', { user_hmac: updateData.user_hmac });

                if (isHmacUser.length > 0) {
                    return res.json({ status: 1016 });
                }
            }

            if (gender || gender === 0) updateData.gender = gender;
            if (disability_status) updateData.disability_status = disability_status;
            if (allergies) updateData.allergies = allergies;
            if (insurance_type || insurance_type === 0) updateData.insurance_type = insurance_type;
            if (chronic_diseases) updateData.chronic_diseases = chronic_diseases;
            if (name) updateData.name = name;
            if (surname) updateData.surname = surname;
            if (birthDate) updateData.birth_date = birthDate;

            if (ppUrl !== null) updateData.profile_picture = ppUrl;

            if (tc && name && surname && birthDate) {
                const isVerify = await tcVerify.verifyID({
                    tc, name, surname, birthDate
                });

                if (isVerify.status === 'success') {
                    if (isVerify.result) {
                        if (Object.keys(updateData).length > 0) {
                            await db.updateRecord('users', userO[0].id, updateData);

                            const userUpdated = await db.getRecord('users', { id: userO[0].id });

                            const requiredFields = ['tc', 'name', 'surname'];
                            let aC = !requiredFields.some(field => !userUpdated[0][field]);

                            aC = aC && (userUpdated[0].insurance_type !== undefined) && (userUpdated[0].gender !== undefined) &&
                                (userUpdated[0].insurance_type >= 0) && (userUpdated[0].gender >= 0);


                            return res.json({ status: 200, user: userUpdated, allCompleted: aC });
                        }
                    } else {
                        return res.json({ status: 9001 });
                    }
                } else {
                    return res.json({ status: 9002 });
                }
            } else {
                return res.json({ status: 9003 });
            }

            return res.json({ status: 200, user, ms: duration + "ms" });
        } else {
            return res.json({ status: 1001, ms: duration + "ms" });
        }
    } catch (err) {
        console.log(err);
    }
});

router.post('/platform/:platform', async (req, res) => {
    const { platform } = req.params;
    const { email, token } = req.body;

    try {
        const ipConfig = getIPAdress(req);
        const ip = ipConfig.ipv4;

        if (platform === '0') {
            if (token) {
                await verifyIdToken(token).then(async (payload) => {
                    if (payload) {
                        const userO = await db.getRecord('users', { email });

                        if (userO.length > 0) {
                            const platformIsConnected = await db.getRecord('platforms', { user_id: userO[0].id, platform: 0 });

                            if (platformIsConnected.length > 0) {
                                const requiredFields = ['tc', 'name', 'surname'];
                                let allCompleted = !requiredFields.some(field => !userO[0][field]);

                                allCompleted = allCompleted && (userO[0].insurance_type !== undefined) && (userO[0].gender !== undefined) &&
                                    (userO[0].insurance_type >= 0) && (userO[0].gender >= 0);

                                const user = {};

                                const questID = await db.createRecord('quests', {
                                    user_id: userO[0].id,
                                    quest_time: new Date(Date.now()),
                                    ip_adress: ip,
                                    is_verify: 1
                                });

                                user.token = await sign({ session: questID || 0 });

                                return res.json({ status: 200, user, allCompleted });
                            } else {
                                const requiredFields = ['tc', 'name', 'surname'];
                                let allCompleted = !requiredFields.some(field => !userO[0][field]);

                                allCompleted = allCompleted && (userO[0].insurance_type !== undefined) && (userO[0].gender !== undefined) &&
                                    (userO[0].insurance_type >= 0) && (userO[0].gender >= 0);

                                const user = {};

                                await db.createRecord('platforms', {
                                    platform,
                                    user_id: userO[0].id
                                });

                                const questID = await db.createRecord('quests', {
                                    user_id: userO[0].id,
                                    quest_time: new Date(Date.now()),
                                    ip_adress: ip,
                                    is_verify: 1
                                });

                                user.token = await sign({ session: questID || 0 });

                                return res.json({ status: 200, user, allCompleted });
                            }
                        } else {
                            var username = await getUsernameFromEmail(email);

                            let usernameIsValid = await db.getRecord('users', { username });

                            while (usernameIsValid.length > 0) {
                                username = username + (await generateRandom4Digit());
                                usernameIsValid = await db.getRecord('users', { username });
                            }

                            const password = await generateRandomPassword();

                            const userID = await db.createRecord('users', {
                                email,
                                username,
                                password: await sign({ password }),
                                sign_date: new Date(Date.now()),
                                last_login_ip: ip
                            });

                            await db.createRecord('platforms', {
                                platform,
                                user_id: userID
                            });


                            const user = {};

                            const questID = await db.createRecord('quests', {
                                user_id: userID,
                                quest_time: new Date(Date.now()),
                                ip_adress: ip,
                                is_verify: 1
                            });

                            user.token = await sign({ session: questID || 0 });

                            allCompleted = false;

                            return res.json({ status: 200, user, allCompleted });
                        }
                    } else {
                        return res.json({ status: 2001 });
                    }
                }).catch((err) => {
                    console.log('Error:', err);
                });;
            } else {
                return res.json({ status: 2003 })
            }
        } else if (platform === '1') {

        } else if (platform === '2') {

        }
    } catch (err) {
        console.log(err);
    }
});

module.exports = router;