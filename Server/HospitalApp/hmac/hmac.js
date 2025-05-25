const crypto = require("crypto");

function hmacValue(tc) {
    return crypto.createHmac("sha256", process.env.HMACKEY).update(tc).digest("hex");
}

module.exports = {
    hmacValue
}