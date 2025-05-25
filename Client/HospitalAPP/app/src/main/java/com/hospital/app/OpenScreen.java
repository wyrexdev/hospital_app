package com.hospital.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class OpenScreen extends AppCompatActivity {

    BaseHttpClient client;

    TextView pwd;

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_open_screen);

        hideSystemUI();

        pref = getSharedPreferences("User", Context.MODE_PRIVATE);

        Typeface nunitoBold = Typeface.createFromAsset(getAssets(), "Nunito-Bold.ttf");

        pwd = findViewById(R.id.pwd);

        pwd.setTypeface(nunitoBold);

        client = new BaseHttpClient();

        Timer timer = new Timer();

        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String token = getSharedPreferences("User", Context.MODE_PRIVATE).getString("token", "");

                        if(token.equals("") || token.trim().isEmpty()) {
                            if(getSharedPreferences("User", Context.MODE_PRIVATE).getBoolean("isDoneOpenScene", false)){
                                Intent i = new Intent();
                                i.setClass(getApplicationContext(), Login.class);
                                startActivity(i);
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                finish();
                            } else {
                                Intent i = new Intent();
                                i.setClass(getApplicationContext(), MainActivity.class);
                                startActivity(i);
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                finish();
                            }
                        } else {
                            JSONObject jsonBody = new JSONObject();
                            try {
                                jsonBody.put("id", token);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            client.post("/api/v1/user", jsonBody, new BaseHttpClient.JsonResponseCallback() {
                                @Override
                                public void onSuccess(JSONObject response) {
                                    try {
                                        int status = response.getInt("status");

                                        if(status == 200) {
                                            boolean allCompleted = response.getBoolean("allCompleted");

                                            if(allCompleted) {
                                                Intent i = new Intent();
                                                i.setClass(getApplicationContext(), Dashboard.class);
                                                startActivity(i);
                                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                finish();
                                            } else {
                                                Intent i = new Intent();
                                                i.setClass(getApplicationContext(), PersonalSetup.class);
                                                startActivity(i);
                                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                finish();
                                            }
                                        } else if(status == 1017) {
                                            Intent i = new Intent();
                                            i.setClass(getApplicationContext(), VerifyMail.class);
                                            startActivity(i);
                                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                            finish();
                                        } else {
                                            Intent i = new Intent();
                                            i.setClass(getApplicationContext(), Login.class);
                                            startActivity(i);
                                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                            finish();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Log.e("API_RESPONSE", "JSON parsing error: " + e.getMessage());
                                    }
                                }

                                @Override
                                public void onSuccess(JSONArray arrayResponse) {

                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.e("API_POST_ERROR", e.getMessage());
                                }
                            });
                        }
                    }
                });
            }
        };

        timer.schedule(tt, 3000);
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
}