package com.lobotino.collector.async_tasks;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.lobotino.collector.utils.DbHandler;
import com.lobotino.collector.activities.MainActivity;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class AsyncDownloadScaledImage extends AsyncTask<ImageView, Void, Bitmap>
{
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

    private ImageView imageView;
    private int itemId, pictureSize;
    private Uri path;
    private Context context;
    private byte[] blob = null;

    public AsyncDownloadScaledImage(int itemId, int pictureSize, Context context) {  //Get Image from sqlite
        this.itemId = itemId;
        this.pictureSize = pictureSize;
        this.context = context;
    }

    public AsyncDownloadScaledImage(byte[] blob, int pictureSize, Context context) { //Get Image from mysql
        this.pictureSize = pictureSize;
        this.context = context;
        this.blob = blob;
    }

    public AsyncDownloadScaledImage(Uri path, int pictureSize, Context context) { //Get Image from file
        this.pictureSize = pictureSize;
        this.context = context;
        this.itemId = -1;
        this.path = path;
    }

    @Override
    protected Bitmap doInBackground(ImageView... views) {
        if(views.length > 0)
            imageView = views[0];

        if (blob == null) {
            if(itemId == -1) {
                try {
//                    File file = new File(path);
//                    blob =  new byte[(int)file.length()];
                    FileInputStream fis = (FileInputStream)context.getContentResolver().openInputStream(path);
                    ArrayList<Byte> list = new ArrayList<>();
                    int result = fis.read();
                    while(result != -1) {
                        list.add((byte)result);
                        result = fis.read();
                    }
                    blob = new byte[(int)list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        blob[i] = list.get(i);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                SQLiteDatabase mDb;
                try {
                    mDb = MainActivity.dbHandler.getDataBase();
                } catch (SQLException mSQLException) {
                    throw mSQLException;
                }

                Cursor cursor = mDb.query(DbHandler.TABLE_ITEMS, new String[]{DbHandler.KEY_IMAGE}, DbHandler.KEY_ID + " = " + itemId, null, null, null, null);
                if (cursor.moveToFirst()) {
                    blob = cursor.getBlob(cursor.getColumnIndex(DbHandler.KEY_IMAGE));
                }
                cursor.close();
            }
        }

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
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if(imageView != null)
            imageView.setImageBitmap(bitmap);
    }
}
