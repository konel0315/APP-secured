package com.example.basicapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.CheckBox;
import androidx.activity.EdgeToEdge;
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
            if (duplipost() == 1) {
                new Thread(this::sendPostRequest).start();
            }
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

        // 비밀번호 길이 확인
        if (password.length() < 10) {
            message.append("비밀번호는 최소 10자리 이상이어야 합니다.\n");
        }

        // 문자 종류 확인
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

        // 숫자로만 구성되었는지 확인
        if (password.matches("^\\d+$")) {
            message.append("비밀번호는 숫자로만 구성될 수 없습니다.\n");
        }

        return message.toString().trim(); // 최종 메시지를 반환
    }

    private void checkIdDuplicate() {
        String urlString = "https://" + getString(R.string.server_ip) + ":8443/api/question/check";  // 서버 URL
        String username = emailEditText.getText().toString().trim();  // 이메일 입력값

        // JSON 데이터 생성
        String jsonData = "{\n" +
                "    \"username\": \"" + username + "\"\n" +
                "}";

        try {
            // 서버 URL 연결
            URL url = new URL(urlString);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            // SSLContext 설정 (서버 인증서를 신뢰하도록)
            SSLContext sslContext = SSLUtill.createSSLContext(sign.this);
            if (sslContext != null) {
                conn.setSSLSocketFactory(sslContext.getSocketFactory());
            }
            conn.setHostnameVerifier((hostname, session) -> true); // 호스트 이름 검증 비활성화
            // HTTP 요청 설정
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Authorization", "your-fixed-secret-key-12345");
            conn.setDoOutput(true);  // Output stream 사용

            // JSON 데이터 전송
            OutputStream os = conn.getOutputStream();
            os.write(jsonData.getBytes(StandardCharsets.UTF_8));  // UTF-8로 인코딩하여 전송
            os.flush();
            os.close();

            // 응답 코드 확인
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 서버 응답 처리
                InputStream inputStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // 서버 응답을 확인하고, 성공 메시지 표시
                runOnUiThread(() -> {
                    if (response.toString().equals("0")) {
                        // 아이디가 중복되지 않음 (선택된 상태 유지)
                        idCheckBox.setChecked(true);
                    } else {
                        // 아이디가 중복되면 체크박스 상태 해제
                        idCheckBox.setChecked(false);
                        Toast.makeText(sign.this, "중복된 id입니다.", Toast.LENGTH_SHORT).show();
                    }
                    idCheckBox.setEnabled(true); // 요청이 완료되면 체크박스를 다시 활성화
                });
            } else {
                // 응답 실패
                Log.e("PostRequestError", "POST 요청 실패: 응답 코드 " + responseCode);
            }

            conn.disconnect();
        } catch (Exception e) {
            // 예외 발생 시 로그 출력
            Log.e("PostRequestError", "POST 요청 실패", e);
        }
    }

    private int duplipost() {
        String urlString = "https://" + getString(R.string.server_ip) + ":8443/api/question/token";  // 서버 URL
        String username = emailEditText.getText().toString().trim();  // 이메일 입력값

        // JSON 데이터 생성
        String jsonData = "{\n" +
                "    \"username\": \"" + username + "\"\n" +
                "}";

        try {
            // 서버 URL 연결
            URL url = new URL(urlString);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            // SSLContext 설정 (서버 인증서를 신뢰하도록)
            SSLContext sslContext = SSLUtill.createSSLContext(sign.this);
            if (sslContext != null) {
                conn.setSSLSocketFactory(sslContext.getSocketFactory());
            }

            conn.setHostnameVerifier((hostname, session) -> true); // 호스트 이름 검증 비활성화
            // HTTP 요청 설정
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Authorization", "your-fixed-secret-key-12345");
            conn.setDoOutput(true);  // Output stream 사용

            // JSON 데이터 전송
            OutputStream os = conn.getOutputStream();
            os.write(jsonData.getBytes(StandardCharsets.UTF_8));  // UTF-8로 인코딩하여 전송
            os.flush();
            os.close();

            // 응답 코드 확인
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 서버 응답 처리
                InputStream inputStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // 서버 응답을 확인하고, 성공 메시지 표시
                if (response.toString().equals("0")) {
                    return 1;  // 중복되지 않으면 1 반환
                } else {
                    return 0;  // 중복되면 0 반환
                }
            } else {
                // 응답 실패
                Log.e("PostRequestError", "POST 요청 실패: 응답 코드 " + responseCode);
                return 0;
            }

        } catch (Exception e) {
            // 예외 발생 시 로그 출력
            Log.e("PostRequestError", "POST 요청 실패", e);
            return 0;
        }
    }

    private void sendPostRequest() {
        String urlString = "https://" + getString(R.string.server_ip) + ":8443/api/question/sign";  // 서버 URL
        String username = emailEditText.getText().toString().trim();  // 이메일 입력값
        String password = passwordEditText.getText().toString().trim();  // 비밀번호 입력값

        // JSON 데이터 생성
        String jsonData = "{\n" +
                "    \"username\": \"" + username + "\",\n" +
                "    \"password\": \"" + password + "\"\n" +
                "}";

        try {
            // 서버 URL 연결
            URL url = new URL(urlString);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            // SSLContext 설정 (서버 인증서를 신뢰하도록)
            SSLContext sslContext = SSLUtill.createSSLContext(sign.this);
            if (sslContext != null) {
                conn.setSSLSocketFactory(sslContext.getSocketFactory());
            }

            conn.setHostnameVerifier((hostname, session) -> true); // 호스트 이름 검증 비활성화
            // HTTP 요청 설정
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Authorization", "your-fixed-secret-key-12345");
            conn.setDoOutput(true);  // Output stream 사용

            // JSON 데이터 전송
            OutputStream os = conn.getOutputStream();
            os.write(jsonData.getBytes(StandardCharsets.UTF_8));  // UTF-8로 인코딩하여 전송
            os.flush();
            os.close();

            // 응답 코드 확인
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 서버 응답 처리
                InputStream inputStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // 서버 응답을 확인하고, 성공 메시지 표시
                if (response.toString().equals("0")) {
                    // 회원가입 성공 시 종료
                    finish();
                } else {
                    // 응답이 0이 아닌 경우 다른 처리
                    Log.e("PostRequestError", "서버 응답: " + response.toString());
                }
            } else {
                // 응답 실패
                Log.e("PostRequestError", "POST 요청 실패: 응답 코드 " + responseCode);
            }

        } catch (Exception e) {
            // 예외 발생 시 로그 출력
            Log.e("PostRequestError", "POST 요청 실패", e);
        }
    }}