package com.hospital.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

public class User {

    Context context;
    SharedPreferences pref;

    BaseHttpClient client;

    public String username = "";
    public String pp = "";

    public User(Context c) {
        context = c;

        pref = c.getSharedPreferences("User", Context.MODE_PRIVATE);

        client = new BaseHttpClient();
    }

    public String getToken(){
        return pref.getString("token", "");
    }

    public void getUsername(UsernameCallback callback) {
        JSONObject data = new JSONObject();

        try {
            data.put("id", this.getToken());
        } catch (Exception e) {
            Log.e("Username JSONObject Error: ", e.getMessage().toString());
        }

        client.post("/api/v1/user", data, new BaseHttpClient.JsonResponseCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    int status = response.getInt("status");

                    if (status == 200) {
                        username = response.getJSONObject("user").getString("username");
                        callback.onSuccess(username);
                    } else {
                        callback.onFailure("User not found");
                    }
                } catch (Exception e) {
                    Log.e("Username Success Error: ", e.getMessage().toString());
                    callback.onFailure(e.getMessage());
                }
            }

            @Override
            public void onSuccess(JSONArray arrayResponse) {

            }

            @Override
            public void onError(Exception e) {
                Log.e("Username Error: ", e.getMessage().toString());
                callback.onFailure(e.getMessage());
            }
        });
    }

    public interface UsernameCallback {
        void onSuccess(String username);
        void onFailure(String errorMessage);
    }


    public void getPp(PpCallback callback) {
        JSONObject data = new JSONObject();

        try {
            data.put("id", this.getToken());
        } catch (Exception e) {
            Log.e("PP JSONObject Error: ", e.getMessage().toString());
        }

        client.post("/api/v1/user", data, new BaseHttpClient.JsonResponseCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    int status = response.getInt("status");

                    if (status == 200) {
                        pp = response.getJSONObject("user").getString("pp");
                        callback.onSuccess(pp);
                    } else {
                        callback.onFailure("Profile picture not found");
                    }
                } catch (Exception e) {
                    Log.e("PP Success Error", e.getMessage().toString());
                    callback.onFailure(e.getMessage());
                }
            }

            @Override
            public void onSuccess(JSONArray arrayResponse) {

            }

            @Override
            public void onError(Exception e) {
                Log.e("PP Error: ", e.getMessage().toString());
                callback.onFailure(e.getMessage());
            }
        });
    }

    public interface PpCallback {
        void onSuccess(String ppUrl);
        void onFailure(String errorMessage);
    }

}
