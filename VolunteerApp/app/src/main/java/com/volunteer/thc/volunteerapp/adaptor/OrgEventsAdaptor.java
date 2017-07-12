package com.volunteer.thc.volunteerapp.adaptor;

import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.Event;

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
    public void onBindViewHolder(OrgEventsAdaptor.EventViewHolder holder, int position) {

        holder.CardName.setText(EventsList.get(position).getName());
        holder.CardLocation.setText(EventsList.get(position).getLocation());
        holder.CardDate.setText(EventsList.get(position).getDate());


    }

    @Override
    public int getItemCount() {
        return EventsList.size();
    }








    class EventViewHolder extends RecyclerView.ViewHolder{

        TextView CardName;
        TextView CardDate;
        TextView CardLocation;
        ImageView imageView;

        EventViewHolder(View v) {
            super(v);

            CardName= (TextView) v.findViewById(R.id.NameCardElement);
            CardDate= (TextView) v.findViewById(R.id.DateCardElement);
            CardLocation= (TextView) v.findViewById(R.id.LocationCardElement);



        }
    }
}
