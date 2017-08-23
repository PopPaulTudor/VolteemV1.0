package com.volunteer.thc.volunteerapp.adaptor;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.Organiser;
import com.volunteer.thc.volunteerapp.model.Volunteer;

import java.util.ArrayList;

/**
 * Created by Cristi on 8/23/2017.
 */

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.EventViewHolder> {

    private ArrayList<Volunteer> listVolunteer;
    private ArrayList<Organiser> listOrganiser;
    public static final int ORGANISER = 2;
    private int flag = 1;

    public LeaderboardAdapter(ArrayList<Volunteer> listVolunteer) {
        this.listVolunteer = listVolunteer;
    }

    public LeaderboardAdapter(ArrayList<Organiser> listOrganiser, final int flag) {
        this.listOrganiser = listOrganiser;
        this.flag = flag;
    }

    @Override
    public LeaderboardAdapter.EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboard_element, parent, false);
        return new LeaderboardAdapter.EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, final int position) {
        if(flag == 1) {
            holder.userName.setText(listVolunteer.get(position).getFirstname() + " " + listVolunteer.get(position).getLastname());
            holder.userPosition.setText((position + 1) + ".");
            holder.userExperience.setText(listVolunteer.get(position).getExperience() + "");
        } else {
            holder.userName.setText(listOrganiser.get(position).getCompany());
            holder.userPosition.setText((position + 1) + ".");
            holder.userExperience.setText(listOrganiser.get(position).getExperience() + "");
        }
    }

    @Override
    public int getItemCount() {
        if(flag == 1) {
            return listVolunteer.size();
        }
        return listOrganiser.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userExperience, userPosition;
        RelativeLayout item;

        EventViewHolder(View itemView) {
            super(itemView);

            item = (RelativeLayout) itemView.findViewById(R.id.item_view);
            userPosition = (TextView) itemView.findViewById(R.id.leaderboardPosition);
            userName = (TextView) itemView.findViewById(R.id.user_name);
            userExperience = (TextView) itemView.findViewById(R.id.user_experience);
        }
    }
}
