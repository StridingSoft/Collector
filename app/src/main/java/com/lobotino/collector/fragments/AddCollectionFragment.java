package com.lobotino.collector.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lobotino.collector.R;
import com.lobotino.collector.activities.MainActivity;
import com.lobotino.collector.utils.DbHandler;

import java.io.IOException;

/**
 * Created by Олег on 13.07.2019.
 */

public class AddCollectionFragment extends Fragment {


    private View rootView;
    private ActionBar actionBar;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add_collection, container, false);
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.setCurrentFragment(this);

        actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("Добавление коллекции");
        }

        Button buttonAccept = rootView.findViewById(R.id.button_accept);
            buttonAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText etName = (EditText) rootView.findViewById(R.id.etCollectionName);
                    EditText etDesc = (EditText) rootView.findViewById(R.id.etCollectionDesc);
                    String name = etName.getText().toString();
                    String desc = etDesc.getText().toString();

                    TextView tvStatus = (TextView) rootView.findViewById(R.id.tvAddElemStatus);

                    if(name.equals("Название коллекции") || name.length() == 0)
                    {
                        tvStatus.setText("Введите название коллекции!");
                        etName.setText("");
                        return;
                    }
                    if(name.length() < 6)
                    {
                        tvStatus.setText("В названии должно быть больше 6 символов!");
                        return;
                    }
                    if(name.length() > 20)
                    {
                        tvStatus.setText("В названии должно быть меньше 20 символов!");
                        return;
                    }

                    if(desc.equals("Описание коллекции") || desc.length() == 0)
                    {
                        tvStatus.setText("Введите описание коллекции!");
                        etDesc.setText("");
                        return;
                    }
                    if(desc.length() < 10)
                    {
                        tvStatus.setText("В описании должно быть больше 10 символов!");
                        return;
                    }
                    if(desc.length() > 250)
                    {
                        tvStatus.setText("В названии должно быть меньше 250 символов!");
                        return;
                    }


                    AddSectionFragment addSectionFragment = new AddSectionFragment();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isFirstSection", true);
                    bundle.putString("collectionName", name);
                    bundle.putString("collectionDesc", desc);
                    addSectionFragment.setArguments(bundle);
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager
                            .beginTransaction();
                    fragmentTransaction.replace(R.id.content_frame, addSectionFragment);
                    fragmentTransaction.commit();
                }
            });
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //TO DO back сохранения
    }
}
