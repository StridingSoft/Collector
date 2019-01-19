package com.lobotino.collector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

public class DownloadScaledImage extends AsyncTask<Object, Void, Bitmap>
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

    @Override
    protected Bitmap doInBackground(Object... objects){
        try {
            String pathToImage = (String)objects[0];
            imageView = (ImageView)objects[1];
            Context context = (Context) objects[2];
            int pictureSize = (Integer) objects[3];

            InputStream in = context.getAssets().open(pathToImage);

            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();

            int size = calculateInSampleSize(o, pictureSize, pictureSize);
            if(size < 1) size = 1;
            o = new BitmapFactory.Options();
            o.inSampleSize = size;
            o.inPreferredConfig = Bitmap.Config.RGB_565;
            in.close();

            in = context.getAssets().open(pathToImage);
            Bitmap bitmap = BitmapFactory.decodeStream(in, null, o);
            in.close();
            return bitmap;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        imageView.setImageBitmap(bitmap);
    }
}
