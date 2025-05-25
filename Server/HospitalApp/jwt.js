const crypto = require('crypto');

const ALGORITHM = 'aes-256-cbc';
const IV_LENGTH = 16;

function base64urlEncode(buffer) {
    return buffer
        .toString('base64')
        .replace(/=/g, '')
        .replace(/\+/g, '-')
        .replace(/\//g, '_');
}

function base64urlDecode(str) {
    str = str
        .replace(/-/g, '+')
        .replace(/_/g, '/');
    while (str.length % 4) {
        str += '=';
    }
    return str;
}

function sign(payload, options = {}) {
    const secret = process.env.SECRETKEY;

    if (!secret || secret.length !== 32) {
        throw new Error('SECRETKEY must be 32 characters long for AES-256-CBC');
    }

    const iv = crypto.randomBytes(IV_LENGTH);

    const payloadWithExp = { ...payload };

    if (options.expiresIn) {
        const exp = Math.floor(Date.now() / 1000) + options.expiresIn;
        payloadWithExp.exp = exp;
    }

    const jsonData = JSON.stringify(payloadWithExp);

    const cipher = crypto.createCipheriv(ALGORITHM, Buffer.from(secret), iv);
    let encrypted = cipher.update(jsonData);
    encrypted = Buffer.concat([encrypted, cipher.final()]);

    const token = base64urlEncode(Buffer.concat([iv, encrypted]));

    return token;
}

function verify(token) {
    const secret = process.env.SECRETKEY;

    if (!secret || secret.length !== 32) {
        throw new Error('SECRETKEY must be 32 characters long for AES-256-CBC');
    }

    const tokenBuffer = Buffer.from(base64urlDecode(token), 'base64');

    const iv = tokenBuffer.slice(0, IV_LENGTH);
    const encryptedText = tokenBuffer.slice(IV_LENGTH);

    const decipher = crypto.createDecipheriv(ALGORITHM, Buffer.from(secret), iv);
    let decrypted = decipher.update(encryptedText);
    decrypted = Buffer.concat([decrypted, decipher.final()]);

    const decryptedJSON = decrypted.toString();

    let payload;
    try {
        payload = JSON.parse(decryptedJSON);
    } catch (err) {
        return { valid: false, error: 'Invalid payload' };
    }

    if (payload.exp && Math.floor(Date.now() / 1000) > payload.exp) {
        return { valid: false, error: 'Token expired' };
    }

    return { valid: true, payload };
}

module.exports = { sign, verify };