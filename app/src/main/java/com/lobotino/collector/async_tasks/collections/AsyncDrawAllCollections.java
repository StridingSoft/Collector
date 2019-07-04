package com.lobotino.collector.async_tasks.collections;


import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.widget.RelativeLayout;

import com.lobotino.collector.fragments.CollectionsFragment;
import com.lobotino.collector.utils.DbHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
    private RelativeLayout layout;
    private SQLiteDatabase mDb;
    private Context context;
    private ActionBar actionBar;
    private FragmentManager fm;

    public AsyncDrawAllCollections(Context context, SQLiteDatabase mDb,RelativeLayout layout, ActionBar actionBar, FragmentManager fm) {
        this.layout = layout;
        this.mDb = mDb;
        this.context = context;
        this.fm = fm;
        this.actionBar = actionBar;
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
                if (fragmentType.equals("myCollections") || !DbHandler.isOnline(context)) {
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
                                    if (fragmentType.equals("comCollections"))
                                        cursorItems = mDb.query(DbHandler.TABLE_ITEMS, new String[]{DbHandler.KEY_ID}, DbHandler.KEY_SECTION_ID + " = " + secId, null, null, null, null);
                                    else
                                        cursorItems = mDb.query(DbHandler.TABLE_ITEMS, new String[]{DbHandler.KEY_ID}, DbHandler.KEY_SECTION_ID + " = ? AND " + DbHandler.KEY_ITEM_STATUS + " = ?", new String[]{secId + "", "in"}, null, null, null);

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

                    Connection connection = DbHandler.getConnection(context);


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
                                            if(cursorSection.getCount() <= 0){
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
                                                }
                                                catch (SQLException e) {
                                                    e.printStackTrace();
                                                }
                                            }else{
                                                if(cursorSection.moveToFirst()) {
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
        AsyncCurrentItem currentItem = new AsyncCurrentItem((int)values[0], (int)values[1], (int)values[2], (String)values[3], "collection", context, mDb, fm, layout, actionBar);
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
