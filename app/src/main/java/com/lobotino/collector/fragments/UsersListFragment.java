package com.lobotino.collector.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lobotino.collector.R;
import com.lobotino.collector.activities.MainActivity;
import com.lobotino.collector.async_tasks.AsyncDrawAllUsers;

/**
 * Created by Олег on 14.07.2019.
 */

public class UsersListFragment extends Fragment {

    protected ActionBar actionBar;

    public AsyncDrawAllUsers currentAsync;

    public enum Users {WISHED_PEOPLE, TRADED_PEOPLE}
    private String usersTypeString = "", itemName = "";
    private int itemId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_users_list, container, false);
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.setCurrentFragment(this);
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();


        if(savedInstanceState != null)
        {
            usersTypeString = savedInstanceState.getString("usersTypeString");
            itemName = savedInstanceState.getString("itemName");
            itemId = savedInstanceState.getInt("itemId");
        }else{
            usersTypeString = getArguments().getString("usersTypeString");
            itemName = getArguments().getString("itemName");
            itemId = getArguments().getInt("itemId");
        }

        Users usersType;
        if (usersTypeString.equals(Users.WISHED_PEOPLE.toString())){
            usersType =  Users.WISHED_PEOPLE;
            actionBar.setTitle(("Желают " + actionBar.getTitle()));
        }
        else {
            usersType = Users.TRADED_PEOPLE;
            actionBar.setTitle(("Обменяют " + actionBar.getTitle()));
        }

        Context context = getActivity().getBaseContext();
        LinearLayout layout = rootView.findViewById(R.id.linear_layout_user_list);

        float dip = 3;
        Resources r = getResources();
        int lineHeight = (int)TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );



//        if(usersType.equals(Users.WISHED_PEOPLE))
//            textViewDesc.setText("Коллекционеры, желающие получить '" + itemName + "'");
//        else
//            textViewDesc.setText("Коллекционеры, желающие обменять '" + itemName + "'");
//        textViewDesc.setTextColor(Color.parseColor("#ffffff"));



        currentAsync = new AsyncDrawAllUsers(context, layout, getFragmentManager(), usersType, itemId, lineHeight);
        currentAsync.execute();

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("usersTypeString", usersTypeString);
        outState.putString("itemName", itemName);
        outState.putInt("itemId", itemId);
    }
}
