package com.lobotino.collector.async_tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lobotino.collector.fragments.UsersListFragment;
import com.lobotino.collector.utils.DbHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.lobotino.collector.activities.MainActivity.dbHandler;



public class AsyncDrawAllUsers extends AsyncTask<Void, String, Void>{

    private Context context;
    private RelativeLayout layout;
    private int lastUserId, itemId, textViewId;
    private UsersListFragment.Users usersType;

    public AsyncDrawAllUsers(Context context, RelativeLayout layout, UsersListFragment.Users usersType, int itemId, int textViewId) {
        this.context = context;
        this.layout = layout;
        this.usersType = usersType;
        this.itemId = itemId;
        this.textViewId = textViewId;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        layout.removeAllViews();

    }

    @Override
    protected Void doInBackground(Void... voids) {
        Connection connection = dbHandler.getConnection(context);

        Statement st = null;
        ResultSet rs = null;
        Statement st1 = null;
        ResultSet rs1 = null;
        try {
            if (connection != null) {
                String status = usersType.equals(UsersListFragment.Users.TRADED_PEOPLE) ? DbHandler.STATUS_TRADE : DbHandler.STATUS_WISH;
                String SQL = "SELECT " + DbHandler.KEY_USER_ID + " FROM " + DbHandler.TABLE_USERS_ITEMS + " WHERE " + DbHandler.KEY_ITEM_ID + " = " + itemId +
                        " AND " + DbHandler.KEY_ITEM_STATUS + " LIKE '" + status + "'";

                st = connection.createStatement();
                rs = st.executeQuery(SQL);
                if (rs != null) {
                    while (rs.next()) {
                        int userId = rs.getInt(1);

                        SQL = "SELECT " + DbHandler.KEY_USER_NAME + " FROM " + DbHandler.TABLE_USERS + " WHERE " + DbHandler.KEY_ID + " = " + userId;
                        st1 = connection.createStatement();
                        rs1 = st1.executeQuery(SQL);
                        if (rs1 != null) {
                            rs1.next();
                            String userName = rs1.getString(1);
                            rs1.close();

                            publishProgress(userName);
                        }
                        st1.close();
                    }
                    rs.close();
                }
                st.close();

            }

        } catch (SQLException e) {
            e.printStackTrace();
            try {
            if(st != null)
                st.close();
            if(rs != null)
                rs.close();
            if(st1 != null)
                st1.close();
            if(rs1 != null)
                rs1.close();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        String userName = values[0];
        TextView tvUser = new TextView(context);
        tvUser.setText(userName);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.setMargins();
        //TO DO Анимация загрузки каждой картинки + красивый лист пользователей на обмен!
        if(lastUserId != 0)
            params.addRule(RelativeLayout.BELOW, lastUserId);
        else
            params.addRule(RelativeLayout.BELOW, textViewId);
        layout.addView(tvUser);
        lastUserId = tvUser.getId();
    }
}
