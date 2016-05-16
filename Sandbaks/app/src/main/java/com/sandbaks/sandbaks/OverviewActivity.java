package com.sandbaks.sandbaks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class OverviewActivity extends AppCompatActivity {
    public static final String URL = "http://159.203.202.117/app/timecard.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setElevation(0);
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, new OverviewFragment().newInstance()).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout, menu);
        return true;
    }

    public static class OverviewFragment extends Fragment {
        JSONObject user;
        TextView lastweek;
        TextView thisweek;
        Button contacts;
        Button tickets;
        public OverviewFragment newInstance() {
            return new OverviewFragment();
        }

        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container,
                                 Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            // Load User
            user = getSavedUser(getActivity());
            // Set Action Bar Title
            try {
                getActivity().setTitle("Hello " + user.getString("firstname") + " " + user.getString("lastname") + ",");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // Get User's Timecard
            try {
                new getUserTimecard().execute(new URL(URL+"?uid="+user.getString("id")));
            } catch (MalformedURLException | JSONException e) {
                e.printStackTrace();
            }
            // Get Contacts if first time
            File file = new File(getActivity().getFilesDir(), "contacts");
            if (!file.exists()) {
                MyIntentService.getContacts(getActivity());
            }

            View rv = inflater.inflate(R.layout.fragment_overview, container, false);

            thisweek = (TextView) rv.findViewById(R.id.current);
            lastweek = (TextView) rv.findViewById(R.id.last);
            contacts = (Button) rv.findViewById(R.id.contacts);
            tickets = (Button) rv.findViewById(R.id.tickets);

            contacts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent iContact = new Intent(getActivity(), ContactsActivity.class);
                    startActivity(iContact);
                }
            });
            return rv;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.logout) {
                File external = getActivity().getExternalFilesDir(null);
                File file = new File(external, "user.txt");
                if (file.exists()) {
                    file.delete();
                    getActivity().finish();
                }
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        // Get JSONObeject form URL
        private class getUserTimecard extends AsyncTask<java.net.URL, Integer, JSONObject> {
            @Override
            protected JSONObject doInBackground(java.net.URL... urls) {
                String result = null;
                URLConnection connection;
                for (URL queryURL : urls) {
                    try {
                        connection = queryURL.openConnection();
                        result = IOUtils.toString(connection.getInputStream());
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                JSONObject timecard = null;
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(result != null ? result : null);
                    timecard = jsonObject.getJSONObject("timecard");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return timecard;
            }

            protected void onPostExecute(JSONObject timecard) {
                if (timecard != null) {
                    try {
                        thisweek.setText(timecard.getString("current"));
                        lastweek.setText(timecard.getString("last"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public JSONObject getSavedUser(Context context) {
            String result = "";
            File external = context.getExternalFilesDir(null);
            File file = new File(external, "user.txt");
            try {
                FileInputStream fin= new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fin);
                char[] data = new char[2048];
                int size;
                try {
                    while ((size = isr.read(data))>0){
                        String readData = String.copyValueOf(data,0,size);
                        result += readData;
                        data = new char[2048];
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            JSONObject object = null;
            try {
                object = new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return object;
        }
    }
}