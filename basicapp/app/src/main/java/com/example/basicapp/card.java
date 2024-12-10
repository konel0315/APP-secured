package com.example.basicapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.InputStreamReader;
import android.util.Log;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;



public class card extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_ui);

        Button btn4Coins = findViewById(R.id.btn_4_coins);
        Button btn10Coins = findViewById(R.id.btn_10_coins);
        Button btn30Coins = findViewById(R.id.btn_30_coins);
        Button btn50Coins = findViewById(R.id.btn_50_coins);

        btn4Coins.setOnClickListener(view -> showPaymentDialog(4));
        btn10Coins.setOnClickListener(view -> showPaymentDialog(10));
        btn30Coins.setOnClickListener(view -> showPaymentDialog(30));
        btn50Coins.setOnClickListener(view -> showPaymentDialog(50));
    }

    private void showPaymentDialog(int coinAmount) {
        // 다이얼로그 레이아웃 설정
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_payment, null);

        EditText edtCardNumber = dialogView.findViewById(R.id.edt_card_number);
        EditText edtExpiryDate = dialogView.findViewById(R.id.edt_expiry_date);
        EditText edtCVC = dialogView.findViewById(R.id.edt_cvc);

        new AlertDialog.Builder(this)
                .setTitle(coinAmount + "코인 구매")
                .setView(dialogView)
                .setPositiveButton("결제", (dialog, which) -> {
                    String cardNumber = edtCardNumber.getText().toString();
                    String expiryDate = edtExpiryDate.getText().toString();
                    String cvc = edtCVC.getText().toString();

                    // 결제 정보 검증
                    if (validatePaymentInfo(cardNumber, expiryDate, cvc)) {
                        // 비동기 처리 예시 (예: 결제 API 호출)
                        processPayment(coinAmount, cardNumber, expiryDate, cvc);
                    } else {
                        Toast.makeText(this, "결제 정보가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private boolean validatePaymentInfo(String cardNumber, String expiryDate, String cvc) {
        // 카드 번호 길이, CVC 길이, 만료일 등 기본적인 검증
        return cardNumber.length() == 16 && cvc.length() == 3 && !expiryDate.isEmpty();
    }

    private void processPayment(int coinAmount, String cardNumber, String expiryDate, String cvc) {
        // 서버 URL 설정
        String urlString = "http://" + getString(R.string.server_ip) + ":8443/api/card/validate";  // 서버 URL (비워둠)

        // 카드 정보 JSON 데이터 생성
        String jsonData = "{\n" +
                "    \"cardNumber\": \"" + cardNumber + "\",\n" +
                "    \"cvc\": \"" + cvc + "\",\n" +
                "    \"expiryDate\": \"" + expiryDate + "\"\n" +
                "}";

        // 비동기 처리 (백그라운드 스레드에서 네트워크 요청 수행)
        new Thread(() -> {
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

                    // 서버 응답 확인 및 Toast 메시지
                    runOnUiThread(() -> {
                        if (response.toString().equals("카드 정보가 처리되었습니다!")) {
                            Toast.makeText(this, coinAmount + "코인 구매 완료!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "결제 처리 실패", Toast.LENGTH_SHORT).show();
                        }
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
        }).start();
    }
}

