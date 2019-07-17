package com.lobotino.collector.async_tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;



public class AsyncGetBitmapsFromUri extends AsyncTask<Void, Void, Bitmap[]>
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
        return inSampleSize;
    }

    private Uri pathToImage;
    private int imgSize, imgMiniSize;
    private Context context;

    public AsyncGetBitmapsFromUri(Uri pathToImage,int imgSize, int imgMiniSize, Context context) {
        this.pathToImage = pathToImage;
        this.imgSize = imgSize;
        this.imgMiniSize = imgMiniSize;
        this.context = context;
    }

    @Override
    protected Bitmap[] doInBackground(Void... voids){
        try {
            //InputStream in = context.getAssets().open(pathToImage);

            InputStream in = context.getContentResolver().openInputStream(pathToImage);

            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();

            int size = calculateInSampleSize(o, imgSize, imgSize);
            if(size < 1) size = 1;
            o = new BitmapFactory.Options();
            o.inSampleSize = size;
            o.inPreferredConfig = Bitmap.Config.RGB_565;

            //in = context.getAssets().open(pathToImage);
            in = context.getContentResolver().openInputStream(pathToImage);
            Bitmap bitmap1 = BitmapFactory.decodeStream(in, null, o);
            in.close();


//            in = context.getAssets().open(pathToImage);
            in = context.getContentResolver().openInputStream(pathToImage);
            o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();

            size = calculateInSampleSize(o, imgMiniSize, imgMiniSize);
            if(size < 1) size = 1;
            o = new BitmapFactory.Options();
            o.inSampleSize = size;
            o.inPreferredConfig = Bitmap.Config.RGB_565;


//            in = context.getAssets().open(pathToImage);
            in = context.getContentResolver().openInputStream(pathToImage);
            Bitmap bitmap2 = BitmapFactory.decodeStream(in, null, o);
            in.close();

            return new Bitmap[]{bitmap1, bitmap2};
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

//    @Override
//    protected void onPostExecute(Bitmap[] bitmaps) {
//        super.onPostExecute(bitmaps);
//        if(bitmaps.length == 2) {
//            try {
//                AsyncInsertItemIntoServer mdbInsertImage = new AsyncInsertItemIntoServer(secId, name, desc, date);
//                mdbInsertImage.execute(bitmaps);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
