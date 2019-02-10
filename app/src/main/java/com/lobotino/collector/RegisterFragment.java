package com.lobotino.collector;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Олег on 19.01.2019.
 */

public class RegisterFragment extends Fragment {

    private View rootView;
    private RelativeLayout layout;
    private ActionBar actionBar;
    private Context context;
    private DbHandler dbHandler;
    private SQLiteDatabase mDb;
    private int screenWidth, screenHeight;
    private EditText etLogin, etPassword, etConfirmPassword;
    private TextView regStatus;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_sign_in, container, false);
        layout = rootView.findViewById(R.id.relative_layout_sign_in);
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle("Регистрация");

        NavigationActivity navigationActivity = (NavigationActivity) getActivity();
        navigationActivity.setCurrentFragment(this);
        context = getActivity().getBaseContext();
        dbHandler = NavigationActivity.dbHandler;
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;

        regStatus = rootView.findViewById(R.id.tvRegisterStatus);
        etLogin = rootView.findViewById(R.id.etLogin);
        etPassword = rootView.findViewById(R.id.etPassword);
        etConfirmPassword = rootView.findViewById(R.id.etConfirmPassword);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(4 * screenWidth/5, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margins = screenWidth/10;
        params.setMargins(margins, screenHeight/5, margins, 0);
        params.addRule(RelativeLayout.ALIGN_PARENT_START);
        etLogin.setLayoutParams(params);
        int white = Color.parseColor("#ffffff");
        etLogin.setTextColor(white);
        etLogin.setHintTextColor(white);
        etLogin.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        etPassword.setTextColor(white);
        etPassword.setHintTextColor(white);
        etPassword.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        etConfirmPassword.setTextColor(white);
        etConfirmPassword.setHintTextColor(white);
        etConfirmPassword.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        regStatus.setTextColor(white);
        regStatus.setTypeface(Typeface.DEFAULT_BOLD);

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

        Button regButton = rootView.findViewById(R.id.btnRegisterConfirm);
        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registration();
            }
        });
        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(margins + 10, 0, 0, 0);
        params.addRule(RelativeLayout.ALIGN_TOP, regButton.getId());
        regStatus.setLayoutParams(params);

        return rootView;
    }


    private void registration() {
        ContentValues contentValues = new ContentValues();

        String login = etLogin.getText().toString();
        String password = etPassword.getText().toString();
        String confPassword = etConfirmPassword.getText().toString();

        if (login.equals("")) {
            regStatus.setText("Введите логин!");
            etPassword.setText("");
            etConfirmPassword.setText("");
            return;
        }
        if (password.equals("")) {
            etConfirmPassword.setText("");
            regStatus.setText("Введите пароль!");
            return;
        } else {
            if (confPassword.equals("")) {
                etPassword.setText("");
                etConfirmPassword.setText("");
                regStatus.setText("Подтвердите пароль!");
                return;
            }
        }

        if (!password.equals(confPassword)) {
            etPassword.setText("");
            etConfirmPassword.setText("");
            regStatus.setText("Пароли не совпадают!");
            return;
        }

        //-------------------ХЕШИРОВАНИЕ-------------------

        /*Cursor cursor = mDb.query(DbHandler.TABLE_USERS, null, DbHandler.KEY_LOGIN + " = ?", new String[]{login}, null, null, null);
        if (!cursor.moveToFirst()) {
            try {
                password = password + DbHandler.SALT; //Добавление соли

                byte[] dataBytes = password.getBytes();
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(dataBytes);

                byte[] mdbytes = md.digest();

                StringBuffer passwordSb = new StringBuffer();
                for (int j = 0; j < mdbytes.length; j++) {
                    String s = Integer.toHexString(0xff & mdbytes[j]);
                    s = (s.length() == 1) ? "0" + s : s;
                    passwordSb.append(s);
                }


                SimpleDateFormat sdf = new SimpleDateFormat("dd.mm.yyyy");
                String currentDate = sdf.format(new Date());

                contentValues.put(DbHandler.KEY_USER_ID, DbHandler.globalUserID++);
                contentValues.put(DbHandler.KEY_LOGIN, login);
                contentValues.put(DbHandler.KEY_PASSWORD_HASH, passwordSb.toString());
                contentValues.put(DbHandler.KEY_REGISTER_DATE, currentDate);
            } catch (NoSuchAlgorithmException ex) {
                ex.printStackTrace();
            }


            etLogin.setText("");
            etPassword.setText("");
            etConfirmPassword.setText("");
            long index = mDb.insert(DbHandler.TABLE_USERS, null, contentValues);

            if (index == -1)
                regStatus.setText("Неизвестная ошибка");
            else {
                regStatus.setText("Успешно!");
            }
        } else {
            regStatus.setText("Такой логин уже занят!");
        }*/
    }
}
