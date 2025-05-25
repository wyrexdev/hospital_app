function getVerifyText(username, id) {
    return `
<!DOCTYPE html>
<html lang="tr">
  <head>
    <meta charset="UTF-8">
    <title>Hesabınızı Doğrulayın</title>
  </head>
  <body style="margin: 0; padding: 0; background-color: #f4f4f4; font-family: Arial, Helvetica, sans-serif;">
    
    <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%" style="padding: 20px 0;">
      <tr>
        <td align="center">
          
          <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 0 10px rgba(0,0,0,0.1);">
            <tr>
              <td style="background-color: #4A90E2; color: #ffffff; padding: 20px; text-align: center; font-size: 24px; font-weight: bold;">
                Hospital App
              </td>
            </tr>

            <tr>
              <td style="padding: 30px;">
                <h1 style="color: #333333; font-size: 22px; margin: 0 0 20px;">Merhaba ${username},</h1>

                <p style="color: #555555; font-size: 16px; line-height: 1.5; margin: 0 0 20px;">
                  Hesabınızı başarıyla oluşturduk! Şimdi hesabınızın güvenliğini sağlamak için doğrulama işlemini tamamlamanız gerekiyor.
                </p>

                <p style="color: #555555; font-size: 16px; line-height: 1.5; margin: 0 0 30px;">
                  Hesabınızı doğrulamak için aşağıdaki bağlantıya tıklayın:
                </p>

                <table cellpadding="0" cellspacing="0" width="100%">
                  <tr>
                    <td align="center">
                      <a href="https://kisetsuna.com/verify/${id}"
                         style="background-color: #4A90E2; color: #ffffff; padding: 15px 30px; text-decoration: none; border-radius: 5px; font-size: 16px; display: inline-block;">
                        Hesabımı Doğrula
                      </a>
                    </td>
                  </tr>
                </table>

                <p style="color: #999999; font-size: 12px; line-height: 1.5; margin: 30px 0 0;">
                  Eğer bu isteği siz yapmadıysanız, bu e-postayı görmezden gelebilirsiniz.
                </p>

              </td>
            </tr>

            <tr>
              <td style="background-color: #f0f0f0; color: #aaaaaa; text-align: center; font-size: 12px; padding: 15px;">
                &copy; 2025 Kisetsuna. Tüm hakları saklıdır.
              </td>
            </tr>

          </table>

        </td>
      </tr>
    </table>

  </body>
</html>
    `;
}

module.exports = {
    getVerifyText
}