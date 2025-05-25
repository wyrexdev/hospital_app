package com.hospital.app;

import android.content.Context;

public class Utils {

    public static String CDN_URL = "https://cdn.kisetsuna.com/";

    private static WebSocketManager webSocketManager = null;

    public static WebSocketManager getInstance(Context context) {
        if (webSocketManager == null) {
            synchronized (Utils.class) {
                if (webSocketManager == null) {
                    String token = context.getSharedPreferences("User", Context.MODE_PRIVATE).getString("token", "00000000-0000-0000-0000-000000000000");
                    webSocketManager = new WebSocketManager(context);
                    webSocketManager.connect(token);
                }
            }
        }

        return webSocketManager;
    }
}
