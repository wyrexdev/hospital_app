const { OAuth2Client } = require('google-auth-library');

const CLIENT_ID = process.env.CLIENTID;

const client = new OAuth2Client(CLIENT_ID);

async function verifyIdToken(idToken) {
    try {
        const ticket = await client.verifyIdToken({
            idToken: idToken,
            audience: CLIENT_ID,
        });

        const payload = ticket.getPayload();
        return payload;

    } catch (error) {
        console.error('Token doğrulama hatası:', error);
        return null;
    }
}


module.exports = {
    verifyIdToken
}