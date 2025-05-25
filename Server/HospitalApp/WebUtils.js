const config = require('./config.json');

const isLocal = config.IS_LOCAL;

function getUrlOfCDN() {
    try {
        if(isLocal) {
            return `http://localhost:${config.CDN_PORT}`;
        } else {
            return `${config.CDN_URL}`;
        }
    } catch (err) {
        console.log(err);
    }
};

module.exports = {
    getUrlOfCDN
}