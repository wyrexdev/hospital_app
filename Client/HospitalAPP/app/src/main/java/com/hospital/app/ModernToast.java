package com.hospital.app;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ModernToast {

    public static void showCustomToast(Context context, String message) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.toast_layout, null);

        Typeface nunitoBold = Typeface.createFromAsset(context.getAssets(), "Nunito-Bold.ttf");

        TextView text = layout.findViewById(R.id.toast_message);
        text.setText(message);

        text.setTypeface(nunitoBold);

        Toast toast = new Toast(context.getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
}
