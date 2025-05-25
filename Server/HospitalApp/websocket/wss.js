const { logger } = require('../winstonLogger');
const { sign, verify } = require('../jwt');
const socketIo = require('socket.io');
const db = require('../database');

class WebSocketManager {
    constructor(server) {
        this.io = socketIo(server, {
            cors: {
                origin: '*',
                methods: ['GET', 'POST'],
                allowedHeaders: ['Content-Type'],
            },
            transports: ['websocket', 'polling'],
            pingInterval: 10000,
            pingTimeout: 5000,
        });

        this.clients = new Map();
        logger.info('Socket.IO Başlatıldı.');

        this.messageCallback = null;

        this.io.on('connection', async (socket) => {
            await this.handleConnection(socket);
        });
    }

    async handleConnection(socket) {
        socket.on('authenticate', async (token) => {
            if (!token) {
                socket.emit('error', { error: 'Token eksik' });
                socket.disconnect();
                return;
            }

            try {
                const verifyToken = await verify(token);
                const questID = verifyToken.payload.session;

                const quest = await db.getRecord('quests', { id: questID });
                const user = await db.getRecord('users', { id: quest[0].user_id });

                const userId = (user[0].id).toString();

                await db.updateRecord('users', user[0].id, { is_active: 1 });

                this.clients.set(userId, socket);
                logger.info(`User ${userId} connected`);

                socket.on('disconnect', async () => {
                    this.clients.delete(userId);
                    logger.info(`User ${userId} disconnected`);
                    
                    await db.updateRecord('users', user[0].id, { is_active: 0 });
                });

                socket.on('message', (message) => {
                    logger.info(`User ${userId} sent message: ${message}`);

                    if (this.messageCallback) {
                        const data = {
                            userId: userId,
                            message: message
                        };
                        this.messageCallback(data);
                    }
                });

            } catch (err) {
                logger.error('Token doğrulama başarısız', err);
                socket.emit('error', { error: 'Geçersiz token' });
                socket.disconnect();
            }
        });
    }

    on(eventName, callback) {
        if (typeof callback !== 'function') {
            throw new Error('Callback bir fonksiyon olmalı');
        }
    
        this.io.on('connection', (socket) => {
            socket.on(eventName, (data) => {
                callback(socket, data);
            });
        });
    
        logger.info(`Dinleyici eklendi -> ${eventName}`);
    }
    
    getMessage(callback) {
        if (typeof callback !== 'function') {
            throw new Error('Callback bir fonksiyon olmalı');
        }

        this.messageCallback = callback;
        logger.info('Global message listener aktif.');
    }

    sendMessageToUser(userId, message) {
        const socket = this.clients.get(userId.toString());
        if (socket) {
            socket.emit('message', message);
        } else {
            logger.warn(`User ${userId} is not connected`);
        }
    }

    sendMessageToAll(message) {
        this.clients.forEach((socket, userId) => {
            socket.emit('message', message);
        });
    }

    closeConnections() {
        this.clients.forEach((socket, userId) => {
            socket.disconnect();
        });
        this.clients.clear();
        this.messageCallback = null;
    }
}

module.exports = { WebSocketManager };