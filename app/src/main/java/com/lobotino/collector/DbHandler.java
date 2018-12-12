package com.lobotino.collector;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHandler extends SQLiteOpenHelper{

   public static final int DATABASE_VERSION = 3;
   public static final String DATABASE_NAME = "collectionsDb";
   public static final String TABLE_NAME = "collections";
   public static final String TABLE_USERS = "users";

   public static final String KEY_LOGIN = "_login";
   public static final String KEY_USER_ID = "_userId";
   public static final String KEY_GLOBAL_USER_ID = "_globalUserId";
   public static final String KEY_USER_SALT = "_userSalt";
   public static final String KEY_PASSWORD_HASH = "_passwordHash";
   public static final String KEY_REGISTER_DATE = "_registerDate";
   public static final String KEY_COLLECTION = "_collection";
   public static final String KEY_SET = "_set";
   public static final String KEY_ELEMENT = "_element";

    public static int globalUserID = 0;

    public DbHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(DbHandler.TABLE_USERS, null, null, null, null, null, null);
        if(cursor.moveToFirst()) {
            globalUserID = cursor.getInt(cursor.getColumnIndex(DbHandler.KEY_GLOBAL_USER_ID));
        }else{
            globalUserID = 0;
        }
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + "("
                + KEY_COLLECTION + " TEXT,"
                + KEY_SET + " TEXT,"
                + KEY_ELEMENT + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_USERS + "("
                + KEY_GLOBAL_USER_ID + " INTEGER,"
                + KEY_USER_ID + " INTEGER,"
                + KEY_LOGIN + " TEXT,"
                + KEY_PASSWORD_HASH + " TEXT,"
                + KEY_USER_SALT + " TEXT,"
                + KEY_REGISTER_DATE + " TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_NAME);
        db.execSQL("drop table if exists " + TABLE_USERS);
        onCreate(db);
    }
}
