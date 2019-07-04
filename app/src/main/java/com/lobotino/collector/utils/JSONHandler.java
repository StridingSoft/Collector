package com.lobotino.collector.utils;

import android.content.Context;

import com.google.gson.Gson;
import com.lobotino.collector.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class JSONHandler {

    public static boolean exportToJSON(Context context, CurrentUser currentUser) {

        Gson gson = new Gson();

        String jsonString = gson.toJson(currentUser);
        FileOutputStream fileOutputStream = null;

        String fileName = context.getString(R.string.user_info);

        try {
            File f = context.getFileStreamPath(fileName);
            if (f.exists()) {
                f.delete();
                f.createNewFile();
            }

            fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fileOutputStream.write(jsonString.getBytes());

            DbHandler.USER_ID = currentUser.getId();
            DbHandler.USER_LOGIN = currentUser.getLogin();
            DbHandler.USER_PASS = currentUser.getPass();
            DbHandler.USER_EMAIL = currentUser.getEmail();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    public static CurrentUser importFromJSON(Context context) {

        InputStreamReader streamReader = null;
        FileInputStream fileInputStream = null;
        try{
            String fileName = context.getString(R.string.user_info);
            fileInputStream = context.openFileInput(fileName);
            streamReader = new InputStreamReader(fileInputStream);
            Gson gson = new Gson();
            CurrentUser currentUser = gson.fromJson(streamReader, CurrentUser.class);
            return currentUser;
        }
        catch (FileNotFoundException ex){
            ex.printStackTrace();
        }
        finally {
            if (streamReader != null) {
                try {
                    streamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static class CurrentUser {
        private int id;
        private String login, pass, email;

        public CurrentUser(int id, String login, String pass, String email) {
            this.id = id;
            this.login = login;
            this.pass = pass;
            this.email = email;
        }

        public int getId() {
            return id;
        }

        public String getLogin() {
            return login;
        }

        public String getPass() {
            return pass;
        }

        public String getEmail() {
            return email;
        }
    }
}
