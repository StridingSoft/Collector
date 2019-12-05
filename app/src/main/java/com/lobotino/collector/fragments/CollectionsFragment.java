package com.lobotino.collector.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.lobotino.collector.async_tasks.collections.AsyncCurrentItem;
import com.lobotino.collector.async_tasks.collections.AsyncDrawAllCollections;
import com.lobotino.collector.async_tasks.collections.AsyncDrawAllItems;
import com.lobotino.collector.async_tasks.collections.AsyncDrawAllSections;
import com.lobotino.collector.utils.DbHandler;
import com.lobotino.collector.activities.MainActivity;
import com.lobotino.collector.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class CollectionsFragment extends Fragment {

    private DbHandler dbHandler;
    private SQLiteDatabase mDb;
    private String[] galleryPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private View rootView;
    private Context context;

    private RelativeLayout relativeLayout;
    private ScrollView scrollView;
    private Button buttonBack;
    private ActionBar actionBar;
    private String pathToImage;
    private int currentUserId = -1;


    public static GradientDrawable gradientBackground;
    public static float dp = 0;
    public static int countImages = 0, currentId = 0, currentCollection = 0, currentSection = 0, tempId, lastLeftId, lastRightId,
            maxImageSize, screenWidth, externalMargins, topMargin, botMargin, puddingsSize, firstTopMargin, checkImageSize;;
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

    @SuppressLint("RestrictedApi")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        offers = new ArrayList<AsyncCurrentItem>();
        rootView = inflater.inflate(R.layout.fragment_collections, container, false);
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.setCurrentFragment(this);
        context = getActivity().getBaseContext();
        dbHandler = MainActivity.dbHandler;
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        dp = mainActivity.getResources().getDisplayMetrics().density;

//        maxImageSize = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 214, getResources().getDisplayMetrics()));
        externalMargins = (int)(8 * dp);
        puddingsSize = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        topMargin = (int)(8*dp);
        botMargin = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        firstTopMargin = (int) (8 *dp);


        maxImageSize = screenWidth/2 - externalMargins - externalMargins/2;
        checkImageSize = maxImageSize * 6 / 7;
        relativeLayout = (RelativeLayout) rootView.findViewById(R.id.realtive_layout_1);
        scrollView = (ScrollView) rootView.findViewById(R.id.scroll_view_id_1);
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        try {
            dbHandler.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }
        mDb = dbHandler.getDataBase();

//        externalMargins = screenWidth / 11;
//        topMargin = screenWidth / 9;
//        botMargin = screenWidth / 16;
//        puddingsSize = maxImageSize / 30; //15
//        firstTopMargin = screenWidth / 16;

        gradientBackground = new GradientDrawable();
        gradientBackground.setCornerRadius(4);
        gradientBackground.setColor(getResources().getColor(R.color.colorPrimary));

        int id;
        if (savedInstanceState != null) {
            fragmentType = savedInstanceState.getString(DbHandler.COL_TYPE);
            fragmentStatus = savedInstanceState.getString("status");
            id = savedInstanceState.getInt("id");
            sectionTitle = savedInstanceState.getString("sectionTitle");
            collectionTitle = savedInstanceState.getString("collectionTitle");
            currentUserId = savedInstanceState.getInt("currentUserId");
        } else {
            fragmentType = getArguments().getString(DbHandler.COL_TYPE);
            fragmentStatus = getArguments().getString("status");
            id = getArguments().getInt("id");
            sectionTitle = getArguments().getString("sectionTitle");
            collectionTitle = getArguments().getString("collectionTitle");
            currentUserId = getArguments().getInt("currentUserId");
        }

        if(MainActivity.fab != null && DbHandler.isUserLogin())
            MainActivity.fab.setVisibility(fragmentType.equals(DbHandler.COM_COLLECTIONS) ? View.VISIBLE : View.INVISIBLE);

        if (!EasyPermissions.hasPermissions(context, galleryPermissions)) {
               EasyPermissions.requestPermissions(getActivity(), "Access for storage",
                    101, galleryPermissions);
        }

        switch (fragmentStatus) {
            case "collection": {
                FloatingActionButton fab = getActivity().findViewById(R.id.fab);
                if(fab != null) relativeLayout.removeView(fab);
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
                if (fragmentType.equals(DbHandler.MY_COLLECTIONS) || fragmentType.equals(DbHandler.COM_COLLECTIONS))
                    printAllCollections();
                else {
                    printAllCollections(currentUserId);
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
        new AsyncDrawAllCollections(relativeLayout, context, mDb, actionBar, getFragmentManager()).execute();
    }

    public void printAllCollections(int userId){
        new AsyncDrawAllCollections(relativeLayout, context, mDb, actionBar, getFragmentManager(), userId).execute();
    }

    public void printAllSections(){
        new AsyncDrawAllSections(relativeLayout, currentCollection, collectionTitle, context, mDb, actionBar, getFragmentManager()).execute();
    }

    public void printAllSections(int userId){
        new AsyncDrawAllSections(relativeLayout, currentCollection, collectionTitle, context, mDb, actionBar, getFragmentManager(), userId).execute();
    }

    public void printAllItems(){
        new AsyncDrawAllItems(relativeLayout, sectionTitle, currentSection,  context, mDb, actionBar, getFragmentManager()).execute();
    }

    public void printAllItems(int userId){
        new AsyncDrawAllItems(relativeLayout, sectionTitle, currentSection,  context, mDb, actionBar, getFragmentManager(), userId).execute();
    }




    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        clearOffers();
        outState.putString("status", fragmentStatus);
        outState.putString(DbHandler.COL_TYPE, fragmentType);
        outState.putString("sectionTitle", sectionTitle);
        outState.putString("collectionTitle", collectionTitle);
        if(currentUserId != -1)
            outState.putInt("currentUserId", currentUserId);

        if (fragmentStatus.equals("collection"))
            outState.putInt("id", currentCollection);
        else if (fragmentStatus.equals("section"))
            outState.putInt("id", currentSection);
    }
}
