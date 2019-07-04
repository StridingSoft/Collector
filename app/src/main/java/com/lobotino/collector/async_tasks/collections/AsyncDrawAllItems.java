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
    private int secId;
    private Context context;
    private SQLiteDatabase mDb;
    private ActionBar actionBar;
    private RelativeLayout layout;
    private FragmentManager fm;

    public AsyncDrawAllItems(String sectionName, int secId, Context context, SQLiteDatabase mDb,  RelativeLayout layout, ActionBar actionBar, FragmentManager fm) {
        this.sectionName = sectionName;
        this.secId = secId;
        this.context = context;
        this.mDb = mDb;
        this.actionBar = actionBar;
        this.layout = layout;
        this.fm = fm;
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
                if(fragmentType.equals("comCollections")) {
                    if (!DbHandler.isOnline(context)) {
                        cursorItems = mDb.query(DbHandler.TABLE_ITEMS, new String[]{DbHandler.KEY_ID}, DbHandler.KEY_SECTION_ID + " = " + secId, null, null, null, null);
                        if (cursorItems.getCount() > 0 && cursorItems.moveToFirst()) {
                            do {
                                publishProgress(cursorItems.getInt(cursorItems.getColumnIndex(DbHandler.KEY_ID)), secId);
                            } while (cursorItems.moveToNext());
                        }
                        cursorItems.close();
                    } else {
                        Connection connection = DbHandler.getConnection(context);

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
                    cursorItems = mDb.query(DbHandler.TABLE_ITEMS, new String[]{DbHandler.KEY_ID}, DbHandler.KEY_SECTION_ID + " = ? AND " + DbHandler.KEY_ITEM_STATUS + " = ?", new String[]{secId + "", "in"}, null, null, null);
                    if (cursorItems.getCount() > 0 && cursorItems.moveToFirst()) {
                        do {
                            publishProgress(cursorItems.getInt(cursorItems.getColumnIndex(DbHandler.KEY_ID)), secId);
                        } while (cursorItems.moveToNext());
                    }
                    cursorItems.close();
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
        AsyncCurrentItem currentItem = new AsyncCurrentItem(values[0], values[1], "item", context, mDb, fm, layout, actionBar);
        offers.add(currentItem);
        currentItem.execute();
    }
}
