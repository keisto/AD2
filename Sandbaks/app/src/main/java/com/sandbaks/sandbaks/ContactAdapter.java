package com.sandbaks.sandbaks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ContactAdapter extends ArrayAdapter<Contact> {

    public ContactAdapter(Context context, ArrayList<Contact> contacts) {
        super(context, 0, contacts);
    }
    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View v, ViewGroup parent) {
        // Get the data item for this position
        Contact contact = getItem(position);

        v = LayoutInflater.from(getContext()).inflate(R.layout.contact_item, parent, false);
        // Get UI Elements
        TextView name = (TextView) v.findViewById(R.id.name);
        TextView title = (TextView) v.findViewById(R.id.title);
        TextView phone = (TextView) v.findViewById(R.id.phone);
        ImageView color = (ImageView) v.findViewById(R.id.color);
        // Set UI Values
        if(contact.getAccess()==1) {
            // Forman/Laborer
            color.setBackgroundColor(Color.parseColor("#74818C"));
            title.setText("Employee");
        }
        if(contact.getAccess()>1 && contact.getAccess()<6){
            // Management
            color.setBackgroundColor(Color.parseColor("#70FF61"));
            title.setText("Management");
        }
        if(contact.getAccess()==6){
            // Emergency
            color.setBackgroundColor(Color.parseColor("#FBAE17"));
            title.setText("Emergency");
        }
        name.setText(contact.getName());
        phone.setText(contact.getPhone());
        // Display View
        return v;
    }
}