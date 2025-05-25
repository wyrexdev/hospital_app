package com.hospital.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

public class Register extends AppCompatActivity {

    TextView hhgy, th, vkgy, bhy, login;
    EditText password, email, username;
    Button register;
    LinearLayout google, facebook;

    BaseHttpClient client;
    ProgressDialog coreprog;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        hideSystemUI();

        client = new BaseHttpClient();

        sharedPreferences = getSharedPreferences("User", Context.MODE_PRIVATE);

        hhgy = findViewById(R.id.hhgy);
        th = findViewById(R.id.th);
        vkgy = findViewById(R.id.vkgy);
        bhy = findViewById(R.id.bhy);
        register = findViewById(R.id.register);

        password = findViewById(R.id.password);
        email = findViewById(R.id.email);
        username = findViewById(R.id.username);

        login = findViewById(R.id.login);

        google = findViewById(R.id.google);
        facebook = findViewById(R.id.facebook);

        Typeface nunitoBold = Typeface.createFromAsset(getAssets(), "Nunito-Bold.ttf");

        hhgy.setTypeface(nunitoBold);
        th.setTypeface(nunitoBold);
        vkgy.setTypeface(nunitoBold);
        bhy.setTypeface(nunitoBold);
        register.setTypeface(nunitoBold);

        username.setTypeface(nunitoBold);
        email.setTypeface(nunitoBold);
        password.setTypeface(nunitoBold);

        login.setTypeface(nunitoBold);

        ViewUtils.rippleRoundStroke(google, "#ffffff", "#f2f2f2", 25, 0, "#000000");
        ViewUtils.rippleRoundStroke(facebook, "#ffffff", "#f2f2f2", 25, 0, "#000000");

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoader(true);

                String usernameS = username.getText().toString();
                String emailS = email.getText().toString();
                String passwordS = password.getText().toString();

                if(!usernameS.isEmpty() && !usernameS.isBlank() && !usernameS.toString().equals("") && !emailS.isBlank() && !emailS.isEmpty() && !emailS.toString().equals("") && !passwordS.isBlank() && !passwordS.isEmpty() && !passwordS.toString().equals("")) {
                    JSONObject data = new JSONObject();

                    try {
                        data.put("username", usernameS);
                        data.put("email", emailS);
                        data.put("password", passwordS);
                    } catch (Exception e) {

                    }
                    client.post("/api/v1/register", data, new BaseHttpClient.JsonResponseCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            try {
                                int status = response.getInt("status");

                                if(status == 200) {
                                    String token = response.getJSONObject("user").getString("token");

                                    boolean isVerify = response.getBoolean("verify");

                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("token", token);
                                    editor.apply();

                                    if(isVerify) {
                                        Intent intent = new Intent();
                                        intent.setClass(getApplicationContext(), PersonalSetup.class);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        finish();
                                    } else {
                                        Intent intent = new Intent();
                                        intent.setClass(getApplicationContext(), VerifyMail.class);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        finish();
                                    }

                                    openLoader(false);
                                } else if(status == 429) {
                                    ModernToast.showCustomToast(Register.this, "Kısa süre içerisinde aşırı kayıt işlemi tespit edilmiştir. Lütfen 30 dakika içerisinde tekrar deneyiniz.");
                                    openLoader(false);
                                } else if(status == 1002) {
                                    ModernToast.showCustomToast(Register.this, "Geçersiz e-posta adresi.");
                                    openLoader(false);
                                } else if(status == 1003) {
                                    ModernToast.showCustomToast(Register.this, "Geçersiz şifre.");
                                    openLoader(false);
                                } else if(status == 1008) {
                                    ModernToast.showCustomToast(Register.this, "Geçersiz kullanıcı adı.");
                                    openLoader(false);
                                } else if(status == 1004) {
                                    ModernToast.showCustomToast(Register.this, "Geçersiz e-posta adresi.");
                                    openLoader(false);
                                } else if(status == 1005) {
                                    ModernToast.showCustomToast(Register.this, "Şifre 8 haneden küçük olamaz.");
                                    openLoader(false);
                                } else if(status == 1006) {
                                    ModernToast.showCustomToast(Register.this, "Şifre en az bir özel karakter içermelidir.");
                                    openLoader(false);
                                } else if(status == 1007) {
                                    ModernToast.showCustomToast(Register.this, "Şifre en az bir tane sayı içermelidir.");
                                    openLoader(false);
                                } else if(status == 1009) {
                                    ModernToast.showCustomToast(Register.this, "E-posta adresine kayıtlı bir hesap zaten bulunmaktadır. Hesabınız varsa, lütfen bilgilerinizi girerek giriş yapınız.");
                                    openLoader(false);
                                } else if(status == 1010) {
                                    ModernToast.showCustomToast(Register.this, "Kullanıcı adı kullanılmaktadır. Lütfen farklı bir kullanıcı adı deneyiniz.");
                                    openLoader(false);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e("API_RESPONSE", "JSON parsing error: " + e.getMessage());
                                openLoader(false);
                            }
                        }

                        @Override
                        public void onSuccess(JSONArray arrayResponse) {

                        }

                        @Override
                        public void onError(Exception e) {
                            e.printStackTrace();
                            Log.e("API_RESPONSE", "JSON parsing error: " + e.getMessage());
                            openLoader(false);
                        }
                    });
                } else {
                    ModernToast.showCustomToast(Register.this, "Lütfen Tüm Bilgilerinizi Doldurduğunuzdan Emin olun.");
                    openLoader(false);
                }
            }
        });

        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setClass(getApplicationContext(), Login.class);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });

        // ViewUtils.rippleRoundStroke(facebook, "#232323", "#333232", 25, 0, "#000000");
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    public void openLoader(final boolean _visibility) {
        if (_visibility) {
            if (coreprog == null){
                coreprog = new ProgressDialog(this);
                coreprog.setCancelable(false);
                coreprog.setCanceledOnTouchOutside(false);

                coreprog.requestWindowFeature(Window.FEATURE_NO_TITLE);  coreprog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));

            }
            coreprog.show();
            coreprog.setContentView(R.layout.loading);


            LinearLayout linear2 = (LinearLayout)coreprog.findViewById(R.id.linear2);

            LinearLayout back = (LinearLayout)coreprog.findViewById(R.id.background);

            LinearLayout layout_progress = (LinearLayout)coreprog.findViewById(R.id.layout_progress);

            android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
            gd.setColor(Color.parseColor("#FFFFFF"));
            gd.setCornerRadius(45);
            gd.setStroke(0, Color.WHITE);
            linear2.setBackground(gd);

            RadialProgressBar progress = new RadialProgressBar(this);
            layout_progress.addView(progress);
        }
        else {
            if (coreprog != null){
                coreprog.dismiss();
            }
        }
    }
}