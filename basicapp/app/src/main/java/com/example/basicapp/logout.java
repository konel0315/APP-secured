package com.example.basicapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class logout extends AppCompatActivity {

    private static final String TAG = "logout";
    private TextView textViewUsername;
    private Button buttonLogout;
    private TokenStorage tokenStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logout);

        tokenStorage = new TokenStorage(this);
        textViewUsername = findViewById(R.id.textViewUsername);
        buttonLogout = findViewById(R.id.buttonLogout);

        String username = tokenStorage.getUsername();
        if (username != null) {
            textViewUsername.setText(username);
        } else {
            textViewUsername.setText("No User");
        }

        buttonLogout.setOnClickListener(v -> {
            WebSocketActivity activity = WebSocketActivity.getInstance();
            if (activity != null) {
                activity.closeWebSocket();
            } else {
                Log.w(TAG, "WebSocketActivity instance is null.");
            }
            tokenStorage.clearToken();
            finish();
        });
    }
}
