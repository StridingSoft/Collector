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
