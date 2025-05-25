const cassandra = require('cassandra-driver');

const { logger } = require('./winstonLogger');

const tables = require('./tables');

const client = new cassandra.Client({
    contactPoints: [process.env.IP],
    localDataCenter: process.env.DATACENTER,
    keyspace: process.env.KEYSPACE
});

client.connect()
    .then(async () => {
        logger.info('ScyllaDB ile bağlantı kuruldu!');

        logger.info('Tablolar kontrol ediliyor...');

        await createTable('users', tables.users);
        await createTable('platforms', tables.platforms);
        await createTable('verify', tables.verify);
        await createTable('quests', tables.quests);
        await createTable('rooms', tables.rooms);
        await createTable('messages', tables.messages);
        await createTable('hospital_applies', tables.hospitalApplies);
        await createTable('hospitals', tables.hospitals);
        await createTable('appointments', tables.appointments);
    })
    .catch(error => {
        logger.error('Bağlantı hatası:', error);
    });

async function tableExists(table) {
    logger.info(`${table} Tablosu kontrol ediliyor...`);

    const query = `
            SELECT table_name FROM system_schema.tables
            WHERE keyspace_name = ? AND table_name = ?
        `;

    try {
        const result = await client.execute(query, [process.env.KEYSPACE, table], { prepare: true });

        if (result.rowLength > 0) {
            logger.info(`${table} Tablosu geçerli.`);
            return true;
        } else {
            logger.info(`${table} Tablosu bulunamadı!`);
            return false;
        }
    } catch (err) {
        logger.error(err);
        return false;
    }
}

async function createTable(tableName, query) {
    const table = await tableExists(tableName);
    if(!table) {
        logger.info(tableName + ' tablosu oluşturuluyor...');

        await client.execute(query, [], { prepare: true });

        logger.info(tableName + ' tablosu başarı ile oluşturuldu!');
    }
}

module.exports = {
    client,
    tableExists
}