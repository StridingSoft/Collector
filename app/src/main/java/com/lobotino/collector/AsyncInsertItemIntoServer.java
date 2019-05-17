package com.lobotino.collector;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AsyncInsertItemIntoServer extends AsyncTask<Bitmap, Void, Void> {

    private int id, secId;
    private String name, description, date;

    public AsyncInsertItemIntoServer(int id, int secId, String name, String description, String date) {
        this.id = id;
        this.secId = secId;
        this.name = name;
        this.date = date;
        this.description = description;
    }

    private long imgNormalSize, imgMiniSize;

    @Override
    protected Void doInBackground(Bitmap... bitmaps) {
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            Connection con = null;
            PreparedStatement prepared = null;
            try {
                con = DriverManager.getConnection(DbHandler.MSSQL_DB, DbHandler.MSSQL_LOGIN, DbHandler.MSSQL_PASS);
                if (con != null) {
                    String SQL = "INSERT into Items VALUES(?,?,?,?,?,?,?)";
                    prepared = con.prepareStatement(SQL);


                    Bitmap bitmapNormalSize = bitmaps[0];
                    ByteArrayOutputStream outputStreamNormal = new ByteArrayOutputStream();
                    bitmapNormalSize.compress(Bitmap.CompressFormat.PNG, 0, outputStreamNormal);
                    byte[] dataNormal = outputStreamNormal.toByteArray();
                    imgNormalSize = dataNormal.length;

                    Bitmap bitmapMiniSize = bitmaps[1];
                    ByteArrayOutputStream outputStreamMini = new ByteArrayOutputStream();
                    bitmapMiniSize.compress(Bitmap.CompressFormat.WEBP, 0, outputStreamMini);
                    byte[] dataMini = outputStreamMini.toByteArray();
                    imgMiniSize = dataMini.length;

                    prepared.setInt(1, id);
                    prepared.setString(2, name);
                    prepared.setString(3, description);
                    prepared.setInt(4, secId);
                    prepared.setBytes(5, dataNormal);
                    prepared.setBytes(6, dataMini);
                    prepared.setString(7, date);
                    prepared.addBatch();

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
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);
        Log.d("AsyncDownload", "SUCCES DOWNLOAD №" + id + " with normal size " + imgNormalSize + " Bytes and with mini size " + imgMiniSize + " Bytes.");
    }
}
