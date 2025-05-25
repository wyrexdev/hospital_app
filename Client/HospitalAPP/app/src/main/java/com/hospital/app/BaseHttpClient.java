package com.hospital.app;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BaseHttpClient {

    private static final String BASE_URL = "https://kisetsuna.com";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public interface JsonResponseCallback {
        void onSuccess(JSONObject response);
        void onSuccess(JSONArray arrayResponse);
        void onError(Exception e);
    }

    public void get(String endpoint, JsonResponseCallback callback) {
        String fullUrl = BASE_URL + endpoint;

        executorService.execute(() -> {
            try {
                URL url = new URL(fullUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestProperty("Accept", "application/json");

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String response = readStream(connection);
                    JSONObject jsonResponse = new JSONObject(response);

                    mainThreadHandler.post(() -> callback.onSuccess(jsonResponse));

                } else {
                    throw new Exception("HTTP GET error: " + responseCode);
                }

            } catch (Exception e) {
                mainThreadHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void post(String endpoint, JSONObject jsonBody, JsonResponseCallback callback) {
        String fullUrl = BASE_URL + endpoint;

        executorService.execute(() -> {
            try {
                URL url = new URL(fullUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setDoOutput(true);

                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");

                OutputStream os = new BufferedOutputStream(connection.getOutputStream());
                os.write(jsonBody.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    String response = readStream(connection);

                    mainThreadHandler.post(() -> {
                        try {
                            if (response.trim().startsWith("[")) {
                                JSONArray jsonArray = new JSONArray(response);
                                callback.onSuccess(jsonArray);
                            } else {
                                JSONObject jsonObject = new JSONObject(response);
                                callback.onSuccess(jsonObject);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });

                } else {
                    throw new Exception("HTTP POST error: " + responseCode);
                }

            } catch (Exception e) {
                mainThreadHandler.post(() -> callback.onError(e));
            }
        });
    }

    private String readStream(HttpURLConnection connection) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }
}

