package com.example.basicapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class warning extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db_warning);  // 경고 화면 레이아웃 연결
    }

    @Override
    public void onBackPressed() {
        // 뒤로 가기 버튼을 눌러도 아무 일도 일어나지 않도록 함
        // 아무 동작도 하지 않음
        super.onBackPressed();
    }
}
