package com.lobotino.collector;

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

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    public static DbHandler dbHandler;

    private Fragment currentFragment;

    public void setCurrentFragment(Fragment fragment){
        currentFragment = fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHandler = new DbHandler(this);
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
            bundle.putString("type", "myCollections");
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
                    if(fragment.getStatus() != "all") {
                        switch (fragment.getStatus()) {
                            case "section": {
                                if (fragment.getType().equals("myCollections"))
                                    fragment.drawAllUserSections(fragment.getCurrentCollection());
                                else
                                    fragment.drawAllSections(fragment.getCurrentCollection());
                                break;
                            }
                            case "collection":
                            case "all": {
                                if (fragment.getType().equals("myCollections"))
                                    fragment.drawAllUserCollections();
                                else
                                    fragment.drawAllCollections();
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_my_collections) {
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
        } else{
            if(id == R.id.nav_community_colletions)
            {
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
