package com.lobotino.collector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE;
import static android.os.Process.setThreadPriority;

public class DbHandler extends SQLiteOpenHelper{

    public static final int DATABASE_VERSION = 52;
    public static final String DB_NAME = "info.db";
    public static String DB_PATH = "";

    public static int USER_ID = 0;

    public static final String SALT = "AF*$#d+_21xsM";

    public static final String TABLE_COLLECTIONS = "Collections";
    public static final String TABLE_SECTIONS = "Sections";
    public static final String TABLE_ITEMS = "Items";
    public static final String TABLE_USER_ITEMS = "User_items";

    public static final String KEY_ID = "Id";
    public static final String KEY_NAME = "Name";
    public static final String KEY_DESCRIPTION = "Description";
    public static final String KEY_SECTION_ID = "SectionId";
    public static final String KEY_COLLECTION_ID = "CollectionId";
    public static final String KEY_IMAGE = "Image";
    public static final String KEY_MINI_IMAGE = "MiniImage";
    public static final String KEY_ITEM_STATUS = "Status";
    public static final String KEY_DATE_OF_CHANGE = "DateOfChange";

    public static final String STATUS_IN = "in";
    public static final String STATUS_SELLING = "on sell";
    public static final String STATUS_TRADE = "on trade";

    public final static String MSSQL_DB = "jdbc:jtds:sqlserver://wpl19.hosting.reg.ru:1433:/u0351346_Collectioner";
    public final static String MSSQL_LOGIN = "u0351346_Collectioner";
    public final static String MSSQL_PASS= "eXcl17&7";

    public static boolean needToReconnect = true;

    private static Connection connection;

    public static int globalUserID = 0;

    private SQLiteDatabase mdb;

    public DbHandler(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
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

    private class SetConnectionThread extends AsyncTask<Void, Void, Connection>
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

    public void logAllItems()
    {
        Cursor cursor = mdb.query(DbHandler.TABLE_ITEMS, null, null, null, null, null, null, null);
        if(cursor.moveToFirst())
        {
            do{
                int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                int secId = cursor.getInt(cursor.getColumnIndex(KEY_SECTION_ID));
                String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));

                Log.d("Item:", "ID:" + id + ", " + "SecID:" + secId + ", " + "NAME:"+name);
            }while(cursor.moveToNext());
        }
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

                AsyncDownloadImgToServer insertImage = new AsyncDownloadImgToServer(name, desc, pathToImage, id, idSection, date, 400, 1024, mContext);
                insertImage.execute();
                cursorImages.moveToNext();
            }
        }
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

}
