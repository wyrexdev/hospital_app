const users = require('./users');
const mail = require('./mail');
const verify = require('./verify');

(async () => {
    try {
        const status = await mail.sendMail(users.noreply, 'omerkarakasgrp39@gmail.com', 'TİTLE', verify.getVerifyText("sa", 123));
        console.log('Mail Gönderildi:', status);
    } catch (err) {
        console.error('Mail gönderirken hata oluştu:', err.message);
    }
})();
