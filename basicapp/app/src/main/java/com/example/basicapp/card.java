package com.example.basicapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;


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
        // 실제 결제 로직 처리 (예시)
        // 예시로 네트워크 요청을 하거나, 결제 API를 호출하는 코드를 추가해야 합니다.

        // 예시로 1초 후에 결제 완료 처리
        new Handler().postDelayed(() -> {
            Toast.makeText(this, coinAmount + "코인 구매 완료!", Toast.LENGTH_SHORT).show();
        }, 1000);
    }}

