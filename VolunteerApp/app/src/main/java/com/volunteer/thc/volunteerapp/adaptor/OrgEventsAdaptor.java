package com.volunteer.thc.volunteerapp.adaptor;

import android.media.Image;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.presentation.DisplayEventActivity;

import java.security.interfaces.DSAKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by poppa on 12.07.2017.
 */

public class OrgEventsAdaptor extends RecyclerView.Adapter<OrgEventsAdaptor.EventViewHolder>{


    private List<Event> EventsList;


    public OrgEventsAdaptor(List<Event> list){
        EventsList= list;

    }

    @Override
    public OrgEventsAdaptor.EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_element, parent, false);

        return new EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(OrgEventsAdaptor.EventViewHolder holder, final int position) {

        holder.cardName.setText(EventsList.get(position).getName());
        holder.cardLocation.setText(EventsList.get(position).getLocation());
        holder.cardDate.setText(EventsList.get(position).getDate());
        holder.cardName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DisplayEventActivity displayEventActivity= new DisplayEventActivity();
                displayEventActivity.onExpandEvent(EventsList.get(position));

            }
        });


    }

    @Override
    public int getItemCount() {
        return EventsList.size();
    }








    class EventViewHolder extends RecyclerView.ViewHolder{

        TextView cardName;
        TextView cardDate;
        TextView cardLocation;
        ImageView imageView;
        CardView cardView;

        EventViewHolder(View v) {
            super(v);

            cardName= (TextView) v.findViewById(R.id.NameCardElement);
            cardDate= (TextView) v.findViewById(R.id.DateCardElement);
            cardLocation= (TextView) v.findViewById(R.id.LocationCardElement);
            cardView= (CardView) v.findViewById(R.id.CardElement);



        }
    }
}
