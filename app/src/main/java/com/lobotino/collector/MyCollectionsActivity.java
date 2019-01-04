package com.lobotino.collector;

import android.Manifest;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MyCollectionsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private DbHandler dbHandler;
    private SQLiteDatabase mDb;
    private String[] galleryPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private List<ImageView> listImageViews;
    private List<String> listPaths;
    private int pictureSize;

    private ScrollView scrollView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.content_main2);
        scrollView = (ScrollView) findViewById(R.id.scroll_view_id_1);
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver observer = scrollView.getViewTreeObserver();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    observer.removeOnGlobalLayoutListener(this);
                } else {
                    observer.removeGlobalOnLayoutListener(this);
                }
                observer.addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        //Your code here
                    }
                });
            }
        });


        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();




        //Открытие базы данных

        dbHandler = new DbHandler(this);

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




//---------------Создание Layout




        RelativeLayout layout = (RelativeLayout) findViewById(R.id.relative_layout_1);
        RelativeLayout.LayoutParams params;

        Cursor cursor = mDb.query(DbHandler.TABLE_ITEMS, null, null, null, null, null, null);

        int pathIndex = cursor.getColumnIndex(DbHandler.KEY_ITEM_IMAGE_PATH);
        int puddingsSize = getApplicationContext().getResources().getDisplayMetrics().widthPixels / 15;
        int tempId;
        int lastLeftId = -1;
        int lastRightId = -1;
        int countImages = 0;

        String pathToImage;

        listImageViews = new ArrayList<ImageView>();
        listPaths = new ArrayList<String>();
        pictureSize = getApplicationContext().getResources().getDisplayMetrics().widthPixels / 2;

        int currentId = 0;
        if (EasyPermissions.hasPermissions(this, galleryPermissions)) {
            if (cursor.moveToFirst()) {
                do {
                    params = new RelativeLayout.LayoutParams(pictureSize, pictureSize);
                    if (countImages % 2 == 0) {
                        if (lastLeftId == -1) {
                            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                            params.addRule(RelativeLayout.ALIGN_PARENT_START);
                        } else {
                            params.addRule(RelativeLayout.BELOW, lastLeftId);
                            params.addRule(RelativeLayout.ALIGN_START, lastLeftId);
                        }

                    } else {
                        if (lastRightId == -1) {
                            params.addRule(RelativeLayout.END_OF, lastLeftId);
                            params.addRule(RelativeLayout.ALIGN_TOP, lastLeftId);
                        } else {
                            params.addRule(RelativeLayout.BELOW, lastRightId);
                            params.addRule(RelativeLayout.ALIGN_LEFT, lastRightId);
                        }
                    }

                    pathToImage = cursor.getString(pathIndex);
                    listPaths.add(pathToImage);

                    ImageView currentImageView = new ImageView(this);
                    currentImageView.setLayoutParams(params);
                    currentImageView.setPadding(puddingsSize, puddingsSize / 4, puddingsSize, puddingsSize / 4);
                    tempId = View.generateViewId();
                    currentImageView.setId(tempId);

                    DownloadScaledImage downloadScaledImage = new DownloadScaledImage();
                    downloadScaledImage.execute(currentId++);

                    layout.addView(currentImageView, countImages);
                    listImageViews.add(currentImageView);

                    if (countImages % 2 == 0)
                        lastLeftId = tempId;
                    else
                        lastRightId = tempId;

                    countImages++;
                } while (cursor.moveToNext());


            } else {
                EasyPermissions.requestPermissions(this, "Access for storage",
                        101, galleryPermissions);
            }
        }

        /*final ViewTreeObserver.OnScrollChangedListener onScrollChangedListener = new
                ViewTreeObserver.OnScrollChangedListener() {

                    @Override
                    public void onScrollChanged() {
                        //do stuff here
                    }
                };

        final ScrollView scrollView = (ScrollView) findViewById(R.id.scroll_view_id_1);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            private ViewTreeObserver observer;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (observer == null) {
                    observer = scrollView.getViewTreeObserver();
                    observer.addOnScrollChangedListener(onScrollChangedListener);
                }
                else if (!observer.isAlive()) {
                    observer.removeOnScrollChangedListener(onScrollChangedListener);
                    observer = scrollView.getViewTreeObserver();
                    observer.addOnScrollChangedListener(onScrollChangedListener);
                }

                return false;
            }
        });*/

        //---
        //Найдем компоненты в XML разметке
        //imageView = (ImageView) findViewById(R.id.);
        //textView = (TextView) findViewById(R.id.textView3);

        //Пропишем обработчик клика кнопки
//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String product = "";
//                imageView.setVisibility(View.INVISIBLE);
//
//                Cursor cursor = mDb.query(DbHandler.TABLE_ITEMS, null, null, null, null, null, null);
//                cursor.moveToFirst();
//                int indexItemId = cursor.getColumnIndex(DbHandler.KEY_ITEM_ID);
//                int indexItemName = cursor.getColumnIndex(DbHandler.KEY_ITEM_NAME);
//                int indexItemDescription = cursor.getColumnIndex(DbHandler.KEY_ITEM_DESCRIPTION);
//                while (!cursor.isAfterLast()) {
//                    product += cursor.getInt(indexItemId) + " " + cursor.getString(indexItemName) + " " + cursor.getString(indexItemDescription) + "\n\n";
//                    cursor.moveToNext();
//                }
//
//                cursor.close();
//                textView.setText(product);
//            }
//        });
    }


    //Уменьшает размер одной картинки из базы данных по её ID адресу

    public class DownloadScaledImage extends AsyncTask<Integer, Void, Bitmap>
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

        private int id;

        @Override
        protected Bitmap doInBackground(Integer... integers){
            id = integers[0];
            try {
                String pathToImage = listPaths.get(id);

                InputStream in = getApplicationContext().getAssets().open(pathToImage); //Ваш InputStream
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(in, null, o);
                in.close();
                int size = calculateInSampleSize(o, pictureSize, pictureSize);

                o = new BitmapFactory.Options();

                o.inSampleSize = size;
                o.inPreferredConfig = Bitmap.Config.RGB_565;

                in.close();
                in = getApplicationContext().getAssets().open(pathToImage);
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
            listImageViews.get(id).setImageBitmap(bitmap);
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_community_colletions) {

        } else if (id == R.id.nav_my_collections) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {

    }
}
