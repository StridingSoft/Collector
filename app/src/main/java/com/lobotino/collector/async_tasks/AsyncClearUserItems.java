package com.lobotino.collector.async_tasks;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.lobotino.collector.utils.DbHandler;


public class AsyncClearUserItems extends AsyncTask<Void, Void, Void>
{
    private SQLiteDatabase mDb;

    public AsyncClearUserItems(SQLiteDatabase mDb) {
        this.mDb = mDb;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbHandler.KEY_ITEM_STATUS, DbHandler.STATUS_MISS);
        mDb.update(DbHandler.TABLE_ITEMS, contentValues, DbHandler.KEY_ITEM_STATUS + " != ?", new String[]{DbHandler.STATUS_MISS});
        return null;
    }
}
