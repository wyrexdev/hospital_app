const { client } = require('./scylladb');
const cassandra = require('cassandra-driver');

async function createRecord(table, data) {
    const keys = Object.keys(data);
    const values = Object.values(data);

    const id = cassandra.types.Uuid.random();

    const query = `INSERT INTO ${process.env.KEYSPACE}.${table} (id, ${keys.join(', ')}) VALUES (${id}, ${keys.map((_, index) => `?`).join(', ')})`;
    
    try {
        await client.execute(query, values, { prepare: true });

        return id;
    } catch (err) {
        console.error(err);
    }
}

async function getRecord(table, filter = {}) {
    if (!filter || Object.keys(filter).length === 0) {
        return null;
    }

    const filterKeys = Object.keys(filter);
    const filterValues = Object.values(filter);

    const whereClause = filterKeys.map((key, index) => `${key} = ?`).join(' AND ');

    const query = `SELECT * FROM ${process.env.KEYSPACE}.${table} WHERE ${whereClause} ALLOW FILTERING`;

    try {
        const result = await client.execute(query, filterValues, { prepare: true });

        if (result.rowLength === 0) {
            return [];
        }

        return result.rows;
    } catch (err) {
        console.error('Error getting record:', err);
        return null;
    }
}


async function updateRecord(table, id, updatedData) {
    const setClause = Object.keys(updatedData).map((key, index) => `${key} = ?`).join(', ');
    const values = [...Object.values(updatedData), id];

    const query = `UPDATE ${process.env.KEYSPACE}.${table} SET ${setClause} WHERE id = ?`;

    try {
        await client.execute(query, values, { prepare: true });
    } catch (err) {
        console.error('Error updating record:', err);
    }
}

async function deleteRecord(table, key) {
    const query = `DELETE FROM ${process.env.KEYSPACE}.${table} WHERE id = ?`;

    try {
        await client.execute(query, [key], { prepare: true });
        console.log('Record deleted successfully!');
    } catch (err) {
        console.error('Error deleting record:', err);
    }
}

async function getAllData(table) {
    const query = `SELECT * FROM ${process.env.KEYSPACE}.${table}`;

    try {
        const result = await client.execute(query, [], { prepare: true });

        if (result.rowLength === 0) {
            return [];
        }

        return result.rows;
    } catch (err) {
        console.error('Error getting record:', err);
        return null;
    }
}

module.exports = { createRecord, getRecord, updateRecord, deleteRecord, getAllData };
