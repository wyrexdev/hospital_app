require('dotenv').config();

const express = require('express');
const app = express();

const cdn = express();

const http = require('http');

const config = require('./config.json');
const PORT = config.PORT;
const CDN_PORT = config.CDN_PORT;
const WEBSOCKET_PORT = config.WEBSOCKET_PORT;

app.set('view engine', 'ejs');

const bodyParser = require('body-parser');

const { init } = require('./app/socket/chatSocket');

const { WebSocketManager } = require('./websocket/wss');

const { cron } = require('./app/crontab/cron');

const { getIPAdress } = require('./Utils');

const server = http.createServer((req, res) => {
    res.writeHead(200, { 'Content-Type': 'text/plain' });
    res.end('Socket.IO Server');
});
const wsManager = new WebSocketManager(server);

init(wsManager);

module.exports = {
    wsManager
}

const requestIp = require('request-ip');
app.use(requestIp.mw())

const { logger } = require('./winstonLogger');

const { rateLimitMiddleware } = require('./RateLimiter');

cdn.use(express.static(__dirname + "/public"));

app.use(bodyParser.urlencoded({ extended: true, limit: "50mb" }));
app.use(bodyParser.json({ limit: "50mb", extended: true }));

app.use(rateLimitMiddleware);

app.set('trust proxy', true);

app.use((req, res, next) => {
    const ip = getIPAdress(req).ip;
    const userAgent = req.headers['user-agent'];
    const method = req.method;
    const url = req.originalUrl;
    const protocol = req.protocol;
    const host = req.headers.host;

    logger.info({
        message: 'Yeni İstek Alındı',
        zaman: new Date().toISOString(),
        ip,
        method,
        url: `${protocol}://${host}${url}`,
        userAgent,
        referer: req.headers['referer'] || 'Yok',
        contentType: req.headers['content-type'] || 'Yok'
    });

    next();
});

// <------ ROUTERS START ----> //

const api = require('./app/api/v1/api');
app.use('/api/v1/', api);

const generalRouter = require('./app/routers/generalRouter');
app.use('/', generalRouter);

const verifyRouter = require('./app/routers/verifyRouter');
app.use('/', verifyRouter);

// <------ ROUTERS END ------> //

app.listen(PORT, () => {
    logger.info(`Sunucu ${PORT} Portunda Çalışmaya Başladı`);
    logger.info(`URL: http://localhost:${PORT}/`);
});

cdn.listen(CDN_PORT, () => {
    logger.info(`CDN Sunucusu ${CDN_PORT} Portunda Çalışmaya Başladı`);
    logger.info(`URL: http://localhost:${CDN_PORT}/`);
});

server.listen(WEBSOCKET_PORT, () => {
    logger.info(`WebSocket Sunucusu ${WEBSOCKET_PORT} Portunda Çalışmaya Başladı`);
});