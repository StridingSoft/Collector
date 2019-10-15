package com.lobotino.collector.async_tasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.lobotino.collector.utils.DbHandler;
import com.lobotino.collector.activities.MainActivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.lobotino.collector.activities.MainActivity.dbHandler;

public class AsyncSetItemStatus extends AsyncTask<String, Void, Void>
{
    private int itemId;
    private Context context;
    private Connection connection;
    private SQLiteDatabase mDb;

    public AsyncSetItemStatus(int itemId, Context context) {
        mDb = dbHandler.getDataBase();
        this.context = context;
        this.itemId = itemId;
    }

    @Override
    protected Void doInBackground(String... strings) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbHandler.KEY_ITEM_STATUS, strings[0]);
        mDb.update(DbHandler.TABLE_ITEMS, contentValues, DbHandler.KEY_ID + " = " + itemId, null);

        connection = dbHandler.getConnection(context);

        if (connection != null) {
            String status = strings[0];
            String SQL;
            PreparedStatement prSt = null, prSt1 = null;
            try {
                switch (status) {
                    case DbHandler.STATUS_WISH: {
                        SQL = "insert into " + DbHandler.TABLE_USERS_ITEMS + "(" + DbHandler.KEY_USER_ID + "," + DbHandler.KEY_ITEM_ID + "," + DbHandler.KEY_ITEM_STATUS +
                                ") values(" + DbHandler.USER_ID + ", " + itemId +  ", '" + DbHandler.STATUS_WISH + "')";
                        prSt = connection.prepareStatement(SQL);
                        prSt.executeUpdate();
                        break;
                    }
                    case DbHandler.STATUS_TRADE: {
                        SQL = "update " + DbHandler.TABLE_USERS_ITEMS + " SET " + DbHandler.KEY_ITEM_STATUS + " = '" + DbHandler.STATUS_TRADE +
                                "' where " + DbHandler.KEY_USER_ID + " = " + DbHandler.USER_ID + " and " + DbHandler.KEY_ITEM_ID + " = " + itemId;
                        prSt = connection.prepareStatement(SQL);
                        prSt.executeUpdate();
                        break;
                    }
                    case DbHandler.STATUS_IN: {
                        SQL = "delete from " + DbHandler.TABLE_USERS_ITEMS + " where " + DbHandler.KEY_USER_ID + " = " + DbHandler.USER_ID + " and " +
                                DbHandler.KEY_ITEM_ID + " = " + itemId;
                        prSt = connection.prepareStatement(SQL);
                        prSt.executeUpdate();

                        SQL = "insert into " + DbHandler.TABLE_USERS_ITEMS + "(" + DbHandler.KEY_USER_ID + "," + DbHandler.KEY_ITEM_ID + "," + DbHandler.KEY_ITEM_STATUS +
                                ") values(" + DbHandler.USER_ID + ", " + itemId + ", '" + DbHandler.STATUS_IN + "')";
                        prSt1 = connection.prepareStatement(SQL);
                        prSt1.executeUpdate();

                       // checkIn(itemId);
                        break;
                    }
                    case DbHandler.STATUS_MISS: {
                        SQL = "delete from " + DbHandler.TABLE_USERS_ITEMS + " where " + DbHandler.KEY_USER_ID + " = " + DbHandler.USER_ID + " and " +
                                DbHandler.KEY_ITEM_ID + " = " + itemId;
                        prSt = connection.prepareStatement(SQL);
                        prSt.executeUpdate();
                        //checkMiss(itemId);
                        break;
                    }
                }
                if(prSt != null) prSt.close();
                if(prSt1 != null) prSt1.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (prSt != null) prSt.close();
                    if (prSt1 != null) prSt1.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            try {
                if (prSt != null) prSt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

//    private void checkIn(int itemId) throws SQLException {
//        int secId = getSectiondId(itemId);
//        if(secId == -1) {
//            setSectionStatus(DbHandler.STATUS_IN, secId);
//
//            int collectionId = getCollectionId(secId);
//            if(collectionId == -1)
//                setCollectionStatus(DbHandler.STATUS_IN, collectionId);
//        }
//    }
//
//    private void checkMiss(int itemId) throws SQLException {
//        int secId = getSectiondId(itemId);
//        if(secId != -1) {
//            setSectionStatus(DbHandler.STATUS_MISS, secId);
//
//            int collectionId = getCollectionId(secId);
//            if(collectionId != -1)
//                setCollectionStatus(DbHandler.STATUS_MISS, collectionId);
//        }
//    }

    private int getSectiondId(int itemId) throws SQLException {
        String SQL = "SELECT " + DbHandler.KEY_SECTION_ID + " FROM " + DbHandler.TABLE_ITEMS +
                " WHERE " + DbHandler.KEY_ID + " = " + itemId;
        Statement st1 = connection.createStatement();
        ResultSet rs1 = st1.executeQuery(SQL);
        if(rs1 != null) {
            rs1.next();
            int secId = rs1.getInt(1);
            rs1.close();
            st1.close();
            return secId;
        } else {
            st1.close();
            return -1;
        }
    }

    private int getCollectionId(int secId) throws SQLException
    {
        String SQL = "SELECT " + DbHandler.KEY_COLLECTION_ID + " FROM " + DbHandler.TABLE_SECTIONS +
                " WHERE " + DbHandler.KEY_ID + " = " + secId;
        Statement st1 = connection.createStatement();
        ResultSet rs1 = st1.executeQuery(SQL);
        if(rs1 != null) {
            rs1.next();
            int collectionId = rs1.getInt(1);
            rs1.close();
            st1.close();
            return collectionId;
        } else {
            st1.close();
            return -1;
        }
    }

//    private void setSectionStatus(String status, int itemId) throws SQLException {
//        int secId = getSectiondId(itemId);
//        if(secId == -1) return;
//
//        String SQL = "SELECT " + DbHandler.KEY_USER_ID + " FROM " + DbHandler.TABLE_USERS_SECTIONS + " WHERE " + DbHandler.KEY_USER_ID + " = " +
//                DbHandler.USER_ID + " AND " + DbHandler.KEY_SECTION_ID + " = " + secId;
//        Statement st2 = connection.createStatement();
//        ResultSet rs2 = st2.executeQuery(SQL);
//        if (rs2 != null) {
//            rs2.close();
//        } else {
//            PreparedStatement prSt;
//
//            switch (status) {
//                case DbHandler.STATUS_IN: {
//                    SQL = "insert into " + DbHandler.TABLE_USERS_SECTIONS + "(" + DbHandler.KEY_USER_ID + "," + DbHandler.KEY_SECTION_ID +
//                            ") values(" + DbHandler.USER_ID + ", " + secId + ")";
//                    prSt = connection.prepareStatement(SQL);
//                    prSt.executeUpdate();
//                    break;
//                }
//                case DbHandler.STATUS_MISS: {
//                    SQL = "delete from " + DbHandler.TABLE_USERS_SECTIONS + " where " + DbHandler.KEY_USER_ID + " = " + DbHandler.USER_ID + " and " +
//                            DbHandler.KEY_SECTION_ID + " = " + secId;
//                    prSt = connection.prepareStatement(SQL);
//                    prSt.executeUpdate();
//                    break;
//                }
//            }
//        }
//    }
//
// private void setCollectionStatus(String status, int secId) throws SQLException {
//        int collectionId = getCollectionId(secId);
//        if(collectionId == -1) return;
//
//        String SQL = "SELECT " + DbHandler.KEY_USER_ID + " FROM " + DbHandler.TABLE_USERS_COLLECTIONS + " WHERE " + DbHandler.KEY_USER_ID + " = " +
//                DbHandler.USER_ID + " AND " + DbHandler.KEY_COLLECTION_ID + " = " + collectionId;
//        Statement st2 = connection.createStatement();
//        ResultSet rs2 = st2.executeQuery(SQL);
//        if (rs2 != null) {
//            rs2.close();
//        } else {
//            PreparedStatement prSt = null;
//            switch (status) {
//                case DbHandler.STATUS_IN: {
//                    SQL = "insert into " + DbHandler.TABLE_USERS_COLLECTIONS + "(" + DbHandler.KEY_USER_ID + "," + DbHandler.KEY_COLLECTION_ID +
//                            ") values(" + DbHandler.USER_ID + ", " + collectionId + ")";
//                    prSt = connection.prepareStatement(SQL);
//                    prSt.executeUpdate();
//                    break;
//                }
//                case DbHandler.STATUS_MISS: {
//                    SQL = "delete from " + DbHandler.TABLE_USERS_COLLECTIONS + " where " + DbHandler.KEY_USER_ID + " = " + DbHandler.USER_ID + " and " +
//                            DbHandler.KEY_COLLECTION_ID + " = " + collectionId;
//                    prSt = connection.prepareStatement(SQL);
//                    prSt.executeUpdate();
//                    break;
//                }
//            }
//            if(prSt != null) prSt.close();
//        }
//    }

}
