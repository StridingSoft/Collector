package com.lobotino.collector;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.sql.SQLException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            }
        }catch(SQLException e){
            e.printStackTrace();
        }catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}
