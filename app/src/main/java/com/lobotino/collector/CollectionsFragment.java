package com.lobotino.collector;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import pub.devrel.easypermissions.EasyPermissions;

public class CollectionsFragment extends Fragment {

    private DbHandler dbHandler;
    private SQLiteDatabase mDb;
    private String[] galleryPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private View rootView;
    private Context context;
    private int pictureSize, screenWidth, externalMargins, topMargin, botMargin, puddingsSize, firstTopMargin, lastLeftId, lastRightId, tempId;
    private GradientDrawable gradientDrawable;
    private RelativeLayout layout;
    private ScrollView scrollView;
    private Button buttonBack;
    private ActionBar actionBar;
    private String pathToImage;
    private int countImages = 0;
    private int currentId = 0;
    private String currentTitle = "";
    private List<AsyncCurrentItem> offers;

    private String fragmentType = "myCollections";  //myCollections or comCollections
    private String fragmentStatus = "all";

    private int currentCollection = 0;
    private int currentSection = 0;

    private Connection connection;

    public int getCurrentSection(){
        return currentSection;
    }

    public String getType()
    {
        return fragmentType;
    }

    public String getStatus()
    {
        return fragmentStatus;
    }

    public String getCurrentTitle() { return currentTitle; }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        offers = new ArrayList<AsyncCurrentItem>();
        rootView = inflater.inflate(R.layout.fragment_my_collections, container, false);
        NavigationActivity navigationActivity = (NavigationActivity) getActivity();
        navigationActivity.setCurrentFragment(this);
        context = getActivity().getBaseContext();
        dbHandler = NavigationActivity.dbHandler;
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        pictureSize = Math.round((float) (screenWidth / 3));
        layout = (RelativeLayout) rootView.findViewById(R.id.relative_layout_1);
        scrollView = (ScrollView) rootView.findViewById(R.id.scroll_view_id_1);
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        try {
            dbHandler.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }
            mDb = dbHandler.getDataBase();

        externalMargins = screenWidth / 11;
        topMargin = screenWidth / 9;
        botMargin = screenWidth / 16;
        puddingsSize = pictureSize / 30; //15
        firstTopMargin = screenWidth / 16;

        gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(15);
        //gradientDrawable.setColor(Color.parseColor("#180c28"));
        gradientDrawable.setColors(new int[]{Color.parseColor("#5d000e"), Color.parseColor("#430014")});


        buttonBack = new Button(context);
        int buttonSize = screenWidth / 12;
        RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        int margin = screenWidth / 60;
        buttonParams.setMargins(0, margin, 0, 0);
        buttonBack.setText("");
        buttonBack.setLayoutParams(buttonParams);
        buttonBack.setId(View.generateViewId());
        buttonBack.setBackgroundResource(R.drawable.ic_action_name);

        String type, status;
        int id;
        if(savedInstanceState != null) {
            type = savedInstanceState.getString("type");
            status = savedInstanceState.getString("status");
            id = savedInstanceState.getInt("id");
            currentTitle = savedInstanceState.getString("title");
        }else {
            type = getArguments().getString("type");
            status = getArguments().getString("status");
            id = getArguments().getInt("id");
            currentTitle = getArguments().getString("title");
        }

        if (!EasyPermissions.hasPermissions(context, galleryPermissions)) {
                EasyPermissions.requestPermissions(this, "Access for storage",
                        101, galleryPermissions);
            }

            switch (status) {
                case "collection": {
                    currentCollection = id;
                    if (type.equals("myCollections"))
                        printAllSections();
                    else
                        printAllSections();
                    break;
                }
                case "section": {
                    currentSection = id;
                    if (type.equals("myCollections"))
                        printAllItems();
                    else
                        printAllItems();
                    break;
                }
                case "item": {
                    CurrentItemFragment currentItemFragment = new CurrentItemFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("id", id);
                    bundle.putString("status", savedInstanceState.getString("status"));
                    currentItemFragment.setArguments(bundle);
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.content_frame, currentItemFragment).commit();
                    break;
                }
                default: {
                    if (type.equals("myCollections"))
                        printAllCollections();
                    else {
                        printAllCollections();
                    }
                }
            }
            fragmentType = type;
            fragmentStatus = status;


        return rootView;
    }

    public void clearOffers()
    {
        for(int i = 0; i < offers.size(); i++)
        {
            offers.get(i).cancel(true);
        }
        offers.clear();
    }

    public void closeConnection()
    {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void printAllCollections(){
        new AsyncDrawAllCollections().execute();
    }

    public void printAllSections(){
        new AsyncDrawAllSections(currentCollection, currentTitle).execute();
    }

    public void printAllSections(int colId){
        new AsyncDrawAllSections(colId, currentTitle).execute();
    }

    public void printAllItems(){
        new AsyncDrawAllItems(currentSection, currentTitle).execute();
    }

    public void printAllItems(int secId){
        new AsyncDrawAllItems(secId, currentTitle).execute();
    }

    private TextView getTextViewBySide(String text, int countImages)
    {
        RelativeLayout.LayoutParams textViewParams = new RelativeLayout.LayoutParams(pictureSize, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView textView = new TextView(context);
        textView.setTextColor(Color.parseColor("#ffffff"));
        textView.setText(text);
        textView.setGravity(Gravity.CENTER);

        if (countImages % 2 == 0) {
            lastLeftId = tempId;
            textViewParams.addRule(RelativeLayout.BELOW, lastLeftId);
            textViewParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            textViewParams.setMargins(externalMargins, 5, 0, botMargin);
        } else {
            lastRightId = tempId;
            textViewParams.addRule(RelativeLayout.BELOW, lastRightId);
            textViewParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            textViewParams.setMargins(0, 5, externalMargins, botMargin);
        }
        textView.setLayoutParams(textViewParams);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        return textView;
    }

    private RelativeLayout.LayoutParams getImageParamsBySide(int countImages)
    {
        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(pictureSize, pictureSize);
        if (countImages % 2 == 0) {
            if (lastLeftId == -1) {
                imageParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                imageParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                imageParams.setMargins(externalMargins, firstTopMargin, 0, 0);
            } else {
                imageParams.addRule(RelativeLayout.BELOW, lastLeftId);
                imageParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                imageParams.setMargins(externalMargins, topMargin, 0, 0);
            }
        } else {
            if (lastRightId == -1) {
                imageParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                imageParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                imageParams.setMargins(0, firstTopMargin, externalMargins, 0);
            } else {
                imageParams.addRule(RelativeLayout.BELOW, lastRightId);
                imageParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                imageParams.setMargins(0, topMargin, externalMargins, 0);
            }
        }
        return imageParams;
    }

    private class AsyncCurrentItem extends AsyncTask<String, Void, Bitmap>
    {
        private String SQL;
        private String name, desc, status;
        private int collectionId, secId, itemId;


        public AsyncCurrentItem(int itemId, int secId, String status) {
            this.itemId = itemId;
            this.secId = secId;
            this.status = status;
        }

        public AsyncCurrentItem(int itemId, int secId, String name, String status) {
            this.itemId = itemId;
            this.name = name;
            this.status = status;
            this.secId = secId;
        }

        public AsyncCurrentItem(int itemId, int secId, int collectionId, String name, String status) {
            this.itemId = itemId;
            this.name = name;
            this.status = status;
            this.collectionId = collectionId;
            this.secId = secId;
        }


        private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                final int heightRatio = Math.round((float) height / (float) reqHeight);
                final int widthRatio = Math.round((float) width / (float) reqWidth);
                inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            }
            return inSampleSize - 1;
        }


        @Override
        protected Bitmap doInBackground(String... query) {
            try {

                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                Statement st = null;
                ResultSet rs = null;
                try {
                    Cursor cursor = mDb.query(DbHandler.TABLE_ITEMS, null, DbHandler.KEY_ID + " = " + itemId, null, null, null, null);
                    int count = cursor.getCount();
                    byte[] blob = null;
                    if (count > 0) {
                        if (cursor.moveToFirst()) {
                            blob = cursor.getBlob(cursor.getColumnIndex(DbHandler.KEY_MINI_IMAGE));
                            if (status.equals("item"))
                                name = cursor.getString(cursor.getColumnIndex(DbHandler.KEY_NAME));
                        }
                    } else {
                        if(DbHandler.isOnline(context)) {
                            if(DbHandler.needToReconnect)
                                connection = DbHandler.setNewConnection(DriverManager.getConnection(DbHandler.MSSQL_DB, DbHandler.MSSQL_LOGIN, DbHandler.MSSQL_PASS));
                            else
                                connection = DbHandler.getConnection();
                        }

                        if (connection != null) {
                            SQL = "SELECT * FROM " + DbHandler.TABLE_ITEMS + " WHERE " + DbHandler.KEY_ID + " = " + this.itemId;
                            st = connection.createStatement();
                            rs = st.executeQuery(SQL);

                            if (rs != null && !isCancelled()) {
                                rs.next();
                                int id = rs.getInt(1);
                                String tname = rs.getString(2);
                                if (status.equals("item")) name = tname;

                                String desc = rs.getString(3);
                                blob = rs.getBytes(6);
                                String serverDateStr = rs.getString(7);

                                ContentValues contentValues = new ContentValues();
                                contentValues.put(DbHandler.KEY_ID, id);
                                contentValues.put(DbHandler.KEY_SECTION_ID, secId);
                                contentValues.put(DbHandler.KEY_NAME, tname);
                                contentValues.put(DbHandler.KEY_DESCRIPTION, desc);
                                contentValues.put(DbHandler.KEY_MINI_IMAGE, blob);
                                contentValues.put(DbHandler.KEY_ITEM_STATUS, "missing");
                                contentValues.put(DbHandler.KEY_DATE_OF_CHANGE, serverDateStr);
                                mDb.insert(DbHandler.TABLE_ITEMS, null, contentValues);

                                rs.close();
                                st.close();
                            }
                        }
                    }

                    cursor.close();
                    BitmapFactory.Options o = new BitmapFactory.Options();
                    o.inJustDecodeBounds = true;
                    BitmapFactory.decodeByteArray(blob, 0, blob.length, o);


                    int size = calculateInSampleSize(o, pictureSize, pictureSize);
                    if (size < 1) size = 1;
                    o = new BitmapFactory.Options();
                    o.inSampleSize = size;
                    o.inPreferredConfig = Bitmap.Config.RGB_565;

                    Bitmap bitmap = BitmapFactory.decodeByteArray(blob, 0, blob.length, o);

                    return bitmap;
                } catch (SQLException e) {
                    e.printStackTrace();
                }  finally {
                    try {
                        if (rs != null) rs.close();
                        if (st != null) st.close();
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
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {

                ImageView currentImageView = new ImageView(context);

                currentImageView.setImageBitmap(bitmap);
                currentImageView.setBackground(gradientDrawable);
                currentImageView.setLayoutParams(getImageParamsBySide(countImages));
                currentImageView.setPadding(puddingsSize, puddingsSize, puddingsSize, puddingsSize);
                tempId = View.generateViewId();
                currentImageView.setId(tempId);
                currentImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clearOffers();
                        switch (status) {
                            case "item": {
                                CurrentItemFragment currentItemFragment = new CurrentItemFragment();
                                Bundle bundle = new Bundle();
                                bundle.putInt("id", itemId);
                                bundle.putInt("itemId", itemId);
                                bundle.putInt("sectionId", secId);
                                bundle.putString("type", fragmentType);
                                bundle.putString("title", currentTitle);
                                currentItemFragment.setArguments(bundle);
                                FragmentManager fragmentManager = getFragmentManager();
                                fragmentManager.beginTransaction().replace(R.id.content_frame, currentItemFragment).commit();
                                break;
                            }
                            case "section": {
                                AsyncDrawAllItems drawAllItems = new AsyncDrawAllItems(secId, name);
                                drawAllItems.execute();
                                break;
                            }
                            case "collection": {
                                AsyncDrawAllSections drawAllSections = new AsyncDrawAllSections(collectionId, name);
                                drawAllSections.execute();
                                break;
                            }
                        }
                    }
                });
                layout.addView(currentImageView, currentId++);
                layout.addView(getTextViewBySide(name, countImages), currentId++);
                countImages++;
            }
        }
    }

    private class AsyncDrawAllItems extends AsyncTask<Void, Integer, Void>
    {
        private String SQL, sectionName;
        private int secId;

        public AsyncDrawAllItems(int secId, String sectionName) {
            this.secId = secId;
            this.sectionName = sectionName;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            actionBar.setTitle(sectionName);
            currentTitle = sectionName;
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
                    if (!DbHandler.isOnline(context)) {
                        cursorItems = mDb.query(DbHandler.TABLE_ITEMS, new String[]{DbHandler.KEY_ID}, DbHandler.KEY_SECTION_ID + " = " + secId, null, null, null, null);
                        if (cursorItems.getCount() > 0 && cursorItems.moveToFirst()) {
                            do {
                                publishProgress(cursorItems.getInt(cursorItems.getColumnIndex(DbHandler.KEY_ID)), secId);
                            } while (cursorItems.moveToNext());
                        }
                        cursorItems.close();
                    } else {
                        if(DbHandler.isOnline(context)) {
                            if(DbHandler.needToReconnect)
                                connection = DbHandler.setNewConnection(DriverManager.getConnection(DbHandler.MSSQL_DB, DbHandler.MSSQL_LOGIN, DbHandler.MSSQL_PASS));
                            else
                                connection = DbHandler.getConnection();
                        }

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
            AsyncCurrentItem currentItem = new AsyncCurrentItem(values[0], values[1], "item");
            offers.add(currentItem);
            currentItem.execute();
        }
    }

    private class AsyncDrawAllSections extends AsyncTask<Void, Object, String>
    {
        int collectionId;
        String SQL, collectionName;


        public AsyncDrawAllSections(int collectionId, String collectionName) {
            currentCollection = collectionId;
            this.collectionId = collectionId;
            this.collectionName = collectionName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            actionBar.setTitle(collectionName);
            currentTitle = collectionName;
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
                    if(!DbHandler.isOnline(context)) {
                        Cursor cursorSections = mDb.query(DbHandler.TABLE_SECTIONS, new String[]{DbHandler.KEY_ID, DbHandler.KEY_NAME}, DbHandler.KEY_COLLECTION_ID + " = " + collectionId, null, null, null, null);
                        if (cursorSections.getCount() > 0 && cursorSections.moveToFirst()) {
                            do {
                                String name = cursorSections.getString(cursorSections.getColumnIndex(DbHandler.KEY_NAME));
                                int secId = cursorSections.getInt(cursorSections.getColumnIndex(DbHandler.KEY_ID));
                                Cursor cursorItems = mDb.query(DbHandler.TABLE_ITEMS, new String[]{DbHandler.KEY_ID}, DbHandler.KEY_SECTION_ID + " = " + secId, null, null, null, null);
                                if (cursorItems.getCount() > 0 && cursorItems.moveToFirst()) {
                                    int idItem = cursorItems.getInt(cursorItems.getColumnIndex(DbHandler.KEY_ID));
                                    publishProgress(new Object[]{idItem, secId, name});
                                }
                            } while (cursorSections.moveToNext());
                        }
                    } else {
                        if(DbHandler.isOnline(context)) {
                            if(DbHandler.needToReconnect)
                                connection = DbHandler.setNewConnection(DriverManager.getConnection(DbHandler.MSSQL_DB, DbHandler.MSSQL_LOGIN, DbHandler.MSSQL_PASS));
                            else
                                connection = DbHandler.getConnection();
                        }

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
                                                publishProgress(new Object[]{itemId, secId, name});
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
                                                publishProgress(new Object[]{itemId, secId, name});
                                            }
                                            cursorItems.close();
                                        }
                                    }
                                    cursorSections.close();
                                }
                            }
                        }
                    }
                }catch (SQLException e) {
                    e.printStackTrace();
                }  finally {
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
            AsyncCurrentItem currentItem = new AsyncCurrentItem((int)values[0], (int)values[1], (String)values[2], "section");
            offers.add(currentItem);
            currentItem.execute();
        }
    }

    public class AsyncDrawAllCollections extends AsyncTask<Void, Object, Void>
    {
        String SQL;

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
                    if (!DbHandler.isOnline(context)) {
                        Cursor cursorCollections = mDb.query(DbHandler.TABLE_COLLECTIONS, new String[]{DbHandler.KEY_ID, DbHandler.KEY_NAME}, null, null, null, null, null);
                        if (cursorCollections.moveToFirst()) {
                            do {
                                int collectionId = cursorCollections.getInt(cursorCollections.getColumnIndex(DbHandler.KEY_ID));
                                String name = cursorCollections.getString(cursorCollections.getColumnIndex(DbHandler.KEY_NAME));
                                Cursor cursorSection = mDb.query(DbHandler.TABLE_SECTIONS, new String[]{DbHandler.KEY_ID}, DbHandler.KEY_COLLECTION_ID + " = " + collectionId, null, null, null, null);
                                if (cursorSection.moveToFirst()) {
                                    int secId = cursorSection.getInt(cursorSection.getColumnIndex(DbHandler.KEY_ID));
                                    Cursor cursorItem = mDb.query(DbHandler.TABLE_ITEMS, new String[]{DbHandler.KEY_ID}, DbHandler.KEY_SECTION_ID + " = " + secId, null, null, null, null);
                                    if (cursorItem.moveToFirst()) {
                                        int itemId = cursorItem.getInt(cursorItem.getColumnIndex(DbHandler.KEY_ID));
                                        publishProgress(new Object[]{itemId, secId, collectionId, name});
                                    }
                                    cursorItem.close();
                                }
                                cursorSection.close();
                            } while (cursorCollections.moveToNext());
                        }
                        cursorCollections.close();
                    } else {
                        if (DbHandler.needToReconnect)
                            if(DbHandler.isOnline(context)) {
                                if(DbHandler.needToReconnect)
                                    connection = DbHandler.setNewConnection(DriverManager.getConnection(DbHandler.MSSQL_DB, DbHandler.MSSQL_LOGIN, DbHandler.MSSQL_PASS));
                                else
                                    connection = DbHandler.getConnection();
                            }

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
                                                            publishProgress(new Object[]{itemId, secId, collectionId, name});
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
                                                            publishProgress(new Object[]{itemId, secId, collectionId, name});
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
                                                    publishProgress(new Object[]{itemId, secId, collectionId, name});
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
            AsyncCurrentItem currentItem = new AsyncCurrentItem((int)values[0], (int)values[1], (int)values[2], (String)values[3], "collection");
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("status", fragmentStatus);
        outState.putString("type", fragmentType);
        outState.putString("title", currentTitle);
        if (fragmentStatus.equals("collection"))
            outState.putInt("id", currentCollection);
        else if (fragmentStatus.equals("section"))
            outState.putInt("id", currentSection);
    }
}
