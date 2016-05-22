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
import android.widget.ListView;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
        private JSONObject user;
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
        user = getSavedUser(getActivity());
        View rv = inflater.inflate(R.layout.fragment_contacts, container, false);
        listView = (ListView) rv.findViewById(R.id.listView);
        return rv;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ArrayList<Contact> arr = readData();
        Collections.sort(arr, new Comparator<Contact>() {
            @Override
            public int compare(Contact one, Contact two) {
                String id1 = one.getName().split(" ")[0];
                String id2 = two.getName().split(" ")[0];

                return id1.compareTo(id2);
            }
        });

        ArrayList<Contact> secondSort = arr;

        Collections.sort(secondSort, new Comparator<Contact>() {
                @Override
                public int compare(Contact one, Contact two) {
                    String id1 = String.valueOf(one.getAccess());
                    String id2 = String.valueOf(two.getAccess());

                    return id2.compareTo(id1);
                }
        });

        listView.setAdapter(new ContactAdapter(getActivity(), secondSort));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(), ContactActivity.class);
                if (arr != null) {
                    i.putExtra(Contact.NAME,   arr.get(position).getName());
                    i.putExtra(Contact.PHONE,  arr.get(position).getPhone());
                    i.putExtra(Contact.EMAIL,  arr.get(position).getEmail());
                    i.putExtra(Contact.ACCESS, arr.get(position).getAccess());
                }
                startActivity(i);
            }
        });
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
            // View Self
            if (user != null) {
            Intent i = new Intent(getActivity(), ContactActivity.class);
                try {
                    i.putExtra(Contact.NAME,   user.getString("firstname")
                            + " " + user.getString("lastname"));
                    i.putExtra(Contact.PHONE,  user.getString("phone"));
                    i.putExtra(Contact.EMAIL,  user.getString("email"));
                    i.putExtra(Contact.ACCESS, user.getInt("access"));
                    startActivity(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

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
                .replace(android.R.id.content, new ContactsFragment().newInstance(this)).commit();
        super.onResume();
    }
}
