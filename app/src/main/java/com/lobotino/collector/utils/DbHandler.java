package com.lobotino.collector.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.lobotino.collector.R;
import com.lobotino.collector.activities.LoginActivity;
import com.lobotino.collector.async_tasks.AsyncClearTable;
import com.lobotino.collector.async_tasks.AsyncGetBitmapsFromUri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DbHandler extends SQLiteOpenHelper{

    public static final int DATABASE_VERSION = 63;
    public static final String DB_NAME = "info.db";
    public static String DB_PATH = "";

    public static int USER_ID = -1;
    public static String USER_LOGIN = "";
    public static String USER_PASS = "";
    public static String USER_EMAIL = "";

    public static String SALT;

    public static final String TABLE_COLLECTIONS = "Collections";
    public static final String TABLE_SECTIONS = "Sections";
    public static final String TABLE_ITEMS = "Items";
    public static final String TABLE_USERS_ITEMS = "Users_Items";
    public static final String TABLE_USERS = "Users";

    public static final String KEY_ID = "Id";
    public static final String KEY_USER_ID = "IdUser";
    public static final String KEY_ITEM_ID = "IdItem";
    public static final String KEY_NAME = "Name";
    public static final String KEY_DESCRIPTION = "Description";
    public static final String KEY_SECTION_ID = "SectionId";
    public static final String KEY_COLLECTION_ID = "CollectionId";
    public static final String KEY_IMAGE = "Image";
    public static final String KEY_MINI_IMAGE = "MiniImage";
    public static final String KEY_ITEM_STATUS = "Status";
    public static final String KEY_DATE_OF_CHANGE = "DateOfChange";
    public static final String KEY_LOGIN = "Login";
    public static final String KEY_USER_NAME = "UserName";
    public static final String KEY_PASSWORD = "Password";
    public static final String KEY_ROLE_ID = "RoleId";
    public static final String KEY_REG_DATE = "RegDate";
    public static final String KEY_EMAIL = "Email";
    public static final String KEY_LAST_ACTIVITY_DAYE = "LastActivityDate";

    public static final String STATUS_IN = "in";
    public static final String STATUS_TRADE = "trade";
    public static final String STATUS_WISH = "wish";
    public static final String STATUS_MISS = "missing";

    public static final String MY_COLLECTIONS = "myCollections";
    public static final String COM_COLLECTIONS = "comCollections";

    public static final String COL_TYPE = "collectionType";

    public static String MSSQL_DB;
    public static String MSSQL_LOGIN;
    public static String MSSQL_PASS;

    public static boolean needToReconnect = true, needToSync = false;

    private static Connection connection;

    public static int globalUserID = 0;

    private SQLiteDatabase mdb;

    public static DbHandler instance;

    public static DbHandler getInstance(Context context)
    {
        if(instance == null) return new DbHandler(context);
        return instance;
    }

    public DbHandler(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
        MSSQL_DB = context.getString(R.string.database_db);
        MSSQL_LOGIN = context.getString(R.string.database_login);
        MSSQL_PASS = context.getString(R.string.database_pass);
        SALT = context.getString(R.string.salt);


        JSONHandler.CurrentUser currentUser = JSONHandler.importFromJSON(context);
        if(currentUser != null)
        {
            USER_ID = currentUser.getId();
            USER_LOGIN = currentUser.getLogin();
            USER_PASS = currentUser.getPass();
            USER_EMAIL = currentUser.getEmail();
        }

        if (android.os.Build.VERSION.SDK_INT >= 17) {
            String path = context.getApplicationInfo().dataDir + "/databases/";
            DB_PATH = path;
        } else
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        this.mContext = context;

        copyDataBase();

        try {
            mdb = this.getReadableDatabase();
        }catch (android.database.SQLException e)
        {
            e.printStackTrace();
        }
    }

    public Connection getConnection(Context context) {
        if (isOnline(context)) {
            if (needToReconnect) {
                needToReconnect = false;
                try {
                    connection = DriverManager.getConnection(DbHandler.MSSQL_DB, DbHandler.MSSQL_LOGIN, DbHandler.MSSQL_PASS);
                    if(!connection.isClosed()) {
                        new AsyncSetLastActivityDate().execute();

                        if(needToSync) syncUserItems();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return connection;
        } else {
            needToReconnect = true;
            return null;
        }
    }


//    public static Connection setNewConnection(Connection con)
//    {
//        connection = con;
//        needToReconnect = false;
//        return connection;
//    }

    private class setConnectionThread extends AsyncTask<Void, Void, Connection>
    {
        @Override
        protected Connection doInBackground(Void... voids) {
            try {
                connection = DriverManager.getConnection(MSSQL_DB, MSSQL_LOGIN, MSSQL_PASS);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static boolean isOnline(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting())
        {
            return true;
        }
        needToReconnect = true;
        connection = null;
        return false;
    }

    public void clearCash() //TO DO чистить секции и коллекции!
    {
  //      getDataBase().delete(TABLE_ITEMS, KEY_ITEM_STATUS + " = ?", new String[]{"missing"});
//        getDataBase().delete(TABLE_ITEMS, null, null);
//        getDataBase().delete(TABLE_SECTIONS, null, null);
//        getDataBase().delete(TABLE_COLLECTIONS, null, null);
//        syncUserItems();
        Log.d("mDb", "Cash clear succes");
    }

    public void syncUserItems()
    {
        if(isOnline(mContext))
            new AsyncSyncUserItems(this.getWritableDatabase()).execute(); //mdb == null?
        else
            needToSync = true;
    }

    public void changeUser(Context context) //Нужна проверка на онлайн
    {
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + context.getString(R.string.user_info);
        File f = context.getFileStreamPath(context.getString(R.string.user_info));
        if (f.exists()) {
            f.delete();
        }

        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    public void logAllItems()
    {
        Cursor cursor = mdb.query(DbHandler.TABLE_ITEMS, null, null, null, null, null, null, null);
        if(cursor.moveToFirst())
        {
            do{
                int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                int secId = cursor.getInt(cursor.getColumnIndex(KEY_SECTION_ID));
                String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                String date = cursor.getString(cursor.getColumnIndex(KEY_DATE_OF_CHANGE));
                Log.d("Item:", "ID:" + id + ", " + "SecID:" + secId + ", " + "NAME:"+name + ", " + "DATEOFCHANGE:" + date);
            }while(cursor.moveToNext());
        }
        cursor.close();
    }

    private void clearTableOnServer(String table)
    {
        AsyncClearTable asyncClearTable = new AsyncClearTable();
        asyncClearTable.execute(table);
    }


    private void fillItemsOnServer()
    {
        Cursor cursorImages = mdb.query(DbHandler.TABLE_ITEMS, null, null, null, null, null, null);

        if(cursorImages.moveToFirst()) {
            for(int i = 0; i < 10; i++)
                cursorImages.moveToNext();
            for(int i = 0; i < 12; i++) {
                int id = cursorImages.getInt(cursorImages.getColumnIndex(KEY_ID));
                int idSection = cursorImages.getInt(cursorImages.getColumnIndex(KEY_SECTION_ID));
                String name = cursorImages.getString(cursorImages.getColumnIndex(KEY_NAME));
                String desc = cursorImages.getString(cursorImages.getColumnIndex(KEY_DESCRIPTION));
                int columnIndex = cursorImages.getColumnIndex(KEY_IMAGE);
                String pathToImage = cursorImages.getString(columnIndex);
                DateFormat orig = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss");
                String date = orig.format(Calendar.getInstance().getTime());

//                AsyncGetBitmapsFromUri insertImage = new AsyncGetBitmapsFromUri(pathToImage,400, 1024, mContext);
//                insertImage.execute();
                cursorImages.moveToNext();
            }
        }
        cursorImages.close();
    }

    private void fillSectiondOnServer(){
        Cursor cursorSections = mdb.query(DbHandler.TABLE_SECTIONS, null,null,null,null,null,null);
        if(cursorSections.moveToFirst())
        {
            do{
                int id = cursorSections.getInt(cursorSections.getColumnIndex(KEY_ID));
                int colId = cursorSections.getInt(cursorSections.getColumnIndex(KEY_COLLECTION_ID));
                String name = cursorSections.getString(cursorSections.getColumnIndex(KEY_NAME));
                String desc = cursorSections.getString(cursorSections.getColumnIndex(KEY_DESCRIPTION));


            }while(cursorSections.moveToNext());
        }
    }

    private void fillDbOnServerDifSizes()
    {
        Cursor cursorImages = mdb.query(DbHandler.TABLE_ITEMS, null, null, null, null, null, null);

        if(cursorImages.moveToFirst()) {
            for(int i = 5; i < 13; i++) {
                double size = Math.pow(2, i);
                String desc = cursorImages.getString(cursorImages.getColumnIndex(KEY_DESCRIPTION));
                int columnIndex = cursorImages.getColumnIndex(KEY_IMAGE);
                String pathToImage = cursorImages.getString(columnIndex);

              //  AsyncGetBitmapsFromUri insertImage = new AsyncGetBitmapsFromUri(size + "", desc, pathToImage, i, 0, 128, (int)size, mContext);
              //  insertImage.execute();
            }
        }
        cursorImages.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion)
            mNeedUpdate = true;
    }


    private SQLiteDatabase mDataBase;
    private final Context mContext;
    private boolean mNeedUpdate = false;


    public void updateDataBase() throws IOException {
        if (mNeedUpdate) {
            File dbFile = new File(DB_PATH + DB_NAME);
            if (dbFile.exists())
                dbFile.delete();

            copyDataBase();

            mNeedUpdate = false;
        }
    }

    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DB_NAME);
        return dbFile.exists();
    }

    private void copyDataBase() {
        if (!checkDataBase()) {
            this.getReadableDatabase();
            this.close();
            try {
                copyDBFile();
            } catch (IOException mIOException) {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    private void copyDBFile() throws IOException {
        InputStream mInput = mContext.getAssets().open(DB_NAME);
        OutputStream mOutput = new FileOutputStream(DB_PATH + DB_NAME);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0)
            mOutput.write(mBuffer, 0, mLength);
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    public boolean openDataBase() throws SQLException {
        String path = DB_PATH + DB_NAME;
        mDataBase = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        return mDataBase != null;
    }

    public SQLiteDatabase getDataBase()
    {
        return mDataBase;
    }

    @Override
    public synchronized void close() {
        if (mDataBase != null)
            mDataBase.close();
        super.close();
    }

    public long insert(String table, String nullColumnHack, ContentValues values)
    {
        return mDataBase.insert(table,nullColumnHack, values);
    }

    private static class AsyncSetLastActivityDate extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {
            String SQL = "update " + DbHandler.TABLE_USERS + " SET " +  DbHandler.KEY_LAST_ACTIVITY_DAYE + " = ? WHERE " +  DbHandler.KEY_ID + " = " + DbHandler.USER_ID;
            try {
                PreparedStatement pSt = connection.prepareStatement(SQL);

                DateFormat orig = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss");
                String date = orig.format(Calendar.getInstance().getTime());
                pSt.setString(1, date);
                pSt.execute();
                pSt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class AsyncSyncUserItems extends AsyncTask<Void, Void, Void>    //Синхронизация предметов (вызывается при смене пользователя)
    {
        private Connection connection;
        private SQLiteDatabase mDb;

        public AsyncSyncUserItems(SQLiteDatabase mDb) {
            this.mDb = mDb;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            Statement st1 = null;
            ResultSet rs1 = null;
            Statement st2 = null;
            ResultSet rs2 = null;
            Statement st3 = null;
            ResultSet rs3 = null;
            Statement st4 = null;
            ResultSet rs4 = null;
            try {
                connection = getConnection(mContext);

                //Выставить missing ВСЕМ предметам пользователя
                ContentValues contentValues = new ContentValues();
                contentValues.put(DbHandler.KEY_ITEM_STATUS, "missing");
                mDb.update(DbHandler.TABLE_ITEMS, contentValues, DbHandler.KEY_ITEM_STATUS + " = ?", new String[]{"in"});

                //Из онлайн базы достать все предметы, которые есть у нового пользователя
                String SQL = "SELECT " + DbHandler.KEY_ITEM_ID + " FROM " + DbHandler.TABLE_USERS_ITEMS + " WHERE "
                        + DbHandler.KEY_USER_ID + " = " + DbHandler.USER_ID;
                st1 = connection.createStatement();
                rs1 = st1.executeQuery(SQL);
                while(rs1.next())
                {
                    int itemId = rs1.getInt(1);


                    Cursor cursorItems = mDb.query(DbHandler.TABLE_ITEMS, new String[]{DbHandler.KEY_ID}, DbHandler.KEY_ID + " = ?", new String[]{itemId + ""}, null, null, null);
                    if (cursorItems.getCount() > 0 && cursorItems.moveToFirst()) {
                        contentValues = new ContentValues();
                        contentValues.put(DbHandler.KEY_ITEM_STATUS, "in");
                        mDb.update(DbHandler.TABLE_ITEMS, contentValues, DbHandler.KEY_ID + " = " + itemId, null);
                    }else {
                        SQL = "SELECT " + DbHandler.KEY_ID + ", " + DbHandler.KEY_SECTION_ID  + ", " + DbHandler.KEY_NAME  + ", " + DbHandler.KEY_DESCRIPTION  + ", " +
                                DbHandler.KEY_MINI_IMAGE + ", " + DbHandler.KEY_DATE_OF_CHANGE + " FROM " + DbHandler.TABLE_ITEMS + " WHERE " + DbHandler.KEY_ID + " = " + itemId;
                        st2 = connection.createStatement();
                        rs2 = st2.executeQuery(SQL);
                        if(rs2.next()) {
                            int id = rs2.getInt(1);
                            int secId = rs2.getInt(2);
                            String name = rs2.getString(3);
                            String desc = rs2.getString(4);
                            byte[] blob = rs2.getBytes(5);
                            String serverDateStr = rs2.getString(6);

                            st2.close();
                            rs2.close();

                            contentValues = new ContentValues();
                            contentValues.put(DbHandler.KEY_ID, id);
                            contentValues.put(DbHandler.KEY_SECTION_ID, secId);
                            contentValues.put(DbHandler.KEY_NAME, name);
                            contentValues.put(DbHandler.KEY_DESCRIPTION, desc);
                            contentValues.put(DbHandler.KEY_MINI_IMAGE, blob);
                            contentValues.put(DbHandler.KEY_ITEM_STATUS, "in");
                            contentValues.put(DbHandler.KEY_DATE_OF_CHANGE, serverDateStr);
                            mDb.insert(DbHandler.TABLE_ITEMS, null, contentValues);

                            //Качаем секцию
                            Cursor cursorSections = mDb.query(DbHandler.TABLE_SECTIONS, new String[]{DbHandler.KEY_ID}, DbHandler.KEY_ID + " = ?", new String[]{secId + ""}, null, null, null);
                            if (cursorSections.getCount() == 0) {
                                SQL = "SELECT * FROM " + DbHandler.TABLE_SECTIONS + " WHERE "
                                        + DbHandler.KEY_ID + " = " + secId;
                                st3 = connection.createStatement();
                                rs3 = st3.executeQuery(SQL);

                                if (rs3.next()) {
                                    int collectionId = rs3.getInt(2);

                                    contentValues = new ContentValues();
                                    contentValues.put(DbHandler.KEY_ID, rs3.getInt(1));
                                    contentValues.put(DbHandler.KEY_COLLECTION_ID, collectionId);
                                    contentValues.put(DbHandler.KEY_NAME, rs3.getString(3));
                                    contentValues.put(DbHandler.KEY_DESCRIPTION, rs3.getString(4));
                                    mDb.insert(DbHandler.TABLE_SECTIONS, null, contentValues);

                                    st3.close();
                                    rs3.close();

                                    //Качаем коллекцию
                                    Cursor cursorCollection = mDb.query(DbHandler.TABLE_COLLECTIONS, new String[]{DbHandler.KEY_ID}, DbHandler.KEY_ID + " = ?", new String[]{collectionId + ""}, null, null, null);
                                    if (cursorCollection.getCount() == 0) {
                                        SQL = "SELECT * FROM " + DbHandler.TABLE_COLLECTIONS + " WHERE "
                                                + DbHandler.KEY_ID + " = " + collectionId;
                                        st4 = connection.createStatement();
                                        rs4 = st4.executeQuery(SQL);
                                        if (rs4.next()) {
                                            contentValues = new ContentValues();
                                            contentValues.put(DbHandler.KEY_ID, rs4.getInt(1));
                                            contentValues.put(DbHandler.KEY_NAME, rs4.getString(2));
                                            contentValues.put(DbHandler.KEY_DESCRIPTION, rs4.getString(3));
                                            mDb.insert(DbHandler.TABLE_COLLECTIONS, null, contentValues);

                                            st4.close();
                                            rs4.close();
                                        }
                                    }
                                    cursorCollection.close();
                                }
                            }
                            cursorSections.close();
                        }
                    }
                    cursorItems.close();

                 }
                st1.close();
                rs1.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
                try {
                    if (st1 != null)
                        st1.close();
                    if (rs1 != null)
                        rs1.close();
                    if (st2 != null)
                        st2.close();
                    if (rs2 != null)
                        rs2.close();
                    if (st3 != null)
                        st3.close();
                    if (rs3 != null)
                        rs3.close();
                    if (st4 != null)
                        st4.close();
                    if (rs4 != null)
                        rs4.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            return null;
        }
    }

}
