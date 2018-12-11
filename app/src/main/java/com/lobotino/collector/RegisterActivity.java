package com.lobotino.collector;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;



public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {



    DbHandler dbHandler;
    EditText etLogin, etPassword;
    Button btnBack, btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etLogin = (EditText) findViewById(R.id.etLogin);
        etPassword = (EditText) findViewById(R.id.etPassword);

        btnBack = (Button) findViewById(R.id.btnBackToMain);
        btnBack.setOnClickListener(this);

        btnConfirm = (Button) findViewById(R.id.btnConfirmRegister);
        btnConfirm.setOnClickListener(this);


        dbHandler = MainActivity.dbHandler;

    }



    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btnConfirmRegister: {
                SQLiteDatabase database = dbHandler.getWritableDatabase();
                ContentValues contentValues = new ContentValues();

                String Login = etLogin.getText().toString();
                String Password = etPassword.getText().toString();

                try {
                    byte[] dataBytes = Password.getBytes();
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    md.update(dataBytes);

                    byte[] mdbytes = md.digest();

                    StringBuffer sb = new StringBuffer();
                    for (int j = 0; j < mdbytes.length; j++) {
                        String s = Integer.toHexString(0xff & mdbytes[j]);
                        s = (s.length() == 1) ? "0" + s : s;
                        sb.append(s);
                    }

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    String currentDate = sdf.format(new Date());

                    contentValues.put(DbHandler.KEY_LOGIN, Login);
                    contentValues.put(DbHandler.KEY_PASSWORD_HASH, sb.toString());
                    contentValues.put(DbHandler.KEY_USER_ID, MainActivity.globalUserID++);
                    contentValues.put(DbHandler.KEY_REGISTER_DATE, currentDate);

                    database.insert(DbHandler.TABLE_USERS, null, contentValues);

                    etLogin.setText("");
                    etPassword.setText("");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                break;
            }
            case R.id.btnBackToMain: {
                Intent intObj = new Intent(this, MainActivity.class);
                startActivity(intObj);
                break;
            }
        }
    }
}
