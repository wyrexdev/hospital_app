package com.hospital.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class Doctor extends AppCompatActivity {

    LinearLayout back;
    CircleImageView pp;
    TextView name;
    Button appointment;

    Database db;

    ProgressDialog coreprog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doctor);

        hideSystemUI();

        openLoader(true);

        String token = getSharedPreferences("User", Context.MODE_PRIVATE).getString("token", "");
        String doctorUuid = getIntent().getStringExtra("uuid");

        db = new Database(getApplicationContext(), token);

        name = findViewById(R.id.name);
        pp = findViewById(R.id.pp);
        back = findViewById(R.id.back);
        appointment = findViewById(R.id.appointment);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        appointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLoader(true);

                AppointmentUtils.createAppointment(getApplicationContext(), token, doctorUuid);

                openLoader(false);
            }
        });

        db.get("users", "id=" + doctorUuid, new Database.OnCompleteListener() {
            @Override
            public void onSuccess(JSONArray message) throws JSONException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject data = message.getJSONObject(0);

                            name.setText(data.getString("name") + " " + data.getString("surname"));

                            RequestOptions requestOptions = new RequestOptions()
                                    .transform(new RoundedCorners(100));

                            Glide.with(getApplicationContext())
                                    .load("https://cdn.kisetsuna.com/" + data.getString("profile_picture"))
                                    .apply(requestOptions)
                                    .into(pp);

                            openLoader(false);
                        } catch (Exception e) {

                        }
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {

            }
        });

        db.getWs().getMessage(new WebSocketManager.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(String message) throws JSONException {

            }


            @Override
            public void onError(Throwable throwable) {

            }
        });
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
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