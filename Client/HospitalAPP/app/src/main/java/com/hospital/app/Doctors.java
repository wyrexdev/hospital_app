package com.hospital.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class Doctors extends Fragment {

    RecyclerView doctorsRecycler;
    TextInputEditText doctorEmail;
    Button addDoctor;

    BaseHttpClient client;
    Database db;

    DoctorsAdapter adapter;
    ArrayList<Doctor> doctorArrayList;

    ProgressDialog coreprog;

    TextView noAccess;
    LinearLayout doctorsLinear;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_doctors, container, false);

        String token = getActivity().getSharedPreferences("User", Context.MODE_PRIVATE).getString("token", "");

        doctorArrayList = new ArrayList<>();
        adapter = new DoctorsAdapter(doctorArrayList);

        doctorsLinear = view.findViewById(R.id.doctorsLinear);
        noAccess = view.findViewById(R.id.noAccess);

        doctorsRecycler = view.findViewById(R.id.doctors);
        doctorsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        doctorsRecycler.setAdapter(adapter);

        addDoctor = view.findViewById(R.id.addDoctor);
        doctorEmail = view.findViewById(R.id.d_email);

        addDoctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addDoctor(doctorEmail.getText().toString());
            }
        });

        client = new BaseHttpClient();
        db = new Database(getActivity(), token);

        db.get("users", "id=t-o-k-e-n", new Database.OnCompleteListener() {
            @Override
            public void onSuccess(JSONArray message) throws JSONException, ParseException {
                JSONObject user = message.getJSONObject(0);

                int userType = user.getInt("user_type");

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(userType == 0 || userType == 1) {
                            noAccess.setVisibility(View.VISIBLE);
                            doctorsLinear.setVisibility(View.GONE);
                        } else {
                            noAccess.setVisibility(View.GONE);
                            doctorsLinear.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {

            }
        });

        OnBackPressedDispatcher dispatcher = getActivity().getOnBackPressedDispatcher();
        dispatcher.addCallback(getActivity(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        updateList();

        return view;
    }

    public void addDoctor(String email) {
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ModernToast.showCustomToast(getActivity().getApplicationContext(), "E-Posta Adresi Boş Bırakalamaz!");
            return;
        }

        db.get("users", "email=" + email, new Database.OnCompleteListener() {
            @Override
            public void onSuccess(JSONArray message) throws JSONException {
                String userId = message.getJSONObject(0).getString("id");

                db.update("users", userId, "user_type=1", new Database.OnSuccessListener() {
                    @Override
                    public void onSuccess(boolean isDone) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    ModernToast.showCustomToast(getActivity().getApplicationContext(), "Doktor Başarı ile Eklendi!");
                                    updateList();

                                    doctorEmail.setText("");
                                }, 100);
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
                        Log.e("Message Received", message);
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
    }

    public void removeDoctor(String email) {
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ModernToast.showCustomToast(getActivity().getApplicationContext(), "E-Posta Adresi Boş Bırakalamaz!");
            return;
        }

        db.get("users", "email=" + email, new Database.OnCompleteListener() {
            @Override
            public void onSuccess(JSONArray message) throws JSONException {
                String userId = message.getJSONObject(0).getString("id");

                db.update("users", userId, "user_type=0", new Database.OnSuccessListener() {
                    @Override
                    public void onSuccess(boolean isDone) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    ModernToast.showCustomToast(getActivity().getApplicationContext(), "Doktor Başarı ile Kaldırıldı!");
                                    updateList();
                                }, 100);
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
    }

    public void updateList() {
        doctorArrayList.clear();

        openLoader(true);

        db.get("users", "user_type=1", new Database.OnCompleteListener() {
            @Override
            public void onSuccess(JSONArray message) throws JSONException {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            int start = doctorArrayList.size();

                            for (int i = 0; i < message.length(); i++) {
                                Doctor doctor = new Doctor();
                                try {
                                    doctor.setDoctorName(message.getJSONObject(i).getString("name") + " " + message.getJSONObject(i).getString("surname"));
                                    doctor.setDoctorEmail(message.getJSONObject(i).getString("email"));
                                    doctor.setDoctorProfilePicture(Utils.CDN_URL + message.getJSONObject(i).getString("profile_picture"));
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                doctorArrayList.add(doctor);
                            }

                            adapter.notifyDataSetChanged();
                        }, 100);
                    }
                });
                openLoader(false);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("Doctors", "Veri çekme hatası: ", throwable);
                openLoader(false);
            }
        });
    }

    class DoctorsAdapter extends RecyclerView.Adapter<DoctorsAdapter.DoctorsViewHolder> {

        ArrayList<Doctor> doctors;

        public DoctorsAdapter(ArrayList<Doctor> doctors) {
            this.doctors = doctors;
        }

        @NonNull
        @Override
        public DoctorsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doctor, parent, false);
            return new DoctorsViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull DoctorsViewHolder holder, int position) {
            Doctor doctor = doctors.get(position);
            holder.doctorName.setText(doctor.getDoctorName());
            holder.doctorEmail.setText(doctor.getDoctorEmail());

            RequestOptions requestOptions = new RequestOptions()
                    .transform(new RoundedCorners(100));

            Glide.with(holder.itemView.getContext())
                    .load(doctor.getDoctorProfilePicture())
                    .apply(requestOptions)
                    .into(holder.doctorPP);

            holder.remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeDoctor(doctor.getDoctorEmail());
                }
            });
        }

        @Override
        public int getItemCount() {
            return doctors.size();
        }

        class DoctorsViewHolder extends RecyclerView.ViewHolder {

            TextView doctorName, doctorEmail;
            CircleImageView doctorPP;
            Button remove;

            public DoctorsViewHolder(@NonNull View itemView) {
                super(itemView);
                doctorName = itemView.findViewById(R.id.name);
                doctorEmail = itemView.findViewById(R.id.email);
                doctorPP = itemView.findViewById(R.id.pp);
                remove = itemView.findViewById(R.id.remove);
            }
        }
    }

    class Doctor {
        private String doctorName;
        private String doctorEmail;
        private String doctorProfilePicture;

        public void setDoctorEmail(String doctorEmail) {
            this.doctorEmail = doctorEmail;
        }

        public void setDoctorName(String doctorName) {
            this.doctorName = doctorName;
        }

        public void setDoctorProfilePicture(String doctorProfilePicture) {
            this.doctorProfilePicture = doctorProfilePicture;
        }

        public String getDoctorEmail() {
            return doctorEmail;
        }

        public String getDoctorName() {
            return doctorName;
        }

        public String getDoctorProfilePicture() {
            return doctorProfilePicture;
        }
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
}
