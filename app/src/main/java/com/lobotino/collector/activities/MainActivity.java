package com.lobotino.collector.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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

import com.lobotino.collector.fragments.AddCollectionFragment;
import com.lobotino.collector.fragments.AddItemFragment;
import com.lobotino.collector.fragments.AddSectionFragment;
import com.lobotino.collector.fragments.NeedToSignInFragment;
import com.lobotino.collector.fragments.ProfileFragment;
import com.lobotino.collector.fragments.SettingsFragment;
import com.lobotino.collector.utils.DbHandler;
import com.lobotino.collector.R;
import com.lobotino.collector.fragments.CollectionsFragment;
import com.lobotino.collector.fragments.CurrentItemFragment;

import java.sql.SQLException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    public final static String
            ALL = "all",
            COLLECTION = "collection",
            SECTION = "section",
            ITEM = "item";

    public static DbHandler dbHandler = null;

    private Fragment currentFragment;

    public static MenuItem addElemMenu;

    public void setCurrentFragment(Fragment fragment){
        currentFragment = fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHandler = DbHandler.getInstance(this);
        try {
            dbHandler.openDataBase();
        }catch(SQLException e)
        {
            e.printStackTrace();
        }


        setContentView(R.layout.activity_navigation);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_community_colletions);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();


        if(savedInstanceState == null || savedInstanceState.getBoolean("isEmpty"))
        {
            currentFragment = new CollectionsFragment();
            Bundle bundle = new Bundle();
            bundle.putString(DbHandler.COL_TYPE, DbHandler.COM_COLLECTIONS);
            bundle.putString("status", "all");
            currentFragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.content_frame, currentFragment);
            fragmentTransaction.commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isEmpty", currentFragment == null);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (currentFragment != null) {
                if (currentFragment instanceof CollectionsFragment) {
                    CollectionsFragment fragment = (CollectionsFragment) currentFragment;

                    switch (fragment.getStatus()) {
                        case SECTION: {
                            CollectionsFragment.clearOffers();
                            if (fragment.getType().equals(DbHandler.MY_COLLECTIONS))
                                fragment.printAllSections();
                            else
                                fragment.printAllSections();

                            break;
                        }
                        case COLLECTION: {
                            CollectionsFragment.clearOffers();
                            if (fragment.getType().equals(DbHandler.MY_COLLECTIONS))
                                fragment.printAllCollections();
                            else
                                fragment.printAllCollections();
                            break;
                        }
                    }
                }else{
                    if(currentFragment instanceof CurrentItemFragment)
                    {
                        CurrentItemFragment fragment = (CurrentItemFragment) currentFragment;
                        CollectionsFragment collectionsFragment = new CollectionsFragment();
                        Bundle bundle = new Bundle();
                        bundle.putInt("id", fragment.getSectionId());
                        bundle.putString(DbHandler.COL_TYPE, fragment.getCollectionsType());
                        bundle.putString("status", "section");
                        bundle.putString("sectionTitle", fragment.getArguments().getString("sectionTitle"));
                        bundle.putString("collectionTitle", fragment.getArguments().getString("collectionTitle"));

                        collectionsFragment.setArguments(bundle);
                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager
                                .beginTransaction();
                        fragmentTransaction.replace(R.id.content_frame, collectionsFragment);
                        fragmentTransaction.commit();
                    }else{
                        if(currentFragment instanceof AddItemFragment)
                        {
                            CollectionsFragment collectionsFragment = new CollectionsFragment();
                            AddItemFragment fragment = (AddItemFragment) currentFragment;
                            Bundle bundle = new Bundle();
                            bundle.putInt("id", fragment.getSectionId());
                            bundle.putString(DbHandler.COL_TYPE, DbHandler.COM_COLLECTIONS);
                            bundle.putString("status", "section");
                            bundle.putString("sectionTitle", fragment.getArguments().getString("sectionTitle"));
                            bundle.putString("collectionTitle", fragment.getArguments().getString("collectionTitle"));

                            collectionsFragment.setArguments(bundle);
                            FragmentManager fragmentManager = getFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager
                                    .beginTransaction();
                            fragmentTransaction.replace(R.id.content_frame, collectionsFragment);
                            fragmentTransaction.commit();
                        }
                    }
                }
            }
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_element, menu);
        addElemMenu = menu.findItem(R.id.action_add_element);
        if(!DbHandler.isUserLogin()) addElemMenu.setVisible(false);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (id == R.id.action_add_element) {
            if (currentFragment != null) {
                if (currentFragment instanceof CollectionsFragment) {
                    CollectionsFragment fragment = (CollectionsFragment) currentFragment;

                    String status = fragment.getStatus();
                    switch (status) {
                        case ALL:{
                            AddCollectionFragment addCollectionFragment = new AddCollectionFragment();

                            FragmentManager fragmentManager = getFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager
                                    .beginTransaction();
                            fragmentTransaction.replace(R.id.content_frame, addCollectionFragment);
                            fragmentTransaction.commit();
                            break;
                        }
                        case COLLECTION:{
                            AddSectionFragment addSectionFragment = new AddSectionFragment();
                            Bundle bundle = new Bundle();
                            bundle.putInt("collectionId", CollectionsFragment.currentCollection);
                            bundle.putString(DbHandler.COL_TYPE, fragment.getArguments().getString(DbHandler.COL_TYPE));
                            bundle.putString("sectionTitle", fragment.sectionTitle);
                            bundle.putString("collectionTitle", fragment.getArguments().getString("collectionTitle"));

                            addSectionFragment.setArguments(bundle);

                            FragmentManager fragmentManager = getFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager
                                    .beginTransaction();
                            fragmentTransaction.replace(R.id.content_frame, addSectionFragment);
                            fragmentTransaction.commit();
                            break;
                        }
                        case SECTION:{
                            AddItemFragment addElemFragment = new AddItemFragment();
                            Bundle bundle = new Bundle();
                            bundle.putInt("secId", CollectionsFragment.currentSection);
                            bundle.putString(DbHandler.COL_TYPE, fragment.getArguments().getString(DbHandler.COL_TYPE));
                            bundle.putString("sectionTitle", fragment.sectionTitle);
                            bundle.putString("collectionTitle", fragment.getArguments().getString("collectionTitle"));

                            addElemFragment.setArguments(bundle);

                            FragmentManager fragmentManager = getFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager
                                    .beginTransaction();
                            fragmentTransaction.replace(R.id.content_frame, addElemFragment);
                            fragmentTransaction.commit();
                            break;
                        }

                    }
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();

        switch (id)
        {
            case R.id.nav_my_collections :{
                addElemMenu.setVisible(false);
                if(DbHandler.isUserLogin()) {
                    currentFragment = new CollectionsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(DbHandler.COL_TYPE, DbHandler.MY_COLLECTIONS);
                    bundle.putString("status", "all");
                    currentFragment.setArguments(bundle);
                }else{
                    currentFragment = new NeedToSignInFragment();
                }
                fragmentTransaction.replace(R.id.content_frame, currentFragment);
                fragmentTransaction.commit();
                break;
            }
            case R.id.nav_community_colletions : {
                currentFragment = new CollectionsFragment();
                Bundle bundle = new Bundle();
                bundle.putString(DbHandler.COL_TYPE, DbHandler.COM_COLLECTIONS);
                bundle.putString("status", "all");
                currentFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.content_frame, currentFragment);
                fragmentTransaction.commit();
                break;
            }
            case R.id.nav_my_profile:{
                addElemMenu.setVisible(false);
                if(DbHandler.isUserLogin()) {
                    currentFragment = new ProfileFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("user_id", DbHandler.USER_ID);
                    currentFragment.setArguments(bundle);
                }else{
                    currentFragment = new NeedToSignInFragment();
                }
                fragmentTransaction.replace(R.id.content_frame, currentFragment);
                fragmentTransaction.commit();
                break;
            }
            case R.id.nav_settings:{
                addElemMenu.setVisible(false);
                currentFragment = new SettingsFragment();
                fragmentTransaction.replace(R.id.content_frame, currentFragment);
                fragmentTransaction.commit();
                break;
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {

    }
}
