package com.hospital.app;

import android.content.Context;

import org.json.JSONException;

public class AppointmentUtils {

    public static void createAppointment (Context a, String userToken, String doctorId) {
        WebSocketManager ws = Utils.getInstance(a);
        ws.sendMessage("5->" + userToken + "|" + doctorId + "|" + "asd");
    }
}
