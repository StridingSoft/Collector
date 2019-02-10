package com.lobotino.collector;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    DbHandler dbHandler;
    EditText etLogin, etPassword;
    Button btnBack, btnConfirm;
    TextView loginStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLogin = (EditText) findViewById(R.id.etLoginLogin);
        etPassword = (EditText) findViewById(R.id.etPasswordLogin);

        btnBack = (Button) findViewById(R.id.btnBackToMainFromLogin);
        btnBack.setOnClickListener(this);

        btnConfirm = (Button) findViewById(R.id.btnLoginConfirm);
        btnConfirm.setOnClickListener(this);

        loginStatus = (TextView) findViewById(R.id.tvLoginStatus);

       // dbHandler = MainActivity.dbHandler;

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btnLoginConfirm: {

                String login = etLogin.getText().toString();
                String password = etPassword.getText().toString();

                SQLiteDatabase database = dbHandler.getReadableDatabase();



                boolean flagLogin = false;


                try {
                    String SaltPassword = password + DbHandler.SALT;
                    byte[] dataBytes = SaltPassword.getBytes();
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    md.update(dataBytes);

                    byte[] mdbytes = md.digest();

                    StringBuffer passwordHash = new StringBuffer();
                    for (int j = 0; j < mdbytes.length; j++) {
                        String s = Integer.toHexString(0xff & mdbytes[j]);
                        s = (s.length() == 1) ? "0" + s : s;
                        passwordHash.append(s);
                    }

                   /* Cursor cursor = database.query(DbHandler.TABLE_USERS, null, DbHandler.KEY_LOGIN + " = '" + login + "'", null, null, null, null);

                    if(cursor.moveToFirst() && cursor.getCount() >= 1) {
                        String dbPass = cursor.getString(cursor.getColumnIndex(DbHandler.KEY_PASSWORD_HASH));

                        if (passwordHash.toString().equals(dbPass)) flagLogin = true;
                    }
*/
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }


                if (flagLogin) {
                    loginStatus.setText("Успешно!");
                 //   Intent intObj = new Intent(this, MainActivity.class);
                 //   startActivity(intObj);
                    break;
                } else {
                    loginStatus.setText("Нерпавильный логин или пароль");
                }

                break;
            }

            case R.id.btnBackToMainFromLogin:{
               // Intent intObj = new Intent(this, MainActivity.class);
            //    startActivity(intObj);
                break;
            }
        }
    }
}

