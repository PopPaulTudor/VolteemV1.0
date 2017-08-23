package com.volunteer.thc.volunteerapp.adaptor;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.Volunteer;

import java.util.ArrayList;

/**
 * Created by Cristi on 8/23/2017.
 */

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.EventViewHolder> {

    private ArrayList<Volunteer> listVolunteer;

    public LeaderboardAdapter(ArrayList<Volunteer> listVolunteer) {
        this.listVolunteer = listVolunteer;
    }

    @Override
    public LeaderboardAdapter.EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboard_element, parent, false);
        return new LeaderboardAdapter.EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, final int position) {
        holder.nameVolunteer.setText(listVolunteer.get(position).getFirstname() + " " + listVolunteer.get(position).getLastname());
        holder.volunteerPosition.setText((position + 1) + ".");
        holder.expVolunteer.setText(listVolunteer.get(position).getExperience() + "");
    }

    @Override
    public int getItemCount() {
        return listVolunteer.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        TextView nameVolunteer, expVolunteer, volunteerPosition;
        RelativeLayout item;

        EventViewHolder(View itemView) {
            super(itemView);

            item = (RelativeLayout) itemView.findViewById(R.id.item_view);
            volunteerPosition = (TextView) itemView.findViewById(R.id.leaderboardPosition);
            nameVolunteer = (TextView) itemView.findViewById(R.id.volunteer_name);
            expVolunteer = (TextView) itemView.findViewById(R.id.volunteer_experience);
        }
    }
}
