const { createLogger, format, transports } = require('winston');
const { consoleFormat } = require('winston-console-format');

const logger = createLogger({
    level: "silly",
    format: format.combine(
        format.timestamp(),
        format.ms(),
        format.errors({ stack: true }),
        format.splat(),
        format.json()
    ),
    defaultMeta: { service: "Test" },
    transports: [
        new transports.File({ filename: 'logs/combined.log' }),
        new transports.File({ filename: 'logs/error.log', level: 'error' }),
        new transports.Console({
            format: format.combine(
                format.colorize({ all: true }),
                format.padLevels(),
                consoleFormat({
                    showMeta: true,
                    metaStrip: ["timestamp", "service"],
                    inspectOptions: {
                        depth: Infinity,
                        colors: true,
                        maxArrayLength: Infinity,
                        breakLength: 120,
                        compact: Infinity,
                    },
                })
            ),
        }),
    ],
});


module.exports = {
    logger
}