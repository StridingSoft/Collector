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
import com.lobotino.collector.async_tasks.AsyncGetBitmapsFromUri;
import com.lobotino.collector.async_tasks.add_elements.AsyncAddItemToServer;
import com.lobotino.collector.async_tasks.add_elements.AsyncAddSectionToServer;
import com.lobotino.collector.utils.DbHandler;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

/**
 * Created by Олег on 13.07.2019.
 */

public class AddSectionFragment extends Fragment {

    private int collectionId;
    private String collectionName, collectionDesc;

    private DbHandler dbHandler;
    private SQLiteDatabase mDb;
    private View rootView;
    private Context context;
    private int screenWight, screenHeight;
    private GradientDrawable gradientDrawable;
    private ActionBar actionBar;
    private Fragment currentFragment = this;

    private boolean isFirstSection;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add_section, container, false);
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.setCurrentFragment(this);

        context = getActivity().getBaseContext();
        dbHandler = MainActivity.dbHandler;
        screenWight = context.getResources().getDisplayMetrics().widthPixels;
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        RelativeLayout layout = (RelativeLayout) rootView.findViewById(R.id.relative_layout_2);


        isFirstSection = false;
        if(savedInstanceState != null)
        {
            isFirstSection = savedInstanceState.getBoolean("isFirstSection");
            collectionName = savedInstanceState.getString("collectionName");
            collectionDesc = savedInstanceState.getString("collectionDesc");
            collectionId = savedInstanceState.getInt("collectionId");
        }else{
            isFirstSection = getArguments().getBoolean("isFirstSection");
            collectionName = getArguments().getString("collectionName");
            collectionDesc = getArguments().getString("collectionDesc");
            collectionId = getArguments().getInt("collectionId");
        }

        actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Добавление " + (isFirstSection ? "первой " : "") + "секции");
        }

        try {
            dbHandler.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }
        try {
            mDb = dbHandler.getWritableDatabase();
        } catch (SQLException mSQLException) {
            throw mSQLException;
        }


        Button buttonAccept = rootView.findViewById(R.id.button_accept);
            buttonAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText etName = (EditText) rootView.findViewById(R.id.etSectionName);
                    EditText etDesc = (EditText) rootView.findViewById(R.id.etSectionDesc);
                    String name = etName.getText().toString();
                    String desc = etDesc.getText().toString();

                    TextView tvStatus = (TextView) rootView.findViewById(R.id.tvAddElemStatus);

                    if(name.equals("Название секции") || name.length() == 0)
                    {
                        tvStatus.setText("Введите название секции!");
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

                    if(desc.equals("Описание секции") || desc.length() == 0)
                    {
                        tvStatus.setText("Введите описание секции!");
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


                    AddItemFragment addItemFragment = new AddItemFragment();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isFirstItem", true);
                    bundle.putString("sectionName", name);
                    bundle.putString("sectionDesc", desc);
                    bundle.putInt("collectionId", collectionId);
                    if(collectionName != null && !collectionName.isEmpty()) {
                        bundle.putString("collectionName", collectionName);
                        bundle.putString("collectionDesc", collectionDesc);
                    }
                    addItemFragment.setArguments(bundle);
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager
                            .beginTransaction();
                    fragmentTransaction.replace(R.id.content_frame, addItemFragment);
                    fragmentTransaction.commit();
                }
            });
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(collectionName != null && !collectionName.isEmpty()) {
            outState.putString("collectionName", collectionName);
            outState.putString("collectionDesc", collectionDesc);
        }
        outState.putInt("collectionId", collectionId);
        outState.putBoolean("isFirstSection", isFirstSection);
    }
}
