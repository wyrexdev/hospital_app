const nodemailer = require('nodemailer');

async function sendMail(user, email, title, content) {
    const transporter = nodemailer.createTransport({
        host: 'mail.anihime.com',
        port: 587,
        secure: false,
        auth: {
            user: user.from,
            pass: user.password,
        },
        tls: {
            rejectUnauthorized: false,
        }
    });

    const mailOptions = {
        from: user.from,
        to: email,
        subject: title,
        text: "",
        html: content
    };

    try {
        const info = await transporter.sendMail(mailOptions);
        console.log(info)
        return 200;
    } catch (error) {
        console.error('Mail gönderim hatası:', error.message);
        return 502;
    }
}

module.exports = {
    sendMail
};
