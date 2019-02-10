package com.lobotino.collector;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;



public class AsyncClearTable extends AsyncTask<String, Void, Void> {

    private String SQL = "DELETE FROM ";

    @Override
    protected Void doInBackground(String... strings) {
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            Connection con = null;
            PreparedStatement prepared = null;
            try {
                con = DriverManager.getConnection(DbHandler.MSSQL_DB, DbHandler.MSSQL_LOGIN, DbHandler.MSSQL_PASS);
                if (con != null) {
                    prepared = con.prepareStatement(SQL + strings[0]);

                    prepared.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
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
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.d("MDB", "SUCCES CLEAR");
    }
}
