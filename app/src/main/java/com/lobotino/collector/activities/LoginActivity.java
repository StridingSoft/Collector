package com.lobotino.collector.activities;

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

import com.lobotino.collector.utils.DbHandler;
import com.lobotino.collector.utils.JSONHandler;
import com.lobotino.collector.R;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity{

//    private View rootView;
    private RelativeLayout layout;
    private ActionBar actionBar;
    private Context context;
    private DbHandler dbHandler;
    private int screenWidth, screenHeight;
    private EditText etLogin, etPassword;
    private TextView loginStatus;

    @Nullable
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_login);

        layout = (RelativeLayout) findViewById(R.id.relative_layout_sign_in);
        actionBar = getSupportActionBar();
        actionBar.setTitle("Вход");

        context = getBaseContext();
        dbHandler = DbHandler.getInstance(context);
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;

        loginStatus = (TextView)findViewById(R.id.tvLoginStatus);
        etLogin = (EditText)findViewById(R.id.etLoginLogin);
        etPassword = (EditText)findViewById(R.id.etLoginPassword);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(4 * screenWidth / 5, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margins = screenWidth / 10;
        params.setMargins(margins, screenHeight / 8, margins, 0);
        params.addRule(RelativeLayout.ALIGN_PARENT_START);
        etLogin.setLayoutParams(params);
        int white = Color.parseColor("#ffffff");
        etLogin.setTextColor(white);
        etLogin.setHintTextColor(white);
        etLogin.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        etPassword.setTextColor(white);
        etPassword.setHintTextColor(white);
        etPassword.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        loginStatus.setTextColor(white);
        loginStatus.setTypeface(Typeface.DEFAULT_BOLD);

        try {
            dbHandler.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }

        Button loginButton = (Button)findViewById(R.id.btnGoToLogin);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        Button regButton = (Button)findViewById(R.id.buttonGoToReg);
        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToRegistration();
            }
        });
    }


    private void login() {
        String login = etLogin.getText().toString();
        String password = etPassword.getText().toString();

        if (login.equals("")) {
            loginStatus.setText("Введите логин");
            etPassword.setText("");
            return;
        }
        if (password.equals("")) {
            loginStatus.setText("Введите пароль");
            return;
        }

        password = password + DbHandler.SALT;
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
            etPassword.setText("");

            int index = new AsyncLoginTask(login, hashPass).execute().get();
            switch (index) {
                case 0: {
                    loginStatus.setText("Успешно!");
                    dbHandler.syncUserItems();
                    Intent intent = new Intent(context, MainActivity.class);
                    startActivity(intent);
                    break;
                }
                case 1: {
                    loginStatus.setText("Неправильный пароль или логин");
                    break;
                }
                case 2: {
                    loginStatus.setText("Ошибка соединения");
                    break;
                }
                default: loginStatus.setText("Неизвестная ошибка");
            }

        } catch (NoSuchAlgorithmException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            loginStatus.setText("Неизвестная ошибка");
        }
    }

    protected void goToRegistration()
    {
        Intent intent = new Intent(context, RegistrationActivity.class);
        startActivity(intent);
    }


    class AsyncLoginTask extends AsyncTask<Void, Void, Integer> {

        private String login, hashPass;

        public AsyncLoginTask(String login, String hashPass) {
            this.login = login;
            this.hashPass = hashPass;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            Connection connection = null;
            Statement st1 = null;
            ResultSet rs1 = null;
            try {
                connection = dbHandler.getConnection(context);
                if(connection == null) return 2;

                String SQL = "SELECT " + DbHandler.KEY_ID + ", " + DbHandler.KEY_EMAIL + " FROM " + DbHandler.TABLE_USERS + " WHERE " + DbHandler.KEY_LOGIN + " like '" + login +
                        "' and " + DbHandler.KEY_PASSWORD + " like '" + hashPass + "'";
                st1 = connection.createStatement();
                rs1 = st1.executeQuery(SQL);
                if(rs1 != null && rs1.next())
                {
                    int id = rs1.getInt(1);
                    String email = rs1.getString(2);
                    st1.close();
                    rs1.close();
                    JSONHandler.CurrentUser currentUser = new JSONHandler.CurrentUser(id, login, hashPass,email);
                    JSONHandler.exportToJSON(context, currentUser);
                    return 0;
                }else{
                    st1.close();
                    rs1.close();
                    return 1;
                }
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
                try {
                    if (st1 != null)
                        st1.close();
                    if (rs1 != null)
                        rs1.close();
                } catch (java.sql.SQLException e1) {
                    e1.printStackTrace();
                    return -1;
                }
                return -1;
            }
        }
    }
}
