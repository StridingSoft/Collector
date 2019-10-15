package com.lobotino.collector.async_tasks.add_elements;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.lobotino.collector.utils.DbHandler;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class AsyncAddSectionToServer extends AsyncTask<Void, Void, Integer> {

    private int collectionId;
    private String name, description;

    public AsyncAddSectionToServer(int collectionId, String name, String description) {
        this.collectionId = collectionId;
        this.name = name;
        this.description = description;
    }

    public AsyncAddSectionToServer(int collectionId, String name) {
        this.collectionId = collectionId;
        this.name = name;
        this.description = "";
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
                    String SQL = "INSERT into " + DbHandler.TABLE_SECTIONS +"(" + DbHandler.KEY_COLLECTION_ID + ", " + DbHandler.KEY_NAME + ", " + DbHandler.KEY_DESCRIPTION +  ") VALUES(?,?,?)";
                    prepared = con.prepareStatement(SQL);

                    prepared.setInt(1, collectionId);
                    prepared.setString(2, name);
                    prepared.setString(3, description);
                    prepared.addBatch();

                    prepared.executeUpdate();

                    Statement st = con.createStatement();
                    SQL =   "SELECT " + DbHandler.KEY_ID +
                            " FROM " + DbHandler.TABLE_SECTIONS +
                            " WHERE " + DbHandler.KEY_NAME + " LIKE '" + name + "'" +
                            " AND " + DbHandler.KEY_DESCRIPTION + " LIKE '" + description + "'" +
                            " AND " + DbHandler.KEY_COLLECTION_ID + " = " + collectionId;
                    ResultSet rs = st.executeQuery(SQL);
                    if(rs != null) {
                        if (rs.next()) {
                            int sectionId = rs.getInt(1);
                            return sectionId;
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
        Log.d("AsyncSectionDownload", "SECTION SUCCESS DOWNLOADED" );
    }
}
