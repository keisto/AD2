package com.sandbaks.sandbaks;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class ContactsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_contacts);
        getSupportActionBar().setElevation(0);
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, new ContactsFragment().newInstance(this)).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contacts, menu);
        return true;
    }


    public static class ContactsFragment extends Fragment {
        private Context mContext;
        private ListView listView;

    public ContactsFragment newInstance(Context context) {
        mContext = context.getApplicationContext();
        return new ContactsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        getActivity().setTitle("Contacts");
        View rv = inflater.inflate(R.layout.fragment_contacts, container, false);
        listView = (ListView) rv.findViewById(R.id.listView);
        return rv;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ArrayList<Contact> arr = readData();
        listView.setAdapter(new ContactAdapter(getActivity(), arr));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.refresh) {
            // Get Contacts
            MyIntentService.getContacts(getActivity());
            listView.invalidateViews();
            return true;
        }
        if (id == R.id.self) {
            // View Self - MileStone 2
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Read Data
    public ArrayList readData() {
        ArrayList<Contact> arr = new ArrayList<>();
        try {
            String fileString = new File(getActivity().getApplicationContext()
                    .getFilesDir(),"") + File.separator+"contacts";
            File file = new File(fileString);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream is = new ObjectInputStream(fis);
                arr = (ArrayList<Contact>) is.readObject();
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
                .replace(android.R.id.content, new ContactsFragment().newInstance(this)).commit();
        super.onResume();
    }
}
