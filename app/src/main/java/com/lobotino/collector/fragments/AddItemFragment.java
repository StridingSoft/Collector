package com.lobotino.collector.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.lobotino.collector.R;
import com.lobotino.collector.activities.MainActivity;
import com.lobotino.collector.async_tasks.AsyncGetBitmapsFromUri;
import com.lobotino.collector.async_tasks.add_elements.AsyncAddCollectionToServer;
import com.lobotino.collector.async_tasks.add_elements.AsyncAddItemToServer;
import com.lobotino.collector.async_tasks.add_elements.AsyncAddSectionToServer;
import com.lobotino.collector.utils.DbHandler;

import java.io.IOException;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Олег on 13.07.2019.
 */

public class AddItemFragment extends Fragment {

    private static final int PICKFILE_RESULT_CODE = 1;

    private int itemId;
    private int sectionId, collectionId;
    private String sectionTitle, sectionName, sectionDesc, collectionName, collectionDesc;
    private boolean isFirstItem;

    private DbHandler dbHandler;
    private SQLiteDatabase mDb;
    private View rootView;
    private Context context;
    private int pictureSize, screenWight, screenHeight, puddingsSize, topMargin;
    private GradientDrawable gradientDrawable;
    private RelativeLayout layout;
    private ScrollView scrollView;
    private ActionBar actionBar;
    private ImageButton buttonDonwload, buttonAccept;
    private TextView tvDescriptionTitle, tvDescription;
    private Connection connection;
    private boolean inMyCollection = false;
    private ImageView imageView;
    private Uri imageUri;
    public int getItemId(){
        return itemId;
    }
    private Fragment currentFragment = this;

    public int getSectionId(){
        return sectionId;
    }

    public String getSectionTitle()
    {
        return sectionTitle;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add_item, container, false);
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.setCurrentFragment(this);

        imageView = rootView.findViewById(R.id.elemImage);
        if(savedInstanceState != null) {
            String imageUriString = savedInstanceState.getString("imageUri");
            if(imageUriString != null) {
                this.imageUri = Uri.parse(imageUriString);
                imageView.setImageURI(imageUri);
            }
            sectionTitle = savedInstanceState.getString("sectionTitle");
            sectionId = savedInstanceState.getInt("secId");
            collectionId = savedInstanceState.getInt("collectionId");
            isFirstItem = savedInstanceState.getBoolean("isFirstItem");
            collectionName = savedInstanceState.getString("collectionName");
            collectionDesc = savedInstanceState.getString("collectionDesc");
            sectionName = savedInstanceState.getString("sectionName");
            sectionDesc = savedInstanceState.getString("sectionDesc");

        }else {
            sectionId = getArguments().getInt("secId");
            collectionId = getArguments().getInt("collectionId");
            sectionTitle = getArguments().getString("sectionTitle");
            isFirstItem = getArguments().getBoolean("isFirstItem");
            collectionName = getArguments().getString("collectionName");
            collectionDesc = getArguments().getString("collectionDesc");
            sectionName = getArguments().getString("sectionName");
            sectionDesc = getArguments().getString("sectionDesc");
        }


        context = getActivity().getBaseContext();
        dbHandler = MainActivity.dbHandler;
        screenWight = context.getResources().getDisplayMetrics().widthPixels;
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        pictureSize = Math.round((float) (screenHeight /3*2));
        layout = (RelativeLayout) rootView.findViewById(R.id.relative_layout_2);
        scrollView = (ScrollView) rootView.findViewById(R.id.scroll_view_current_item);
        actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Добавление " + (isFirstItem ? "первого " : "") + "предмета");
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

        puddingsSize = pictureSize / 30; //15
        topMargin = screenWight / 20;

        gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(15);
        gradientDrawable.setColors(new int[]{Color.parseColor("#5d000e"), Color.parseColor("#430014")});

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,PICKFILE_RESULT_CODE);
            }
        });

        Button buttonAccept = rootView.findViewById(R.id.button_accept);
            buttonAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText etName = (EditText) rootView.findViewById(R.id.etElemName);
                    EditText etDesc = (EditText) rootView.findViewById(R.id.etElemDesc);
                    String name = etName.getText().toString();
                    String desc = etDesc.getText().toString();

                    TextView tvStatus = (TextView) rootView.findViewById(R.id.tvAddElemStatus);

                    if(name.equals("Название элемента") || name.length() == 0)
                    {
                        tvStatus.setText("Введите название элемента!");
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

                    if(desc.equals("Описание элемента") || desc.length() == 0)
                    {
                        tvStatus.setText("Введите описание элемента!");
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

                    if(imageUri == null)
                    {
                        tvStatus.setText("Выберите изображение!");
                        return;
                    }

                    DateFormat orig = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss");
                    String date = orig.format(Calendar.getInstance().getTime());

                    try {
                        if (collectionId == 0 && collectionName != null && !collectionName.isEmpty())
                            collectionId = new AsyncAddCollectionToServer(collectionName, collectionDesc).execute().get();

                        if (sectionId == 0 && sectionName != null && !sectionName.isEmpty())
                            sectionId = new AsyncAddSectionToServer(collectionId, sectionName, sectionDesc).execute().get();

                        new AsyncAddItemToServer(sectionId, collectionId, name, desc, date).execute(new AsyncGetBitmapsFromUri(imageUri, 1024,  1024, context).execute().get());

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    CollectionsFragment collectionsFragment = new CollectionsFragment();
                    AddItemFragment fragment = (AddItemFragment)currentFragment;
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
            });
//
//
//            ImageView imageView = new ImageView(context);
//            imageView.setBackground(gradientDrawable);
//            //imageView.setPadding(puddingsSize, puddingsSize, puddingsSize, puddingsSize);
//
//
////            CurrentItemFragment.DownloadScaledImage downloadScaledImage = new CurrentItemFragment.DownloadScaledImage(itemId, imageView);
////            downloadScaledImage.execute();
//
//            GradientDrawable buttonBackground = new GradientDrawable();
//            buttonBackground.setCornerRadius(17);
//            buttonBackground.setColors(new int[]{Color.parseColor("#5d000e"), Color.parseColor("#430014")});
//
//            int buttonSize = screenHeight > screenWight ? screenWight / 10 : screenHeight / 10;
//            int buttonMargin = screenHeight > screenWight ? screenWight / 22 : screenHeight / 22;
//
//            buttonDonwload = new ImageButton(context);
//            Drawable img = context.getDrawable(R.drawable.trade_button);
//            buttonDonwload.setImageDrawable(img);
//            buttonDonwload.setBackground(buttonBackground);
//            int buttonTradeId = View.generateViewId();
//            buttonDonwload.setId(buttonTradeId);
//            RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
//            buttonParams.addRule(RelativeLayout.BELOW, imageView.getId());
//            buttonParams.addRule(RelativeLayout.CENTER_HORIZONTAL,  imageView.getId());
//            buttonParams.setMargins(0, buttonMargin, 0, 0);
//            buttonDonwload.setLayoutParams(buttonParams);
//
//
//            buttonAccept = new ImageButton(context);
//
//            Cursor cursor = mDb.query(DbHandler.TABLE_ITEMS, new String[]{DbHandler.KEY_ITEM_STATUS}, DbHandler.KEY_ID + " = " + itemId, null, null, null, null);
//
//            if(cursor.moveToFirst())
//            {
//                if(cursor.getString(cursor.getColumnIndex(DbHandler.KEY_ITEM_STATUS)).equals("in"))
//                {
//                    inMyCollection = true;
//                }
//            }
//            cursor.close();
//
//            img = context.getDrawable(inMyCollection ? R.drawable.ic_in_my_collection : R.drawable.i_have_it_button);
//            buttonAccept.setImageDrawable(img);
//            buttonAccept.setBackground(buttonBackground);
//            buttonParams = new RelativeLayout.LayoutParams(buttonSize, buttonSize);
//            buttonParams.addRule(RelativeLayout.BELOW,  imageView.getId());
//            buttonParams.addRule(RelativeLayout.ALIGN_END, buttonTradeId);
//            buttonParams.setMargins(0, buttonMargin, buttonMargin + buttonSize, 0);
//            buttonAccept.setLayoutParams(buttonParams);
//            buttonAccept.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
////                    Cursor cursor = mDb.query(DbHandler.TABLE_ITEMS, new String[]{DbHandler.KEY_ITEM_STATUS}, DbHandler.KEY_ID + " = " + itemId, null, null, null, null);
////                    if(cursor.moveToFirst())
////                    {
//////                        String newStatus;
//////                        //  ContentValues contentValues = new ContentValues();
//////                        if(cursor.getString(cursor.getColumnIndex(DbHandler.KEY_ITEM_STATUS)).equals("in")) {
//////                            newStatus = "missing";
//////                            inMyCollection = false;
//////                        }
//////                        else {
//////                            newStatus = "in";
//////                            inMyCollection = true;
//////                        }
//////                        AsyncSetItemStatus asyncSetItemStatus = new AsyncSetItemStatus(itemId, context);
//////                        asyncSetItemStatus.execute(newStatus);
//////                        cursor.close();
////                    }
////                    cursor.close();
//                }
//            });
//
//            int descMargin = screenHeight > screenWight ? screenWight /11 : screenHeight /11;
//
//            tvDescriptionTitle = new TextView(context);
//            tvDescriptionTitle.setText("Описание:");
//            RelativeLayout.LayoutParams descTitleParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            descTitleParams.addRule(RelativeLayout.BELOW,  imageView.getId());
//            descTitleParams.addRule(RelativeLayout.ALIGN_LEFT,  imageView.getId());
//            descTitleParams.setMargins(0, buttonSize + descMargin, 0, 0);
//            tvDescriptionTitle.setLayoutParams(descTitleParams);
//            int tvDescTitleId = View.generateViewId();
//            tvDescriptionTitle.setId(tvDescTitleId);
//            tvDescriptionTitle.setTextColor(Color.parseColor("#ffffff"));
//            tvDescriptionTitle.setTypeface(Typeface.DEFAULT_BOLD);
//            tvDescriptionTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
//
//            int descWidth = screenHeight > screenWight ? screenWight - 2*topMargin : screenHeight - 2*topMargin;
//            tvDescription = new TextView(context);
//            tvDescription.setText(itemDesc);
//            RelativeLayout.LayoutParams descParams = new RelativeLayout.LayoutParams(descWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
//            descParams.addRule(RelativeLayout.BELOW, tvDescTitleId);
//            descParams.addRule(RelativeLayout.ALIGN_LEFT,  imageView.getId());
//            tvDescription.setLayoutParams(descParams);
//            int tvDescId = View.generateViewId();
//            tvDescription.setId(tvDescId);
//            tvDescription.setTextColor(Color.parseColor("#ffffff"));
//            tvDescription.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 28);
//
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(imageUri != null)
            outState.putString("imageUri", imageUri.toString());
        outState.putString("sectionTitle", sectionTitle);
        outState.getInt("collectionId", collectionId);
        outState.getBoolean("isFirstItem", isFirstItem);
        outState.getString("collectionName", collectionName);
        outState.getString("collectionDesc", collectionDesc);
        outState.getString("sectionName", sectionName);
        outState.getString("sectionDesc", sectionDesc);
        outState.putInt("secId", sectionId);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case PICKFILE_RESULT_CODE:
                if(resultCode == RESULT_OK){
                    Uri filePathUri = data.getData();
                    imageUri = filePathUri;
                    imageView.setImageURI(filePathUri);
                }
                break;
        }
    }

}
