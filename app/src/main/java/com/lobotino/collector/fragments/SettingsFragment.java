package com.lobotino.collector.fragments;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.lobotino.collector.R;
import com.lobotino.collector.activities.MainActivity;
import com.lobotino.collector.activities.RegistrationActivity;
import com.lobotino.collector.utils.DbHandler;

import static com.lobotino.collector.activities.MainActivity.dbHandler;

/**
 * Created by Олег on 19.07.2019.
 */

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.setCurrentFragment(this);

        ActionBar actionBar = mainActivity.getSupportActionBar();
        if(actionBar != null)
        {
            actionBar.setTitle("Настройки");
        }

        Button buttonChangeUser = rootView.findViewById(R.id.button_change_user);
        buttonChangeUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DbHandler.isOnline(rootView.getContext())) {
                    dbHandler.changeUser(rootView.getContext());
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
                    builder.setTitle("Проверьте подключение с интернетом.")
                            .setCancelable(false)
                            .setNegativeButton("ОК",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });
        return rootView;
    }
}
