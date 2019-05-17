package com.lobotino.collector;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;


public class RegistrationActivity extends AppCompatActivity {

    private RelativeLayout layout;
    private ActionBar actionBar;
    private Context context;
    private DbHandler dbHandler;
    private SQLiteDatabase mDb;
    private int screenWidth, screenHeight;
    private EditText etLogin, etPassword, etConfirmPassword, etEmail;
    private TextView regStatus;

    @Nullable
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Регистрация");

        setContentView(R.layout.content_registration);

        context = getBaseContext();
        dbHandler = NavigationActivity.dbHandler;
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;

        regStatus = (TextView)findViewById(R.id.tvRegisterStatus);
        etLogin = (EditText)findViewById(R.id.etLogin);
        etPassword = (EditText)findViewById(R.id.etPassword);
        etConfirmPassword = (EditText)findViewById(R.id.etConfirmPassword);
        etEmail = (EditText)findViewById(R.id.etEmail);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(4 * screenWidth / 5, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margins = screenWidth / 10;
        params.setMargins(margins, screenHeight / 8, margins, 0);
        params.addRule(RelativeLayout.ALIGN_PARENT_START);
        etLogin.setLayoutParams(params);
        int white = Color.parseColor("#ffffff");
        etLogin.setTextColor(white);
        etLogin.setHintTextColor(white);
        etLogin.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        etEmail.setTextColor(white);
        etEmail.setHintTextColor(white);
        etEmail.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
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

        Button regButton = (Button)findViewById(R.id.btnRegisterConfirm);
        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registration();
            }
        });
        Button loginButton = (Button)findViewById(R.id.btnGoToLogin);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToLogin();
            }
        });
    }

    private void goToLogin()
    {
        Intent intent = new Intent(context, LoginActivity.class);
        startActivity(intent);
    }

    private void registration() {
        ContentValues contentValues = new ContentValues();

        String login = etLogin.getText().toString();
        String password = etPassword.getText().toString();
        String confPassword = etConfirmPassword.getText().toString();
        String email = etEmail.getText().toString();

        if (login.equals("")) {
            regStatus.setText("Введите логин");
            etPassword.setText("");
            etConfirmPassword.setText("");
            return;
        }
        if (email.equals("")) {
            regStatus.setText("Введите почту");
            etPassword.setText("");
            etConfirmPassword.setText("");
            return;
        }
        if (password.equals("")) {
            etConfirmPassword.setText("");
            regStatus.setText("Введите пароль");
            return;
        } else {
            if (confPassword.equals("")) {
                etPassword.setText("");
                etConfirmPassword.setText("");
                regStatus.setText("Подтвердите пароль");
                return;
            }
        }

        if (!password.equals(confPassword)) {
            etPassword.setText("");
            etConfirmPassword.setText("");
            regStatus.setText("Пароли не совпадают");
            return;
        }

        if(password.length() < 6)
        {
            etPassword.setText("");
            etConfirmPassword.setText("");
            regStatus.setText("В пароле должно быть больше 6 символов");
            return;
        }

        //-------------------ХЕШИРОВАНИЕ-------------------


        password = password + DbHandler.SALT; //Добавление соли

        byte[] dataBytes = password.getBytes();
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");

            md.update(dataBytes);

            byte[] mdbytes = md.digest();

            StringBuffer stringBuffer = new StringBuffer();
            for (int j = 0; j < mdbytes.length; j++) {
                String s = Integer.toHexString(0xff & mdbytes[j]);
                s = (s.length() == 1) ? "0" + s : s;
                stringBuffer.append(s);
            }
            String hashPass = stringBuffer.toString();

            etLogin.setText("");
            etEmail.setText("");
            etPassword.setText("");
            etConfirmPassword.setText("");

            int index = new AsyncRegistrationTask(login, email, hashPass).execute().get();
            switch (index) {
                case 0: {
                    regStatus.setText("Успешно!");
                    Intent intent = new Intent(context, NavigationActivity.class);
                    startActivity(intent);
                    break;
                }
                case 1: {
                    regStatus.setText("Ошибка соединения");
                    break;
                }
                case 2: {
                    regStatus.setText("Такой логин уже используется");
                    break;
                }
                case 3: {
                    regStatus.setText("Такая почта уже используется");
                    break;
                }
                default: regStatus.setText("Неизвестная ошибка");
            }

        } catch (NoSuchAlgorithmException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            regStatus.setText("Неизвестная ошибка");
        }
    }


    class AsyncRegistrationTask extends AsyncTask<Void, Void, Integer> {

        private String login, hashPass, email;

        public AsyncRegistrationTask(String login, String email, String hashPass) {
            this.login = login;
            this.hashPass = hashPass;
            this.email = email;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            Connection connection = null;
            Statement st1 = null;
            Statement st2 = null;
            Statement st3 = null;
            ResultSet rs1 = null;
            ResultSet rs2 = null;
            ResultSet rs3= null;
            try {
                if (DbHandler.isOnline(context)) {
                    if (DbHandler.needToReconnect)
                        connection = DbHandler.setNewConnection(DriverManager.getConnection(DbHandler.MSSQL_DB, DbHandler.MSSQL_LOGIN, DbHandler.MSSQL_PASS));
                    else
                        connection = DbHandler.getConnection();
                } else return 1;

                String SQL = "SELECT " + DbHandler.KEY_ID +" FROM " + DbHandler.TABLE_USERS + " WHERE " + DbHandler.KEY_LOGIN + " like '" + login + "'";
                st1 = connection.createStatement();
                rs1 = st1.executeQuery(SQL);
                if(rs1 != null && rs1.next())
                {
                    st1.close();
                    rs1.close();
                    return 2;
                }else{
                    st1.close();
                    rs1.close();
                }

                SQL = "SELECT " + DbHandler.KEY_ID +" FROM " + DbHandler.TABLE_USERS + " WHERE " + DbHandler.KEY_EMAIL + " like '" + email + "'";
                st2 = connection.createStatement();
                rs2 = st2.executeQuery(SQL);
                if(rs2 != null && rs2.next())
                {
                    st2.close();
                    rs2.close();
                    return 3;
                } else {
                    st2.close();
                    rs2.close();
                }

                SQL = "insert into " + DbHandler.TABLE_USERS + "(" + DbHandler.KEY_LOGIN + "," + DbHandler.KEY_USER_NAME + "," +
                        DbHandler.KEY_PASSWORD + "," + DbHandler.KEY_ROLE_ID + "," + DbHandler.KEY_REG_DATE + "," + DbHandler.KEY_EMAIL +","+
                        DbHandler.KEY_LAST_ACTIVITY_DAYE  + ") VALUES(?,?,?,?,?,?,?)";
                PreparedStatement pSt = connection.prepareStatement(SQL);
                DateFormat orig = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss");
                String date = orig.format(Calendar.getInstance().getTime());

                pSt.setString(1, login);
                pSt.setString(2, login);
                pSt.setString(3, hashPass);
                pSt.setInt(4,0);
                pSt.setString(5, date);
                pSt.setString(6, email);
                pSt.setString(7, date);
                pSt.execute();
                pSt.close();

                SQL = "SELECT " + DbHandler.KEY_ID +" FROM " + DbHandler.TABLE_USERS + " WHERE " + DbHandler.KEY_LOGIN + " like '" + login + "'";
                st3 = connection.createStatement();
                rs3 = st3.executeQuery(SQL);
                if(rs3 != null && rs3.next())
                {
                    int id = rs3.getInt(1);
                    JSONHelper.CurrentUser currentUser = new JSONHelper.CurrentUser(id, login, hashPass);
                    JSONHelper.exportToJSON(context, currentUser);
                    st3.close();
                    rs3.close();
                }
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
                try {
                    if (st1 != null)
                        st1.close();
                    if (rs1 != null)
                        rs1.close();
                    if (st2 != null)
                        st2.close();
                    if (rs2 != null)
                        rs2.close();
                    if (st3 != null)
                        st3.close();
                    if (rs3 != null)
                        rs3.close();
                } catch (java.sql.SQLException e1) {
                    e1.printStackTrace();
                    return -1;
                }
                return -1;
            }
            return 0;
        }
    }
}

