package com.lobotino.collector.async_tasks;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lobotino.collector.R;
import com.lobotino.collector.fragments.ProfileFragment;
import com.lobotino.collector.fragments.UsersListFragment;
import com.lobotino.collector.utils.DbHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.lobotino.collector.activities.MainActivity.dbHandler;



public class AsyncDrawAllUsers extends AsyncTask<Void, String, Void>{

    private Context context;
    private LinearLayout layout;
    private int itemId, whiteColor, lineColor;
    private UsersListFragment.Users usersType;
    private LinearLayout.LayoutParams textViewParams, lineParams;
    private FragmentManager fragmentManager;
    private boolean isEmpty = true;
    private int lineHeight;

    public AsyncDrawAllUsers(Context context, LinearLayout layout, FragmentManager fragmentManager, UsersListFragment.Users usersType, int itemId, int lineHeight) {
        this.context = context;
        this.layout = layout;
        this.usersType = usersType;
        this.itemId = itemId;
        this.fragmentManager = fragmentManager;
        this.lineHeight = lineHeight;

        whiteColor = Color.parseColor("#ffffff");
        lineColor = Color.parseColor("#003100");

        textViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textViewParams.gravity = Gravity.CENTER_HORIZONTAL;

        lineParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, lineHeight);
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

                            String[] paramsList = new String[]{userId + "", userName};
                            publishProgress(paramsList);
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
        if(isEmpty)
        {
            isEmpty = false;
            TextView tvTitle = new TextView(context);
            tvTitle.setText("Список пользователей");
            tvTitle.setTextColor(whiteColor);
            tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
            tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
            tvTitle.setLayoutParams(textViewParams);
            layout.addView(tvTitle);

            View line = new View(context);
            line.setBackgroundColor(whiteColor);
            line.setLayoutParams(lineParams);
            layout.addView(line);
        }


        final int userId = Integer.parseInt(values[0]);
        String userName = values[1];
        TextView tvUser = new TextView(context);
        tvUser.setText(userName);
        tvUser.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 28);
        tvUser.setTextColor(whiteColor);
        tvUser.setLayoutParams(textViewParams);
        tvUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = fragmentManager
                        .beginTransaction();

                Fragment profileFragment = new ProfileFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("user_id", userId);
                profileFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.content_frame, profileFragment);
                fragmentTransaction.commit();
            }
        });
        layout.addView(tvUser);

        View line = new View(context);
        line.setBackgroundColor(whiteColor);
        line.setLayoutParams(lineParams);
        layout.addView(line);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if(isEmpty) {
            layout.removeAllViews();
            TextView tvEmpty = new TextView(context);
            tvEmpty.setText("К сожалению, нет пользователей, соответствующих данному запросу");
            tvEmpty.setTextColor(whiteColor);
            tvEmpty.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 23);
            tvEmpty.setTypeface(Typeface.DEFAULT_BOLD);
            tvEmpty.setGravity(Gravity.CENTER);
            textViewParams.setMargins(0,lineHeight * 10,0,0);
            tvEmpty.setLayoutParams(textViewParams);
            layout.addView(tvEmpty);
        }
    }
}
