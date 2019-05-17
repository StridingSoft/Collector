package com.lobotino.collector;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AsyncSetItemStatus extends AsyncTask<String, Void, Void>
{
    private int itemId;
    private Context context;
    private Connection connection;
    private SQLiteDatabase mDb;

    public AsyncSetItemStatus(int itemId, Context context) {
        mDb = NavigationActivity.dbHandler.getDataBase();
        this.context = context;
        this.itemId = itemId;
    }

    @Override
    protected Void doInBackground(String... strings) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbHandler.KEY_ITEM_STATUS, strings[0]);
        mDb.update(DbHandler.TABLE_ITEMS, contentValues, DbHandler.KEY_ID + " = " + itemId, null);

        if(DbHandler.isOnline(context)) {
            if (DbHandler.needToReconnect) {
                try {
                    DbHandler.setNewConnection(DriverManager.getConnection(DbHandler.MSSQL_DB, DbHandler.MSSQL_LOGIN, DbHandler.MSSQL_PASS));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                connection = DbHandler.getConnection();
            }

            if(connection != null) {
                String status = strings[0];
                String SQL;
                PreparedStatement prSt = null;
                try {
                    switch (status) {
                        case "in": {
                            SQL = "insert into " + DbHandler.TABLE_USERS_ITEMS + "(" + DbHandler.KEY_USER_ID + "," + DbHandler.KEY_ITEM_ID +
                                    ") values(" + DbHandler.USER_ID + "," + itemId + ")";
                            prSt = connection.prepareStatement(SQL);
                            prSt.executeUpdate();
                            break;
                        }
                        case "missing": {
                            SQL = "delete from " + DbHandler.TABLE_USERS_ITEMS + " where " + DbHandler.KEY_USER_ID + " = " + DbHandler.USER_ID + " and " +
                                    DbHandler.KEY_ITEM_ID + " = " + itemId;
                            prSt = connection.prepareStatement(SQL);
                            prSt.executeUpdate();
                            break;
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if(prSt != null) prSt.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if(prSt != null) prSt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }
}
