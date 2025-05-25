package com.hospital.app;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class NetworkUtils {

    public interface OnSuccessCallback {
        void onSuccess(JSONObject response);
    }

    public interface OnErrorCallback {
        void onError(String error);
    }


    public static void uploadJsonAndFileAsync(String requestUrl, JSONObject jsonData, File fileToUpload, String fileName,
                                              OnSuccessCallback onSuccess, OnErrorCallback onError) {

        new Thread(() -> {
            String boundary = UUID.randomUUID().toString();
            String LINE_FEED = "\r\n";
            String charset = "UTF-8";
            HttpURLConnection connection = null;

            try {
                URL url = new URL(requestUrl);
                connection = (HttpURLConnection) url.openConnection();

                connection.setUseCaches(false);
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                OutputStream outputStream = connection.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);

                addJsonPart(writer, boundary, "json", jsonData);

                if (fileToUpload != null) {
                    addFilePart(writer, outputStream, boundary, fileName, fileToUpload);
                }

                writer.append("--").append(boundary).append("--").append(LINE_FEED);
                writer.flush();
                writer.close();

                int status = connection.getResponseCode();

                if (status == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder responseStringBuilder = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        responseStringBuilder.append(line);
                    }

                    reader.close();

                    JSONObject jsonResponse = new JSONObject(responseStringBuilder.toString());

                    if (onSuccess != null) {
                        onSuccess.onSuccess(jsonResponse);
                    }

                } else {
                    if (onError != null) {
                        onError.onError("Server returned non-OK status: " + status);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (onError != null) {
                    onError.onError("Error: " + e.getMessage());
                }

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

        }).start();
    }

    private static void addJsonPart(PrintWriter writer, String boundary, String fieldName, JSONObject jsonData) {
        String LINE_FEED = "\r\n";
        writer.append("--").append(boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"").append(fieldName).append("\"").append(LINE_FEED);
        writer.append("Content-Type: application/json; charset=UTF-8").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(jsonData.toString()).append(LINE_FEED);
        writer.flush();
    }

    private static void addFilePart(PrintWriter writer, OutputStream outputStream, String boundary, String fieldName, File uploadFile) throws IOException {
        String LINE_FEED = "\r\n";
        String fileName = uploadFile.getName();
        writer.append("--").append(boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"").append(fieldName)
                .append("\"; filename=\"").append(fileName).append("\"").append(LINE_FEED);
        writer.append("Content-Type: ").append("application/octet-stream").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();

        writer.append(LINE_FEED);
        writer.flush();
    }
}