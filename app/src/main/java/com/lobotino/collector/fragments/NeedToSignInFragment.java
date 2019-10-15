package com.lobotino.collector.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.lobotino.collector.R;
import com.lobotino.collector.activities.LoginActivity;
import com.lobotino.collector.activities.MainActivity;
import com.lobotino.collector.activities.RegistrationActivity;



public class NeedToSignInFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.content_need_to_sign_in, container, false);
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.setCurrentFragment(this);


        ActionBar actionBar = mainActivity.getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle("Необходим вход");


        Button buttonReg = rootView.findViewById(R.id.button_registration);
        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(rootView.getContext(), RegistrationActivity.class);
                startActivity(intent);
            }
        });

        Button buttonLogin = rootView.findViewById(R.id.button_sign_in);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(rootView.getContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        return rootView;
    }
}
