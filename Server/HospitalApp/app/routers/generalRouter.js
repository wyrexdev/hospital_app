const router = require('express').Router();

router.get('/privacy-policy', async (req, res) => {
    try {
        res.render('privacy-policy');
    } catch (err) {
        console.log(err);
    }
});

router.get('/terms', async (req, res) => {
    try {
        res.render('terms');
    } catch (err) {
        console.log(err);
    }
});

router.get('/data-delete', async (req, res) => {
    try {
        res.render('data-delete');
    } catch (err) {
        console.log(err);
    }
});

module.exports = router;