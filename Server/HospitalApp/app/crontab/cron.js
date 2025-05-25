const cron = require('node-cron');
const { client } = require('../../scylladb');
const { logger } = require('../../winstonLogger');

cron.schedule('0 * * * *', async () => {
    try {
        logger.info('🕒 Verify temizleyici çalışıyor...');

        const now = new Date();

        const selectQuery = `SELECT id FROM hospitalapp.verify WHERE expire < ? ALLOW FILTERING;`;
        const result = await client.execute(selectQuery, [now], { prepare: true });

        if (result.rowLength === 0) {
            logger.info('🔍 Temizlenecek doğrulama kaydı bulunamadı.');
            return;
        }

        for (const row of result.rows) {
            const deleteQuery = `DELETE FROM hospitalapp.verify WHERE id = ?;`;
            await client.execute(deleteQuery, [row.id], { prepare: true });
            logger.info(`🗑️ Kayıt silindi ➜ id: ${row.id}`);
        }

        logger.info('✅ Eski doğrulanmamış verify kayıtları temizlendi.');
    } catch (err) {
        logger.error('❌ Verify temizleme sırasında hata:', err);
    }
});

(async () => {
    try {
        logger.info('🕒 Verify temizleyici çalışıyor...');

        const now = new Date();

        const selectQuery = `SELECT id FROM hospitalapp.verify WHERE expire < ? ALLOW FILTERING;`;
        const result = await client.execute(selectQuery, [now], { prepare: true });

        if (result.rowLength === 0) {
            logger.info('🔍 Temizlenecek doğrulama kaydı bulunamadı.');
            return;
        }

        for (const row of result.rows) {
            const deleteQuery = `DELETE FROM hospitalapp.verify WHERE id = ?;`;
            await client.execute(deleteQuery, [row.id], { prepare: true });
            logger.info(`🗑️ Kayıt silindi ➜ id: ${row.id}`);
        }

        logger.info('✅ Eski doğrulanmamış verify kayıtları temizlendi.');
    } catch (err) {
        logger.error('❌ Verify temizleme sırasında hata:', err);
    }
})();

module.exports = {
    cron
}