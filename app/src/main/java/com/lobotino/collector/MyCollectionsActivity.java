package com.lobotino.collector;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.io.InputStream;

public class MyCollectionsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private DbHandler dbHandler;
    private SQLiteDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        int countImages = 0;
        int tempId;
        int lastLeftId = -1;
        int lastRightId = -1;

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.relative_layout_1);
        RelativeLayout.LayoutParams params;

        Cursor cursor = mDb.query(DbHandler.TABLE_ITEMS, null, null, null, null, null, null);
        int pathIndex = cursor.getColumnIndex(DbHandler.KEY_ITEM_IMAGE_PATH);
        String pathToImage;
        InputStream inputStream = null;
        int puddingsSize = getApplicationContext().getResources().getDisplayMetrics().widthPixels / 15;

        if (cursor.moveToFirst()) {
            do{
                int pictureSize = getApplicationContext().getResources().getDisplayMetrics().widthPixels / 2;
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

                ImageView imageView = new ImageView(this);
                imageView.setImageDrawable(Drawable.createFromPath(pathToImage));
                imageView.setLayoutParams(params);

                try{
                    inputStream = getApplicationContext().getAssets().open(pathToImage);
                    Drawable d = Drawable.createFromStream(inputStream, null);
                    imageView.setImageDrawable(d);
                    inputStream.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                finally {
                    try{
                        if(inputStream!=null)
                            inputStream.close();
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                }

                imageView.setPadding(puddingsSize, puddingsSize / 4, puddingsSize, puddingsSize / 4);
                tempId = View.generateViewId();
                imageView.setId(tempId);
                layout.addView(imageView, countImages);

                if (countImages % 2 == 0)
                    lastLeftId = tempId;
                else
                    lastRightId = tempId;

                countImages++;
            } while (cursor.moveToNext());
        }


        //---
        //Найдем компоненты в XML разметке
        //imageView = (ImageView) findViewById(R.id.);
        //textView = (TextView) findViewById(R.id.textView3);

        //Пропишем обработчик клика кнопки
/*        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String product = "";
                imageView.setVisibility(View.INVISIBLE);

                Cursor cursor = mDb.query(DbHandler.TABLE_ITEMS, null, null, null, null, null, null);
                cursor.moveToFirst();
                int indexItemId = cursor.getColumnIndex(DbHandler.KEY_ITEM_ID);
                int indexItemName = cursor.getColumnIndex(DbHandler.KEY_ITEM_NAME);
                int indexItemDescription = cursor.getColumnIndex(DbHandler.KEY_ITEM_DESCRIPTION);
                while (!cursor.isAfterLast()) {
                    product += cursor.getInt(indexItemId) + " " + cursor.getString(indexItemName) + " " + cursor.getString(indexItemDescription) + "\n\n";
                    cursor.moveToNext();
                }

                cursor.close();
                textView.setText(product);
            }
        });*/


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
