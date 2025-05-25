package com.hospital.app;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Database {

    WebSocketManager ws;

    String userToken;

    public interface OnCompleteListener {
        void onSuccess(JSONArray message) throws JSONException, ParseException;
        void onError(Throwable throwable);
    }

    public interface OnSuccessListener {
        void onSuccess(boolean isDone);
        void onError(Throwable throwable);
    }

    public Database (Context context, String userToken) {
        ws = Utils.getInstance(context);

        this.userToken = userToken;
    }

    public void create(String table, String data, OnSuccessListener listener) {
        ws.sendMessage("7->" + userToken + "|" + table + "|" + data);

        CompletableFuture<Integer> future = new CompletableFuture<>();

        ws.getMessage(new WebSocketManager.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(String message) throws JSONException {
                if(message.startsWith("7->")) {
                    int status = Integer.parseInt(message.split("\\|")[2]);
                    future.complete(status);
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }
        });

        future.whenComplete((message, exception) -> {
            if (exception == null) {
                listener.onSuccess(message == 1);
            } else {
                listener.onError(exception);
            }
        });
    }

    public void get(String table, String where, OnCompleteListener listener) {
        String id = UUID.randomUUID().toString();
        String message = "6->" + id + "|" + userToken + "|" + table + "|" + where;

        ws.sendMessage(message);

        ws.getMessage(new WebSocketManager.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(String message) throws JSONException, ParseException {
                if (message.startsWith("6->")) {
                    String[] parts = message.split("\\|");
                    String incomingId = parts[1];
                    String jsonArrayString = parts[3];

                    if (incomingId.equals(id)) {
                        JSONArray jsonArray = new JSONArray(jsonArrayString);
                        listener.onSuccess(jsonArray);
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }
        });
    }


    public void update(String table, String id, String data, OnSuccessListener listener) {
        ws.sendMessage("8->" + userToken + "|" + table + "|" + id + "|" + data);

        CompletableFuture<Integer> future = new CompletableFuture<>();

        ws.getMessage(new WebSocketManager.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(String message) throws JSONException {
                if(message.startsWith("8->")) {
                    int status = Integer.parseInt(message.split("\\|")[2]);
                    future.complete(status);
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }
        });

        future.whenComplete((message, exception) -> {
            if (exception == null) {
                listener.onSuccess(message == 1);
            } else {
                listener.onError(exception);
            }
        });
    }

    public void delete(String table, String key, OnSuccessListener listener) {
        ws.sendMessage("9->" + userToken + "|" + table + "|" + key);

        CompletableFuture<Integer> future = new CompletableFuture<>();

        ws.getMessage(new WebSocketManager.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(String message) throws JSONException {
                if(message.startsWith("9->")) {
                    int status = Integer.parseInt(message.split("\\|")[2]);
                    future.complete(status);
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }
        });

        future.whenComplete((message, exception) -> {
            if (exception == null) {
                listener.onSuccess(message == 1);
            } else {
                listener.onError(exception);
            }
        });
    }

    public WebSocketManager getWs() {
        return ws;
    }
}
