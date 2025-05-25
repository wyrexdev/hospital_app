package com.hospital.app;

import static android.content.Context.MODE_PRIVATE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdminMyAppointments extends Fragment {

    RecyclerView appointments;

    Database db;

    ArrayList<Apply> applicationsArray;

    AppointmentsAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_my_appointments, container, false);

        appointments = view.findViewById(R.id.appointments);

        OnBackPressedDispatcher dispatcher = getActivity().getOnBackPressedDispatcher();
        dispatcher.addCallback(getActivity(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        applicationsArray = new ArrayList<>();

        adapter = new AppointmentsAdapter(applicationsArray);
        appointments.setAdapter(adapter);
        appointments.setLayoutManager(new LinearLayoutManager(getActivity()));

        db = new Database(getContext().getApplicationContext(),
                getActivity().getSharedPreferences("User", MODE_PRIVATE).getString("token", ""));

        db.get("appointments", "doctor_id=t-o-k-e-n", new Database.OnCompleteListener() {
            @Override
            public void onSuccess(JSONArray message) throws JSONException, ParseException {
                for(int i = 0; i < message.length(); i++) {
                    JSONObject data = message.getJSONObject(i);

                    Apply app = new Apply();
                    app.setDate(data.getString("appointment_date"));
                    app.setTime(data.getString("appointment_time"));

                    db.get("users", "id=" + data.getString("user_id"), new Database.OnCompleteListener() {
                        @Override
                        public void onSuccess(JSONArray n) throws JSONException, ParseException {
                            JSONObject user = n.getJSONObject(0);

                            app.setUsername(user.getString("name") + " " + user.getString("surname"));
                            app.setProfilePicture(Utils.CDN_URL + user.getString("profile_picture"));
                            app.setEmail(user.getString("email"));

                            applicationsArray.add(app);


                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }
                    });
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }
        });
        return view;
    }

    class AppointmentsAdapter extends RecyclerView.Adapter<AppointmentsAdapter.AppointmentsHolder> {

        ArrayList<Apply> applications;

        public AppointmentsAdapter(ArrayList<Apply> applications) {
            this.applications = applications;
        }

        @NonNull
        @Override
        public AppointmentsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new AppointmentsHolder(LayoutInflater.from(getActivity()).inflate(R.layout.appointment, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull AppointmentsHolder holder, int position) {
            RequestOptions requestOptions = new RequestOptions()
                    .transform(new RoundedCorners(100));

            Glide.with(getActivity())
                    .load(applications.get(position).getProfilePicture())
                    .apply(requestOptions)
                    .into(holder.pp);

            holder.name.setText(applications.get(position).getUsername() + " - " + applications.get(position).getDate() + " - " + applications.get(position).getTime());
            holder.email.setText(applications.get(position).getEmail());
        }

        @Override
        public int getItemCount() {
            return applications.size();
        }

        class AppointmentsHolder extends RecyclerView.ViewHolder {

            CircleImageView pp;
            TextView name, email;

            public AppointmentsHolder(@NonNull View itemView) {
                super(itemView);

                pp = itemView.findViewById(R.id.pp);
                name = itemView.findViewById(R.id.name);
                email = itemView.findViewById(R.id.email);
            }
        }
    }

    class Apply {

        public String username;
        public String date;
        public String time;
        public String email;
        public String profilePicture;

        public String getProfilePicture() {
            return profilePicture;
        }

        public String getEmail() {
            return email;
        }

        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }

        public String getUsername() {
            return username;
        }

        public void setProfilePicture(String profilePicture) {
            this.profilePicture = profilePicture;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
}