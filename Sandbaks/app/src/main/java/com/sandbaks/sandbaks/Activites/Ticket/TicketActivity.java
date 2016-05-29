package com.sandbaks.sandbaks.Activites.Ticket;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sandbaks.sandbaks.R;
import com.sandbaks.sandbaks.Serializables.Ticket;
import com.sandbaks.sandbaks.Services.MyIntentService;

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
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class TicketActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setElevation(0);
        Bundle b = getIntent().getExtras().getBundle("Ticket");
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, new TicketFragment().newInstance(b)).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_ticket, menu);
        return true;
    }

    public static class TicketFragment extends Fragment {
        Ticket ticket;
        JSONObject user;
        public TicketFragment newInstance(Bundle b) {
            TicketFragment f = new TicketFragment();
            f.setArguments(b);
            return f;
        }

        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container,
                                 Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            // Get Current User
            user = getSavedUser(getActivity());
            View rv = inflater.inflate(R.layout.fragment_ticket, container, false);

            TextView company = (TextView) rv.findViewById(R.id.company);
            TextView attention = (TextView) rv.findViewById(R.id.attention);
            TextView location = (TextView) rv.findViewById(R.id.location);
            TextView description = (TextView) rv.findViewById(R.id.description);
            TextView start = (TextView) rv.findViewById(R.id.start);
            TextView end = (TextView) rv.findViewById(R.id.end);
            TextView supervisor = (TextView) rv.findViewById(R.id.supervisor);
            TextView job = (TextView) rv.findViewById(R.id.job);
            TextView work = (TextView) rv.findViewById(R.id.work);
            TextView afe = (TextView) rv.findViewById(R.id.afe);

            // Set Action Bar Title
            getActivity().setTitle("Ticket");
            if (getArguments() != null) {
                ticket = (Ticket) getArguments().getSerializable("Ticket");
                company.setText(ticket.getCompany());
                attention.setText(ticket.getAttention());
                location.setText(ticket.getLocation());
                description.setText(ticket.getDescription());
                start.setText(ticket.getStart());
                end.setText(ticket.getEnd());
                supervisor.setText(ticket.getSupervisor());
                job.setText(ticket.getJob());
                work.setText(ticket.getWork());
                afe.setText(ticket.getAfe());
            }
            return rv;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.edit) {
                // Edit Ticket
                if (isConnected()) {
                    if (ticket.getId()!=0) {
                        getActivity().finish();
                        Intent i = new Intent(getActivity(), TicketFormActivity.class);
                        Bundle b = new Bundle();
                        b.putSerializable("Ticket", ticket);
                        i.putExtra("Ticket", b);
                        startActivity(i);
                    } else {
                        Toast.makeText(getActivity(), "Upload Ticket First.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Internet Required.", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            if (id == R.id.copy) {
                // Copy Ticket
                if (isConnected()) {
                    if (ticket.getId()!=0) {
                        try {
                            String url = "copyticket.php?ticket=" + ticket.getId()
                                    + "&u=" + user.getString("username")
                                    + "&p=" + user.getString("password");
                            new copyTicket().execute(new URL(MyIntentService.URL + url));
                        } catch (MalformedURLException | JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Upload Ticket First.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Internet Required.", Toast.LENGTH_SHORT).show();
                }

                return true;
            }
            if (id == R.id.delete) {
                // Delete Ticket
                if (isConnected()) {
                    if (ticket.getId()!=0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                        builder.setTitle("Delete Ticket");
                        builder.setMessage("Are you sure?");

                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    String url = "deleteticket.php?ticket=" + ticket.getId()
                                            + "&u=" + user.getString("username")
                                            + "&p=" + user.getString("password");
                                    new deleteTicket().execute(new URL(MyIntentService.URL + url));
                                } catch (MalformedURLException | JSONException e) {
                                    e.printStackTrace();
                                }
                                dialog.dismiss();
                            }
                        });

                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        AlertDialog alert = builder.create();
                        alert.show();
                    } else {
                        Toast.makeText(getActivity(), "Upload Ticket First.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Internet Required.", Toast.LENGTH_SHORT).show();
                }

                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private class deleteTicket extends AsyncTask<URL, Integer, Boolean> {
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
                    System.out.println(result);

                    return jsonResult.get("result") == 0;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                if(aBoolean) {
                    // Save Ticket
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

        private class copyTicket extends AsyncTask<URL, Integer, Boolean> {
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
                    // Save Ticket
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
                    for (int x = 0; x < jsonResult.length(); x++) {
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
                    getActivity().finish();
                }
            }
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

        // Load User Info
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
