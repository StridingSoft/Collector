package com.lobotino.collector;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {


    DbHandler dbHandler;
    EditText etLogin, etPassword, etConfirmPassword;
    Button btnBack, btnConfirm;
    TextView regStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        etLogin = (EditText) findViewById(R.id.etLogin);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etConfirmPassword = (EditText) findViewById(R.id.etConfirmPassword);

        btnBack = (Button) findViewById(R.id.btnBackToMain);
        btnBack.setOnClickListener(this);

        btnConfirm = (Button) findViewById(R.id.btnRegisterConfirm);
        btnConfirm.setOnClickListener(this);

        regStatus = (TextView) findViewById(R.id.tvRegisterStatus);

       // dbHandler = MainActivity.dbHandler;

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btnRegisterConfirm: {
                SQLiteDatabase database = dbHandler.getWritableDatabase();
                ContentValues contentValues = new ContentValues();

                String Login = etLogin.getText().toString();
                String Password = etPassword.getText().toString();
                String ConfPassword = etConfirmPassword.getText().toString();

                if (Login.equals("")) {
                    regStatus.setText("Введите логин!");
                    etPassword.setText("");
                    etConfirmPassword.setText("");
                    break;
                }
                if (Password.equals("")) {
                    etConfirmPassword.setText("");
                    regStatus.setText("Введите пароль!");
                    break;
                } else {
                    if (ConfPassword.equals("")) {
                        etPassword.setText("");
                        etConfirmPassword.setText("");
                        regStatus.setText("Подтвердите пароль!");
                        break;
                    }
                }

                if (!Password.equals(ConfPassword)) {
                    etPassword.setText("");
                    etConfirmPassword.setText("");
                    regStatus.setText("Пароли не совпадают!");
                    break;
                }

                //-------------------ХЕШИРОВАНИЕ-------------------

                try {

                    Password = Password + DbHandler.SALT; //Добавление соли

                    byte[] dataBytes = Password.getBytes();
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    md.update(dataBytes);

                    byte[] mdbytes = md.digest();

                    StringBuffer passwordSb = new StringBuffer();
                    for (int j = 0; j < mdbytes.length; j++) {
                        String s = Integer.toHexString(0xff & mdbytes[j]);
                        s = (s.length() == 1) ? "0" + s : s;
                        passwordSb.append(s);
                    }


                    SimpleDateFormat sdf = new SimpleDateFormat("MM.dd.yyyy");
                    String currentDate = sdf.format(new Date());

                    contentValues.put(DbHandler.KEY_USER_ID, DbHandler.globalUserID++);
                    contentValues.put(DbHandler.KEY_LOGIN, Login);
                    contentValues.put(DbHandler.KEY_PASSWORD_HASH, passwordSb.toString());
                    contentValues.put(DbHandler.KEY_REGISTER_DATE, currentDate);

                    database.insert(DbHandler.TABLE_USERS, null, contentValues);

                    etLogin.setText("");
                    etPassword.setText("");
                    etConfirmPassword.setText("");
                    regStatus.setText("Успешно!");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                break;
            }
            case R.id.btnBackToMain: {
           //     Intent intObj = new Intent(this, MainActivity.class);
            //    startActivity(intObj);
                break;
            }
        }
    }
}
