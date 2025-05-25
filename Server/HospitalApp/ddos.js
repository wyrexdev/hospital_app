const axios = require('axios');

const url = 'https://kisetsuna.com/api/v1';

const sendRequests = async () => {
    for (let i = 0; i < 25; i++) {
        try {
            await axios.get(url);
            console.log(`Request ${i + 1} sent`);
        } catch (error) {
            console.error('Request failed:', error);
        }
    }
};

const startTest = () => {
    setInterval(sendRequests, 1000);
};

//startTest();
sendRequests();