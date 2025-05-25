package com.hospital.app;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfile extends AppCompatActivity {

    CircleImageView pp;

    TextView username, name;

    LinearLayout settings;

    WebSocketManager webSocketManager;

    ProgressDialog coreprog;

    int isHospital = 0; String applyDate, hospitalName;

    LinearLayout edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

        hideSystemUI();

        settings = findViewById(R.id.settings);

        name = findViewById(R.id.name);
        username = findViewById(R.id.username);
        pp = findViewById(R.id.pp);

        edit = findViewById(R.id.edit);

        openLoader(true);

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), Settings.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), PersonalSetup.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        webSocketManager = Utils.getInstance(this);

        String uid = getSharedPreferences("User", Context.MODE_PRIVATE).getString("token", "00000000-0000-0000-0000-000000000000");

        webSocketManager.sendMessage("2->" + uid);

        webSocketManager.getMessage(new WebSocketManager.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(String message) {
                if(message.startsWith("2->")) {
                    String userInformations = message.split("->")[1];

                    try {
                        JSONObject user = new JSONObject(userInformations);

                        String nameText = (user.getString("name").replaceAll(" ",""))  + " " + (user.getString("surname").replaceAll(" ",""));
                        String usernameText = "@" + user.getString("username");
                        String ppUrl = user.getString("pp");

                        isHospital = user.getInt("hospitalApply");

                        hospitalName = user.getString("hospitalName");
                        applyDate = user.getString("hospitalApplyDate");

                        RequestOptions requestOptions = new RequestOptions()
                                .transform(new RoundedCorners(100));

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                name.setText(nameText);
                                username.setText(usernameText);

                                Glide.with(getApplicationContext())
                                        .load(ppUrl)
                                        .centerCrop()
                                        .apply(requestOptions)
                                        .into(pp);
                            }
                        });

                        openLoader(false);
                    } catch (Exception e) {
                        Log.e("WebSocket User Error: ", e.getMessage().toString());
                        openLoader(false);
                    }
                } else if(message.equals("2")) {
                    openLoader(false);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ModernToast.showCustomToast(getApplicationContext(), "Bu Kullanıcı Bulunamadı");

                            finish();
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        }
                    });
                }
            }


            @Override
            public void onError(Throwable throwable) {

            }
        });

        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        dispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
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