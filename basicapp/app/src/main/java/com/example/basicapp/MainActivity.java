package com.example.basicapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

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

        // TokenStorage 초기화
        tokenStorage = new TokenStorage(this);

        b1 = findViewById(R.id.loginButton);
        b2 = findViewById(R.id.matchButton);
        b3 = findViewById(R.id.paymentButton);

        b1.setOnClickListener(view -> {
            String token = tokenStorage.getToken(); // 클릭 시마다 최신 토큰을 가져오기
            Log.d("MainActivity", "token : " + token);

            if (token == null || token.isEmpty()) {
                navigateToLogin();
            } else {
                verifyToken(token, isValid -> {
                    if (isValid) {
                        navigateToLogout();
                    } else {
                        navigateToLogin();
                    }
                });
            }
        });

        b2.setOnClickListener(view -> {
            String token = tokenStorage.getToken(); // 클릭 시마다 최신 토큰을 가져오기
            Log.d("MainActivity", "token : " + token);
            if (token == null || token.isEmpty()) {
                Toast.makeText(getApplicationContext(), "토큰이 없습니다. 로그인해주세요.", Toast.LENGTH_SHORT).show();
            } else {
                verifyToken(token, isValid -> {
                    if (isValid) {
                        navigateToWebSocketActivity();
                    } else {
                        Toast.makeText(getApplicationContext(), "토큰이 유효하지 않습니다. 로그인해주세요.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        b3.setOnClickListener(view -> navigateToPayment());
    }

    private void navigateToLogin() {
        Intent intent = new Intent(getApplicationContext(), login.class);
        startActivity(intent);
    }

    private void navigateToLogout() {
        Intent intent = new Intent(getApplicationContext(), logout.class);
        startActivity(intent);
    }

    private void navigateToWebSocketActivity() {
        Intent intent = new Intent(getApplicationContext(), WebSocketActivity.class);
        startActivity(intent);
    }

    private void navigateToPayment() {
        Intent intent = new Intent(getApplicationContext(), card.class);
        startActivity(intent);
    }

    private void verifyToken(String token, TokenValidationCallback callback) {
        new Thread(() -> {
            String urlString = "https://" + getString(R.string.server_ip) + ":8443/api/question/token";
            try {
                URL url = new URL(urlString);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

                // SSL 설정
                SSLContext sslContext = SSLUtill.createSSLContext(MainActivity.this);
                if (sslContext != null) {
                    conn.setSSLSocketFactory(sslContext.getSocketFactory());
                }
                conn.setHostnameVerifier((hostname, session) -> true);
                conn.setRequestMethod("POST");

                // Content-Type을 'text/plain'으로 변경
                conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
                conn.setRequestProperty("Authorization", "your-fixed-secret-key-12345");
                conn.setDoOutput(true);

                // 그냥 token 값만 보내기
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(token.getBytes(StandardCharsets.UTF_8)); // token만 전송
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    boolean isValid = "0".equals(response.toString().trim());
                    runOnUiThread(() -> callback.onValidationResult(isValid));
                } else {
                    Log.e("VerifyTokenError", "POST 요청 실패: 응답 코드 " + responseCode);
                    runOnUiThread(() -> callback.onValidationResult(false));
                }

            } catch (Exception e) {
                Log.e("VerifyTokenException", "토큰 검증 중 오류 발생", e);
                runOnUiThread(() -> callback.onValidationResult(false));
            }
        }).start();
    }

    interface TokenValidationCallback {
        void onValidationResult(boolean isValid);
    }
}
