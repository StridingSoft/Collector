package com.lobotino.collector;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;


public class CommunityCollectionsFragment extends Fragment {

    private DbHandler dbHandler;
    private SQLiteDatabase mDb;
    private String[] galleryPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private View rootView;
    private Context context;
    private int pictureSize, screenWidth, externalMargins, topMargin, botMargin, puddingsSize, firstTopMargin, lastLeftId, lastRightId, tempId;
    private GradientDrawable gradientDrawable;
    private RelativeLayout layout;
    private ScrollView scrollView;
    private Button buttonBack;
    private ActionBar actionBar;
    private String pathToImage;

    public String fragmentStatus = "collections";

    public int currentCollection = 0;
    public int currentSection = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_my_collections, container, false);
        context = getActivity().getBaseContext();
        dbHandler = NavigationActivity.dbHandler;
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        pictureSize = Math.round((float) (screenWidth / 3));
        layout = (RelativeLayout) rootView.findViewById(R.id.relative_layout_1);
        scrollView = (ScrollView) rootView.findViewById(R.id.scroll_view_id_1);
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

        externalMargins = screenWidth / 11;
        topMargin = screenWidth / 9;
        botMargin = screenWidth / 16;
        puddingsSize = pictureSize / 30; //15
        firstTopMargin = screenWidth / 16;

        gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(15);
        //gradientDrawable.setColor(Color.parseColor("#180c28"));
        gradientDrawable.setColors(new int[]{Color.parseColor("#5d000e"), Color.parseColor("#430014")});


        buttonBack = new Button(context);
        int buttonSize = screenWidth/12;
        RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        int margin =screenWidth/60;
        buttonParams.setMargins(0, margin, 0, 0);
        buttonBack.setText("");
        buttonBack.setLayoutParams(buttonParams);
        buttonBack.setId(View.generateViewId());
        buttonBack.setBackgroundResource(R.drawable.ic_action_name);

        drawAllCollections();

        return rootView;
    }

    private TextView getTextViewBySide(String text, int countImages)
    {
        RelativeLayout.LayoutParams textViewParams = new RelativeLayout.LayoutParams(pictureSize, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView textView = new TextView(context);
        textView.setTextColor(Color.parseColor("#ffffff"));
        textView.setText(text);
        textView.setGravity(Gravity.CENTER);

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
        textView.setLayoutParams(textViewParams);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        return textView;
    }

    private RelativeLayout.LayoutParams getImageParamsBySide(int countImages)
    {
        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(pictureSize, pictureSize);
        if (countImages % 2 == 0) {
            if (lastLeftId == -1) {
                imageParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                imageParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                imageParams.setMargins(externalMargins, firstTopMargin, 0, 0);
            } else {
                imageParams.addRule(RelativeLayout.BELOW, lastLeftId);
                imageParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                imageParams.setMargins(externalMargins, topMargin, 0, 0);
            }
        } else {
            if (lastRightId == -1) {
                imageParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                imageParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                imageParams.setMargins(0, firstTopMargin, externalMargins, 0);
            } else {
                imageParams.addRule(RelativeLayout.BELOW, lastRightId);
                imageParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                imageParams.setMargins(0, topMargin, externalMargins, 0);
            }
        }
        return imageParams;
    }


    public void drawAllItems(final int sectionId)
    {
        currentSection = sectionId;
        fragmentStatus = "items";
        layout.removeAllViews();
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawAllSections(currentCollection);
            }
        });
        layout.addView(buttonBack);
        scrollView.scrollTo(0, 0);

        Cursor cursorCurrentSection = mDb.query(DbHandler.TABLE_SECTIONS, new String[]{DbHandler.KEY_SECTION_NAME}, DbHandler.KEY_SECTION_ID + " = " + sectionId, null, null, null, null);
        if(cursorCurrentSection.moveToFirst())
            actionBar.setTitle(cursorCurrentSection.getString(cursorCurrentSection.getColumnIndex(DbHandler.KEY_SECTION_NAME)));
        cursorCurrentSection.close();

        String columns[] = new String[]{DbHandler.KEY_ITEM_ID, DbHandler.KEY_ITEM_SECTION_ID, DbHandler.KEY_ITEM_NAME, DbHandler.KEY_ITEM_IMAGE_PATH};
        String selection =  DbHandler.KEY_ITEM_SECTION_ID + " = " + sectionId;

        Cursor cursorItemInSection = mDb.query(DbHandler.TABLE_ITEMS, columns, selection, null, null, null, null);

        int pathIndex = cursorItemInSection.getColumnIndex(DbHandler.KEY_ITEM_IMAGE_PATH);
        int nameIndex = cursorItemInSection.getColumnIndex(DbHandler.KEY_ITEM_NAME);
        int itemIdIndex = cursorItemInSection.getColumnIndex(DbHandler.KEY_ITEM_ID);

        lastLeftId = -1;
        lastRightId = -1;
        int countImages = 0;
        int currentId = 0;
        if (EasyPermissions.hasPermissions(context, galleryPermissions)) {
            if (cursorItemInSection.moveToFirst()) {
                do {
                    int currentItemId = cursorItemInSection.getInt(itemIdIndex);
                        pathToImage = cursorItemInSection.getString(pathIndex);

                        ImageView currentImageView = new ImageView(context);
                        currentImageView.setBackground(gradientDrawable);
                        currentImageView.setLayoutParams(getImageParamsBySide(countImages));
                        currentImageView.setPadding(puddingsSize, puddingsSize, puddingsSize, puddingsSize);
                        tempId = View.generateViewId();
                        currentImageView.setId(tempId);

                        Object offer[] = {pathToImage, currentImageView, context, pictureSize};
                        DownloadScaledImage downloadScaledImage = new DownloadScaledImage();
                        downloadScaledImage.execute(offer);

                        layout.addView(currentImageView, currentId++);
                        layout.addView(getTextViewBySide(cursorItemInSection.getString(nameIndex), countImages), currentId++);
                        countImages++;
                } while (cursorItemInSection.moveToNext());
                cursorItemInSection.close();
            }
        } else {
            EasyPermissions.requestPermissions(this, "Access for storage",
                    101, galleryPermissions);
        }
    }

    public void drawAllSections(int collectionId)
    {
        currentCollection = collectionId;
        fragmentStatus = "sections";
        layout.removeAllViews();
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawAllCollections();
            }
        });
        layout.addView(buttonBack);
        scrollView.scrollTo(0, 0);

        Cursor cursorCurrentCollection = mDb.query(DbHandler.TABLE_COLLECTIONS, new String[]{DbHandler.KEY_COLLECTION_NAME}, DbHandler.KEY_COLLECTION_ID + " = " + collectionId, null, null, null, null);
        if(cursorCurrentCollection.moveToFirst())
            actionBar.setTitle(cursorCurrentCollection.getString(cursorCurrentCollection.getColumnIndex(DbHandler.KEY_COLLECTION_NAME)));
        cursorCurrentCollection.close();


        String selection =  DbHandler.KEY_SECTION_COLLECTION_ID + " = " + collectionId;
        String columns[] = new String[]{DbHandler.KEY_SECTION_ID, DbHandler.KEY_SECTION_NAME, DbHandler.KEY_SECTION_COLLECTION_ID};
        Cursor cursorUserSections = mDb.query(DbHandler.TABLE_SECTIONS, columns, selection, null, null, null, null);

        List<Integer> listSectionsIds = new ArrayList<Integer>();
        int nameIndex = cursorUserSections.getColumnIndex(DbHandler.KEY_SECTION_NAME);
        int idSectionIndex = cursorUserSections.getColumnIndex(DbHandler.KEY_SECTION_ID);
        lastLeftId = -1;
        lastRightId = -1;
        int countImages = 0;
        int currentId = 0;
        if (EasyPermissions.hasPermissions(context, galleryPermissions)) {
            if (cursorUserSections.moveToFirst()) {
                do {
                    final int currentSectionId = cursorUserSections.getInt(idSectionIndex);
                    if(!listSectionsIds.contains(currentSectionId)) {
                        pathToImage = "";
                        Cursor cursorUserItems = mDb.query(DbHandler.TABLE_ITEMS, new String[]{DbHandler.KEY_ITEM_IMAGE_PATH}, DbHandler.KEY_ITEM_SECTION_ID + " = " + currentSectionId, null, null, null, null);
                        if(cursorUserItems.moveToFirst()) {
                            pathToImage = cursorUserItems.getString(cursorUserItems.getColumnIndex(DbHandler.KEY_ITEM_IMAGE_PATH));
                        }
                        cursorUserItems.close();

                        ImageView currentImageView = new ImageView(context);
                        currentImageView.setBackground(gradientDrawable);
                        currentImageView.setLayoutParams(getImageParamsBySide(countImages));
                        currentImageView.setPadding(puddingsSize, puddingsSize, puddingsSize, puddingsSize);
                        currentImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                drawAllItems(currentSectionId);
                            }
                        });

                        tempId = View.generateViewId();
                        currentImageView.setId(tempId);

                        Object offer[] = {pathToImage, currentImageView, context, pictureSize};
                        DownloadScaledImage downloadScaledImage = new DownloadScaledImage();
                        downloadScaledImage.execute(offer);

                        layout.addView(currentImageView, currentId++);
                        layout.addView(getTextViewBySide(cursorUserSections.getString(nameIndex), countImages), currentId++);
                        countImages++;
                    }
                } while (cursorUserSections.moveToNext());
                cursorUserSections.close();
            }
        } else {
            EasyPermissions.requestPermissions(this, "Access for storage",
                    101, galleryPermissions);
        }
    }

    public void drawAllCollections()
    {
        fragmentStatus = "collections";
        layout.removeAllViews();
        scrollView.scrollTo(0, 0);
        actionBar.setTitle("Коллекции сообщества");


        Cursor cursorCollections = mDb.query(DbHandler.TABLE_COLLECTIONS, new String[]{DbHandler.KEY_COLLECTION_NAME, DbHandler.KEY_COLLECTION_ID}, null, null, null, null, null, null);

        lastLeftId = -1;
        lastRightId = -1;
        int countImages = 0;
        int currentId = 0;
        int idCollectionsIndex = cursorCollections.getColumnIndex(DbHandler.KEY_COLLECTION_ID);
        if (EasyPermissions.hasPermissions(context, galleryPermissions)) {
            if (cursorCollections.moveToFirst()) {
                do {
                    final int currentCollId = cursorCollections.getInt(idCollectionsIndex);

                        pathToImage = "";
                        Cursor tempSectionsCursor = mDb.query(DbHandler.TABLE_SECTIONS, null, DbHandler.KEY_SECTION_COLLECTION_ID + " = " + currentCollId, null, null, null, null);
                        if(tempSectionsCursor.moveToFirst()) {
                            int tempSectionId = tempSectionsCursor.getInt(tempSectionsCursor.getColumnIndex(DbHandler.KEY_SECTION_ID));
                            Cursor tempItemsCursor = mDb.query(DbHandler.TABLE_ITEMS, null, DbHandler.KEY_ITEM_SECTION_ID + " = " + tempSectionId, null, null, null, null);
                            if (tempItemsCursor.moveToFirst()) {
                                pathToImage = tempItemsCursor.getString(tempItemsCursor.getColumnIndex(DbHandler.KEY_ITEM_IMAGE_PATH));
                            }
                            tempItemsCursor.close();
                        }
                        tempSectionsCursor.close();

                        ImageView currentImageView = new ImageView(context);
                        currentImageView.setBackground(gradientDrawable);
                        currentImageView.setLayoutParams(getImageParamsBySide(countImages));
                        currentImageView.setPadding(puddingsSize, puddingsSize, puddingsSize, puddingsSize);
                        currentImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                drawAllSections(currentCollId);
                            }
                        });

                        tempId = View.generateViewId();
                        currentImageView.setId(tempId);

                        Object offer[] = {pathToImage, currentImageView, context, pictureSize};
                        DownloadScaledImage downloadScaledImage = new DownloadScaledImage();
                        downloadScaledImage.execute(offer);

                        layout.addView(currentImageView, currentId++);
                        layout.addView(getTextViewBySide(cursorCollections.getString(cursorCollections.getColumnIndex(DbHandler.KEY_COLLECTION_NAME)), countImages), currentId++);
                        countImages++;
                } while (cursorCollections.moveToNext());
                cursorCollections.close();
            }
        } else {
            EasyPermissions.requestPermissions(this, "Access for storage",
                    101, galleryPermissions);
        }
    }
}
