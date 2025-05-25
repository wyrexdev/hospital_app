const Redis = require('ioredis');
const redis = new Redis();

function hasNumber(str) {
    const regex = /\d/;
    return regex.test(str);
}

function hasSpecialCharacter(str) {
    const regex = /[^a-zA-Z0-9]/;
    return regex.test(str);
}

function getIPAdress(req) {
    var ip = req.headers['x-forwarded-for'] || req.socket.remoteAddress;

    ip = ip.toString();

    if (!ip) return null;

    if (ip.includes(',')) {
        ip = ip.split(',')[0];
    }

    ip = ip.replace('::ffff:', '').trim();

    return { ip };
}

const checkLoginAttempts = async (ip) => {
    const successKey = `login:success:${ip}`;
    const failKey = `login:fail:${ip}`;

    const successCount = await redis.get(successKey);

    const failCount = await redis.get(failKey);

    if (successCount >= process.env.MAXATTEMPTS || failCount >= process.env.MAXATTEMPTS) {
        return false;
    }

    return true;
};

const checkRegisterAttempts = async (ip) => {
    const key = `register:${ip}`;

    const count = await redis.get(key);

    if (count >= process.env.MAXATTEMPTS) {
        return false;
    }

    return true;
};

const redisExpire = async (key) => {
    await redis.expire(key, process.env.TIMEFRAME);
};

const incrementLoginAttempt = async (ip, isSuccess = false) => {
    const key = isSuccess ? `login:success:${ip}` : `login:fail:${ip}`;
    await redis.incr(key);
    await redisExpire(key);
};

const incrementRegisterAttempt = async (ip) => {
    const key = `register:${ip}`;
    await redis.incr(key);
    await redisExpire(key);
}

module.exports = {
    hasSpecialCharacter,
    hasNumber,
    getIPAdress,
    checkLoginAttempts,
    redisExpire,
    incrementLoginAttempt,
    incrementRegisterAttempt,
    checkRegisterAttempts
}