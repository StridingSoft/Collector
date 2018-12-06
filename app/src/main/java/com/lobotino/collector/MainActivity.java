package com.lobotino.collector;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.sql.SQLException;
import java.util.List;

<<<<<<< HEAD
<<<<<<< HEAD
public class MainActivity extends AppCompatActivity implements View.OnClickListener{
<<<<<<< HEAD

    Button btnAdd, btnRead, btnClear;
    EditText etCollection, etSet, etElement;
    TextView tvResult;

    DbHandler dbHandler;
=======
>>>>>>> 937a5b2
=======
public class MainActivity extends AppCompatActivity {
>>>>>>> parent of 937a5b2... Поднята база SQlite, рабочее состояние

=======
public class MainActivity extends AppCompatActivity {

>>>>>>> parent of 937a5b2... Поднята база SQlite, рабочее состояние
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
<<<<<<< HEAD
<<<<<<< HEAD
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);

        btnRead = (Button) findViewById(R.id.btnRead);
        btnRead.setOnClickListener(this);

        btnClear = (Button) findViewById(R.id.btnClear);
        btnClear.setOnClickListener(this);

        etCollection = (EditText) findViewById(R.id.etCollection);
        etSet = (EditText) findViewById(R.id.etSet);
        etElement = (EditText) findViewById(R.id.etElement);

        tvResult = (TextView) findViewById(R.id.result);

        dbHandler = new DbHandler(this);
    }



    @Override
    public void onClick(View v) {

        String collection = etCollection.getText().toString();
        String set = etSet.getText().toString();
        String element = etElement.getText().toString();

        SQLiteDatabase database = dbHandler.getWritableDatabase();

        ContentValues contentValues = new ContentValues();


        switch (v.getId()) {

            case R.id.btnAdd: {
                contentValues.put(DbHandler.KEY_COLLECTION, collection);
                contentValues.put(DbHandler.KEY_SET, set);
                contentValues.put(DbHandler.KEY_ELEMENT, element);

                database.insert(DbHandler.TABLE_NAME, null, contentValues);

                etCollection.setText("");
                etSet.setText("");
                etElement.setText("");
                break;
            }

            case R.id.btnRead: {
                Cursor cursor = database.query(DbHandler.TABLE_NAME, null, null, null, null, null, null);
                String result = "";
                if (cursor.moveToFirst()) {
                    int colIndex = cursor.getColumnIndex(DbHandler.KEY_COLLECTION);
                    int setIndex = cursor.getColumnIndex(DbHandler.KEY_SET);
                    int elemIndex = cursor.getColumnIndex(DbHandler.KEY_ELEMENT);
                    do {
<<<<<<< HEAD
                        result = result + (cursor.getString(colIndex) +
                                ", " + cursor.getString(setIndex) +
                                ", " + cursor.getString(elemIndex) + "\n");
=======
                        result = result + cursor.getString(colIndex) +
                                ", " + cursor.getString(setIndex) +
                                ", " + cursor.getString(elemIndex) + "\n";
>>>>>>> 937a5b2
                    } while (cursor.moveToNext());
                } else
                    result = "0 rows";

                tvResult.setText(result);
                cursor.close();
                break;
            }

            case R.id.btnClear: {
                database.delete(DbHandler.TABLE_NAME, null, null);
                break;
=======
=======
>>>>>>> parent of 937a5b2... Поднята база SQlite, рабочее состояние
        try {
            System.out.println("GetInstanse!");
            DbHandler dbHandler = DbHandler.getInstance();
            // Добавляем запись
            dbHandler.addElement(new Element(1, "Музей", 200, "Развлечения"));
            dbHandler.addElement(new Element(2, "Бакуганы", 2001, "Развлечения"));
            // Получаем все записи и выводим их на консоль
            List<Element> elements = dbHandler.getAllProducts();
            for (Element element : elements) {
                System.out.println(element.toString());
<<<<<<< HEAD
>>>>>>> parent of 937a5b2... Поднята база SQlite, рабочее состояние
=======
>>>>>>> parent of 937a5b2... Поднята база SQlite, рабочее состояние
            }
        }catch(SQLException e){
            e.printStackTrace();
        }catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}
