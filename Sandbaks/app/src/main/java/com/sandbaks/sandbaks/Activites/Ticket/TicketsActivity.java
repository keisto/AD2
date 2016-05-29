package com.sandbaks.sandbaks.Activites.Ticket;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.sandbaks.sandbaks.Activites.Contact.ContactActivity;
import com.sandbaks.sandbaks.Activites.Main.LoginActivity;
import com.sandbaks.sandbaks.Adapters.TicketAdapter;
import com.sandbaks.sandbaks.Serializables.Contact;
import com.sandbaks.sandbaks.Services.MyIntentService;
import com.sandbaks.sandbaks.R;
import com.sandbaks.sandbaks.Serializables.Ticket;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TicketsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_tickets);
        getSupportActionBar().setElevation(0);
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, new TicketsFragment().newInstance(this)).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tickets, menu);
        return true;
    }

    public static class TicketsFragment extends Fragment {
        private Context mContext;
        private ListView listView;
        TicketAdapter adapter;
        SwipeRefreshLayout refresher;
        JSONObject user;
        int ticketId = 0;

        public TicketsFragment newInstance(Context context) {
            mContext = context.getApplicationContext();
            return new TicketsFragment();
        }


        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            getActivity().setTitle("Tickets");

            View rv = inflater.inflate(R.layout.fragment_tickets, container, false);
            user = getSavedUser(getActivity());
            listView = (ListView) rv.findViewById(R.id.listView);
            ArrayList<Ticket> arr = readData();
            ArrayList<Ticket> stored = readStored();
            if (stored.size()>0){
                for (int i = 0; i < stored.size(); i++) {
                    arr.add(stored.get(i));
                }
            }
            Collections.sort(arr, new Comparator<Ticket>() {
                @Override
                public int compare(Ticket one, Ticket two) {
                    String id1 = String.valueOf(one.getStatus());
                    String id2 = String.valueOf(two.getStatus());
                    return id2.compareTo(id1);
                }
            });
            adapter = new TicketAdapter(getActivity(), arr);
            listView.setAdapter(adapter);
            refresher = (SwipeRefreshLayout) rv.findViewById(R.id.refresher);
            refresher.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            // Get Tickets
                            if (isConnected()) {
                                ArrayList<Ticket> stored = readStored();
                                if (stored.size() > 0) {
                                    // Upload Stored
                                    try {
                                        String p = user.getString("password");
                                        String u = user.getString("username");
                                        String url = "http://159.203.202.117/app/login.php?u=" + u + "&p=" + p;
                                        new verifyUser().execute(new URL(url));
                                    } catch (JSONException | MalformedURLException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    try {
                                        new getTickets().execute(new URL(MyIntentService.URL + "tickets.php"));
                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                refresher.setRefreshing(false);
                                Toast.makeText(getActivity(), "No Internet Detected",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );
            return rv;
        }

        // Get Tickets form URL
        private class getTickets extends AsyncTask<URL, Integer, Boolean> {
            @Override
            protected Boolean doInBackground(URL... urls) {
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
                JSONArray jsonResult = null;
                ArrayList<Ticket> arr = new ArrayList<>();
                try {
                    jsonResult = new JSONArray(result);
                    for (int x = 0; x < jsonResult.length() ; x++) {
                        int i = Integer.parseInt(jsonResult.getJSONObject(x).getString("id"));
                        String c = jsonResult.getJSONObject(x).getString("company");
                        String a = jsonResult.getJSONObject(x).getString("attention");
                        String w = jsonResult.getJSONObject(x).getString("work");
                        String f = jsonResult.getJSONObject(x).getString("afe");
                        String j = jsonResult.getJSONObject(x).getString("job");
                        String l = jsonResult.getJSONObject(x).getString("location");
                        String s = jsonResult.getJSONObject(x).getString("supervisor");
                        String t = jsonResult.getJSONObject(x).getString("start");
                        String e = jsonResult.getJSONObject(x).getString("end");
                        String d = jsonResult.getJSONObject(x).getString("description");
                        String y = jsonResult.getJSONObject(x).getString("createdby");
                        Float h = (float) 0;
                        if (jsonResult.getJSONObject(x).getString("hours") != null) {
                            h = Float.valueOf(jsonResult.getJSONObject(x).getString("hours"));
                        }
                        int u = Integer.parseInt(jsonResult.getJSONObject(x).getString("status"));
                        arr.add(new Ticket(i, c, a, l, s, d, y, t, e, f, j, w, h, u));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
                File ticketsFile = new File(getActivity().getApplicationContext()
                        .getFilesDir(), "tickets");
                try {
                    // Load Data and Append New Data
                    ObjectOutputStream os = new ObjectOutputStream(
                            new FileOutputStream(ticketsFile));
                    os.writeObject(arr);
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                if(aBoolean) {
                    final ArrayList<Ticket> arr = readData();
                    ArrayList<Ticket> stored = readStored();
                    if (stored.size()>0){
                        for (int i = 0; i < stored.size(); i++) {
                            arr.add(stored.get(i));
                        }
                    }
                    Collections.sort(arr, new Comparator<Ticket>() {
                        @Override
                        public int compare(Ticket one, Ticket two) {
                            String id1 = String.valueOf(one.getStatus());
                            String id2 = String.valueOf(two.getStatus());

                            return id2.compareTo(id1);
                        }
                    });
                    adapter = new TicketAdapter(getActivity(), arr);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent i = new Intent(getActivity(), TicketActivity.class);
                            Bundle b = new Bundle();
                            b.putSerializable("Ticket", arr.get(position));
                            i.putExtra("Ticket", b);
                            startActivity(i);
                        }
                    });
                    refresher.setRefreshing(false);
                }
            }
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            final ArrayList<Ticket> arr = readData();
            ArrayList<Ticket> stored = readStored();
            if (stored.size()>0){
                for (int i = 0; i < stored.size(); i++) {
                    arr.add(stored.get(i));
                }
            }
            Collections.sort(arr, new Comparator<Ticket>() {
                @Override
                public int compare(Ticket one, Ticket two) {
                    String id1 = String.valueOf(one.getStatus());
                    String id2 = String.valueOf(two.getStatus());

                    return id2.compareTo(id1);
                }
            });
            listView.setAdapter(new TicketAdapter(getActivity(), arr));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent i = new Intent(getActivity(), TicketActivity.class);
                    Bundle b = new Bundle();
                    b.putSerializable("Ticket", arr.get(position));
                    i.putExtra("Ticket", b);
                    startActivity(i);
                }
            });

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.add) {
                // Add New Ticket
                Intent i = new Intent(getActivity(), TicketFormActivity.class);
                startActivity(i);
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        // Read Data
        public ArrayList readData() {
            ArrayList<Ticket> arr = new ArrayList<>();
            try {
                String fileString = new File(getActivity().getApplicationContext()
                        .getFilesDir(), "") + File.separator + "tickets";
                File file = new File(fileString);
                if (file.exists()) {
                    FileInputStream fis = new FileInputStream(file);
                    ObjectInputStream is = new ObjectInputStream(fis);
                    arr = (ArrayList<Ticket>) is.readObject();
                    is.close();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return arr;
        }

        // Check to see if internet is available before calling for URL
        public boolean isConnected() {
            ConnectivityManager mgr = (ConnectivityManager)
                    getActivity().getSystemService(CONNECTIVITY_SERVICE);
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

        // Read Data
        public ArrayList readStored() {
            ArrayList<Ticket> arr = new ArrayList<>();
            try {
                String fileString = new File(getActivity().getApplicationContext()
                        .getFilesDir(), "") + File.separator + "stored";
                File file = new File(fileString);
                if (file.exists()) {
                    FileInputStream fis = new FileInputStream(file);
                    ObjectInputStream is = new ObjectInputStream(fis);
                    arr = (ArrayList<Ticket>) is.readObject();
                    is.close();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return arr;
        }

        private class verifyUser extends AsyncTask<URL, Integer, Boolean> {
            @Override
            protected Boolean doInBackground(URL... urls) {
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
                JSONObject jsonResult = null;
                try {
                    jsonResult = new JSONObject(result).getJSONObject("user");
                    return jsonResult.get("id") != 0;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                if(aBoolean) {
                    ArrayList<Ticket> stored = readStored();
                    if (stored.size()>0) {
                        for (int i = 0; i < stored.size(); i++) {

                            // Upload Ticket
                            String c = stored.get(i).getCompany().trim();
                            String a = stored.get(i).getAttention().trim();
                            String w = stored.get(i).getWork().trim();
                            String f = stored.get(i).getAfe().trim();
                            String l = stored.get(i).getLocation().trim();
                            String j = stored.get(i).getJob().trim();
                            String u = stored.get(i).getSupervisor().trim();
                            String d = stored.get(i).getDescription().trim();
                            String s = stored.get(i).getStart().trim();
                            String e = stored.get(i).getEnd().trim();
                            String status = "0";
                            if (!c.equals("") && !a.equals("") && !l.equals("") && !j.equals("")
                                    && !u.equals("") && !d.equals("") && !s.equals("") && !e.equals("")) {
                                if (!w.equals("") || !f.equals("")) {
                                    // Complete Ticket
                                    c = stored.get(i).getCompany().replace(" ", "%20");
                                    a = stored.get(i).getAttention().replace(" ", "%20");
                                    w = stored.get(i).getWork().replace(" ", "%20");
                                    f = stored.get(i).getAfe().replace(" ", "%20");
                                    l = stored.get(i).getLocation().replace(" ", "%20");
                                    j = stored.get(i).getJob().replace(" ", "%20");
                                    u = stored.get(i).getSupervisor().replace(" ", "%20");
                                    d = stored.get(i).getDescription().replace(" ", "%20");
                                    s = stored.get(i).getStart().replace(" ", "%20");
                                    e = stored.get(i).getEnd().replace(" ", "%20");
                                    status = "2";
                                }
                            } else {
                                // Not Complete
                                c = stored.get(i).getCompany().replace(" ", "%20");
                                a = stored.get(i).getAttention().replace(" ", "%20");
                                w = stored.get(i).getWork().replace(" ", "%20");
                                f = stored.get(i).getAfe().replace(" ", "%20");
                                l = stored.get(i).getLocation().replace(" ", "%20");
                                j = stored.get(i).getJob().replace(" ", "%20");
                                u = stored.get(i).getSupervisor().replace(" ", "%20");
                                d = stored.get(i).getDescription().replace(" ", "%20");
                                s = stored.get(i).getStart().replace(" ", "%20");
                                e = stored.get(i).getEnd().replace(" ", "%20");
                                status = "1";
                            }
                            String url = null;
                            try {
                                String username = user.getString("firstname") + "%20"
                                        + user.getString("lastname");
                                url = "http://159.203.202.117/app/newticket.php"
                                        + "?c=" + c + "&a=" + a + "&w=" + w + "&f=" + f + "&l="
                                        + l + "&j=" + j + "&u=" + u + "&d=" + d + "&s=" + s
                                        + "&e=" + e + "&user=" + username + "&id=" + user.getString("id")
                                        + "&status=" + status + "&ticket=" + ticketId;
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                            try {
                                new uploadTicket().execute(new URL(url));
                            } catch (MalformedURLException error) {
                                error.printStackTrace();
                            }
                        }
                        // All Done Delete Stored Tickets
                        String fileString = new File(getActivity().getApplicationContext()
                                .getFilesDir(), "") + File.separator + "stored";
                        File file = new File(fileString);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                } else {
                    // Go to Login
                    returnLogin();
                }
            }
        }

        public void returnLogin() {
            // Go to Login
            Intent i = new Intent(getActivity(), LoginActivity.class);
            getActivity().startActivity(i);
        }

        private class uploadTicket extends AsyncTask<URL, Integer, Boolean> {
            @Override
            protected Boolean doInBackground(URL... urls) {
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
                JSONObject jsonResult = null;
                try {
                    jsonResult = new JSONObject(result);
                    return jsonResult.get("result") != 0;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                if(aBoolean) {
                    // Load New Tickets
                    if(isConnected()) {
                        try {
                            new getTickets().execute(new URL(MyIntentService.URL + "tickets.php"));
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
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

    @Override
    public void onResume() {
        // Load List View
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new TicketsFragment().newInstance(this)).commit();
        super.onResume();
    }
}