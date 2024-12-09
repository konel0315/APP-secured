package com.example.basicapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "chat.db";
    private static final int DATABASE_VERSION = 1;

    // 테이블 생성 쿼리
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS messages (id INTEGER PRIMARY KEY AUTOINCREMENT, sender TEXT, message TEXT)";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 데이터베이스 업그레이드 시 호출
        db.execSQL("DROP TABLE IF EXISTS messages");
        onCreate(db);
    }

    // 메시지 저장
    public void saveMessage(String sender, String message) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO messages (sender, message) VALUES (?, ?)", new Object[]{sender, message});
        db.close();
    }

    // 모든 메시지 읽기
    public Cursor getAllMessages() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT sender, message FROM messages ORDER BY id ASC", null);
    }
    public void close() {
        super.close();
    }
}
