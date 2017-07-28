package com.volunteer.thc.volunteerapp.adaptor;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.Volunteer;

import java.util.ArrayList;

/**
 * Created by poppa on 28.07.2017.
 */

public class EventVolunteersAdapter extends RecyclerView.Adapter<EventVolunteersAdapter.EventViewHolder> {

    private  ArrayList<Volunteer> listVolunteer;
    private String classParent;

    public EventVolunteersAdapter(ArrayList<Volunteer> list,String classParent){
        listVolunteer=list;
        this.classParent=classParent;
    }



    @Override
    public EventVolunteersAdapter.EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.volunteer_element, parent, false);

        return new EventVolunteersAdapter.EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(EventVolunteersAdapter.EventViewHolder holder, int position) {

        holder.nameVolunteer.setText(listVolunteer.get(position).getFirstname()+listVolunteer.get(position).getLastname());
        if(classParent.contains("accept")){
            holder.expPhoneVolunteer.setText(listVolunteer.get(position).getPhone());
        }else{
            holder.expPhoneVolunteer.setText(listVolunteer.get(position).getExperience());
        }

    }

    @Override
    public int getItemCount() {
        return listVolunteer.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {


        TextView nameVolunteer;
        TextView expPhoneVolunteer;


        EventViewHolder(View itemView) {
            super(itemView);

            nameVolunteer=(TextView) itemView.findViewById(R.id.name_volunteer_element);
            expPhoneVolunteer=(TextView) itemView.findViewById(R.id.exp_phone_volunteer_element);

        }
    }
}
