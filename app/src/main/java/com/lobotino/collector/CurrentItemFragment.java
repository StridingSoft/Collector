package com.lobotino.collector;

import android.app.Fragment;
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

import java.io.IOException;
import java.io.InputStream;


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


        Cursor cursorItems = mDb.query(DbHandler.TABLE_ITEMS, null, DbHandler.KEY_ITEM_ID + " = " + itemId, null, null, null, null);
        if(cursorItems.moveToFirst())
        {
            int pathToImageIndex = cursorItems.getColumnIndex(DbHandler.KEY_ITEM_IMAGE_PATH);
            int itemNameIndex = cursorItems.getColumnIndex(DbHandler.KEY_ITEM_NAME);
            int itemDescIndex = cursorItems.getColumnIndex(DbHandler.KEY_ITEM_DESCRIPTION);
            String imagePath = cursorItems.getString(pathToImageIndex);
            String itemName = cursorItems.getString(itemNameIndex);
            String itemDesc = cursorItems.getString(itemDescIndex);

            actionBar.setTitle(itemName);




            ImageView imageView = new ImageView(context);

            imageView.setBackground(gradientDrawable);

            //imageView.setPadding(puddingsSize, puddingsSize, puddingsSize, puddingsSize);
            int imageId = View.generateViewId();
            imageView.setId(imageId);
            Object offer[] = {imagePath, imageView, context, pictureSize};
            DownloadScaledImage downloadScaledImage = new DownloadScaledImage();
            downloadScaledImage.execute(offer);




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
            img = context.getDrawable(R.drawable.i_have_it_button);
            buttonHaveIt.setImageDrawable(img);
            buttonHaveIt.setBackground(buttonBackground);
            buttonParams = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
            buttonParams.addRule(RelativeLayout.BELOW, imageId);
            buttonParams.addRule(RelativeLayout.ALIGN_END, buttonTradeId);
            buttonParams.setMargins(0, buttonMargin, buttonMargin + buttonSize, 0);
            buttonHaveIt.setLayoutParams(buttonParams);


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
            tvDescription.setTypeface(Typeface.DEFAULT_BOLD);
            tvDescription.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("id", itemId);
    }

    private class DownloadScaledImage extends AsyncTask<Object, Void, Bitmap>
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
            RelativeLayout.LayoutParams imageParams;
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();


            int maxWidth = screenWight - 2*topMargin;
            if(screenHeight > screenWight) {
                int maxHeightPortrait = screenHeight * 2 / 3;
                    if (height > width)
                        imageParams = new RelativeLayout.LayoutParams(getNewWidth(width, height, maxHeightPortrait), maxHeightPortrait);
                    else
                        imageParams = new RelativeLayout.LayoutParams(maxWidth, getNewHeight(width, height, maxWidth));
            } else {
                imageParams = new RelativeLayout.LayoutParams(maxWidth, getNewHeight(width, height, maxWidth));
            }


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

    private int getNewWidth(int width, int height, float scaledHeight){
        float k = height/scaledHeight;
        return Math.round(width/k);
    }

    private int getNewHeight(int width, int height, float scaledWidth){
        float k = width/scaledWidth;
        return Math.round(height/k);
    }
}
