package com.lobotino.collector;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DbHandler extends SQLiteOpenHelper{

    public static final int DATABASE_VERSION = 55;
    public static final String DB_NAME = "info.db";
    public static String DB_PATH = "";

    public static int USER_ID = -1;
    public static String USER_LOGIN = "";
    public static String USER_PASS = "";

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
    public static final String STATUS_SELLING = "on sell";
    public static final String STATUS_TRADE = "on trade";

    public static String MSSQL_DB;
    public static String MSSQL_LOGIN;
    public static String MSSQL_PASS;

    public static boolean needToReconnect = true;

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

        JSONHelper.CurrentUser currentUser = JSONHelper.importFromJSON(context);
        if(currentUser != null)
        {
            USER_ID = currentUser.getId();
            USER_LOGIN = currentUser.getLogin();
            USER_PASS = currentUser.getPass();
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

        //new SetConnectionThread().execute();
        //Delete!
       // clearTableOnServer(TABLE_ITEMS);
        //fillItemsOnServer();
        //fillDbOnServerDifSizes();
    }

    public static Connection getConnection() {
        return connection;
    }

    public static Connection setNewConnection(Connection con)
    {
        connection = con;
        needToReconnect = false;
        return connection;
    }

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

    public void clearCash()
    {
        getDataBase().delete(TABLE_ITEMS, null, null);
        getDataBase().delete(TABLE_SECTIONS, null, null);
        getDataBase().delete(TABLE_COLLECTIONS, null, null);
        Log.d("mDb", "Cash clear succes");
    }

    public void syncUserItems()
    {
        new AsyncSyncUserItems(this.getDataBase()).execute();
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

    private void updateUserItems()
    {
        new AsyncUpdateUserItems().execute();
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

                AsyncDownloadImgToServer insertImage = new AsyncDownloadImgToServer(name, desc, pathToImage, id, idSection, date, 400, 1024, mContext);
                insertImage.execute();
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

              //  AsyncDownloadImgToServer insertImage = new AsyncDownloadImgToServer(size + "", desc, pathToImage, i, 0, 128, (int)size, mContext);
              //  insertImage.execute();
            }
        }
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

    private class AsyncSyncUserItems extends AsyncTask<Void, Void, Void>
    {
        private Connection connection;
        private SQLiteDatabase mDb;

        public AsyncSyncUserItems(SQLiteDatabase mDb) {
            this.mDb = mDb;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            Statement st = null;
            ResultSet rs = null;
            try {
                if (DbHandler.needToReconnect)
                    connection = DbHandler.setNewConnection(DriverManager.getConnection(DbHandler.MSSQL_DB, DbHandler.MSSQL_LOGIN, DbHandler.MSSQL_PASS));
                else
                    connection = DbHandler.getConnection();

                ContentValues contentValues = new ContentValues();
                contentValues.put(DbHandler.KEY_ITEM_STATUS, "missing");
                mDb.update(DbHandler.TABLE_ITEMS, contentValues, DbHandler.KEY_ITEM_STATUS + " = ?", new String[]{"in"});


                String SQL = "SELECT " + DbHandler.KEY_ITEM_ID + " FROM " + DbHandler.TABLE_USERS_ITEMS + " WHERE "
                        + DbHandler.KEY_USER_ID + " = " + DbHandler.USER_ID;
                st = connection.createStatement();
                rs = st.executeQuery(SQL);
                while(rs.next())
                {
                    int itemId = rs.getInt(1);
                    contentValues = new ContentValues();
                    contentValues.put(DbHandler.KEY_ITEM_STATUS, "in");
                    mDb.update(DbHandler.TABLE_ITEMS, contentValues, DbHandler.KEY_ID + " = " + itemId, null);
                }
                st.close();
                rs.close();

                //TO DO чтобы при нахождении нескачанного итема - качалось
                //И при открытии моих коллекций при отчищенном кеше - качать качать
            }
            catch (SQLException e) {
                e.printStackTrace();
                if (st != null) try {
                    st.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
                if (rs != null) try {
                    rs.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            return null;
        }
    }

}
