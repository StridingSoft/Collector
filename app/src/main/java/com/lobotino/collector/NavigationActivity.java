package com.lobotino.collector;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.sql.SQLException;

import pub.devrel.easypermissions.EasyPermissions;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    public static DbHandler dbHandler = null;

    private Fragment currentFragment;

    public void setCurrentFragment(Fragment fragment){
        currentFragment = fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHandler = new DbHandler(this);
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

        navigationView.setCheckedItem(R.id.nav_my_collections);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();



        if(savedInstanceState == null || savedInstanceState.getBoolean("isEmpty"))
        {
            currentFragment = new CollectionsFragment();
            Bundle bundle = new Bundle();
            bundle.putString("type", "comCollections");
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
                    fragment.clearOffers();
                    if(fragment.getStatus() != "all") {
                        switch (fragment.getStatus()) {
                            case "section": {
                                if (fragment.getType().equals("myCollections"))
                                    fragment.printAllSections();
                                else
                                    fragment.printAllSections();
                                break;
                            }
                            case "collection":{
                                if (fragment.getType().equals("myCollections"))
                                    fragment.printAllCollections();
                                else
                                    fragment.printAllCollections();
                                break;
                            }
                            case "all": {
                                if (fragment.getType().equals("myCollections"))
                                    fragment.printAllCollections();
                                else
                                    fragment.printAllCollections();
                                break;
                            }
                        }
                    }
                }else{
                    if(currentFragment instanceof CurrentItemFragment)
                    {
                        CurrentItemFragment fragment = (CurrentItemFragment) currentFragment;
                        CollectionsFragment collectionsFragment = new CollectionsFragment();
                        Bundle bundle = new Bundle();
                        bundle.putInt("id", fragment.getSectionId());
                        bundle.putString("type", fragment.getType());
                        bundle.putString("status", "section");
                        bundle.putString("title", fragment.getArguments().getString("title"));

                        collectionsFragment.setArguments(bundle);
                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager
                                .beginTransaction();
                        fragmentTransaction.replace(R.id.content_frame, collectionsFragment);
                        fragmentTransaction.commit();
                    }
                }
                } else {
                super.onBackPressed();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_settings, menu);
        getMenuInflater().inflate(R.menu.add_element, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {

            dbHandler.clearCash();
            AlertDialog.Builder builder = new AlertDialog.Builder(NavigationActivity.this);
            builder.setTitle("Кэш очищен!")
                    .setCancelable(false)
                    .setNegativeButton("ОК",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
            return true;
        }
        if(id == R.id.action_log_items)
        {
            dbHandler.logAllItems();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id)
        {
            case R.id.nav_my_collections :{
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager
                        .beginTransaction();

                currentFragment = new CollectionsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("type", "myCollections");
                bundle.putString("status", "all");
                currentFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.content_frame, currentFragment);
                fragmentTransaction.commit();
                break;
            }
            case R.id.nav_community_colletions : {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager
                        .beginTransaction();

                currentFragment = new CollectionsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("type", "comCollections");
                bundle.putString("status", "all");
                currentFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.content_frame, currentFragment);
                fragmentTransaction.commit();
                break;
            }
            case R.id.nav_registration :{
                currentFragment = new RegisterFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager
                        .beginTransaction();
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
