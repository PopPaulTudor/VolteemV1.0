package com.volunteer.thc.volunteerapp.adaptor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.Util.CalendarUtil;
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.presentation.organiser.OrganiserSingleEventActivity;
import com.volunteer.thc.volunteerapp.presentation.volunteer.VolunteerSingleEventActivity;

import java.util.List;

/**
 * Created by poppa on 12.07.2017.
 */

public class OrgEventsAdaptor extends RecyclerView.Adapter<OrgEventsAdaptor.EventViewHolder> {


    private List<Event> EventsList;
    private Context context;

    public OrgEventsAdaptor(List<Event> list, Context context){
        EventsList = list;
        this.context = context;
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
        holder.cardDate.setText(CalendarUtil.getStringDateFromMM(EventsList.get(position).getDeadline()));
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
                if(TextUtils.equals(prefs.getString("user_status", null), "Organiser")) {
                    Intent intent = new Intent(context.getApplicationContext(), OrganiserSingleEventActivity.class);
                    intent.putExtra("SingleEvent", EventsList.get(position));
                    context.startActivity(intent);
                } else {
                    Intent intent = new Intent(context.getApplicationContext(), VolunteerSingleEventActivity.class);
                    intent.putExtra("SingleEvent", EventsList.get(position));
                    context.startActivity(intent);
                }
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
