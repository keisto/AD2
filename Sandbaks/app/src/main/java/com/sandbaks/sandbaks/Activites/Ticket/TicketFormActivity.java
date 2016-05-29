package com.sandbaks.sandbaks.Activites.Ticket;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.sandbaks.sandbaks.Activites.Main.LoginActivity;
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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class TicketFormActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setElevation(0);
        Bundle b = new Bundle();
        if (getIntent().getExtras() !=null) {
            b = getIntent().getExtras().getBundle("Ticket");
        }
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, new TicketFormFragment().newInstance(b)).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ticket, menu);
        return true;
    }

    public static class TicketFormFragment extends Fragment {
        JSONObject user;
        EditText company;
        EditText attention;
        EditText location;
        EditText description;
        EditText start;
        EditText end;
        EditText supervisor;
        EditText job;
        EditText work;
        EditText afe;
        int ticketId = 0;
        String username;
        public TicketFormFragment newInstance(Bundle b) {
            TicketFormFragment f = new TicketFormFragment();
            f.setArguments(b);
            return f;
        }

        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container,
                                 Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            user = getSavedUser(getActivity());
            View rv = inflater.inflate(R.layout.fragment_ticket_form, container, false);

            company = (EditText) rv.findViewById(R.id.company);
            attention = (EditText) rv.findViewById(R.id.attention);
            location = (EditText) rv.findViewById(R.id.location);
            description = (EditText) rv.findViewById(R.id.description);
            start = (EditText) rv.findViewById(R.id.start);
            end = (EditText) rv.findViewById(R.id.end);
            supervisor = (EditText) rv.findViewById(R.id.supervisor);
            job = (EditText) rv.findViewById(R.id.jobnumber);
            work = (EditText) rv.findViewById(R.id.work);
            afe = (EditText) rv.findViewById(R.id.afe);

            // Set Action Bar Title
            getActivity().setTitle("Ticket");
            if (getArguments().getSerializable("Ticket") != null) {
                Ticket ticket = (Ticket) getArguments().getSerializable("Ticket");
                ticketId = ticket.getId();
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
            if (id == R.id.done) {
                // Verify User -> Save Ticket
                if (isConnected()) {
                    try {
                        String p = user.getString("password");
                        String u = user.getString("username");
                        String url = "http://159.203.202.117/app/login.php?u=" + u + "&p=" + p;
                        new verifyUser().execute(new URL(url));
                    } catch (JSONException | MalformedURLException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Save Local
                    // Load Data and Append New Data
                    ArrayList<Ticket> arr = readStored();
                    int ticket = ticketId;
                    String c = company.getText().toString().trim();
                    String a = attention.getText().toString().trim();
                    String w = work.getText().toString().trim();
                    String f = afe.getText().toString().trim();
                    String l = location.getText().toString().trim();
                    String j = job.getText().toString().trim();
                    String u = supervisor.getText().toString().trim();
                    String d = description.getText().toString();
                    String s = start.getText().toString().trim();
                    String e = end.getText().toString().trim();
                    int status = 0;

                    File ticketsFile = new File(getActivity().getApplicationContext()
                            .getFilesDir(), "stored");
                    try {
                        String created = user.getString("firstname") + " " + user.getString("lastname");
                        Float h = Float.valueOf("0");
                        if (!e.trim().equals("") || !s.trim().equals("")) {
                            h = Float.valueOf(e) - Float.valueOf(s);
                        }
                        arr.add(new Ticket(ticket, c, a, l, u, d, created, s, e, f, j, w, h, status));
                        ObjectOutputStream os = new ObjectOutputStream(
                                new FileOutputStream(ticketsFile));
                        os.writeObject(arr);
                        os.close();
                        getActivity().finish();
                    } catch (IOException | JSONException error) {
                        error.printStackTrace();
                    }
                }
                return true;
            }
            return super.onOptionsItemSelected(item);
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
                    // Upload Ticket
                    String c = company.getText().toString().trim();
                    String a = attention.getText().toString().trim();
                    String w = work.getText().toString().trim();
                    String f = afe.getText().toString().trim();
                    String l = location.getText().toString().trim();
                    String j = job.getText().toString().trim();
                    String u = supervisor.getText().toString().trim();
                    String d = description.getText().toString();
                    String s = start.getText().toString().trim();
                    String e = end.getText().toString().trim();
                    String status = "0";
                    if (!c.equals("") && !a.equals("") && !l.equals("") && !j.equals("")
                            && !u.equals("") && !d.equals("") && !s.equals("") && !e.equals("")) {
                        if (!w.equals("") || !f.equals("")) {
                            // Complete Ticket
                            c = company.getText().toString().replace(" ", "%20");
                            a = attention.getText().toString().replace(" ", "%20");
                            w = work.getText().toString().replace(" ", "%20");
                            f = afe.getText().toString().replace(" ", "%20");
                            l = location.getText().toString().replace(" ", "%20");
                            j = job.getText().toString().replace(" ", "%20");
                            u = supervisor.getText().toString().replace(" ", "%20");
                            d = description.getText().toString().replace(" ", "%20");
                            s = start.getText().toString().replace(" ", "%20");
                            e = end.getText().toString().replace(" ", "%20");
                            status = "2";
                        }
                    } else {
                        // Not Complete
                        c = company.getText().toString().replace(" ", "%20");
                        a = attention.getText().toString().replace(" ", "%20");
                        w = work.getText().toString().replace(" ", "%20");
                        f = afe.getText().toString().replace(" ", "%20");
                        l = location.getText().toString().replace(" ", "%20");
                        j = job.getText().toString().replace(" ", "%20");
                        u = supervisor.getText().toString().replace(" ", "%20");
                        d = description.getText().toString().replace(" ", "%20");
                        s = start.getText().toString().replace(" ", "%20");
                        e = end.getText().toString().replace(" ", "%20");
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
                } else {
                    // Go to Login
                    returnLogin();
                }
            }
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

        public void returnLogin() {
            // Go to Login
            Intent i = new Intent(getActivity(), LoginActivity.class);
            getActivity().startActivity(i);
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
    }
}
