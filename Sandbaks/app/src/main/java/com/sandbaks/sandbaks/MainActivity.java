package com.sandbaks.sandbaks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    private UpdateReceiver mReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setElevation(0);
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, new LoginFragment().newInstance()).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    public static class LoginFragment extends Fragment {
        EditText username;
        EditText password;
        public LoginFragment newInstance() {
            return new LoginFragment();
        }

        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container,
                                 Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            getActivity().setTitle("Login");

            View rv = inflater.inflate(R.layout.fragment_login, container, false);
            username = (EditText) rv.findViewById(R.id.username);
            password = (EditText) rv.findViewById(R.id.password);

            return rv;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.login) {
                // Show Overview
                String user = String.valueOf(username.getText());
                String pass = String.valueOf(password.getText());
                if(user.trim().equals("") || pass.trim().equals("")) {
                    Toast.makeText(getActivity(), "Please Enter Username & Password.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    MyIntentService.getUser(getActivity(), user, pass);
                }
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    private class UpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context c, Intent i) {
            if (i.getAction().equals(MyIntentService.RESULT)) {
                if (i.hasExtra(MyIntentService.USER)) {
                    if (i.getBooleanExtra(MyIntentService.USER, false)) {
                        Intent overview = new Intent(c, OverviewActivity.class);
                        startActivity(overview);
                    } else {
                        Toast.makeText(c, "Login Failed. Check Username & Password.",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(c, "No Internet Available.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mReceiver = new UpdateReceiver();
        IntentFilter f = new IntentFilter();
        f.addAction(MyIntentService.RESULT);
        registerReceiver(mReceiver, f);

        // If user is saved... Skip Login Screen
        if(getSavedUser(this)!=null) {
            JSONObject object = getSavedUser(this);
            try {
                String username = object.getString("username");
                String password = object.getString("password");
                MyIntentService.getUser(this, username, password);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    public JSONObject getSavedUser(Context context) {
        String result = "";
        File external = context.getExternalFilesDir(null);
        File file = new File(external, "user.txt");
        if (file.exists()) {
            try {
                FileInputStream fin = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fin);
                char[] data = new char[2048];
                int size;
                try {
                    while ((size = isr.read(data)) > 0) {
                        String readData = String.copyValueOf(data, 0, size);
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
        return null;
    }
}