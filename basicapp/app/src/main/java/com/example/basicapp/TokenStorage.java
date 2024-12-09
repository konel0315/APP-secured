package com.example.basicapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

public class TokenStorage extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tokens.db";  // 데이터베이스 이름
    private static final int DATABASE_VERSION = 1;  // 데이터베이스 버전

    // 테이블 이름과 컬럼 정의
    public static final String TABLE_TOKENS = "tokens";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_TOKEN = "token";

    // 생성자: 데이터베이스 초기화
    public TokenStorage(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 테이블 생성 SQL
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + TABLE_TOKENS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT NOT NULL, " +
                COLUMN_TOKEN + " TEXT NOT NULL);";
        db.execSQL(createTableSQL);  // 테이블 생성 실행
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 데이터베이스 버전 업그레이드 시 테이블을 삭제하고 새로 생성
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TOKENS);
        onCreate(db);  // 새로 테이블을 생성
    }

    // 토큰 저장 메서드
    public long saveToken(String username, String token) {
        SQLiteDatabase db = this.getWritableDatabase();  // 쓰기 가능한 데이터베이스 열기

        ContentValues values = new ContentValues();  // 새로운 ContentValues 객체 생성
        values.put(COLUMN_USERNAME, username);  // 사용자명 저장
        values.put(COLUMN_TOKEN, token);  // 토큰 저장

        // 이미 존재하는 데이터가 있으면 업데이트하고, 없으면 새로 삽입
        return db.insertWithOnConflict(TABLE_TOKENS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // 토큰 가져오기
    public String getToken() {
        SQLiteDatabase db = this.getReadableDatabase();  // 읽기 가능한 데이터베이스 열기
        String token = null;

        // 토큰 값을 쿼리하여 가져오기
        Cursor cursor = db.query(TABLE_TOKENS, new String[]{COLUMN_TOKEN}, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {  // 결과가 있으면 첫 번째 항목으로 이동
            int tokenIndex = cursor.getColumnIndex(COLUMN_TOKEN);  // 토큰 컬럼의 인덱스 가져오기
            if (tokenIndex != -1) {
                token = cursor.getString(tokenIndex);  // 토큰 값을 가져옴
            }
            cursor.close();  // 커서 닫기
        }
        db.close();
        return token;  // 토큰 반환
    }

    // 사용자명 가져오기
    public String getUsername() {
        SQLiteDatabase db = this.getReadableDatabase();  // 읽기 가능한 데이터베이스 열기
        String username = null;

        // 사용자명 값을 쿼리하여 가져오기
        Cursor cursor = db.query(TABLE_TOKENS, new String[]{COLUMN_USERNAME}, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {  // 결과가 있으면 첫 번째 항목으로 이동
            int usernameIndex = cursor.getColumnIndex(COLUMN_USERNAME);  // 사용자명 컬럼의 인덱스 가져오기
            if (usernameIndex != -1) {
                username = cursor.getString(usernameIndex);  // 사용자명 값을 가져옴
            }
            cursor.close();  // 커서 닫기
        }
        db.close();
        return username;  // 사용자명 반환
    }

    // 토큰 삭제 메서드
    public void clearToken() {
        SQLiteDatabase db = this.getWritableDatabase();  // 쓰기 가능한 데이터베이스 열기
        db.delete(TABLE_TOKENS, null, null);  // 테이블의 모든 레코드 삭제
    }
}
