package com.lobotino.collector.async_tasks.collections;


import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.widget.RelativeLayout;

import com.lobotino.collector.utils.DbHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
    private RelativeLayout layout;
    private SQLiteDatabase mDb;
    private Context context;
    private FragmentManager fm;

    public AsyncDrawAllSections(int collectionId, String collectionName, Context context, SQLiteDatabase mDb, RelativeLayout layout,  ActionBar actionBar, FragmentManager fm) {
        this.collectionId = collectionId;
        this.collectionName = collectionName;
        this.actionBar = actionBar;
        this.layout = layout;
        this.mDb = mDb;
        this.context = context;
        this.fm = fm;
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
                if (fragmentType.equals("myCollections") || !DbHandler.isOnline(context)) {
                    Cursor cursorSections = mDb.query(DbHandler.TABLE_SECTIONS, new String[]{DbHandler.KEY_ID, DbHandler.KEY_NAME}, DbHandler.KEY_COLLECTION_ID + " = " + collectionId, null, null, null, null);
                    if (cursorSections.getCount() > 0 && cursorSections.moveToFirst()) {
                        do {
                            String name = cursorSections.getString(cursorSections.getColumnIndex(DbHandler.KEY_NAME));
                            int secId = cursorSections.getInt(cursorSections.getColumnIndex(DbHandler.KEY_ID));
                            Cursor cursorItems;
                            if(fragmentType.equals("comCollections"))
                                cursorItems = mDb.query(DbHandler.TABLE_ITEMS, new String[]{DbHandler.KEY_ID}, DbHandler.KEY_SECTION_ID + " = " + secId, null, null, null, null);
                            else
                                cursorItems = mDb.query(DbHandler.TABLE_ITEMS, new String[]{DbHandler.KEY_ID}, DbHandler.KEY_SECTION_ID + " = ? AND " + DbHandler.KEY_ITEM_STATUS + " = ?", new String[]{secId + "", "in"}, null, null, null);

                            if (cursorItems.getCount() > 0 && cursorItems.moveToFirst()) {
                                int idItem = cursorItems.getInt(cursorItems.getColumnIndex(DbHandler.KEY_ID));
                                publishProgress(idItem, secId, name);
                            }
                            cursorItems.close();
                        } while (cursorSections.moveToNext());
                        cursorSections.close();
                    }
                } else {

                    Connection connection = DbHandler.getConnection(context);

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
        AsyncCurrentItem currentItem = new AsyncCurrentItem((int)values[0], (int)values[1], (String)values[2], "section", context, mDb, fm, layout, actionBar);
        offers.add(currentItem);
        currentItem.execute();
    }
}
