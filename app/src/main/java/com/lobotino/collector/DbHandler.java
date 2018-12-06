package com.lobotino.collector;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHandler extends SQLiteOpenHelper{

    public static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "collectionsDb";
    public static final String TABLE_NAME = "collections";

    public static final String KEY_COLLECTION = "_collection";
    public static final String KEY_SET = "_set";
    public static final String KEY_ELEMENT = "_element";


    public DbHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "(" + KEY_COLLECTION
                + " text," + KEY_SET + " text," + KEY_ELEMENT + " text" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("drop table if exists " + TABLE_NAME);
        onCreate(db);
    }
}
