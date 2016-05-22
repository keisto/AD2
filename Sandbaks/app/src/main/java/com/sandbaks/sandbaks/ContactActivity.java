package com.sandbaks.sandbaks;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setElevation(0);
        Bundle b = getIntent().getExtras();
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, new ContactFragment().newInstance(b)).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contact, menu);
        return true;
    }

    public static class ContactFragment extends Fragment {
        TextView position;
        TextView phone;
        TextView email;

        public ContactFragment newInstance(Bundle b) {
            ContactFragment f = new ContactFragment();
            f.setArguments(b);
            return f;
        }

        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container,
                                 Bundle savedInstanceState) {
            setHasOptionsMenu(true);


            View rv = inflater.inflate(R.layout.fragment_contact, container, false);

            position = (TextView) rv.findViewById(R.id.position);
            phone    = (TextView) rv.findViewById(R.id.phone);
            email    = (TextView) rv.findViewById(R.id.email);

            if (getArguments() != null) {
                // Set Action Bar Title
                getActivity().setTitle(getArguments().getString(Contact.NAME));
                int access = getArguments().getInt(Contact.ACCESS, 0);
                if (access<=1) {
                    position.setText("Employee");
                }
                if (access>1) {
                    position.setText("Management");
                }
                if (access==6) {
                    position.setText("Emergency");
                }
                phone.setText(getArguments().getString(Contact.PHONE));
                email.setText(getArguments().getString(Contact.EMAIL));
            }
            return rv;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.email) {
                // Email Contact
                Intent i = new Intent(Intent.ACTION_SENDTO);
                i.setData(Uri.parse("mailto:" + email.getText().toString()));
                startActivity(i);
                return true;
            }
            if (id == R.id.call) {
                // Call Contact
                Intent i = new Intent(Intent.ACTION_DIAL);
                i.setData(Uri.parse("tel:" + phone.getText().toString().replace("-", "")));
                startActivity(i);
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
