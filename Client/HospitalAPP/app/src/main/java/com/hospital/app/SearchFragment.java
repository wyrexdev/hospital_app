package com.hospital.app;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

import java.text.ParseException;
import java.util.ArrayList;

public class SearchFragment extends Fragment {

    TextInputEditText doctorName;
    RecyclerView recyclerView;

    Database db;

    ArrayList<DoctorItem> doctorItemArrayList = new ArrayList<>();
    ArrayList<DoctorItem> doctorsTop = new ArrayList<>();

    DoctorsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        doctorName = view.findViewById(R.id.doctor);
        recyclerView = view.findViewById(R.id.doctors);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new DoctorsAdapter(doctorsTop);
        recyclerView.setAdapter(adapter);

        String token = getActivity().getSharedPreferences("User", MODE_PRIVATE).getString("token", "");
        db = new Database(getActivity().getApplicationContext(), token);

        db.get("users", "user_type=1", new Database.OnCompleteListener() {
            @Override
            public void onSuccess(JSONArray message) throws JSONException, ParseException {
                doctorItemArrayList.clear();
                for (int i = 0; i < message.length(); i++) {
                    DoctorItem doctor = new DoctorItem();
                    doctor.setName(message.getJSONObject(i).getString("name") + " " + message.getJSONObject(i).getString("surname"));
                    doctor.setPp(Utils.CDN_URL + message.getJSONObject(i).getString("profile_picture"));
                    doctor.setUuid(message.getJSONObject(i).getString("id"));
                    doctorItemArrayList.add(doctor);
                }

                doctorsTop.clear();
                int topCount = Math.min(doctorItemArrayList.size(), 20);
                for (int i = 0; i < topCount; i++) {
                    doctorsTop.add(doctorItemArrayList.get(i));
                }

                adapter.updateList(new ArrayList<>(doctorsTop));
            }

            @Override
            public void onError(Throwable throwable) {
                // error handling
            }
        });

        doctorName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable editable) {
                String searchText = editable.toString().toLowerCase();
                ArrayList<DoctorItem> filteredList = new ArrayList<>();

                if (searchText.isEmpty()) {
                    filteredList.addAll(doctorsTop);
                } else {
                    for (DoctorItem doctor : doctorItemArrayList) {
                        if (doctor.getName().toLowerCase().contains(searchText)) {
                            filteredList.add(doctor);
                        }
                    }
                }

                adapter.updateList(filteredList);
            }
        });

        return view;
    }

    class DoctorsAdapter extends RecyclerView.Adapter<DoctorsAdapter.DoctorsViewHolder> {

        ArrayList<DoctorItem> doctors;

        public DoctorsAdapter(ArrayList<DoctorItem> doctors) {
            this.doctors = doctors;
        }

        public void updateList(ArrayList<DoctorItem> newDoctors) {
            this.doctors = newDoctors;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public DoctorsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new DoctorsViewHolder(LayoutInflater.from(getActivity().getApplicationContext())
                    .inflate(R.layout.search_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull DoctorsViewHolder holder, int position) {
            holder.doctorName.setText(doctors.get(position).getName());

            RequestOptions requestOptions = new RequestOptions()
                    .transform(new RoundedCorners(100));

            Glide.with(holder.itemView.getContext())
                    .load(doctors.get(position).getPp())
                    .apply(requestOptions)
                    .into(holder.doctorImage);

            holder.main.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent();
                    i.setClass(getActivity().getApplicationContext(), Doctor.class);
                    i.putExtra("uuid", doctors.get(position).getUuid());
                    startActivity(i);
                }
            });
        }

        @Override
        public int getItemCount() {
            return doctors.size();
        }

        class DoctorsViewHolder extends RecyclerView.ViewHolder {
            TextView doctorName;
            ImageView doctorImage;
            LinearLayout main;

            public DoctorsViewHolder(@NonNull View itemView) {
                super(itemView);
                doctorImage = itemView.findViewById(R.id.pp);
                doctorName = itemView.findViewById(R.id.name);
                main = itemView.findViewById(R.id.main);
            }
        }
    }

    class DoctorItem {
        public String name;
        public String pp;
        public String uuid;

        public void setName(String name) { this.name = name; }
        public void setPp(String pp) { this.pp = pp; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getName() { return name; }
        public String getPp() { return pp; }
        public String getUuid() { return uuid; }
    }
}
