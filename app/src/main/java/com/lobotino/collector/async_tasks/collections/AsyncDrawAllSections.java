package com.lobotino.collector.async_tasks.collections;


import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.RelativeLayout;

import com.lobotino.collector.utils.DbHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static com.lobotino.collector.activities.MainActivity.dbHandler;
import static com.lobotino.collector.fragments.CollectionsFragment.fragmentType;
import static com.lobotino.collector.fragments.CollectionsFragment.offers;
import static com.lobotino.collector.fragments.CollectionsFragment.currentCollection;
import static com.lobotino.collector.fragments.CollectionsFragment.collectionTitle;
import static com.lobotino.collector.fragments.CollectionsFragment.fragmentStatus;
import static com.lobotino.collector.fragments.CollectionsFragment.lastLeftId ;
import static com.lobotino.collector.fragments.CollectionsFragment.lastRightId;
import static com.lobotino.collector.fragments.CollectionsFragment.countImages;
import static com.lobotino.collector.fragments.CollectionsFragment.currentId;

public class AsyncDrawAllSections extends AsyncTask<Void, Object, String>
{
    private int collectionId;
    private String SQL, collectionName;
    private ActionBar actionBar;
    private SQLiteDatabase mDb;
    private Context context;
    private FragmentManager fm;
    private int userId = -1;
    private RelativeLayout layout;

    public AsyncDrawAllSections(RelativeLayout layout, int collectionId, String collectionName, Context context, SQLiteDatabase mDb,  ActionBar actionBar, FragmentManager fm) {
        this.layout = layout;
        this.collectionId = collectionId;
        this.collectionName = collectionName;
        this.actionBar = actionBar;
        this.mDb = mDb;
        this.context = context;
        this.fm = fm;
        currentCollection = collectionId;
    }

    public AsyncDrawAllSections(RelativeLayout layout, int collectionId, String collectionName, Context context, SQLiteDatabase mDb, ActionBar actionBar, FragmentManager fm, int userId) {
        this.layout = layout;
        this.collectionId = collectionId;
        this.collectionName = collectionName;
        this.actionBar = actionBar;
        this.mDb = mDb;
        this.context = context;
        this.fm = fm;
        this.userId = userId;
        currentCollection = collectionId;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        actionBar.setTitle(collectionName);
        collectionTitle = collectionName;
        fragmentStatus = "collection";
        offers.clear();
        layout.removeAllViews();
        lastLeftId = -1;
        lastRightId = -1;
        countImages = 0;
        currentId = 0;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            Statement stSections = null;
            Statement stItems = null;
            ResultSet rsSections = null;
            ResultSet rsItems = null;
            try {
                if (fragmentType.equals(DbHandler.MY_COLLECTIONS) || !DbHandler.isOnline(context)) {
                    Cursor cursorSections = mDb.query(DbHandler.TABLE_SECTIONS, new String[]{DbHandler.KEY_ID, DbHandler.KEY_NAME}, DbHandler.KEY_COLLECTION_ID + " = " + collectionId, null, null, null, null);
                    if (cursorSections.getCount() > 0 && cursorSections.moveToFirst()) {
                        do {
                            String name = cursorSections.getString(cursorSections.getColumnIndex(DbHandler.KEY_NAME));
                            int secId = cursorSections.getInt(cursorSections.getColumnIndex(DbHandler.KEY_ID));
                            Cursor cursorItems;
                            if(fragmentType.equals(DbHandler.COM_COLLECTIONS))
                                cursorItems = mDb.query(DbHandler.TABLE_ITEMS, new String[]{DbHandler.KEY_ID}, DbHandler.KEY_SECTION_ID + " = " + secId, null, null, null, null);
                            else
                                cursorItems = mDb.query(DbHandler.TABLE_ITEMS, new String[]{DbHandler.KEY_ID}, DbHandler.KEY_SECTION_ID + " = ? AND (" + DbHandler.KEY_ITEM_STATUS + " = ? OR " + DbHandler.KEY_ITEM_STATUS + " = ?)", new String[]{secId + "", DbHandler.STATUS_IN, DbHandler.STATUS_TRADE}, null, null, null);

                            if (cursorItems.getCount() > 0 && cursorItems.moveToFirst()) {
                                int idItem = cursorItems.getInt(cursorItems.getColumnIndex(DbHandler.KEY_ID));
                                publishProgress(idItem, secId, name);
                            }
                            cursorItems.close();
                        } while (cursorSections.moveToNext());
                        cursorSections.close();
                    }
                } else {
                    if(fragmentType.equals(DbHandler.COM_COLLECTIONS)) {
                        Connection connection = dbHandler.getConnection(context);
                        if (connection != null) {
                            SQL = "SELECT * FROM " + DbHandler.TABLE_SECTIONS + " WHERE " + DbHandler.KEY_COLLECTION_ID + " = " + collectionId;
                            stSections = connection.createStatement();
                            rsSections = stSections.executeQuery(SQL);
                            if (rsSections != null) {
                                while (rsSections.next()) {
                                    int secId = rsSections.getInt(1);

                                    Cursor cursorSections = mDb.query(DbHandler.TABLE_SECTIONS, new String[]{DbHandler.KEY_ID, DbHandler.KEY_NAME}, DbHandler.KEY_ID + " = " + secId, null, null, null, null);
                                    if (cursorSections.getCount() <= 0) {
                                        int collectionId = rsSections.getInt(2);
                                        String name = rsSections.getString(3);
                                        String desc = rsSections.getString(4);

                                        ContentValues contentValues = new ContentValues();
                                        contentValues.put(DbHandler.KEY_ID, secId);
                                        contentValues.put(DbHandler.KEY_COLLECTION_ID, collectionId);
                                        contentValues.put(DbHandler.KEY_NAME, name);
                                        contentValues.put(DbHandler.KEY_DESCRIPTION, desc);
                                        mDb.insert(DbHandler.TABLE_SECTIONS, null, contentValues);

                                        SQL = "SELECT " + DbHandler.KEY_ID + " FROM " + DbHandler.TABLE_ITEMS + " WHERE " + DbHandler.KEY_SECTION_ID + " = " + secId;
                                        stItems = connection.createStatement();
                                        rsItems = stItems.executeQuery(SQL);
                                        try {
                                            if (rsItems != null) {
                                                rsItems.next();
                                                int itemId = rsItems.getInt(1);
                                                publishProgress(itemId, secId, name);
                                            }
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        if (cursorSections.moveToFirst()) {
                                            String name = cursorSections.getString(cursorSections.getColumnIndex(DbHandler.KEY_NAME));
                                            Cursor cursorItems = mDb.query(DbHandler.TABLE_ITEMS, new String[]{DbHandler.KEY_ID}, DbHandler.KEY_SECTION_ID + " = " + secId, null, null, null, null);
                                            if (cursorItems.moveToFirst()) {
                                                int itemId = cursorItems.getInt(cursorItems.getColumnIndex(DbHandler.KEY_ID));
                                                publishProgress(itemId, secId, name);
                                            }
                                            cursorItems.close();
                                        }
                                    }
                                    cursorSections.close();
                                }
                            }
                        }
                    }else{
                        String SQLstatusEquals = "";
                        switch (fragmentType) {
                            case DbHandler.USER_WISH_COLLECTIONS: {
                                SQL =   "SELECT " + DbHandler.KEY_ID + ", " + DbHandler.KEY_NAME +
                                        " FROM " + DbHandler.TABLE_SECTIONS +
                                        " WHERE " + DbHandler.KEY_ID +
                                        " IN (SELECT " + DbHandler.KEY_SECTION_ID +
                                        " FROM " + DbHandler.TABLE_ITEMS +
                                        " WHERE " + DbHandler.KEY_COLLECTION_ID + " = "  + collectionId+
                                        " AND " + DbHandler.KEY_ID +
                                        " IN (SELECT " + DbHandler.KEY_ITEM_ID +
                                        " FROM " + DbHandler.TABLE_USERS_ITEMS +
                                        " WHERE " + DbHandler.KEY_USER_ID + " = " + userId +
                                        " AND " + DbHandler.KEY_ITEM_STATUS + " LIKE '" + DbHandler.STATUS_WISH +
                                        "'))";
                                SQLstatusEquals = DbHandler.KEY_ITEM_STATUS + " LIKE '" + DbHandler.STATUS_WISH + "'";
                                break;
                            }
                            case DbHandler.USER_TRADE_COLLECTIONS:{
                                SQL =   "SELECT " + DbHandler.KEY_ID + ", " + DbHandler.KEY_NAME +
                                        " FROM " + DbHandler.TABLE_SECTIONS +
                                        " WHERE " + DbHandler.KEY_ID +
                                        " IN (SELECT " + DbHandler.KEY_SECTION_ID +
                                        " FROM " + DbHandler.TABLE_ITEMS +
                                        " WHERE " + DbHandler.KEY_COLLECTION_ID + " = "  + collectionId+
                                        " AND " + DbHandler.KEY_ID +
                                        " IN (SELECT " + DbHandler.KEY_ITEM_ID +
                                        " FROM " + DbHandler.TABLE_USERS_ITEMS +
                                        " WHERE " + DbHandler.KEY_USER_ID + " = " + userId +
                                        " AND " + DbHandler.KEY_ITEM_STATUS + " LIKE '" + DbHandler.STATUS_TRADE +
                                        "'))";
                                SQLstatusEquals = DbHandler.KEY_ITEM_STATUS + " LIKE '" + DbHandler.STATUS_TRADE + "'";
                                break;
                            }
                            case DbHandler.USER_ALL_COLLECTIONS: {
                                SQL =   "SELECT " + DbHandler.KEY_ID + ", " + DbHandler.KEY_NAME +
                                        " FROM " + DbHandler.TABLE_SECTIONS +
                                        " WHERE " + DbHandler.KEY_ID +
                                        " IN (SELECT " + DbHandler.KEY_SECTION_ID +
                                        " FROM " + DbHandler.TABLE_ITEMS +
                                        " WHERE " + DbHandler.KEY_COLLECTION_ID + " = "  + collectionId+
                                        " AND " + DbHandler.KEY_ID +
                                        " IN (SELECT " + DbHandler.KEY_ITEM_ID +
                                        " FROM " + DbHandler.TABLE_USERS_ITEMS +
                                        " WHERE " + DbHandler.KEY_USER_ID + " = " + userId +
                                        " AND (" + DbHandler.KEY_ITEM_STATUS + " LIKE '" + DbHandler.STATUS_IN +
                                        "' OR " + DbHandler.KEY_ITEM_STATUS + " LIKE '" + DbHandler.STATUS_TRADE + "')))";
                                SQLstatusEquals =
                                        "(" + DbHandler.KEY_ITEM_STATUS + " LIKE '" + DbHandler.STATUS_IN +
                                        "' OR " + DbHandler.KEY_ITEM_STATUS + " LIKE '" +DbHandler.STATUS_TRADE + "')" ;
                                break;
                            }
                        }
                        Connection connection = dbHandler.getConnection(context);
                        if(connection != null) {
                            Statement stUserSections = connection.createStatement();
                            ResultSet rsUserSections = stUserSections.executeQuery(SQL);

                            if (rsUserSections != null) {
                                while (rsUserSections.next()) {
                                    int secId = rsUserSections.getInt(1);
                                    String secName = rsUserSections.getString(2);
                                    SQL = "SELECT TOP 1 " + DbHandler.KEY_ITEM_ID +
                                            " FROM " + DbHandler.TABLE_USERS_ITEMS +
                                            " WHERE " + SQLstatusEquals +
                                            " AND " + DbHandler.KEY_ITEM_ID +
                                            " IN (SELECT " + DbHandler.KEY_ID +
                                            " FROM " + DbHandler.TABLE_ITEMS +
                                            " WHERE " + DbHandler.KEY_SECTION_ID +
                                            " = " + secId + ")";
                                    Statement stCurrentItem = connection.createStatement();
                                    ResultSet rsCurrentItem = stCurrentItem.executeQuery(SQL);
                                    if(rsCurrentItem != null) {
                                        rsCurrentItem.next();
                                        int itemId = rsCurrentItem.getInt(1);
                                        publishProgress(itemId, secId, secName);
                                        rsCurrentItem.close();
                                    }
                                    stCurrentItem.close();
                                }
                                rsUserSections.close();
                            }
                            stUserSections.close();
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (rsSections != null) rsSections.close();
                    if (rsItems != null) rsItems.close();
                    if (stSections != null) stSections.close();
                    if (stItems != null) stItems.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
        AsyncCurrentItem currentItem = new AsyncCurrentItem(layout, (int)values[0], (int)values[1], (String)values[2], "section", context, mDb, fm, actionBar);
        if(userId != -1) {
            currentItem.setUserId(userId);
        }
        offers.add(currentItem);
        currentItem.execute();
    }
}
