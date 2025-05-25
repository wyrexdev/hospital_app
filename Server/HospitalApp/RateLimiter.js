const Redis = require('ioredis');
const redis = new Redis();

const RATE_LIMIT = process.env.RATELIMIT || 10;
const BLOCK_TIME = process.env.BLOCKTIME || 600;

const { getIPAdress } = require('./Utils');

const rateLimitMiddleware = async (req, res, next) => {
    const ip = getIPAdress(req).ip;

    const isBlocked = await redis.get(`blocked:${ip}`);
    if (isBlocked) {
        return res.json({ status: 429 });
    }

    const currentRequests = await redis.get(`requests:${ip}`);
    if (currentRequests) {
        if (parseInt(currentRequests) >= RATE_LIMIT) {
            await blockIp(ip);
            return res.json({ status: 429 });
        }
        await redis.incr(`requests:${ip}`);
    } else {
        await redis.set(`requests:${ip}`, 1, 'EX', 1);
    }

    next();
};

const blockIp = async (ip) => {
    await redis.set(`blocked:${ip}`, 'blocked', 'EX', BLOCK_TIME);
};

module.exports = {
    rateLimitMiddleware
};
