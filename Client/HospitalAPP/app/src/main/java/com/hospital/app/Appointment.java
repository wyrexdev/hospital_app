package com.hospital.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

import java.text.ParseException;

import de.hdodenhof.circleimageview.CircleImageView;

public class Appointment extends AppCompatActivity {

    Database db;

    TextView doctorName, time, date;
    Button cancel;
    CircleImageView pp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_appointment);

        hideSystemUI();

        db = new Database(getApplicationContext(),
                getSharedPreferences("User", MODE_PRIVATE).getString("token", ""));

        doctorName = findViewById(R.id.doctorName);
        time = findViewById(R.id.time);
        date = findViewById(R.id.date);

        cancel = findViewById(R.id.cancel);
        pp = findViewById(R.id.pp);

        String appId = getIntent().getStringExtra("application_id");

        db.get("appointments", "id=" + appId, new Database.OnCompleteListener() {
            @Override
            public void onSuccess(JSONArray message) throws JSONException, ParseException {
                JSONObject data = message.getJSONObject(0);

                String appointmentTime = data.getString("appointment_time");
                String appointmentDate = data.getString("appointment_date");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        time.setText(appointmentTime);
                        date.setText(appointmentDate);
                    }
                });

                db.get("users", "id=" + data.getString("doctor_id"), new Database.OnCompleteListener() {
                    @Override
                    public void onSuccess(JSONArray doctorArray) throws JSONException, ParseException {
                        JSONObject doctor = doctorArray.getJSONObject(0);

                        String name = doctor.getString("name") + " " + doctor.getString("surname");
                        String pfp = doctor.getString("profile_picture");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                doctorName.setText(name);

                                RequestOptions requestOptions = new RequestOptions()
                                        .transform(new RoundedCorners(100));

                                Glide.with(getApplicationContext())
                                        .load(Utils.CDN_URL + pfp)
                                        .apply(requestOptions)
                                        .into(pp);
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.delete("appointments", appId, new Database.OnSuccessListener() {
                    @Override
                    public void onSuccess(boolean isDone) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ModernToast.showCustomToast(getApplicationContext(), "Başarı ile silindi!");

                                Context ctx = getApplicationContext();
                                PackageManager pm = ctx.getPackageManager();
                                Intent intent = pm.getLaunchIntentForPackage(ctx.getPackageName());
                                Intent mainIntent = Intent.makeRestartActivityTask(intent.getComponent());
                                ctx.startActivity(mainIntent);
                                Runtime.getRuntime().exit(0);
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ModernToast.showCustomToast(getApplicationContext(), "Bir Hata oluştu!");
                                finish();
                            }
                        });
                    }
                });
            }
        });
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

}