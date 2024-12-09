package com.example.basicapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Button b1;
    Button b2;
    Button b3;
    private TokenStorage tokenStorage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.main);
        b1 = (Button) findViewById(R.id.loginButton);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String token = tokenStorage.getToken();
                Log.d("MainActivity", "token : " + token);
                if (token == null || token.isEmpty()) {
                    Intent intent1 = new Intent(getApplicationContext(), login.class);
                    startActivity(intent1);
                } else {
                    // 토큰이 있으면 WebSocketActivity로 이동
                    Intent intent1 = new Intent(getApplicationContext(), logout.class);
                    startActivity(intent1);
                }

            }
        });
        b2 = (Button) findViewById(R.id.matchButton);
        tokenStorage = new TokenStorage(this);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 토큰을 확인 (여기서는 TokenStorage.getToken() 메서드를 사용한다고 가정)
                String token = tokenStorage.getToken();
                Log.d("MainActivity", "token : " + token);
                if (token == null || token.isEmpty()) {
                    // 토큰이 없으면 WebSocketActivity로 이동하지 않음, 대신 메시지 표시
                    Toast.makeText(getApplicationContext(), "토큰이 없습니다. 로그인해주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    // 토큰이 있으면 WebSocketActivity로 이동
                    Intent intent1 = new Intent(getApplicationContext(), WebSocketActivity.class);
                    startActivity(intent1);
                }
            }
        });
        b3 = (Button) findViewById(R.id.paymentButton);
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(getApplicationContext(), card.class);
                startActivity(intent1);
            }
        });
    }}
