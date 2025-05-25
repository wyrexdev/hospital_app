const fetch = require('node-fetch');
const { parseString, parseStringPromise  } = require('xml2js');
const { z } = require('zod');

const getAge = (dateString) => {
    const today = new Date();
    const birthDate = new Date(dateString);
    let age = today.getFullYear() - birthDate.getFullYear();
    const m = today.getMonth() - birthDate.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
        age--;
    }
    return age;
};

async function verifyID(body) {
    try {
        const year = new Date(body.birthDate).getFullYear();
        const legalAge = getAge(body.birthDate) >= 18;

        const xmlBody = `<?xml version="1.0" encoding="utf-8"?>
            <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <TCKimlikNoDogrula xmlns="http://tckimlik.nvi.gov.tr/WS">
                  <TCKimlikNo>${body.tc}</TCKimlikNo>
                  <Ad>${body.name}</Ad>
                  <Soyad>${body.surname}</Soyad>
                  <DogumYili>${year}</DogumYili>
                </TCKimlikNoDogrula>
              </soap:Body>
            </soap:Envelope>`;

        const fetchUrl = 'https://tckimlik.nvi.gov.tr/Service/KPSPublic.asmx';

        const headers = {
            'Content-Type': 'text/xml; charset=utf-8',
            'SOAPAction': 'http://tckimlik.nvi.gov.tr/WS/TCKimlikNoDogrula',
        };

        const fetchResponse = await fetch(fetchUrl, {
            method: 'POST',
            headers,
            body: xmlBody
        });

        const xmlResponse = await fetchResponse.text();

        const result = await parseStringPromise(xmlResponse);

        const TCKimlikNoDogrulaResult =
            result['soap:Envelope']['soap:Body'][0]['TCKimlikNoDogrulaResponse'][0]['TCKimlikNoDogrulaResult'][0];

        return {
            status: 'success',
            result: TCKimlikNoDogrulaResult === 'true',
            legalAge
        };

    } catch (error) {
        if (error?.name === 'ZodError') {
            return {
                status: 'error',
                details: error.issues
            };
        } else {
            return {
                status: 'error',
                details: error.message || error
            };
        }
    }
}

module.exports = { verifyID };
