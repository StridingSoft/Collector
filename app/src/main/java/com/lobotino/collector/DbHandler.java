package com.lobotino.collector;

import android.content.Context;
import android.database.Cursor;
<<<<<<< HEAD
<<<<<<< HEAD
import android.database.SQLException;
=======
>>>>>>> Добавлена БД юзеров. Готова регистрация
=======
import android.database.SQLException;
>>>>>>> Много-много радостей
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DbHandler extends SQLiteOpenHelper{

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
    public static final int DATABASE_VERSION = 27;
    public static final String DB_NAME = "info.db";
    public static String DB_PATH = "";

    public static int USER_ID = 0;

=======
    public static final int DATABASE_VERSION = 13;
    public static final String DB_NAME = "info.db";
    public static String DB_PATH = "";

>>>>>>> Много-много радостей
    public static final String SALT = "AF*$#d+_21xsM";

    public static final String TABLE_COLLECTIONS = "collections";
    public static final String TABLE_SECTIONS = "sections";
    public static final String TABLE_ITEMS = "items";
    public static final String TABLE_USERS = "users";
<<<<<<< HEAD
    public static final String TABLE_USERS_COLLECTIONS = "users_collections";
    public static final String TABLE_USERS_SECTIONS = "users_sections";
    public static final String TABLE_USERS_ITEMS = "users_items";
=======
>>>>>>> Много-много радостей

    public static final String KEY_GLOBAL_USER_ID = "_globalUserId";

    public static final String KEY_COLLECTION_ID = "_collectionId";
    public static final String KEY_COLLECTION_CREATOR_ID = "_collectionCreatorId";
    public static final String KEY_COLLECTION_NAME = "_collectionName";
    public static final String KEY_COLLECTION_DESCRIPTION = "_collectionDescription";
    public static final String KEY_COLLECTION_CREATE_DATE = "_collectionCreateDate";

    public static final String KEY_SECTION_ID = "_sectionId";
    public static final String KEY_SECTION_COLLECTION_ID = "_sectionCollectionId";
    public static final String KEY_SECTION_NAME = "_sectionName";
    public static final String KEY_SECTION_DESCRIPTION = "_sectionDescription";

    public static final String KEY_ITEM_ID = "_itemId";
    public static final String KEY_ITEM_SECTION_ID = "_itemSectionId";
    public static final String KEY_ITEM_NAME = "_itemName";
    public static final String KEY_ITEM_DESCRIPTION = "_itemDescription";
    public static final String KEY_ITEM_IMAGE_PATH = "_itemImagePath";

    public static final String KEY_USER_ID = "_userId";
    public static final String KEY_LOGIN = "_login";
    public static final String KEY_PASSWORD_HASH = "_passwordHash";
    public static final String KEY_REGISTER_DATE = "_registerDate";
<<<<<<< HEAD

    public static int globalUserID = 0;
=======
   public static final int DATABASE_VERSION = 1;
=======
   public static final int DATABASE_VERSION = 3;
>>>>>>> Добавлена БД юзеров. Готова регистрация
   public static final String DATABASE_NAME = "collectionsDb";
   public static final String TABLE_NAME = "collections";
   public static final String TABLE_USERS = "users";

   public static final String KEY_LOGIN = "_login";
   public static final String KEY_USER_ID = "_userId";
   public static final String KEY_GLOBAL_USER_ID = "_globalUserId";
   public static final String KEY_USER_SALT = "_userSalt";
   public static final String KEY_PASSWORD_HASH = "_passwordHash";
   public static final String KEY_REGISTER_DATE = "_registerDate";
   public static final String KEY_COLLECTION = "_collection";
   public static final String KEY_SET = "_set";
   public static final String KEY_ELEMENT = "_element";
>>>>>>> Не рабочая версия, вылетает при запуске cursor
=======
>>>>>>> Много-много радостей

    public static int globalUserID = 0;

    public DbHandler(Context context) {
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> Много-много радостей
        super(context, DB_NAME, null, DATABASE_VERSION);


        //--
        if (android.os.Build.VERSION.SDK_INT >= 17)
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        else
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        this.mContext = context;

        copyDataBase();

       this.getReadableDatabase();
        //--


        /*SQLiteDatabase db = this.getWritableDatabase();
<<<<<<< HEAD
=======
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = this.getWritableDatabase();
>>>>>>> Добавлена БД юзеров. Готова регистрация
=======
>>>>>>> Много-много радостей
        Cursor cursor = db.query(DbHandler.TABLE_USERS, null, null, null, null, null, null);
        if(cursor.moveToFirst()) {
            globalUserID = cursor.getInt(cursor.getColumnIndex(DbHandler.KEY_GLOBAL_USER_ID));
        }else{
            globalUserID = 0;
<<<<<<< HEAD
<<<<<<< HEAD
        }*/
=======
        }
>>>>>>> Добавлена БД юзеров. Готова регистрация
=======
        }*/
>>>>>>> Много-много радостей
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> Много-много радостей
      /*  db.execSQL("CREATE TABLE " + TABLE_COLLECTIONS + "("
                + KEY_COLLECTION_ID + " INTEGER,"
                + KEY_COLLECTION_CREATOR_ID + " INTEGER,"
                + KEY_COLLECTION_NAME + " TEXT,"
                + KEY_COLLECTION_CREATE_DATE + " TEXT,"
                + KEY_COLLECTION_DESCRIPTION + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_SECTIONS + "("
                + KEY_SECTION_ID + " INTEGER,"
                + KEY_SECTION_COLLECTION_ID + " INTEGER,"
                + KEY_SECTION_NAME + " TEXT,"
                + KEY_SECTION_DESCRIPTION + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_ITEMS + "("
                + KEY_ITEM_ID + " INTEGER,"
                + KEY_ITEM_SECTION_ID + " INTEGER,"
                + KEY_ITEM_NAME + " TEXT,"
                + KEY_ITEM_DESCRIPTION + " TEXT,"
                + KEY_ITEM_IMAGE_PATH + " TEXT)");
<<<<<<< HEAD
=======
        db.execSQL("CREATE TABLE " + TABLE_NAME + "("
                + KEY_COLLECTION + " TEXT,"
                + KEY_SET + " TEXT,"
                + KEY_ELEMENT + " TEXT)");
>>>>>>> Добавлена БД юзеров. Готова регистрация
=======
>>>>>>> Много-много радостей

        db.execSQL("CREATE TABLE " + TABLE_USERS + "("
                + KEY_GLOBAL_USER_ID + " INTEGER,"
                + KEY_USER_ID + " INTEGER,"
                + KEY_LOGIN + " TEXT,"
                + KEY_PASSWORD_HASH + " TEXT,"
<<<<<<< HEAD
<<<<<<< HEAD
                + KEY_REGISTER_DATE + " TEXT)");
*/
=======
        db.execSQL("create table " + TABLE_NAME + "(" + KEY_COLLECTION
                + " text," + KEY_SET + " text," + KEY_ELEMENT + " text" + ")");

        db.execSQL("create table " + TABLE_USERS + "(" + KEY_GLOBAL_USER_ID
                + " integer," + KEY_USER_ID + " integer," + KEY_LOGIN + " text," + KEY_PASSWORD_HASH + " text,"
                + KEY_REGISTER_DATE + " text)");
>>>>>>> Не рабочая версия, вылетает при запуске cursor
=======
                + KEY_USER_SALT + " TEXT,"
                + KEY_REGISTER_DATE + " TEXT)");
>>>>>>> Добавлена БД юзеров. Готова регистрация
=======
                + KEY_REGISTER_DATE + " TEXT)");
*/
>>>>>>> Много-много радостей

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> Много-много радостей
        if (newVersion > oldVersion)
            mNeedUpdate = true;

       /* db.execSQL("drop table if exists " + TABLE_COLLECTIONS);
        db.execSQL("drop table if exists " + TABLE_SECTIONS);
        db.execSQL("drop table if exists " + TABLE_ITEMS);
<<<<<<< HEAD
        db.execSQL("drop table if exists " + TABLE_USERS);
        onCreate(db);*/
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
        //InputStream mInput = mContext.getResources().openRawResource(R.raw.info);
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
        mDataBase = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        return mDataBase != null;
=======
        db.execSQL("drop table if exists " + TABLE_NAME);
        db.execSQL("drop table if exists " + TABLE_USERS);
        onCreate(db);
>>>>>>> Не рабочая версия, вылетает при запуске cursor
=======
        db.execSQL("drop table if exists " + TABLE_USERS);
        onCreate(db);*/
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
        //InputStream mInput = mContext.getResources().openRawResource(R.raw.info);
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
        mDataBase = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        return mDataBase != null;
>>>>>>> Много-много радостей
    }

    @Override
    public synchronized void close() {
        if (mDataBase != null)
            mDataBase.close();
        super.close();
    }

}
