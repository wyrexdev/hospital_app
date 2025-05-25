const { exec } = require('child_process');
const fs = require('fs');

const clamavPath = '/usr/bin/clamscan';

const scanFileForVirus = (filePath) => {
    return new Promise((resolve, reject) => {
        if (!fs.existsSync(filePath)) {
            return reject('File does not exist');
        }

        exec(`${clamavPath} ${filePath}`, async (err, stdout, stderr) => {
            if (err) {
                console.error('ClamAV ERROR:', err);
                return reject('An error occurred');
            }

            if (stderr) {
                console.error('ClamAV STDERR:', stderr);
                return reject('An error occurred');
            }

            if (stdout.includes('FOUND')) {
                console.warn('VIRUS FOUND!');

                await fs.promises.unlink(filePath);
                
                return resolve(false);
            }

            return resolve(true);
        });
    });
};

module.exports = {
    scanFileForVirus
};
