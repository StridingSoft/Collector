package com.lobotino.collector.async_tasks.add_elements;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.lobotino.collector.utils.DbHandler;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class AsyncAddItemToServer extends AsyncTask<Bitmap, Void, Void> {

    private int secId, collectionId;
    private String name, description, date;


    public AsyncAddItemToServer(int secId, int collectionId, String name, String description, String date) {
        this.secId = secId;
        this.name = name;
        this.date = date;
        this.description = description;
        this.collectionId = collectionId;
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
                    String SQL = "INSERT into Items(" + DbHandler.KEY_SECTION_ID + ", " + DbHandler.KEY_NAME + ", " + DbHandler.KEY_DESCRIPTION + ", " +DbHandler.KEY_IMAGE
                            + ", " + DbHandler.KEY_MINI_IMAGE + ", " + DbHandler.KEY_DATE_OF_CHANGE + ", " + DbHandler.KEY_COLLECTION_ID + ") VALUES(?,?,?,?,?,?,?)";
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

                    prepared.setInt(1, secId);
                    prepared.setString(2, name);
                    prepared.setString(3, description);
                    prepared.setBytes(4, dataNormal);
                    prepared.setBytes(5, dataMini);
                    prepared.setString(6, date);
                    prepared.setInt(7, collectionId);
                    prepared.addBatch();

                    prepared.executeUpdate();
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
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);
        Log.d("AsyncDownload", "SUCCES DOWNLOAD with normal size " + imgNormalSize + " Bytes and with mini size " + imgMiniSize + " Bytes.");
    }
}
