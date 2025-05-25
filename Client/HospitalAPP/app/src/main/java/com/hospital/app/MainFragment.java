package com.hospital.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainFragment extends Fragment {

    User user;
    LinearLayout app_info;
    TextView tna, d_name, d_hour;
    RelativeLayout appointment;
    CircleImageView d_pp;

    ProgressDialog coreprog;

    RecyclerView recommendedDoctors;

    RecommendedDoctorsAdapter adapter;
    ArrayList<DoctorItem> doctorArrayList;

    Database db;

    String applyId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        user = new User(getActivity());

        doctorArrayList = new ArrayList<>();
        adapter = new RecommendedDoctorsAdapter(doctorArrayList);

        String token = getActivity().getSharedPreferences("User", Context.MODE_PRIVATE).getString("token", "");

        db = new Database(getActivity().getApplicationContext(), token);

        app_info = view.findViewById(R.id.app_info);
        appointment = view.findViewById(R.id.lastAppointment);
        tna = view.findViewById(R.id.tna);

        d_hour = view.findViewById(R.id.d_hour);
        d_pp = view.findViewById(R.id.d_pp);
        d_name = view.findViewById(R.id.d_name);
        
        recommendedDoctors = view.findViewById(R.id.r_doctors);
        recommendedDoctors.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        recommendedDoctors.setAdapter(adapter);

        ViewUtils.rippleRoundStroke(app_info, "#ffffff", "#f2f2f2", 100, 0, "#000000");

        app_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setClass(getActivity().getApplicationContext(), Appointment.class);
                i.putExtra("application_id", applyId);
                startActivity(i);
            }
        });

        updateList();

        return view;
    }

    public void getLastAppointment() {
         openLoader(true);

         db.get("appointments", "user_id=t-o-k-e-n", new Database.OnCompleteListener() {
             @Override
             public void onSuccess(JSONArray message) throws JSONException, ParseException {
                 Timer t = new Timer();
                 t.schedule(new TimerTask() {
                     @Override
                     public void run() {
                         getActivity().runOnUiThread(new Runnable() {
                             @Override
                             public void run() {
                                 SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                 Date now = new Date();

                                 JSONObject closestFutureAppointment = null;
                                 Date closestDate = null;


                                 for (int i = 0; i < message.length(); i++) {
                                     JSONObject appointmentD = null;
                                     String date = null;
                                     String time = null;

                                     try {
                                         appointmentD = message.getJSONObject(i);
                                         date = appointmentD.getString("appointment_date"); // dd/mm/yyyy
                                         time = appointmentD.getString("appointment_time"); // hour-00:12-minute
                                         d_hour.setText(appointmentD.getString("appointment_time"));

                                         applyId = appointmentD.getString("id");

                                         db.get("users", "id=" + appointmentD.getString("doctor_id"), new Database.OnCompleteListener() {
                                             @Override
                                             public void onSuccess(JSONArray message) throws JSONException, ParseException {
                                                 JSONObject user = message.getJSONObject(0);

                                                 getActivity().runOnUiThread(new Runnable() {
                                                     @Override
                                                     public void run() {
                                                         try {
                                                             Glide.with(getActivity().getApplicationContext())
                                                                     .load(Utils.CDN_URL + user.getString("profile_picture"))
                                                                     .into(d_pp);
                                                             d_name.setText(user.getString("name") + " " + user.getString("surname"));
                                                         } catch (JSONException e) {
                                                             throw new RuntimeException(e);
                                                         }
                                                     }
                                                 });
                                             }

                                             @Override
                                             public void onError(Throwable throwable) {
                                                 Log.e("ERROR: ", throwable.getMessage());
                                             }
                                         });
                                     } catch (JSONException e) {
                                         throw new RuntimeException(e);
                                     }

                                     String[] timeParts = time.split(":");
                                     String hour = timeParts[0];
                                     String minute = timeParts[1];

                                     String fullDateTime = date + " " + hour + ":" + minute;

                                     Date appointmentDate = null;
                                     try {
                                         appointmentDate = sdf.parse(fullDateTime);
                                     } catch (ParseException e) {
                                         throw new RuntimeException(e);
                                     }

                                     if (appointmentDate.after(now)) {
                                         if (closestDate == null || appointmentDate.before(closestDate)) {
                                             closestDate = appointmentDate;
                                             closestFutureAppointment = appointmentD;
                                         }
                                     }
                                 }

                                 if (closestFutureAppointment != null) {
                                     tna.setVisibility(View.GONE);
                                     appointment.setVisibility(View.VISIBLE);
                                 } else {
                                     tna.setVisibility(View.VISIBLE);
                                     appointment.setVisibility(View.GONE);
                                 }

                                 openLoader(false);
                             }
                         });
                     }
                 }, 100);
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

    public void updateList() {
        doctorArrayList.clear();

        openLoader(true);

        db.get("users", "user_type=1", new Database.OnCompleteListener() {
            @Override
            public void onSuccess(JSONArray message) throws JSONException {
                Timer t = new Timer();
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    for (int i = 0; i < message.length(); i++) {
                                        JSONObject data = message.getJSONObject(i);

                                        DoctorItem doctor = new DoctorItem();
                                        doctor.setUuid(data.getString("id"));
                                        doctor.setDoctorName(data.getString("name") + " " + data.getString("surname"));
                                        doctor.setDoctorProfilePicture("https://cdn.kisetsuna.com/" + data.getString("profile_picture"));
                                        doctorArrayList.add(doctor);
                                    }

                                    adapter.notifyDataSetChanged();
                                    openLoader(false);
                                    getLastAppointment();
                                } catch (Exception e) {
                                    Log.e("ERROR: ", e.getMessage());
                                }
                            }
                        });
                    }
                }, 100);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("Doctors", "Veri çekme hatası: ", throwable);
                openLoader(false);
            }
        });

        db.getWs().getMessage(new WebSocketManager.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(String message) throws JSONException {
                Log.e("Message Received", message);
            }


            @Override
            public void onError(Throwable throwable) {

            }
        });
    }

    public void openLoader(final boolean _visibility) {
        if (_visibility) {
            if (coreprog == null){
                coreprog = new ProgressDialog(getActivity());
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

            RadialProgressBar progress = new RadialProgressBar(getActivity());
            layout_progress.addView(progress);
        }
        else {
            if (coreprog != null){
                coreprog.dismiss();
            }
        }
    }

    class RecommendedDoctorsAdapter extends RecyclerView.Adapter<RecommendedDoctorsAdapter.RecommendedDoctorsViewHolder> {

        ArrayList<DoctorItem> doctors;

        public RecommendedDoctorsAdapter(ArrayList<DoctorItem> doctors) {
            this.doctors = doctors;
        }

        @NonNull
        @Override
        public RecommendedDoctorsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RecommendedDoctorsViewHolder(LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.item_doctor_main, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecommendedDoctorsViewHolder holder, int position) {
            holder.name.setText(doctors.get(position).getDoctorName());
            RequestOptions requestOptions = new RequestOptions()
                    .transform(new RoundedCorners(100));

            Glide.with(holder.itemView.getContext())
                    .load(doctors.get(position).getDoctorProfilePicture())
                    .apply(requestOptions)
                    .into(holder.pp);

            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String doctorUid = doctors.get(position).getUuid();

                    Intent i = new Intent();
                    i.setClass(getActivity(), Doctor.class);
                    i.putExtra("uuid", doctorUid);
                    startActivity(i);
                }
            });
        }

        @Override
        public int getItemCount() {
            return doctors.size();
        }

        class RecommendedDoctorsViewHolder extends RecyclerView.ViewHolder {

            LinearLayout go, card;
            TextView name;
            ImageView pp;

            public RecommendedDoctorsViewHolder(@NonNull View itemView) {
                super(itemView);

                go = itemView.findViewById(R.id.go);
                name = itemView.findViewById(R.id.name);
                pp = itemView.findViewById(R.id.pp);
                card = itemView.findViewById(R.id.card);
            }
        }
    }

    class DoctorItem {
        public String doctorName;
        public String doctorProfilePicture;
        public String uuid;

        public String getUuid() {
            return uuid;
        }

        public String getDoctorName() {
            return doctorName;
        }

        public String getDoctorProfilePicture() {
            return doctorProfilePicture;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public void setDoctorName(String doctorName) {
            this.doctorName = doctorName;
        }

        public void setDoctorProfilePicture(String doctorProfilePicture) {
            this.doctorProfilePicture = doctorProfilePicture;
        }
    }
}
