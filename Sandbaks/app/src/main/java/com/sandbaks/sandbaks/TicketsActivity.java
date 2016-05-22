package com.sandbaks.sandbaks;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
            listView = (ListView) rv.findViewById(R.id.listView);
            ArrayList<Ticket> arr = readData();
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
                            try {
                                new getTickets().execute(new URL(MyIntentService.URL + "tickets.php"));
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
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
                        Float h = Float.valueOf(jsonResult.getJSONObject(x).getString("hours"));
                        int u = Integer.parseInt(jsonResult.getJSONObject(x).getString("status"));
                        arr.add(new Ticket(c, a, l, s, d, y, t, e, f, j, w, h, u));
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
                    ArrayList<Ticket> arr = readData();
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
                    refresher.setRefreshing(false);
                }
            }
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            ArrayList<Ticket> arr = readData();
            Collections.sort(arr, new Comparator<Ticket>() {
                @Override
                public int compare(Ticket one, Ticket two) {
                    String id1 = String.valueOf(one.getStatus());
                    String id2 = String.valueOf(two.getStatus());

                    return id2.compareTo(id1);
                }
            });
            listView.setAdapter(new TicketAdapter(getActivity(), arr));
//            listView.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    System.out.println("ListView");
//                    return false;
//                }
//            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.refresh) {
                // Get Tickets
                MyIntentService.getTickets(getActivity());
                listView.invalidateViews();
                return true;
            }
            if (id == R.id.add) {
                // Add New Ticket
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
    }

    @Override
    public void onResume() {
        // Load List View
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new TicketsFragment().newInstance(this)).commit();
        super.onResume();
    }
}