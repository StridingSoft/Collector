package com.lobotino.collector.fragments;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lobotino.collector.R;
import com.lobotino.collector.activities.MainActivity;
import com.lobotino.collector.async_tasks.AsyncDrawAllUsers;

/**
 * Created by Олег on 14.07.2019.
 */

public class UsersListFragment extends Fragment {

    private View rootView;

    protected ActionBar actionBar;

    public AsyncDrawAllUsers currentAsync;

    public enum Users {WISHED_PEOPLE, TRADED_PEOPLE}
    private String usersTypeString = "", itemName = "";
    private int itemId;

    private Users usersType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_users_list, container, false);
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.setCurrentFragment(this);



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
        usersType = usersTypeString.equals(Users.WISHED_PEOPLE.toString()) ? Users.WISHED_PEOPLE : Users.TRADED_PEOPLE;

        Context context = getActivity().getBaseContext();
        RelativeLayout layout = rootView.findViewById(R.id.rlUserList);
        TextView textViewDesc = rootView.findViewById(R.id.tvUserListDesc);
        if(usersType.equals(Users.WISHED_PEOPLE))
            textViewDesc.setText("Коллекционеры, желающие получить '" + itemName + "'");
        else
            textViewDesc.setText("Коллекционеры, желающие обменять '" + itemName + "'");
        textViewDesc.setTextColor(Color.parseColor("#ffffff"));

        currentAsync = new AsyncDrawAllUsers(context, layout, usersType, itemId, textViewDesc.getId());
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
