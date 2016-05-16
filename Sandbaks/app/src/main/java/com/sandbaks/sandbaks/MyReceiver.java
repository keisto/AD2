package com.sandbaks.sandbaks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class MyReceiver extends BroadcastReceiver {
    public static final String SAVE_CONTACTS = "com.sandbaks.sandbaks.SAVE_CONTACTS";
    public static final String CONTACTS = "com.sandbaks.sandbaks.CONTACTS";
    public static final String READ_CONTACTS = "com.sandbaks.sandbaks.READ_CONTACTS";
    public static final String C_NAME = "com.sandbaks.sandbaks.C_NAME";
    public static final String C_PHONE = "com.sandbaks.sandbaks.C_PHONE";
    public static final String C_EMAIL = "com.sandbaks.sandbaks.C_PHONE";
    public static final String C_ACCESS = "com.sandbaks.sandbaks.C_ACCESS";
    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent i) {
        if (i.getAction().equals(SAVE_CONTACTS)) {
            if (i.hasExtra(CONTACTS)) {
                JSONArray jsonResult = null;
                ArrayList<Contact> arr = new ArrayList<>();
                try {
                    jsonResult = new JSONArray(i.getStringExtra(CONTACTS));
                    for (int x = 0; x < jsonResult.length() ; x++) {
                        String n = jsonResult.getJSONObject(x).getString("name");
                        String p = jsonResult.getJSONObject(x).getString("phone");
                        String e = jsonResult.getJSONObject(x).getString("email");
                        int a = Integer.parseInt(jsonResult.getJSONObject(x).getString("access"));
                        arr.add(new Contact(n, p, e, a));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                FileSystem fs = new FileSystem(context);
                fs.saveContacts(arr);
                Toast.makeText(context, "Contacts Saved", Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressWarnings("unchecked")
    // Filesystem Save/Read
    public static class FileSystem {
        private Context mAppContext;
        private File contactsFile;
        private File ticketsFile;

        public FileSystem(Context context) {
            mAppContext = context.getApplicationContext();
            contactsFile = new File(mAppContext.getFilesDir(), "contacts");
            ticketsFile = new File(mAppContext.getFilesDir(), "tickets");
        }

        // Save Contacts
        public void saveContacts(ArrayList<Contact> arr) {
            try {
                // Load Data and Append New Data
                ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(contactsFile));
                os.writeObject(arr);
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Read Contacts
        public ArrayList readContacts() {
            ArrayList<Contact> arr = null;
            ObjectInputStream is;
            try {
                is = new ObjectInputStream(new FileInputStream(contactsFile));
                arr = (ArrayList<Contact>) is.readObject();
                is.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return arr;
        }
    }
}
