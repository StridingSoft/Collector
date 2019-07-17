package com.lobotino.collector.fragments;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
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

import com.lobotino.collector.async_tasks.collections.AsyncCurrentItem;
import com.lobotino.collector.async_tasks.collections.AsyncDrawAllCollections;
import com.lobotino.collector.async_tasks.collections.AsyncDrawAllItems;
import com.lobotino.collector.async_tasks.collections.AsyncDrawAllSections;
import com.lobotino.collector.utils.DbHandler;
import com.lobotino.collector.activities.MainActivity;
import com.lobotino.collector.R;
import com.lobotino.collector.async_tasks.AsyncSetItemStatus;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class CollectionsFragment extends Fragment {

    private DbHandler dbHandler;
    private SQLiteDatabase mDb;
    private String[] galleryPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private View rootView;
    private Context context;

    private RelativeLayout layout;
    private ScrollView scrollView;
    private Button buttonBack;
    private ActionBar actionBar;
    private String pathToImage;


    public static GradientDrawable gradientRedDrawable, gradientGreenDrawable;
    public static int countImages = 0, currentId = 0, currentCollection = 0, currentSection = 0, tempId, lastLeftId, lastRightId,
            pictureSize, screenWidth, externalMargins, topMargin, botMargin, puddingsSize, firstTopMargin, checkImageSize;;
    public static String collectionTitle = "", sectionTitle = "";
    public static List<AsyncCurrentItem> offers;
    public static String fragmentType = DbHandler.MY_COLLECTIONS;  //myCollections or comCollections
    public static String fragmentStatus = "all";


    public int getCurrentSection(){
        return currentSection;
    }

    public String getType()
    {
        return fragmentType;
    }

    public String getStatus()
    {
        return fragmentStatus;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        offers = new ArrayList<AsyncCurrentItem>();
        rootView = inflater.inflate(R.layout.fragment_my_collections, container, false);
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.setCurrentFragment(this);
        context = getActivity().getBaseContext();
        dbHandler = MainActivity.dbHandler;
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        pictureSize = Math.round((float) (screenWidth / 3));
        checkImageSize = pictureSize * 6 / 7;
        layout = (RelativeLayout) rootView.findViewById(R.id.relative_layout_1);
        scrollView = (ScrollView) rootView.findViewById(R.id.scroll_view_id_1);
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        try {
            dbHandler.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }
        mDb = dbHandler.getDataBase();

        externalMargins = screenWidth / 11;
        topMargin = screenWidth / 9;
        botMargin = screenWidth / 16;
        puddingsSize = pictureSize / 30; //15
        firstTopMargin = screenWidth / 16;

        gradientRedDrawable = new GradientDrawable();
        gradientRedDrawable.setCornerRadius(15);
        gradientRedDrawable.setColors(new int[]{Color.parseColor("#5d000e"), Color.parseColor("#430014")});

        gradientGreenDrawable = new GradientDrawable();
        gradientGreenDrawable.setCornerRadius(15);
        gradientGreenDrawable.setColors(new int[]{Color.parseColor("#009910"), Color.parseColor("#00820E")});


        buttonBack = new Button(context);
        int buttonSize = screenWidth / 12;
        RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        int margin = screenWidth / 60;
        buttonParams.setMargins(0, margin, 0, 0);
        buttonBack.setText("");
        buttonBack.setLayoutParams(buttonParams);
        buttonBack.setId(View.generateViewId());
        buttonBack.setBackgroundResource(R.drawable.ic_action_name);

        int id;
        if (savedInstanceState != null) {
            fragmentType = savedInstanceState.getString(DbHandler.COL_TYPE);
            fragmentStatus = savedInstanceState.getString("status");
            id = savedInstanceState.getInt("id");
            sectionTitle = savedInstanceState.getString("sectionTitle");
            collectionTitle = savedInstanceState.getString("collectionTitle");
        } else {
            fragmentType = getArguments().getString(DbHandler.COL_TYPE);
            fragmentStatus = getArguments().getString("status");
            id = getArguments().getInt("id");
            sectionTitle = getArguments().getString("sectionTitle");
            collectionTitle = getArguments().getString("collectionTitle");
        }

        if (!EasyPermissions.hasPermissions(context, galleryPermissions)) {
            EasyPermissions.requestPermissions(this, "Access for storage",
                    101, galleryPermissions);
        }

        switch (fragmentStatus) {
            case "collection": {
                currentCollection = id;
                if (fragmentType.equals(DbHandler.MY_COLLECTIONS))
                    printAllSections();
                else
                    printAllSections();
                break;
            }
            case "section": {
                currentSection = id;
                if (fragmentType.equals(DbHandler.MY_COLLECTIONS))
                    printAllItems();
                else
                    printAllItems();
                break;
            }
            case "item": {
                CurrentItemFragment currentItemFragment = new CurrentItemFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("id", id);
                //bundle.putString("status", savedInstanceState.getString("status"));
                bundle.putString(DbHandler.COL_TYPE, fragmentType);
                currentItemFragment.setArguments(bundle);
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, currentItemFragment).commit();
                break;
            }
            default: {
                if (fragmentType.equals(DbHandler.MY_COLLECTIONS))
                    printAllCollections();
                else {
                    printAllCollections();
                }
            }
        }
        return rootView;
    }

    public static void clearOffers()
    {
        for(int i = 0; i < offers.size(); i++)
        {
            offers.get(i).cancel(true);
        }
        offers.clear();
    }

    public void printAllCollections(){
        new AsyncDrawAllCollections(context, mDb, layout, actionBar, getFragmentManager()).execute();
    }

    public void printAllSections(){
        new AsyncDrawAllSections(currentCollection, collectionTitle, context, mDb, layout, actionBar, getFragmentManager()).execute();
    }

    public void printAllSections(int colId){
        new AsyncDrawAllSections(colId, collectionTitle, context, mDb, layout, actionBar, getFragmentManager()).execute();
    }

    public void printAllItems(){
        new AsyncDrawAllItems(sectionTitle, currentSection,  context, mDb, layout, actionBar, getFragmentManager()).execute();
    }

    public void printAllItems(int secId){
        new AsyncDrawAllItems(sectionTitle, secId, context, mDb, layout, actionBar, getFragmentManager()).execute();
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        clearOffers();
        outState.putString("status", fragmentStatus);
        outState.putString(DbHandler.COL_TYPE, fragmentType);
        outState.putString("sectionTitle", sectionTitle);
        outState.putString("collectionTitle", collectionTitle);
        if (fragmentStatus.equals("collection"))
            outState.putInt("id", currentCollection);
        else if (fragmentStatus.equals("section"))
            outState.putInt("id", currentSection);
    }
}
