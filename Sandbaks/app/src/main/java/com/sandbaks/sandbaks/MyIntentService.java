package com.sandbaks.sandbaks;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MyIntentService extends IntentService {
    public static final String USERNAME = "com.sandbaks.sandbaks.USERNAME";
    public static final String PASSWORD = "com.sandbaks.sandbaks.PASSWORD";
    public static final String URL = "http://159.203.202.117/app/";
    public static final String LOGIN = "com.sandbaks.sandbaks.LOGIN";
    public static final String CONTACTS = "com.sandbaks.sandbaks.CONTACTS";
    public static final String TICKETS = "com.sandbaks.sandbaks.TICKETS";
    public static final String RESULT = "com.sandbaks.sandbaks.RESULT";
    public static final String USER = "com.sandbaks.sandbaks.USER";

    public MyIntentService() { super("MyIntentService"); }

    public static void getUser(Context c, String user, String pass) {
        Intent i = new Intent(c, MyIntentService.class);
        i.setAction(LOGIN);
        i.putExtra(USERNAME, user);
        i.putExtra(PASSWORD, pass);
        c.startService(i);
    }

    public static void getContacts(Context c) {
        Intent i = new Intent(c, MyIntentService.class);
        i.setAction(CONTACTS);
        c.startService(i);
    }


    @Override
    protected void onHandleIntent(Intent i) {
        if (i != null) {
            if (LOGIN.equals(i.getAction())) {
                String user = i.getStringExtra(USERNAME);
                String pass = i.getStringExtra(PASSWORD);
                handleActionGetData(user, pass);
            }
            if (CONTACTS.equals(i.getAction())) {
                handleActionGetContacts();
            }
        }
    }

    private void handleActionGetData(String user, String pass) {
        String result;
        JSONObject jsonResult;
        if (isConnected()) {
            try {
                String urlExtras = "login.php" + "?u=" + user + "&p=" + pass;
                URLConnection conn = new URL(URL + urlExtras).openConnection();
                result = IOUtils.toString(conn.getInputStream());
                jsonResult = new JSONObject(result).getJSONObject("user");
                if (jsonResult.get("id") == 0) {
                    Intent i = new Intent(RESULT);
                    i.putExtra(USER, false);
                    sendBroadcast(i);
                } else {
                    jsonResult.put("username", user);
                    jsonResult.put("password", pass);
                    saveUser(jsonResult);
                    Intent i = new Intent(RESULT);
                    i.putExtra(USER, true);
                    sendBroadcast(i);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        } else {
            Intent i = new Intent(RESULT);
            sendBroadcast(i);
        }
    }

    private void handleActionGetContacts() {
        String result;
        JSONArray jsonResult;
        if (isConnected()) {
            try {
                String urlExtras = "contacts.php";
                URLConnection conn = new URL(URL + urlExtras).openConnection();
                result = IOUtils.toString(conn.getInputStream());
//                jsonResult = new JSONArray(result);
//                ArrayList<Contact> arr = new ArrayList<>();
//                for (int i = 0; i < jsonResult.length() ; i++) {
//                    String n = jsonResult.getJSONObject(i).getString("name");
//                    String p = jsonResult.getJSONObject(i).getString("phone");
//                    String e = jsonResult.getJSONObject(i).getString("email");
//                    int a = Integer.parseInt(jsonResult.getJSONObject(i).getString("access"));
//                    arr.add(new Contact(n, p, e, a));
//                }
                saveContacts(result);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Intent i = new Intent(RESULT);
            sendBroadcast(i);
        }
    }
    private void saveUser(JSONObject object) {
        File external = getExternalFilesDir(null);
        File file = new File(external, "user.txt");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write(object.toString());
            osw.flush();
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Save Data
    public void saveContacts(String jsonArray) {
        Intent i = new Intent(MyReceiver.SAVE_CONTACTS);
        i.putExtra(MyReceiver.CONTACTS, jsonArray);
        sendBroadcast(i);
//        try {
//            FileOutputStream fos = new FileOutputStream(
//                    new File(getApplicationContext().getFilesDir(),"")+
//                            File.separator+"contacts");
//            ObjectOutputStream os = new ObjectOutputStream(fos);
//            os.writeObject(arr);
//            os.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    // Check to see if internet is available before calling for URL
    public boolean isConnected() {
        ConnectivityManager mgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = mgr.getActiveNetworkInfo();
        if (netInfo != null) {
            if (netInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                return true;
            } else if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            }
        } else {
            return false;
        }
        return false;
    }
}