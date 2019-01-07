package com.lobotino.collector;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import pub.devrel.easypermissions.EasyPermissions;

public class MyCollectionsFragment extends Fragment {

    private DbHandler dbHandler;
    private SQLiteDatabase mDb;
    private String[] galleryPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private View rootView;
    private Context context;
    private int pictureSize;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_my_collections, container, false);
        context = getActivity().getBaseContext();
        dbHandler = NavigationActivity.dbHandler;

        try {
            dbHandler.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }

        try {
            mDb = dbHandler.getWritableDatabase();
        } catch (SQLException mSQLException) {
            throw mSQLException;
        }

        //Square
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(15);
        gradientDrawable.setColor(Color.parseColor("#180c28"));
        //Square


        RelativeLayout layout = (RelativeLayout) rootView.findViewById(R.id.relative_layout_1);
        RelativeLayout.LayoutParams imageParams, textViewParams;

        Cursor cursor = mDb.query(DbHandler.TABLE_ITEMS, null, null, null, null, null, null);

        int pathIndex = cursor.getColumnIndex(DbHandler.KEY_ITEM_IMAGE_PATH);
        int nameIndex = cursor.getColumnIndex(DbHandler.KEY_ITEM_NAME);

        int tempId;
        int lastLeftId = -1;
        int lastRightId = -1;
        int countImages = 0;

        String pathToImage, currentName;

        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        pictureSize = Math.round((float) (screenWidth / 3));
        int externalMargins = screenWidth / 11;
        int topMargin = screenWidth / 10;
        int botMargin = screenWidth / 17;
        int puddingsSize = pictureSize / 15;

        int currentId = 0;
        if (EasyPermissions.hasPermissions(context, galleryPermissions)) {
            if (cursor.moveToFirst()) {
                do {
                    imageParams = new RelativeLayout.LayoutParams(pictureSize, pictureSize);

                    if (countImages % 2 == 0) {
                        if (lastLeftId == -1) {
                            imageParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                            imageParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                            imageParams.setMargins(externalMargins, screenWidth / 16, 0, 0);
                        } else {
                            imageParams.addRule(RelativeLayout.BELOW, lastLeftId);
                            imageParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                            imageParams.setMargins(externalMargins, topMargin, 0, 0);
                        }

                    } else {
                        if (lastRightId == -1) {
                            imageParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                            imageParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                            imageParams.setMargins(0, screenWidth / 16, externalMargins, 0);
                        } else {
                            imageParams.addRule(RelativeLayout.BELOW, lastRightId);
                            imageParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                            imageParams.setMargins(0, topMargin, externalMargins, 0);
                        }

                    }

                    pathToImage = cursor.getString(pathIndex);

                    ImageView currentImageView = new ImageView(context);
                    currentImageView.setBackground(gradientDrawable);
                    currentImageView.setLayoutParams(imageParams);
                    currentImageView.setPadding(puddingsSize, puddingsSize, puddingsSize, puddingsSize);
                    tempId = View.generateViewId();
                    currentImageView.setId(tempId);

                    Object offer[] = {pathToImage, currentImageView};
                    DownloadScaledImage downloadScaledImage = new DownloadScaledImage();
                    downloadScaledImage.execute(offer);

                    layout.addView(currentImageView, currentId++);


                    textViewParams = new RelativeLayout.LayoutParams(pictureSize, ViewGroup.LayoutParams.WRAP_CONTENT);

                    TextView tvImageName = new TextView(context);
                    currentName = cursor.getString(nameIndex);
                    tvImageName.setTextColor(Color.parseColor("#ffffff"));
                    tvImageName.setText(currentName);
                    tvImageName.setGravity(Gravity.CENTER);

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
                    tvImageName.setLayoutParams(textViewParams);
                    tvImageName.setTypeface(Typeface.DEFAULT_BOLD);

                    layout.addView(tvImageName, currentId++);

                    countImages++;
                } while (cursor.moveToNext());
            }

        } else {
            EasyPermissions.requestPermissions(this, "Access for storage",
                    101, galleryPermissions);
        }
        return rootView;
    }



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
            return inSampleSize;
        }

        private ImageView image;

        @Override
        protected Bitmap doInBackground(Object... objects){
            try {
                String pathToImage = (String)objects[0];
                image = (ImageView)objects[1];

                InputStream in = context.getAssets().open(pathToImage);

                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(in, null, o);
                in.close();

                int size = calculateInSampleSize(o, pictureSize, pictureSize);
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
            image.setImageBitmap(bitmap);
        }
    }
}
