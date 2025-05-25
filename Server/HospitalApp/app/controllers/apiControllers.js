const dns = require('dns');
const util = require('util');

const resolveMx = util.promisify(dns.resolveMx);

async function isEmailDomainValid(email) {
    try {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            return false;
        }

        const domain = email.split('@')[1];

        const mxRecords = await resolveMx(domain);

        if (!mxRecords || mxRecords.length === 0) {
            return false;
        }

        return true;
    } catch (err) {
        console.error(err.message);
        return false;
    }
}

function getUsernameFromEmail(email) {
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

    if (!emailRegex.test(email)) {
        throw new Error('Geçersiz email formatı!');
    }

    const username = email.split('@')[0];

    return username;
}

function generateRandom4Digit() {
    const min = 1000;
    const max = 9999;

    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function generateRandomPassword(length = 16) {
    const lowerChars = 'abcdefghijklmnopqrstuvwxyz';
    const upperChars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    const numbers = '0123456789';
    const specialChars = '!@#$%^&*()_+[]{}|;:,.<>?';

    const allChars = lowerChars + upperChars + numbers + specialChars;

    let password = '';

    password += numbers[Math.floor(Math.random() * numbers.length)];
    password += specialChars[Math.floor(Math.random() * specialChars.length)];
    password += lowerChars[Math.floor(Math.random() * lowerChars.length)];

    for (let i = password.length; i < length; i++) {
        password += allChars[Math.floor(Math.random() * allChars.length)];
    }

    password = password.split('').sort(() => Math.random() - 0.5).join('');

    return password;
}

module.exports = {
    isEmailDomainValid,
    getUsernameFromEmail,
    generateRandom4Digit,
    generateRandomPassword
}