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

public class sign extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private CheckBox idCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.sign);

        // 입력 칸 초기화
        emailEditText = findViewById(R.id.editTextTextEmailAddress2);
        passwordEditText = findViewById(R.id.editTextTextPassword2);

        Button loginButton = findViewById(R.id.registerButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // POST 요청 비동기 실행
                if (!idCheckBox.isChecked()) {
                    // 체크박스가 체크되지 않았다면 토스트 메시지로 사용자에게 알림
                    Toast.makeText(sign.this, "아이디 중복 확인을 완료해주세요.", Toast.LENGTH_SHORT).show();
                    return; // 클릭 이벤트 종료
                }

                // POST 요청 비동기 실행 (체크박스가 체크된 경우만)
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sendPostRequest();
                    }
                }).start();
            }
        });

        idCheckBox = findViewById(R.id.checkBox);  // 체크박스 ID

        // 체크박스 클릭 리스너 설정
        idCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // 체크박스 클릭 시 바로 피드백을 주고 비동기 처리
                runOnUiThread(() -> {
                    emailEditText.setEnabled(false); // 비활성화 (UI 상 회색으로 변경됨)
                    emailEditText.setFocusable(false); // 포커스를 받을 수 없도록 설정
                    idCheckBox.setEnabled(false); // 체크박스를 비활성화 (중복 체크 중에 클릭 방지)
                });
                // 새로운 스레드에서 중복 체크 요청
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        checkIdDuplicate();
                    }
                }).start();
            } else {
                // 체크박스를 해제한 경우
                runOnUiThread(() -> {
                    idCheckBox.setEnabled(true); // 체크박스를 다시 활성화
                    emailEditText.setEnabled(true); // 활성화
                    emailEditText.setFocusableInTouchMode(true);
                });
            }
        });
    }

    private void checkIdDuplicate() {
        String urlString = "http://" + getString(R.string.server_ip) + ":8080/api/question/check";  // 서버 URL
        String username = emailEditText.getText().toString().trim();  // 이메일 입력값

        // JSON 데이터 생성
        String jsonData = "{\n" +
                "    \"username\": \"" + username + "\"\n" +
                "}";

        try {
            // 서버 URL 연결
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // HTTP 요청 설정
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
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

    private void sendPostRequest() {
        String urlString = "http://" + getString(R.string.server_ip) + ":8080/api/question/sign";  // 서버 URL
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
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // HTTP 요청 설정
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
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

            conn.disconnect();
        } catch (Exception e) {
            // 예외 발생 시 로그 출력
            Log.e("PostRequestError", "POST 요청 실패", e);
        }
    }
}
