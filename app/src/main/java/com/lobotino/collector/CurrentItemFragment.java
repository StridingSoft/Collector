package com.lobotino.collector;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.sqlite.core.DB;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import pub.devrel.easypermissions.EasyPermissions;


public class CurrentItemFragment extends Fragment {

    private int itemId;
    private int sectionId;
    private String type;

    private DbHandler dbHandler;
    private SQLiteDatabase mDb;
    private View rootView;
    private Context context;
    private int pictureSize, screenWight, screenHeight, puddingsSize, topMargin;
    private GradientDrawable gradientDrawable;
    private RelativeLayout layout;
    private ScrollView scrollView;
    private Button buttonBack;
    private ActionBar actionBar;
    private ImageButton buttonTrade, buttonSell, buttonHaveIt;
    private TextView tvDescriptionTitle, tvDescription;
    private Connection connection;
    private boolean inMyCollection = false;
    public int getItemId(){
        return itemId;
    }

    public int getSectionId(){
        return sectionId;
    }

    public String getType()
    {
        return type;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_current_item, container, false);
        NavigationActivity navigationActivity = (NavigationActivity) getActivity();
        navigationActivity.setCurrentFragment(this);

        itemId = getArguments().getInt("id");
        sectionId = getArguments().getInt("sectionId");
        type = getArguments().getString("type");

        context = getActivity().getBaseContext();
        dbHandler = NavigationActivity.dbHandler;
        screenWight = context.getResources().getDisplayMetrics().widthPixels;
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        pictureSize = Math.round((float) (screenHeight /3*2));
        layout = (RelativeLayout) rootView.findViewById(R.id.relative_layout_2);
        scrollView = (ScrollView) rootView.findViewById(R.id.scroll_view_current_item);
        actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
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

        puddingsSize = pictureSize / 30; //15
        topMargin = screenWight / 20;

        gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(15);
        //gradientDrawable.setColor(Color.parseColor("#180c28"));
        gradientDrawable.setColors(new int[]{Color.parseColor("#5d000e"), Color.parseColor("#430014")});

        buttonBack = new Button(context);
        int buttonSize = screenWight /12;
        RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        int margin = screenWight /60;
        buttonParams.setMargins(0, margin, 0, 0);
        buttonBack.setText("");
        buttonBack.setBackgroundResource(R.drawable.ic_action_name);
        buttonBack.setLayoutParams(buttonParams);
        buttonBack.setId(View.generateViewId());

        Cursor cursorItems = mDb.query(DbHandler.TABLE_ITEMS, null, DbHandler.KEY_ID + " = " + itemId, null, null, null, null);
        if(cursorItems.moveToFirst())
        {
            int itemNameIndex = cursorItems.getColumnIndex(DbHandler.KEY_NAME);
            int itemDescIndex = cursorItems.getColumnIndex(DbHandler.KEY_DESCRIPTION);
            String itemName = cursorItems.getString(itemNameIndex);
            String itemDesc = cursorItems.getString(itemDescIndex);

            actionBar.setTitle(itemName);

            ImageView imageView = new ImageView(context);
            imageView.setBackground(gradientDrawable);
            //imageView.setPadding(puddingsSize, puddingsSize, puddingsSize, puddingsSize);
            int imageId = View.generateViewId();
            imageView.setId(imageId);

            DownloadScaledImage downloadScaledImage = new DownloadScaledImage(itemId, imageView);
            downloadScaledImage.execute();

            GradientDrawable buttonBackground = new GradientDrawable();
            buttonBackground.setCornerRadius(17);
            buttonBackground.setColors(new int[]{Color.parseColor("#5d000e"), Color.parseColor("#430014")});

            buttonSize = screenHeight > screenWight ? screenWight / 10 : screenHeight / 10;
            int buttonMargin = screenHeight > screenWight ? screenWight / 22 : screenHeight / 22;

            buttonTrade = new ImageButton(context);
            Drawable  img = context.getDrawable(R.drawable.trade_button);
            buttonTrade.setImageDrawable(img);
            buttonTrade.setBackground(buttonBackground);
            int buttonTradeId = View.generateViewId();
            buttonTrade.setId(buttonTradeId);
            buttonParams = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
            buttonParams.addRule(RelativeLayout.BELOW, imageId);
            buttonParams.addRule(RelativeLayout.CENTER_HORIZONTAL, imageId);
            buttonParams.setMargins(0, buttonMargin, 0, 0);
            buttonTrade.setLayoutParams(buttonParams);

            buttonSell = new ImageButton(context);
            img = context.getDrawable(R.drawable.sell_button);
            buttonSell.setImageDrawable(img);
            buttonSell.setBackground(buttonBackground);
            int buttonSellId = View.generateViewId();
            buttonSell.setId(buttonSellId);
            buttonParams = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
            buttonParams.addRule(RelativeLayout.BELOW, imageId);
            buttonParams.addRule(RelativeLayout.ALIGN_START, buttonTradeId);
            buttonParams.setMargins(buttonMargin + buttonSize, buttonMargin, 0, 0);
            buttonSell.setLayoutParams(buttonParams);

            buttonHaveIt = new ImageButton(context);

            Cursor cursor = mDb.query(DbHandler.TABLE_ITEMS, new String[]{DbHandler.KEY_ITEM_STATUS}, DbHandler.KEY_ID + " = " + itemId, null, null, null, null);

            if(cursor.moveToFirst())
            {
                if(cursor.getString(cursor.getColumnIndex(DbHandler.KEY_ITEM_STATUS)).equals("in"))
                {
                    inMyCollection = true;
                }
            }
            cursor.close();

            img = context.getDrawable(inMyCollection ? R.drawable.ic_in_my_collection : R.drawable.i_have_it_button);
            buttonHaveIt.setImageDrawable(img);
            buttonHaveIt.setBackground(buttonBackground);
            buttonParams = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
            buttonParams.addRule(RelativeLayout.BELOW, imageId);
            buttonParams.addRule(RelativeLayout.ALIGN_END, buttonTradeId);
            buttonParams.setMargins(0, buttonMargin, buttonMargin + buttonSize, 0);
            buttonHaveIt.setLayoutParams(buttonParams);
            buttonHaveIt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Cursor cursor = mDb.query(DbHandler.TABLE_ITEMS, new String[]{DbHandler.KEY_ITEM_STATUS}, DbHandler.KEY_ID + " = " + itemId, null, null, null, null);
                    if(cursor.moveToFirst())
                    {
                        String newStatus;
                      //  ContentValues contentValues = new ContentValues();
                        if(cursor.getString(cursor.getColumnIndex(DbHandler.KEY_ITEM_STATUS)).equals("in")) {
                            newStatus = "missing";
                            inMyCollection = false;
                        }
                        else {
                            newStatus = "in";
                            inMyCollection = true;
                        }
                       // contentValues.put(DbHandler.KEY_ITEM_STATUS, newStatus);
                       // mDb.update(DbHandler.TABLE_ITEMS, contentValues, DbHandler.KEY_ID + " = " + itemId, null);

                            AsyncSetItemStatus asyncSetItemStatus = new AsyncSetItemStatus(itemId, context);
                            asyncSetItemStatus.execute(newStatus);
                        cursor.close();

                        buttonHaveIt.setImageResource(inMyCollection ? R.drawable.ic_in_my_collection : R.drawable.i_have_it_button);
                    }
                    cursor.close();
                }
            });

            int descMargin = screenHeight > screenWight ? screenWight /11 : screenHeight /11;

            tvDescriptionTitle = new TextView(context);
            tvDescriptionTitle.setText("Описание:");
            RelativeLayout.LayoutParams descTitleParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            descTitleParams.addRule(RelativeLayout.BELOW, imageId);
            descTitleParams.addRule(RelativeLayout.ALIGN_LEFT, imageId);
            descTitleParams.setMargins(0, buttonSize + descMargin, 0, 0);
            tvDescriptionTitle.setLayoutParams(descTitleParams);
            int tvDescTitleId = View.generateViewId();
            tvDescriptionTitle.setId(tvDescTitleId);
            tvDescriptionTitle.setTextColor(Color.parseColor("#ffffff"));
            tvDescriptionTitle.setTypeface(Typeface.DEFAULT_BOLD);
            tvDescriptionTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);

            int descWidth = screenHeight > screenWight ? screenWight - 2*topMargin : screenHeight - 2*topMargin;
            tvDescription = new TextView(context);
            tvDescription.setText(itemDesc);
            RelativeLayout.LayoutParams descParams = new RelativeLayout.LayoutParams(descWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            descParams.addRule(RelativeLayout.BELOW, tvDescTitleId);
            descParams.addRule(RelativeLayout.ALIGN_LEFT, imageId);
            tvDescription.setLayoutParams(descParams);
            int tvDescId = View.generateViewId();
            tvDescription.setId(tvDescId);
            tvDescription.setTextColor(Color.parseColor("#ffffff"));
            tvDescription.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 28);
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("id", itemId);
    }

    private class DownloadScaledImage extends AsyncTask<Void, Void, Bitmap>
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
            return inSampleSize - 3;
        }

        private String SQL;
        private String name, desc;
        private int secId, itemId;
        ImageView imageView;

        public DownloadScaledImage(int itemId, ImageView imageView) {
            this.itemId = itemId;
            this.imageView = imageView;
        }

        private byte[] blob;

        @Override
        protected Bitmap doInBackground(Void... query) {
            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                Statement st = null;
                ResultSet rs = null;
                Cursor cursorItem = mDb.query(DbHandler.TABLE_ITEMS, new String[]{DbHandler.KEY_NAME, DbHandler.KEY_DESCRIPTION, DbHandler.KEY_IMAGE, DbHandler.KEY_MINI_IMAGE}, DbHandler.KEY_ID + " = " + itemId, null, null, null, null);
                try {
                    if (cursorItem.getCount() > 0 && cursorItem.moveToFirst()) {
                        name = cursorItem.getString(cursorItem.getColumnIndex(DbHandler.KEY_NAME));
                        desc = cursorItem.getString(cursorItem.getColumnIndex(DbHandler.KEY_DESCRIPTION));
                        blob = cursorItem.getBlob(cursorItem.getColumnIndex(DbHandler.KEY_IMAGE));
                        if (blob == null) {
                            if(DbHandler.isOnline(context)) {
                                    if(DbHandler.needToReconnect)
                                        connection = DbHandler.setNewConnection(DriverManager.getConnection(DbHandler.MSSQL_DB, DbHandler.MSSQL_LOGIN, DbHandler.MSSQL_PASS));
                                    else
                                        connection = DbHandler.getConnection();


                                if (connection != null) {
                                    SQL = "SELECT " + DbHandler.KEY_IMAGE + " FROM " + DbHandler.TABLE_ITEMS + " WHERE " + DbHandler.KEY_ID + " = " + this.itemId;
                                    st = connection.createStatement();
                                    rs = st.executeQuery(SQL);
                                    if (rs != null && !isCancelled()) {

                                        rs.next();
                                        blob = rs.getBytes(1);

                                        ContentValues contentValues = new ContentValues();
                                        contentValues.put(DbHandler.KEY_IMAGE, blob);

                                        mDb.update(DbHandler.TABLE_ITEMS, contentValues, DbHandler.KEY_ID + " = " + itemId, null);

                                        rs.close();
                                        st.close();
                                    }
                                }
                            } else{
                                name += " (Оригинал не загружен)";
                                blob = cursorItem.getBlob(cursorItem.getColumnIndex(DbHandler.KEY_MINI_IMAGE));
                            }
                        }
                    } else {
                        if(DbHandler.isOnline(context)) {
                            if(DbHandler.isOnline(context)) {
                                if(DbHandler.needToReconnect)
                                    connection = DbHandler.setNewConnection(DriverManager.getConnection(DbHandler.MSSQL_DB, DbHandler.MSSQL_LOGIN, DbHandler.MSSQL_PASS));
                                else
                                    connection = DbHandler.getConnection();
                            }

                            if (connection != null) {
                                SQL = "SELECT * FROM " + DbHandler.TABLE_ITEMS + " WHERE " + DbHandler.KEY_ID + " = " + this.itemId;
                                st = connection.createStatement();
                                rs = st.executeQuery(SQL);
                                if (rs != null && !isCancelled()) {

                                    rs.next();

                                    int id = rs.getInt(1);
                                    name = rs.getString(2);
                                    desc = rs.getString(3);
                                    int secId = rs.getInt(4);
                                    blob = rs.getBytes(5);
                                    byte[] miniBlob = rs.getBytes(6);
                                    String dateOfChange = rs.getString(7);

                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put(DbHandler.KEY_ID, id);
                                    contentValues.put(DbHandler.KEY_NAME, name);
                                    contentValues.put(DbHandler.KEY_DESCRIPTION, desc);
                                    contentValues.put(DbHandler.KEY_SECTION_ID, secId);
                                    contentValues.put(DbHandler.KEY_IMAGE, blob);
                                    contentValues.put(DbHandler.KEY_MINI_IMAGE, miniBlob);
                                    contentValues.put(DbHandler.KEY_DATE_OF_CHANGE, dateOfChange);
                                    contentValues.put(DbHandler.KEY_ITEM_STATUS, "missing");
                                    mDb.insert(DbHandler.TABLE_ITEMS, null, contentValues);

                                    rs.close();
                                    st.close();
                                }
                            }
                        } else{
                            return null;
                        }
                    }
                } catch (java.sql.SQLException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (rs != null) rs.close();
                        if (st != null) st.close();
                    } catch (java.sql.SQLException e) {
                        throw new RuntimeException(e.getMessage());
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
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if(bitmap != null) {
                imageView.setImageBitmap(bitmap);
                RelativeLayout.LayoutParams imageParams;
                int height = bitmap.getHeight();
                int width = bitmap.getWidth();

                int maxWidth = screenWight - 2 * topMargin;
                if (screenHeight > screenWight) {
                    int maxHeightPortrait = screenHeight * 2 / 3;
                    if (height > width)
                        imageParams = new RelativeLayout.LayoutParams(getNewWidth(width, height, maxHeightPortrait), maxHeightPortrait);
                    else
                        imageParams = new RelativeLayout.LayoutParams(maxWidth, getNewHeight(width, height, maxWidth));
                } else {
                    imageParams = new RelativeLayout.LayoutParams(maxWidth, getNewHeight(width, height, maxWidth));
                }

                actionBar.setTitle(name);
                imageParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                imageParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                imageParams.setMargins(topMargin, topMargin, topMargin, 0);
                imageView.setLayoutParams(imageParams);
                layout.addView(imageView);
                layout.addView(buttonTrade);
                layout.addView(buttonSell);
                layout.addView(buttonHaveIt);
                layout.addView(tvDescriptionTitle);
                layout.addView(tvDescription);
            }
        }
    }

    private int getNewWidth(int width, int height, float scaledHeight){
        float k = height/scaledHeight;
        return Math.round(width/k);
    }

    private int getNewHeight(int width, int height, float scaledWidth){
        float k = width/scaledWidth;
        return Math.round(height/k);
    }
}
