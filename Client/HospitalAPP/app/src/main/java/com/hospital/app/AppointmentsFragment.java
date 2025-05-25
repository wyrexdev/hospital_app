package com.hospital.app;

import static android.content.Context.MODE_PRIVATE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

public class AppointmentsFragment extends Fragment {

    CustomDatePickerView customDatePicker;

    Database db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointments, container, false);

        db = new Database(getActivity().getApplicationContext(),
                getActivity().getSharedPreferences("User", MODE_PRIVATE).getString("token", ""));

        customDatePicker = view.findViewById(R.id.customDatePicker);

        return view;
    }
}
