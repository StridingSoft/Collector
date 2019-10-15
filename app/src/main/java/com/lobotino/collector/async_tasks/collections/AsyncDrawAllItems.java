package com.lobotino.collector.async_tasks.collections;

import android.app.Activity;
import android.app.FragmentManager;
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

import static com.lobotino.collector.activities.MainActivity.dbHandler;
import static com.lobotino.collector.fragments.CollectionsFragment.fragmentType;
import static com.lobotino.collector.fragments.CollectionsFragment.offers;
import static com.lobotino.collector.fragments.CollectionsFragment.lastLeftId ;
import static com.lobotino.collector.fragments.CollectionsFragment.lastRightId;
import static com.lobotino.collector.fragments.CollectionsFragment.countImages;
import static com.lobotino.collector.fragments.CollectionsFragment.currentId;
import static com.lobotino.collector.fragments.CollectionsFragment.fragmentStatus;
import static com.lobotino.collector.fragments.CollectionsFragment.sectionTitle;
import static com.lobotino.collector.fragments.CollectionsFragment.currentSection;


public class AsyncDrawAllItems extends AsyncTask<Void, Integer, Void>
{
    private String SQL, sectionName;
    private int secId, userId = -1;
    private Context context;
    private SQLiteDatabase mDb;
    private ActionBar actionBar;
    private FragmentManager fm;
    private RelativeLayout layout;

    public AsyncDrawAllItems(RelativeLayout layout, String sectionName, int secId, Context context, SQLiteDatabase mDb, ActionBar actionBar, FragmentManager fm) {
        this.layout = layout;
        this.sectionName = sectionName;
        this.secId = secId;
        this.context = context;
        this.mDb = mDb;
        this.actionBar = actionBar;
        this.fm = fm;
    }

    public AsyncDrawAllItems(RelativeLayout layout, String sectionName, int secId, Context context, SQLiteDatabase mDb, ActionBar actionBar, FragmentManager fm, int userId) {
        this.layout = layout;
        this.sectionName = sectionName;
        this.secId = secId;
        this.context = context;
        this.mDb = mDb;
        this.actionBar = actionBar;
        this.fm = fm;
        this.userId = userId;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        actionBar.setTitle(sectionName);
        sectionTitle = sectionName;
        fragmentStatus = "section";
        currentSection = secId;
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
            Statement stSection = null;
            ResultSet rsSection = null;
            Statement stItems = null;
            ResultSet rsItems = null;
            Cursor cursorSection = null;
            Cursor cursorItems = null;
            try {
                if(fragmentType.equals(DbHandler.COM_COLLECTIONS)) {
                    if (!DbHandler.isOnline(context)) {
                        cursorItems = mDb.query(DbHandler.TABLE_ITEMS, new String[]{DbHandler.KEY_ID}, DbHandler.KEY_SECTION_ID + " = " + secId, null, null, null, null);
                        if (cursorItems.getCount() > 0 && cursorItems.moveToFirst()) {
                            do {
                                publishProgress(cursorItems.getInt(cursorItems.getColumnIndex(DbHandler.KEY_ID)), secId);
                            } while (cursorItems.moveToNext());
                        }
                        cursorItems.close();
                    } else {
                        Connection connection = dbHandler.getConnection(context);

                        if (connection != null) {
                            SQL = "SELECT " + DbHandler.KEY_ID + " FROM " + DbHandler.TABLE_ITEMS + " WHERE " + DbHandler.KEY_SECTION_ID + " = " + secId;
                            stItems = connection.createStatement();
                            rsItems = stItems.executeQuery(SQL);

                            if (rsItems != null) {
                                while (rsItems.next()) {
                                    publishProgress(rsItems.getInt(1), secId);
                                }
                            }
                        }
                    }
                } else {
                    if(fragmentType.equals(DbHandler.MY_COLLECTIONS)) {
                        cursorItems = mDb.query(DbHandler.TABLE_ITEMS, new String[]{DbHandler.KEY_ID}, DbHandler.KEY_SECTION_ID + " = ? AND (" + DbHandler.KEY_ITEM_STATUS + " = ? OR " + DbHandler.KEY_ITEM_STATUS + " = ?)", new String[]{secId + "", DbHandler.STATUS_IN, DbHandler.STATUS_TRADE}, null, null, null);
                        if (cursorItems.getCount() > 0 && cursorItems.moveToFirst()) {
                            do {
                                publishProgress(cursorItems.getInt(cursorItems.getColumnIndex(DbHandler.KEY_ID)), secId);
                            } while (cursorItems.moveToNext());
                        }
                        cursorItems.close();
                    }else {

                        switch (fragmentType) {
                            case DbHandler.USER_WISH_COLLECTIONS: {
                                SQL = "SELECT " + DbHandler.KEY_ID +
                                        " FROM " + DbHandler.TABLE_ITEMS +
                                        " WHERE " + DbHandler.KEY_SECTION_ID + " = " + secId +
                                        " AND " + DbHandler.KEY_ID +
                                        " IN (SELECT " + DbHandler.KEY_ITEM_ID +
                                        " FROM " + DbHandler.TABLE_USERS_ITEMS +
                                        " WHERE " + DbHandler.KEY_USER_ID + " = " + userId +
                                        " AND " + DbHandler.KEY_ITEM_STATUS + " LIKE '" + DbHandler.STATUS_WISH + "')";
                                break;
                            }
                            case DbHandler.USER_TRADE_COLLECTIONS:{
                                SQL = "SELECT " + DbHandler.KEY_ID +
                                        " FROM " + DbHandler.TABLE_ITEMS +
                                        " WHERE " + DbHandler.KEY_SECTION_ID + " = " + secId +
                                        " AND " + DbHandler.KEY_ID +
                                        " IN (SELECT " + DbHandler.KEY_ITEM_ID +
                                        " FROM " + DbHandler.TABLE_USERS_ITEMS +
                                        " WHERE " + DbHandler.KEY_USER_ID + " = " + userId +
                                        " AND " + DbHandler.KEY_ITEM_STATUS + " LIKE '" + DbHandler.STATUS_TRADE + "')";
                                break;
                            }
                            case DbHandler.USER_ALL_COLLECTIONS: {
                                SQL = "SELECT " + DbHandler.KEY_ID +
                                        " FROM " + DbHandler.TABLE_ITEMS +
                                        " WHERE " + DbHandler.KEY_SECTION_ID + " = " + secId +
                                        " AND " + DbHandler.KEY_ID +
                                        " IN (SELECT " + DbHandler.KEY_ITEM_ID +
                                        " FROM " + DbHandler.TABLE_USERS_ITEMS +
                                        " WHERE " + DbHandler.KEY_USER_ID + " = " + userId +
                                        " AND (" + DbHandler.KEY_ITEM_STATUS + " LIKE '" + DbHandler.STATUS_IN +
                                        "' OR " + DbHandler.KEY_ITEM_STATUS + " LIKE '" + DbHandler.STATUS_TRADE + "'))";
                                break;
                            }
                        }

                        if (DbHandler.isOnline(context)) {
                            Connection connection = dbHandler.getConnection(context);
                            if (connection != null) {
                                stItems = connection.createStatement();
                                rsItems = stItems.executeQuery(SQL);
                                if (rsItems != null) {
                                    while (rsItems.next()) {
                                        publishProgress(rsItems.getInt(1), secId);
                                    }
                                    rsItems.close();
                                }
                                stItems.close();
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }  finally {
                try {
                    if (rsSection != null) rsSection.close();
                    if (stSection != null) stSection.close();
                    if (rsItems != null) rsItems.close();
                    if (stItems != null) stItems.close();
                    if (cursorSection != null) cursorSection.close();
                    if (cursorItems != null) cursorItems.close();
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
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        AsyncCurrentItem currentItem = new AsyncCurrentItem(layout, values[0], values[1], "item", context, mDb, fm, actionBar);
        if(userId != -1) {
            currentItem.setUserId(userId);
        }
        offers.add(currentItem);
        currentItem.execute();
    }
}
