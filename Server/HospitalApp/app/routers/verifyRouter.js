const router = require('express').Router();
const db = require('../../database');

const { wsManager } = require('../../index');

const { hasNumber, hasSpecialCharacter, getIPAdress, checkLoginAttempts, incrementLoginAttempt, incrementRegisterAttempt, checkRegisterAttempts } = require('../../Utils');

router.get('/verify/:id', async (req, res) => {
    const { id } = req.params;

    try {
        var response = {
            status: 200
        };

        if (!id) {
            response.status = 900;
            return res.render('verify/mail', { response });
        }

        const verifyCodeA = await db.getRecord('verify', { id });

        const verifyCode = verifyCodeA[0];

        if (!verifyCodeA.length > 0) {
            response.status = 900;
            return res.render('verify/mail', { response });
        }

        const ipConfig = await getIPAdress(req);

        const ip = ipConfig.ipv4;

        if (verifyCode.ip !== ip) {
            response.status = 901;
            return res.render('verify/mail', { response });
        }

        const currentTime = Date.now();
        if (currentTime > response.expire) {
            response.status = 902;
            return res.render('verify/mail', { response });
        }

        if (verifyCode.is_used === 1) {
            response.status = 903;
            return res.render('verify/mail', { response });
        }

        await db.updateRecord('verify', id, { is_used: 1 });
        await db.updateRecord('quests', verifyCode.quest_id, { is_verify: 1 });

        const quest = await db.getRecord('quests', { id: verifyCode.quest_id });

        const userUpdated = await db.getRecord('users', { id: quest[0].user_id });

        const requiredFields = ['tc', 'name', 'surname'];
        let aC = !requiredFields.some(field => !userUpdated[0][field]);

        aC = aC && (userUpdated[0].insurance_type !== undefined) && (userUpdated[0].gender !== undefined) &&
            (userUpdated[0].insurance_type >= 0) && (userUpdated[0].gender >= 0);

        if(aC) {
            wsManager.sendMessageToUser(quest[0].user_id, '0');
        } else {
            wsManager.sendMessageToUser(quest[0].user_id, '1');
        }

        return res.render('verify/mail', { response });
    } catch (err) {
        console.log(err);
    }
});

module.exports = router;