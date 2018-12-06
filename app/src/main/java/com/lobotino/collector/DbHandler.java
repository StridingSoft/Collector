package com.lobotino.collector;


import org.sqlite.JDBC;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DbHandler {
    private static final String CONNECTION_PATH = "jdbc:sqlite:collector.db";

    private static DbHandler instance;
    private static Statement statement;

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
    public static synchronized DbHandler getInstance() throws SQLException, ClassNotFoundException {
        if(instance == null)
            instance = new DbHandler();
    public void addElement(Element element)
    {
        try(PreparedStatement statement = this.connection.prepareStatement(
                "INSERT INTO Products(`good`, `price`, `category_name`) " +
                        "VALUES(?, ?, ?)")){
            statement.setObject(1, element.good);
            statement.setObject(2, element.price);
            statement.setObject(3, element.category_name);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
=======
    public static final String KEY_COLLECTION = "_collection";
    public static final String KEY_SET = "_set";
    public static final String KEY_ELEMENT = "_element";
=======
=======
>>>>>>> parent of 937a5b2... Поднята база SQlite, рабочее состояние
    public static synchronized DbHandler getInstance() throws SQLException, ClassNotFoundException {
        if(instance == null)
            instance = new DbHandler();
        return instance;
    }

    private Connection connection;
<<<<<<< HEAD
>>>>>>> parent of 937a5b2... Поднята база SQlite, рабочее состояние
=======
>>>>>>> parent of 937a5b2... Поднята база SQlite, рабочее состояние

    public DbHandler(){

        connection = null;
        try {
            DriverManager.registerDriver(new JDBC());
            connection = DriverManager.getConnection(CONNECTION_PATH);
            System.out.println("База подключена!");
        }
        catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public List<Element> getAllProducts() {

        // Statement используется для того, чтобы выполнить sql-запрос
        try (Statement statement = this.connection.createStatement()) {
            // В данный список будем загружать наши элементы, полученные из БД
            List<Element> elements = new ArrayList<Element>();
            // В resultSet будет храниться результат нашего запроса,
            // который выполняется командой statement.executeQuery()
            ResultSet resultSet = statement.executeQuery("SELECT id, good, price, category_name FROM elements");
            // Проходимся по нашему resultSet и заносим данные в products
            while (resultSet.next()) {
                elements.add(new Element(resultSet.getInt("id"),
                        resultSet.getString("good"),
                        resultSet.getDouble("price"),
                        resultSet.getString("category_name")));
            }
            // Возвращаем наш список
            return elements;

        } catch (SQLException e) {
            e.printStackTrace();
            // Если произошла ошибка - возвращаем пустую коллекцию
            return Collections.emptyList();
        }
    }

<<<<<<< HEAD
<<<<<<< HEAD
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "(" + KEY_COLLECTION
                + " text," + KEY_SET + " text," + KEY_ELEMENT + " text" + ")");
>>>>>>> 937a5b2
=======
=======
>>>>>>> parent of 937a5b2... Поднята база SQlite, рабочее состояние
    public void addElement(Element element)
    {
        try(PreparedStatement statement = this.connection.prepareStatement(
                "INSERT INTO Products(`good`, `price`, `category_name`) " +
                        "VALUES(?, ?, ?)")){
            statement.setObject(1, element.good);
            statement.setObject(2, element.price);
            statement.setObject(3, element.category_name);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
<<<<<<< HEAD
>>>>>>> parent of 937a5b2... Поднята база SQlite, рабочее состояние
=======
>>>>>>> parent of 937a5b2... Поднята база SQlite, рабочее состояние
    }

    public void deleteElement(int id){
        try(PreparedStatement statement = this.connection.prepareStatement("DELETE FROM Products WHERE id = ?")){

            statement.setObject(1, id);
            statement.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
