package com.hospital.app;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

public class Settings extends AppCompatActivity {

    LinearLayout setup_admin, logout;

    ProgressDialog coreprog;

    Database db;

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        setup_admin = findViewById(R.id.setup_admin);
        logout = findViewById(R.id.logout);

        pref = getSharedPreferences("User", Context.MODE_PRIVATE);

        db = new Database(getApplicationContext(),
                getSharedPreferences("User", MODE_PRIVATE).getString("token", ""));

        db.get("users", "id=t-o-k-e-n", new Database.OnCompleteListener() {
            @Override
            public void onSuccess(JSONArray message) throws JSONException, ParseException {
                JSONObject user = message.getJSONObject(0);

                int userType = user.getInt("user_type");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(userType == 0) {
                            setup_admin.setVisibility(View.GONE);
                        }
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {

            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = pref.edit();
                editor.remove("token");
                editor.commit();

                Context ctx = getApplicationContext();
                PackageManager pm = ctx.getPackageManager();
                Intent intent = pm.getLaunchIntentForPackage(ctx.getPackageName());
                Intent mainIntent = Intent.makeRestartActivityTask(intent.getComponent());
                ctx.startActivity(mainIntent);
                Runtime.getRuntime().exit(0);
            }
        });

        ViewUtils.rippleRoundStroke(setup_admin, "#ffffff", "#f2f2f2", 25, 0, "#000000");

        setup_admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoader(true);

                createAdminShortcut(getApplicationContext());
            }
        });

        LinearLayout back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    public void createAdminShortcut(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);

            if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported()) {
                Intent intent = new Intent(context, Admin.class);
                intent.setAction(Intent.ACTION_VIEW);

                ShortcutInfo shortcut = new ShortcutInfo.Builder(context, "admin_shortcut")
                        .setShortLabel("Hospital App Admin")
                        .setLongLabel("Hospital App Admin Paneli")
                        .setIcon(Icon.createWithResource(context, R.drawable.logo_transparan))
                        .setIntent(intent)
                        .build();

                PendingIntent successCallback = PendingIntent.getActivity(
                        context, 0, intent, PendingIntent.FLAG_IMMUTABLE
                );

                shortcutManager.requestPinShortcut(shortcut, null);

                ModernToast.showCustomToast(context, "Admin Kurulumu Başarı ile Yapıldı!");
            } else {
                ModernToast.showCustomToast(context, "Cihazınız kısayol eklemeyi desteklemiyor!");
            }
        } else {
            ModernToast.showCustomToast(context, "Android 8.0 ve üstü gereklidir!");
        }

        openLoader(false);
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