package com.lobotino.collector;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

public class DownloadScaledImage extends AsyncTask<ImageView, Void, Bitmap>
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
    private Context context;
    private byte[] blob = null;

    public DownloadScaledImage(int itemId, int pictureSize, Context context) {  //Get Image from sqlite
        this.itemId = itemId;
        this.pictureSize = pictureSize;
        this.context = context;
    }

    public DownloadScaledImage(byte[] blob, int pictureSize, Context context) { //Get Image from mysql
        this.pictureSize = pictureSize;
        this.context = context;
        this.blob = blob;
    }

    @Override
    protected Bitmap doInBackground(ImageView... views) {
        imageView = views[0];

        if (blob == null) {
            SQLiteDatabase mDb;
            try {
                mDb = NavigationActivity.dbHandler.getDataBase();
            } catch (SQLException mSQLException) {
                throw mSQLException;
            }

            Cursor cursor = mDb.query(DbHandler.TABLE_ITEMS, new String[]{DbHandler.KEY_IMAGE}, DbHandler.KEY_ID + " = " + itemId, null, null, null, null);
            if (cursor.moveToFirst()) {
                blob = cursor.getBlob(cursor.getColumnIndex(DbHandler.KEY_IMAGE));
            }
            cursor.close();
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
        imageView.setImageBitmap(bitmap);
    }
}
