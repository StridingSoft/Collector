package com.lobotino.collector.async_tasks.add_elements;

import android.os.AsyncTask;
import android.util.Log;

import com.lobotino.collector.utils.DbHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by Олег on 20.07.2019.
 */

public class AsyncAddCollectionToServer extends AsyncTask<Void,Void,Integer> {

    String name, description;

    public AsyncAddCollectionToServer(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            Connection con = null;
            PreparedStatement prepared = null;
            try {
                con = DriverManager.getConnection(DbHandler.MSSQL_DB, DbHandler.MSSQL_LOGIN, DbHandler.MSSQL_PASS);
                if (con != null) {
                    String SQL = "INSERT into " + DbHandler.TABLE_COLLECTIONS +"(" + DbHandler.KEY_NAME + ", " + DbHandler.KEY_DESCRIPTION +  ") VALUES(?,?)";
                    prepared = con.prepareStatement(SQL);

                    prepared.setString(1, name);
                    prepared.setString(2, description);
                    prepared.addBatch();

                    prepared.executeUpdate();

                    Statement st = con.createStatement();
                    SQL =   "SELECT " + DbHandler.KEY_ID +
                            " FROM " + DbHandler.TABLE_COLLECTIONS +
                            " WHERE " + DbHandler.KEY_NAME + " LIKE '" + name + "'" +
                            " AND " + DbHandler.KEY_DESCRIPTION + " LIKE '" + description + "'";
                    ResultSet rs = st.executeQuery(SQL);
                    if(rs != null) {
                        if (rs.next()) {
                            int collectionId = rs.getInt(1);
                            return collectionId;
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }finally {
                try {
                    if (prepared != null) prepared.close();
                    if (con != null) con.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Integer v) {
        super.onPostExecute(v);
        Log.d("AsyncCollectionDownload", "COLLECTION SUCCESS DOWNLOADED" );
    }
}
