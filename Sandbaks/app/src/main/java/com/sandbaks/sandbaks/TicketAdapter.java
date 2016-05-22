package com.sandbaks.sandbaks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

public class TicketAdapter extends ArrayAdapter<Ticket> {
    Context mContext;
    public TicketAdapter(Context context, ArrayList<Ticket> tickets) {
        super(context, 0, tickets);
        mContext = context;
    }
    @SuppressLint("ViewHolder")
    @Override
    public View getView(final int position, View v, ViewGroup parent) {
        // Get the data item for this position
        final Ticket ticket = getItem(position);

        v = LayoutInflater.from(getContext()).inflate(R.layout.item_ticket, parent, false);
        // Get UI Elements
        TextView company = (TextView) v.findViewById(R.id.company);
        TextView location = (TextView) v.findViewById(R.id.location);
        TextView date = (TextView) v.findViewById(R.id.date);
        TextView hours = (TextView) v.findViewById(R.id.hours);
        TextView job = (TextView) v.findViewById(R.id.job);
        TextView created = (TextView) v.findViewById(R.id.createdby);
        ImageView color = (ImageView) v.findViewById(R.id.color);
        // Set UI Values
        if(ticket.getStatus()==0) {
            // Not Uploaded
            color.setBackgroundColor(Color.parseColor("#74818C"));
        }
        if(ticket.getStatus()==2){
            // Awaiting Approval
            color.setBackgroundColor(Color.parseColor("#70FF61"));
        }
        if(ticket.getStatus() ==1){
            // Incomplete
            color.setBackgroundColor(Color.parseColor("#FBAE17"));
        }
        company.setText(ticket.getCompany());
        location.setText(ticket.getLocation());
        date.setText(ticket.getStart().split(" ")[0]);
        hours.setText(String.valueOf(ticket.getHours()));
        job.setText(ticket.getJob());
        created.setText(ticket.getCreated());

        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //MyIntentService.deleteTicket(mContext, ticket.getId());
                return false;
            }
        });
        // Display View
        return v;
    }
}