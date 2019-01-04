package com.lobotino.collector;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;



public class MainActivity extends AppCompatActivity implements View.OnClickListener{



    Button btnAdd, btnRead, btnClear, btnGoToReg, btnGoToLogin;
    EditText etCollection, etSet, etElement;
    TextView tvResult;

    public static DbHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);

        btnRead = (Button) findViewById(R.id.btnRead);
        btnRead.setOnClickListener(this);

        btnClear = (Button) findViewById(R.id.btnClear);
        btnClear.setOnClickListener(this);

        btnGoToReg = (Button) findViewById(R.id.btnGoToReg);
        btnGoToReg.setOnClickListener(this);

        btnGoToLogin = (Button) findViewById(R.id.btnGoToLogin);
        btnGoToLogin.setOnClickListener(this);


        etCollection = (EditText) findViewById(R.id.etCollection);
        etSet = (EditText) findViewById(R.id.etSet);
        etElement = (EditText) findViewById(R.id.etElement);


        tvResult = (TextView) findViewById(R.id.result);

        dbHandler = new DbHandler(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btnAdd: {

                SQLiteDatabase database = dbHandler.getWritableDatabase();

                ContentValues contentValues = new ContentValues();

                String collection = etCollection.getText().toString();
                String set = etSet.getText().toString();
                String element = etElement.getText().toString();

//                contentValues.put(DbHandler.KEY_COLLECTION, collection);
//                contentValues.put(DbHandler.KEY_SET, set);
//                contentValues.put(DbHandler.KEY_ELEMENT, element);

              //  database.insert(DbHandler.TABLE_NAME, null, contentValues);

                etCollection.setText("");
                etSet.setText("");
                etElement.setText("");
                break;
            }

            case R.id.btnRead: {
                SQLiteDatabase database = dbHandler.getWritableDatabase();

                Cursor cursor = database.query(DbHandler.TABLE_USERS, null, null, null, null, null, null);
                String result = "USERS:\n";

                if (cursor.moveToFirst()) {
                    int loginIndex = cursor.getColumnIndex(DbHandler.KEY_LOGIN);
                    int passIndex = cursor.getColumnIndex(DbHandler.KEY_PASSWORD_HASH);
                    int idUserIndex = cursor.getColumnIndex(DbHandler.KEY_USER_ID);
                    int regDateIndex = cursor.getColumnIndex(DbHandler.KEY_REGISTER_DATE);

                    do {
                        result = result + ( "ID: " + cursor.getInt(idUserIndex) +
                                "\nLogin: " + cursor.getString(loginIndex) +
                                "\nPassword: " + cursor.getString(passIndex) +
                                "\nRegDate: " + cursor.getString(regDateIndex) + "\n\n");

                    } while (cursor.moveToNext());
                } else
                    result = result + "0 users\n";


//                cursor = database.query(DbHandler.TABLE_NAME, null, null, null, null, null, null);
//                result = result + "COLLECTIONS:\n";
//                if (cursor.moveToFirst()) {
//                    int colIndex = cursor.getColumnIndex(DbHandler.KEY_COLLECTION);
//                    int setIndex = cursor.getColumnIndex(DbHandler.KEY_SET);
//                    int elemIndex = cursor.getColumnIndex(DbHandler.KEY_ELEMENT);
//                    do {
//                        result = result + (cursor.getString(colIndex) +
//                                ", " + cursor.getString(setIndex) +
//                                ", " + cursor.getString(elemIndex) + "\n");
//
////                    } while (cursor.moveToNext());
//                } else
//                    result = result + "0 rows";
//
//                tvResult.setText(result);
//                cursor.close();
                break;
            }

            case R.id.btnClear: {
                SQLiteDatabase database = dbHandler.getWritableDatabase();
                database.delete(DbHandler.TABLE_COLLECTIONS, null, null);
                database.delete(DbHandler.TABLE_SECTIONS, null, null);
                database.delete(DbHandler.TABLE_ITEMS, null, null);
                database.delete(DbHandler.TABLE_USERS, null, null);
                break;
            }
            case R.id.btnGoToLogin:{
                Intent intObj = new Intent(this, LoginActivity.class);
                startActivity(intObj);
                break;
            }

            case R.id.btnGoToReg:{
                Intent intObj = new Intent(this, RegisterActivity.class);
                startActivity(intObj);
                break;
            }
        }
    }
}