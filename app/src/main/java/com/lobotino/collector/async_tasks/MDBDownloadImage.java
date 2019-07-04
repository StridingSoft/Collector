package com.lobotino.collector.async_tasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.lobotino.collector.utils.DbHandler;
import com.lobotino.collector.activities.MainActivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

//РАБОТАЕТ!
public class MDBDownloadImage extends AsyncTask<Integer, Void, Void> {
    private ImageView imageView = null;

    int id, secId, pictureSize;
    String name, desc = "";
    byte[] blob;
    boolean justDownload = false;

    Context context;


    public MDBDownloadImage(ImageView imageView, int pictureSize, Context context, boolean justDownload) {
        this.imageView = imageView;
        this.context = context;
        this.pictureSize = pictureSize;
        this.justDownload = justDownload;
    }

    @Override
    protected Void doInBackground(Integer... ids) {

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            Connection con = null;
            Statement st = null;
            ResultSet rs = null;
            try {

                con = DriverManager.getConnection(DbHandler.MSSQL_DB, DbHandler.MSSQL_LOGIN, DbHandler.MSSQL_PASS);
                if (con != null) {
                    st = con.createStatement();
                    String offer = "SELECT * FROM Items WHERE Id = '" + ids[0] + "'";
                    rs = st.executeQuery(offer);
                    if (rs != null) {
                        rs.next();
                        id = rs.getInt(1);
                        name = rs.getString(2);
                        desc = rs.getString(3);
                        secId = rs.getInt(4);
                        blob = rs.getBytes(5);
                    }
                }
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (st != null) st.close();
                    if (con != null) con.close();
                } catch (java.sql.SQLException e) {
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
        if(blob != null)
        {
            SQLiteDatabase mDb;
            try {
                mDb = MainActivity.dbHandler.getDataBase();
            } catch (SQLException mSQLException) {
                throw mSQLException;
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(DbHandler.KEY_ID, id);
            contentValues.put(DbHandler.KEY_NAME, name);
            contentValues.put(DbHandler.KEY_DESCRIPTION, desc);
            contentValues.put(DbHandler.KEY_SECTION_ID, secId);
            contentValues.put(DbHandler.KEY_IMAGE, blob);

            mDb.insert(DbHandler.TABLE_ITEMS, null, contentValues);

            if(imageView != null)
            {
                AsyncDownloadScaledImage downloadScaledImage = new AsyncDownloadScaledImage(id, pictureSize, context);
                downloadScaledImage.execute(imageView);
            }
        }
    }
}
