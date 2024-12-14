package com.example.basicapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

public class sign extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private CheckBox idCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign);

        emailEditText = findViewById(R.id.editTextTextEmailAddress2);
        passwordEditText = findViewById(R.id.editTextTextPassword2);
        idCheckBox = findViewById(R.id.checkBox);

        Button loginButton = findViewById(R.id.registerButton);
        loginButton.setOnClickListener(v -> {
            if (!idCheckBox.isChecked()) {
                Toast.makeText(sign.this, "아이디 중복 확인을 완료해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            String password = passwordEditText.getText().toString().trim();
            String validationMessage = validatePassword(password);

            if (!validationMessage.isEmpty()) {
                Toast.makeText(sign.this, validationMessage, Toast.LENGTH_LONG).show();
                return;
            }

            // 중복 아이디 확인
            duplipost((isValid, username) -> {
                if (isValid) {
                    // 중복되지 않으면 회원가입 요청
                    new Thread(this::sendPostRequest).start();
                } else {
                    // 중복된 아이디일 경우
                    Toast.makeText(sign.this, "중복된 아이디입니다.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        idCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                runOnUiThread(() -> {
                    emailEditText.setEnabled(false);
                    emailEditText.setFocusable(false);
                    idCheckBox.setEnabled(false);
                });
                new Thread(this::checkIdDuplicate).start();
            } else {
                runOnUiThread(() -> {
                    idCheckBox.setEnabled(true);
                    emailEditText.setEnabled(true);
                    emailEditText.setFocusableInTouchMode(true);
                });
            }
        });
    }

    // 비밀번호 규칙 검증 메서드
    private String validatePassword(String password) {
        StringBuilder message = new StringBuilder();

        if (password.length() < 10) {
            message.append("비밀번호는 최소 10자리 이상이어야 합니다.\n");
        }

        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");

        int charTypeCount = 0;
        if (hasUppercase) charTypeCount++;
        if (hasLowercase) charTypeCount++;
        if (hasDigit) charTypeCount++;
        if (hasSpecialChar) charTypeCount++;

        if (charTypeCount < 2) {
            message.append("비밀번호는 두 종류 이상의 문자(대문자, 소문자, 숫자, 특수문자)를 포함해야 합니다.\n");
        }

        if (password.matches("^\\d+$")) {
            message.append("비밀번호는 숫자로만 구성될 수 없습니다.\n");
        }

        return message.toString().trim(); // 최종 메시지를 반환
    }

    private void checkIdDuplicate() {
        String urlString = "https://" + getString(R.string.server_ip) + ":8443/api/question/check";  // 서버 URL
        String username = emailEditText.getText().toString().trim();  // 이메일 입력값

        String jsonData = "{\n" +
                "    \"username\": \"" + username + "\"\n" +
                "}";

        try {
            URL url = new URL(urlString);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            SSLContext sslContext = SSLUtill.createSSLContext(sign.this);
            if (sslContext != null) {
                conn.setSSLSocketFactory(sslContext.getSocketFactory());
            }
            conn.setHostnameVerifier((hostname, session) -> true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Authorization", "your-fixed-secret-key-12345");
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            os.write(jsonData.getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

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

                runOnUiThread(() -> {
                    if (response.toString().equals("0")) {
                        idCheckBox.setChecked(true); // 아이디가 중복되지 않으면 체크
                    } else {
                        idCheckBox.setChecked(false); // 아이디가 중복되면 체크 해제
                        Toast.makeText(sign.this, "중복된 id입니다.", Toast.LENGTH_SHORT).show();
                    }
                    idCheckBox.setEnabled(true); // 요청이 완료되면 체크박스를 다시 활성화
                });
            } else {
                Log.e("PostRequestError", "POST 요청 실패: 응답 코드 " + responseCode);
            }
            conn.disconnect();
        } catch (Exception e) {
            Log.e("PostRequestError", "POST 요청 실패", e);
        }
    }

    private void sendPostRequest() {
        String urlString = "https://" + getString(R.string.server_ip) + ":8443/api/question/sign";  // 서버 URL
        String username = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        String jsonData = "{\n" +
                "    \"username\": \"" + username + "\",\n" +
                "    \"password\": \"" + password + "\"\n" +
                "}";

        try {
            URL url = new URL(urlString);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            SSLContext sslContext = SSLUtill.createSSLContext(sign.this);
            if (sslContext != null) {
                conn.setSSLSocketFactory(sslContext.getSocketFactory());
            }

            conn.setHostnameVerifier((hostname, session) -> true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Authorization", "your-fixed-secret-key-12345");
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            os.write(jsonData.getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

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

                if (response.toString().equals("0")) {
                    finish(); // 회원가입 성공 시 종료
                } else {
                    Log.e("PostRequestError", "서버 응답: " + response.toString());
                }
            } else {
                Log.e("PostRequestError", "POST 요청 실패: 응답 코드 " + responseCode);
            }

        } catch (Exception e) {
            Log.e("PostRequestError", "POST 요청 실패", e);
        }
    }

    private void duplipost(ValidationCallback callback) {
        String urlString = "https://" + getString(R.string.server_ip) + ":8443/api/question/check";
        String username = emailEditText.getText().toString().trim();

        String jsonData = "{\n" +
                "    \"username\": \"" + username + "\"\n" +
                "}";

        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                SSLContext sslContext = SSLUtill.createSSLContext(sign.this);
                if (sslContext != null) {
                    conn.setSSLSocketFactory(sslContext.getSocketFactory());
                }
                conn.setHostnameVerifier((hostname, session) -> true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Authorization", "your-fixed-secret-key-12345");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(jsonData.getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

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

                    runOnUiThread(() -> callback.onValidationResult(response.toString().equals("0"), username));
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("PostRequestError", "POST 요청 실패", e);
            }
        }).start();
    }
    public interface ValidationCallback {
        void onValidationResult(boolean isValid, String username);  // 중복 여부와 사용자명을 전달
    }
}
