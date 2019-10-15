package com.lobotino.collector.async_tasks.collections;


import android.app.Activity;
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
import static com.lobotino.collector.fragments.CollectionsFragment.fragmentStatus;
import static com.lobotino.collector.fragments.CollectionsFragment.offers;
import static com.lobotino.collector.fragments.CollectionsFragment.lastLeftId ;
import static com.lobotino.collector.fragments.CollectionsFragment.lastRightId;
import static com.lobotino.collector.fragments.CollectionsFragment.countImages;
import static com.lobotino.collector.fragments.CollectionsFragment.currentId;

public class AsyncDrawAllCollections extends AsyncTask<Void, Object, Void>
{
    private String SQL;
    private SQLiteDatabase mDb;
    private Context context;
    private ActionBar actionBar;
    private FragmentManager fm;
    private int userId = -1;
    private RelativeLayout layout;

    public AsyncDrawAllCollections( RelativeLayout layout, Context context, SQLiteDatabase mDb, ActionBar actionBar, FragmentManager fm) {
        this.layout = layout;
        this.mDb = mDb;
        this.context = context;
        this.fm = fm;
        this.actionBar = actionBar;
    }

    public AsyncDrawAllCollections( RelativeLayout layout, Context context, SQLiteDatabase mDb, ActionBar actionBar, FragmentManager fm, int userId) {
        this.layout = layout;
        this.mDb = mDb;
        this.context = context;
        this.fm = fm;
        this.actionBar = actionBar;
        this.userId = userId;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        fragmentStatus = "all";
        offers.clear();

        layout.removeAllViews();
        lastLeftId = -1;
        lastRightId = -1;
        countImages = 0;
        currentId = 0;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");

            Statement stCollections = null;
            Statement stSections = null;
            Statement stItems = null;
            ResultSet rsCollections = null;
            ResultSet rsSections = null;
            ResultSet rsItems = null;
            try {
                if (fragmentType.equals(DbHandler.MY_COLLECTIONS) || !DbHandler.isOnline(context)) {
                    Cursor cursorCollections = mDb.query(DbHandler.TABLE_COLLECTIONS, new String[]{DbHandler.KEY_ID, DbHandler.KEY_NAME}, null, null, null, null, null);
                    if (cursorCollections.moveToFirst()) {
                        do {
                            int collectionId = cursorCollections.getInt(cursorCollections.getColumnIndex(DbHandler.KEY_ID));
                            String name = cursorCollections.getString(cursorCollections.getColumnIndex(DbHandler.KEY_NAME));
                            Cursor cursorSection = mDb.query(DbHandler.TABLE_SECTIONS, new String[]{DbHandler.KEY_ID}, DbHandler.KEY_COLLECTION_ID + " = " + collectionId, null, null, null, null);
                            if (cursorSection.moveToFirst()) {
                                do {
                                    int secId = cursorSection.getInt(cursorSection.getColumnIndex(DbHandler.KEY_ID));

                                    Cursor cursorItems;
                                    if (fragmentType.equals(DbHandler.COM_COLLECTIONS))
                                        cursorItems = mDb.query(DbHandler.TABLE_ITEMS, new String[]{DbHandler.KEY_ID}, DbHandler.KEY_SECTION_ID + " = " + secId, null, null, null, null);
                                    else
                                        cursorItems = mDb.query(DbHandler.TABLE_ITEMS, new String[]{DbHandler.KEY_ID}, DbHandler.KEY_SECTION_ID + " = ? AND (" + DbHandler.KEY_ITEM_STATUS + " = ?" + " OR " + DbHandler.KEY_ITEM_STATUS + " = ?)", new String[]{secId + "", DbHandler.STATUS_IN, DbHandler.STATUS_TRADE}, null, null, null);

                                    if (cursorItems.moveToFirst()) {
                                        int itemId = cursorItems.getInt(cursorItems.getColumnIndex(DbHandler.KEY_ID));
                                        publishProgress(itemId, secId, collectionId, name);
                                        break;
                                    }
                                    cursorItems.close();
                                }while(cursorSection.moveToNext());
                            }
                            cursorSection.close();
                        } while (cursorCollections.moveToNext());
                    }
                    cursorCollections.close();
                } else {
                    if (fragmentType.equals(DbHandler.COM_COLLECTIONS)) {
                        Connection connection = dbHandler.getConnection(context);

                        if (connection != null) {
                            SQL = "SELECT * FROM " + DbHandler.TABLE_COLLECTIONS;
                            stCollections = connection.createStatement();
                            rsCollections = stCollections.executeQuery(SQL);

                            if (rsCollections != null) {
                                while (rsCollections.next()) {
                                    int collectionId = rsCollections.getInt(1);

                                    Cursor cursorCollections = mDb.query(DbHandler.TABLE_COLLECTIONS, new String[]{DbHandler.KEY_ID, DbHandler.KEY_NAME}, DbHandler.KEY_ID + " = " + collectionId, null, null, null, null);
                                    if (cursorCollections.getCount() <= 0) {
                                        String name = rsCollections.getString(2);
                                        String desc = rsCollections.getString(3);

                                        ContentValues cvCollection = new ContentValues();
                                        cvCollection.put(DbHandler.KEY_ID, collectionId);
                                        cvCollection.put(DbHandler.KEY_NAME, name);
                                        cvCollection.put(DbHandler.KEY_DESCRIPTION, desc);
                                        mDb.insert(DbHandler.TABLE_COLLECTIONS, null, cvCollection);

                                        SQL = "SELECT TOP 1 * FROM " + DbHandler.TABLE_SECTIONS + " WHERE " + DbHandler.KEY_COLLECTION_ID + " = " + collectionId;
                                        stSections = connection.createStatement();
                                        rsSections = stSections.executeQuery(SQL);

                                        if (rsSections != null) {
                                            try {
                                                rsSections.next();
                                                int secId = rsSections.getInt(1);

                                                Cursor cursorSection = mDb.query(DbHandler.TABLE_SECTIONS, new String[]{DbHandler.KEY_ID}, DbHandler.KEY_ID + " = " + secId, null, null, null, null);
                                                if (cursorSection.getCount() <= 0) {
                                                    try {
                                                        String nameSection = rsSections.getString(3);
                                                        String descSection = rsSections.getString(4);
                                                        ContentValues cvSection = new ContentValues();
                                                        cvSection.put(DbHandler.KEY_ID, secId);
                                                        cvSection.put(DbHandler.KEY_COLLECTION_ID, collectionId);
                                                        cvSection.put(DbHandler.KEY_NAME, nameSection);
                                                        cvSection.put(DbHandler.KEY_DESCRIPTION, descSection);
                                                        mDb.insert(DbHandler.TABLE_SECTIONS, null, cvSection);

                                                        SQL = "SELECT TOP 1 " + DbHandler.KEY_ID + " FROM " + DbHandler.TABLE_ITEMS + " WHERE " + DbHandler.KEY_SECTION_ID + " = " + secId;
                                                        stItems = connection.createStatement();
                                                        rsItems = stItems.executeQuery(SQL);

                                                        if (rsItems != null) {
                                                            rsItems.next();
                                                            int itemId = rsItems.getInt(1);
                                                            publishProgress(itemId, secId, collectionId, name);
                                                        }
                                                    } catch (SQLException e) {
                                                        e.printStackTrace();
                                                    }
                                                } else {
                                                    if (cursorSection.moveToFirst()) {
                                                        Cursor cursorItem = mDb.query(DbHandler.TABLE_ITEMS, new String[]{DbHandler.KEY_ID}, DbHandler.KEY_SECTION_ID + " = " + secId, null, null, null, null);
                                                        if (cursorItem.moveToFirst()) {
                                                            int itemId = cursorItem.getInt(cursorItem.getColumnIndex(DbHandler.KEY_ID));
                                                            publishProgress(itemId, secId, collectionId, name);
                                                        }
                                                        cursorItem.close();
                                                    }
                                                }
                                                cursorSection.close();
                                            } catch (SQLException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } else {
                                        if (cursorCollections.moveToFirst()) {
                                            String name = cursorCollections.getString(cursorCollections.getColumnIndex(DbHandler.KEY_NAME));
                                            Cursor cursorSection = mDb.query(DbHandler.TABLE_SECTIONS, new String[]{DbHandler.KEY_ID}, DbHandler.KEY_COLLECTION_ID + " = " + collectionId, null, null, null, null);
                                            if (cursorSection.moveToFirst()) {
                                                int secId = cursorSection.getInt(cursorSection.getColumnIndex(DbHandler.KEY_ID));
                                                Cursor cursorItem = mDb.query(DbHandler.TABLE_ITEMS, new String[]{DbHandler.KEY_ID}, DbHandler.KEY_SECTION_ID + " = " + secId, null, null, null, null);
                                                if (cursorItem.moveToFirst()) {
                                                    int itemId = cursorItem.getInt(cursorItem.getColumnIndex(DbHandler.KEY_ID));
                                                    publishProgress(itemId, secId, collectionId, name);
                                                }
                                                cursorItem.close();
                                            }
                                            cursorSection.close();
                                        }
                                        cursorCollections.close();
                                    }
                                }
                            }
                        }
                    }else {
                        switch (fragmentType) {
                            case DbHandler.USER_WISH_COLLECTIONS: {
                                SQL = "SELECT " + DbHandler.KEY_ITEM_ID + " FROM " + DbHandler.TABLE_USERS_ITEMS + " WHERE " + DbHandler.KEY_USER_ID + " = " + userId +
                                        " AND " + DbHandler.KEY_ITEM_STATUS + " LIKE '" + DbHandler.STATUS_WISH + "'";
                                break;
                            }
                            case DbHandler.USER_TRADE_COLLECTIONS: {
                                SQL = "SELECT " + DbHandler.KEY_ITEM_ID + " FROM " + DbHandler.TABLE_USERS_ITEMS + " WHERE " + DbHandler.KEY_USER_ID + " = " + userId +
                                        " AND " + DbHandler.KEY_ITEM_STATUS + " LIKE '" + DbHandler.STATUS_TRADE + "'";
                                break;
                            }
                            case DbHandler.USER_ALL_COLLECTIONS: {
                                SQL = "SELECT " + DbHandler.KEY_ITEM_ID + " FROM " + DbHandler.TABLE_USERS_ITEMS + " WHERE " + DbHandler.KEY_USER_ID + " = " + userId +
                                        " AND (" + DbHandler.KEY_ITEM_STATUS + " LIKE '" + DbHandler.STATUS_IN + "' OR " + DbHandler.KEY_ITEM_STATUS + " LIKE '" + DbHandler.STATUS_TRADE + "')";
                                break;
                            }
                        }
                        Connection connection = dbHandler.getConnection(context);
                        if (connection != null) {
                            Statement st1 = connection.createStatement();
                            ResultSet rs1 = st1.executeQuery(SQL);

                            //Получаем лист коллекций юзера
                            ArrayList<Integer> listCollections = new ArrayList<>();
                            if (rs1 != null) {
                                while (rs1.next()) {
                                    int itemId = rs1.getInt(1);
                                    SQL = "SELECT " + DbHandler.KEY_COLLECTION_ID + ", " + DbHandler.KEY_SECTION_ID + ", " +
                                            DbHandler.KEY_NAME + " FROM " + DbHandler.TABLE_ITEMS + " WHERE " +
                                            DbHandler.KEY_ID + " = " + itemId;

                                    Statement st2 = connection.createStatement();
                                    ResultSet rs2 = st2.executeQuery(SQL);

                                    if (rs2 != null) {
                                        rs2.next();
                                        int collectionId = rs2.getInt(1);

                                        if (!listCollections.contains(collectionId)) {
                                            listCollections.add(collectionId);
                                            int secId = rs2.getInt(2);

                                            SQL = "SELECT " + DbHandler.KEY_NAME + " FROM " + DbHandler.TABLE_COLLECTIONS + " WHERE " +
                                                    DbHandler.KEY_ID + " = " + collectionId;
                                            Statement st3 = connection.createStatement();
                                            ResultSet rs3 = st3.executeQuery(SQL);
                                            rs3.next();

                                            String name = rs3.getString(1);

                                            rs3.close();
                                            st3.close();

                                            publishProgress(itemId, secId, collectionId, name);
                                        }
                                        rs2.close();
                                    }
                                    st2.close();
                                }
                                rs1.close();
                            }
                            st1.close();
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }  finally {
                try {
                    if (rsCollections != null) rsCollections.close();
                    if (rsSections != null) rsSections.close();
                    if (rsItems != null) rsItems.close();
                    if (stCollections != null) stCollections.close();
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
        AsyncCurrentItem currentItem;

            currentItem = new AsyncCurrentItem(layout, (int)values[0], (int)values[1], (int)values[2], (String)values[3], "collection", context, mDb, fm, actionBar);

        if(userId != -1) {
            currentItem.setUserId(userId);
        }

        offers.add(currentItem);
        currentItem.execute();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(fragmentType.equals("comCollections")) actionBar.setTitle("Коллекции сообщества");
        else
        if(fragmentType.equals("myCollections")) actionBar.setTitle("Мои коллекции");
    }
}
