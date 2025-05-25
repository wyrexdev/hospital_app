package com.hospital.app;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.WebSocketListener;

public class VerifyMail extends AppCompatActivity {

    WebSocketManager webSocketManager;
    LinearLayout sendMail;

    BaseHttpClient client;
    ProgressDialog coreprog;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verify_mail);

        client = new BaseHttpClient();

        sharedPreferences = getSharedPreferences("User", Context.MODE_PRIVATE);

        webSocketManager = WebSocketManager.getInstance(VerifyMail.this);

        String token = getSharedPreferences("User", MODE_PRIVATE).getString("token", "");

        webSocketManager.getMessage(new WebSocketManager.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(String message) {
                if(message.equals("0")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ModernToast.showCustomToast(getApplicationContext(), "Hesabınız başarı ile doğrulandı!");
                        }
                    });

                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), Dashboard.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                } else if(message.equals("1")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ModernToast.showCustomToast(getApplicationContext(), "Hesabınız başarı ile doğrulandı!");
                        }
                    });

                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), PersonalSetup.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                }
            }


            @Override
            public void onError(Throwable throwable) {

            }
        });

        sendMail = findViewById(R.id.sendMail);

        ViewUtils.rippleRoundStroke(sendMail, "#ffffff", "#f2f2f2", 25, 0, "#000000");

        sendMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoader(true);

                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("id", token);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                client.post("/api/v1/send-verify-mail", jsonBody, new BaseHttpClient.JsonResponseCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            int status = response.getInt("status");

                            if(status == 200) {
                                ModernToast.showCustomToast(getApplicationContext(), "Doğrulama URL'si başarı ile gönderildi!");
                            } else if(status == 429) {
                                ModernToast.showCustomToast(getApplicationContext(), "Çok fazla doğrulama URL'si istendi! Lütfen daha sonra tekrar deneyin.");
                            } else {
                                ModernToast.showCustomToast(getApplicationContext(), "Bizden kaynaklanan bir sorun var gibi duruyor...");
                            }

                            openLoader(false);
                        } catch (Exception e) {
                            Log.e("API Success Error: ", e.getMessage().toString());
                            openLoader(false);
                        }
                    }

                    @Override
                    public void onSuccess(JSONArray arrayResponse) {

                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("API Error: ", e.getMessage().toString());
                        openLoader(false);
                    }
                });
            }
        });

        hideSystemUI();
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

    private BroadcastReceiver socketReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");

            if (message.equals("0")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ModernToast.showCustomToast(getApplicationContext(), "Hesabınız başarı ile doğrulandı!");
                    }
                });

                Intent newIntent = new Intent(getApplicationContext(), Dashboard.class);
                startActivity(newIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();

            } else if (message.equals("1")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ModernToast.showCustomToast(getApplicationContext(), "Hesabınız başarı ile doğrulandı!");
                    }
                });

                Intent newIntent = new Intent(getApplicationContext(), PersonalSetup.class);
                startActivity(newIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(socketReceiver, new IntentFilter("SOCKET_MESSAGE"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(socketReceiver);
    }
}