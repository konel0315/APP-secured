package com.example.basicapp;

import okhttp3.*;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import org.json.JSONException;

public class WebSocketActivity extends AppCompatActivity {

    private static final String TAG = "WebSocketActivity";
    private static WebSocketActivity instance; // 싱글턴 인스턴스
    private OkHttpClient client;
    private WebSocket webSocket;
    private TokenStorage tokenStorage;
    private DBHelper dbHelper;

    private TextView tvReceivedMessages;
    private EditText edtMessage;
    private ScrollView scrollView;
    private boolean isWebSocketClosed = false; // WebSocket 상태 관리

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_ui);

        instance = this; // 싱글턴 인스턴스 설정
        tvReceivedMessages = findViewById(R.id.tvReceivedMessages);
        edtMessage = findViewById(R.id.edtMessage);
        tokenStorage = new TokenStorage(this);
        dbHelper = new DBHelper(this);

        Button btnSendMessage = findViewById(R.id.btnSendMessage);
        client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("ws://" + getString(R.string.server_ip) + ":8080/chat")
                .build();

        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
                String user = tokenStorage.getUsername();
                JSONObject jsonM = new JSONObject();
                try {
                    jsonM.put("type", "check");
                    jsonM.put("message", user);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON error: " + e.getMessage());
                }
                webSocket.send(jsonM.toString());
                Log.d(TAG, "WebSocket connected.");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);

                Log.d(TAG, "Received message: " + text);

                runOnUiThread(() -> {
                    try {
                        appendMessage(text);
                        dbHelper.saveMessage("Not You", text);
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing message: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                super.onFailure(webSocket, t, response);
                Log.e(TAG, "WebSocket error: " + t.getMessage());
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
                isWebSocketClosed = true;
                Log.d(TAG, "WebSocket closed.");
            }
        };

        webSocket = client.newWebSocket(request, listener);

        btnSendMessage.setOnClickListener(v -> {
            String message = edtMessage.getText().toString();
            if (!message.isEmpty() && webSocket != null) {
                JSONObject jsonMessage = new JSONObject();
                String user = tokenStorage.getUsername();
                try {
                    jsonMessage.put("type", "chat");
                    jsonMessage.put("username", user);
                    jsonMessage.put("message", message);
                    dbHelper.saveMessage("You", message);
                    webSocket.send(jsonMessage.toString());
                    edtMessage.setText("");
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to create JSON: " + e.getMessage());
                }
            }
        });

        loadMessages();
    }

    public static WebSocketActivity getInstance() {
        return instance;
    }

    private void appendMessage(String message) {
        if (tvReceivedMessages != null) {
            String currentText = tvReceivedMessages.getText().toString();
            tvReceivedMessages.setText(currentText + "\n" + message);
        } else {
            Log.e(TAG, "tvReceivedMessages is null");
        }
    }

    private void loadMessages() {
        Cursor cursor = dbHelper.getAllMessages();
        if (cursor != null) {
            try {
                int senderColumnIndex = cursor.getColumnIndex("sender");
                int messageColumnIndex = cursor.getColumnIndex("message");

                if (senderColumnIndex == -1 || messageColumnIndex == -1) {
                    Log.e(TAG, "Invalid column indices");
                    return;
                }

                while (cursor.moveToNext()) {
                    String sender = cursor.getString(senderColumnIndex);
                    String message = cursor.getString(messageColumnIndex);
                    appendMessage(sender + ": " + message);
                }
            } finally {
                cursor.close();
            }
        } else {
            Log.w(TAG, "Cursor is null or no messages found.");
        }
    }

    public void closeWebSocket() {
        if (webSocket != null && !isWebSocketClosed) {
            String user = tokenStorage.getUsername();
            JSONObject jsonM = new JSONObject();
            try {
                jsonM.put("type", "out");
                jsonM.put("message", user);
            } catch (JSONException e) {
                Log.e(TAG, "JSON error: " + e.getMessage());
            }

            webSocket.send(jsonM.toString());
            webSocket.close(1000, "User logged out");
            isWebSocketClosed = true;
            Log.d(TAG, "WebSocket disconnected.");
        } else {
            Log.w(TAG, "WebSocket is already closed or not initialized.");
        }
    }
}
