package com.example.basicapp;

import android.content.Context;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import android.content.ContentValues;
import net.sqlcipher.Cursor;

public class TokenStorage extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tokens.db";  // 데이터베이스 이름
    private static final int DATABASE_VERSION = 1;  // 데이터베이스 버전
    private static final String DATABASE_PASSWORD = "your_secure_password"; // 암호화 키

    // 테이블 이름과 컬럼 정의
    public static final String TABLE_TOKENS = "tokens";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_TOKEN = "token";

    // 생성자: 데이터베이스 초기화
    public TokenStorage(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase.loadLibs(context); // SQLCipher 라이브러리 초기화
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

    // 데이터베이스 암호화에 필요하므로 `getWritableDatabase`와 `getReadableDatabase`를 오버라이드
    public SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase(DATABASE_PASSWORD); // 암호화된 데이터베이스 열기
    }

    public SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase(DATABASE_PASSWORD); // 암호화된 데이터베이스 열기
    }

    // 토큰 저장 메서드
    public long saveToken(String username, String token) {
        SQLiteDatabase db = this.getWritableDatabase();  // 쓰기 가능한 데이터베이스 열기

        // 모든 기존 데이터 삭제
        clearToken();

        // 새로운 토큰 값을 저장
        ContentValues values = new ContentValues();  // 새로운 ContentValues 객체 생성
        values.put(COLUMN_USERNAME, username);  // 사용자명 저장
        values.put(COLUMN_TOKEN, token);  // 토큰 저장

        // 새로 삽입
        long result = db.insert(TABLE_TOKENS, null, values);
        db.close(); // 데이터베이스 닫기
        return result;
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
        db.close(); // 데이터베이스 닫기
    }
}
