package com.hospital.app;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;

public class WebSocketManager {
    private static final String TAG = "WebSocketManager";
    private static WebSocketManager instance;
    private Socket mSocket;
    private boolean isConnected = false;

    public Context aContext;

    WebSocketManager(Context context) {
        try {
            IO.Options options = new IO.Options();

            options.reconnection = true;
            options.reconnectionDelay = 1000;
            options.reconnectionDelayMax = 5000;

            options.transports = new String[]{ WebSocket.NAME };

            mSocket = IO.socket("https://sock.kisetsuna.com", options);

            aContext = context;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static synchronized WebSocketManager getInstance(Context context) {
        if (instance == null) {
            instance = new WebSocketManager(context);
        }

        return instance;
    }

    public void connect(String token) {
        if (isConnected) {
            Log.d(TAG, "Already connected.");
            return;
        }

        mSocket.connect();

        mSocket.emit("authenticate", token);

        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                isConnected = true;
                Log.d(TAG, "Socket.IO connected.");
            }
        });

        mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                isConnected = false;
                Log.d(TAG, "Socket.IO disconnected.");
            }
        });

        mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.e(TAG, "Socket.IO error: " + args[0]);
            }
        });

        mSocket.on("message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String message = (String) args[0];
                Log.d(TAG, "Received message: " + message);
            }
        });
    }

    public void sendMessage(String message) {
        if (mSocket != null) {
            mSocket.emit("message", message);
            Log.d(TAG, "Message sent: " + message);
        } else {
            Log.e(TAG, "Failed to send message. Not connected.");
        }
    }

    public void close() {
        if (mSocket != null) {
            mSocket.disconnect();
            Log.d(TAG, "Socket.IO closed");
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void getMessage(final OnMessageReceivedListener listener) {
        mSocket.on("message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String message = (String) args[0];
                Log.d(TAG, "Received message: " + message);
                try {
                    listener.onMessageReceived(message);
                } catch (JSONException | ParseException e) {
                    throw new RuntimeException(e);
                }

                Intent intent = new Intent("SOCKET_MESSAGE");
                intent.putExtra("message", message);
                aContext.sendBroadcast(intent);
            }
        });
    }

    public interface OnMessageReceivedListener {
        void onMessageReceived(String message) throws JSONException, ParseException;
        void onError(Throwable throwable);
    }
}
