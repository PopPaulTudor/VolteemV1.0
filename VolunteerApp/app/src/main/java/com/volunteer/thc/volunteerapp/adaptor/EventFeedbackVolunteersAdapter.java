package com.volunteer.thc.volunteerapp.adaptor;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.interrface.FeedbackDoneListener;
import com.volunteer.thc.volunteerapp.model.NewsMessage;
import com.volunteer.thc.volunteerapp.model.Volunteer;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Cristi on 8/14/2017.
 */

public class EventFeedbackVolunteersAdapter extends RecyclerView.Adapter<EventFeedbackVolunteersAdapter.EventViewHolder> {
    private ArrayList<Volunteer> listVolunteer;
    private ArrayList<String> volunteerIDs;
    private int mExpandedPosition = -1;
    private ViewGroup parent;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FeedbackDoneListener feedbackDoneListener;
    private String eventName;
    private Calendar date = Calendar.getInstance();

    public EventFeedbackVolunteersAdapter(ArrayList<Volunteer> list, String eventName, ArrayList<String> volunteerIDs, FeedbackDoneListener feedbackDoneListener) {
        listVolunteer = list;
        this.volunteerIDs = volunteerIDs;
        this.feedbackDoneListener = feedbackDoneListener;
        this.eventName = eventName;
    }

    @Override
    public EventFeedbackVolunteersAdapter.EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.volunteer_feedback_element, parent, false);
        this.parent = parent;
        return new EventFeedbackVolunteersAdapter.EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, final int position) {
        holder.nameVolunteer.setText(listVolunteer.get(position).getFirstname() + " " + listVolunteer.get(position).getLastname());
        holder.cityVolunteer.setText("City: " + listVolunteer.get(position).getCity());
        holder.ageVolunteer.setText("Age: " + listVolunteer.get(position).getAge());
        holder.emailVolunteer.setText("Email: " + listVolunteer.get(position).getEmail());
        holder.phoneVolunteer.setText("Phone: " + listVolunteer.get(position).getPhone());
        holder.expVolunteer.setText("Experience: " + listVolunteer.get(position).getExperience());

        final boolean isExpanded = position == mExpandedPosition;
        holder.expandableItem.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.item.setActivated(isExpanded);
        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mExpandedPosition = isExpanded ? -1 : position;
                notifyDataSetChanged();
            }
        });
        holder.giveFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View feedbackDialogView = LayoutInflater.from(parent.getContext()).inflate(R.layout.feedback_alert_dialog, null);
                final EditText mFeedback = (EditText) feedbackDialogView.findViewById(R.id.feedback);
                final AlertDialog feedbackDialog = new AlertDialog.Builder(parent.getContext())
                        .setView(feedbackDialogView)
                        .setTitle("Feedback")
                        .setMessage("Write your feedback about " + listVolunteer.get(position).getFirstname() + " " +
                                listVolunteer.get(position).getLastname())
                        .setCancelable(false)
                        .setPositiveButton("DONE", null)
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .create();
                feedbackDialog.show();
                feedbackDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String feedbackText = mFeedback.getText().toString();
                        if (!feedbackText.isEmpty()) {
                            feedbackDialog.dismiss();
                            notifyDataSetChanged();
                            mDatabase.child("users").child("volunteers").child(volunteerIDs.get(position)).child("feedback")
                                    .child(user.getUid()).setValue(feedbackText);
                            String newsID = mDatabase.child("news").push().getKey();
                            mDatabase.child("news").child(newsID).setValue(new NewsMessage(date.getTimeInMillis(), newsID, "soon", user.getUid(), volunteerIDs.get(position),
                                    "You have received feedback for your activity at " + eventName, NewsMessage.FEEDBACK, false, false));
                            volunteerIDs.remove(position);
                            listVolunteer.remove(position);
                            Toast.makeText(parent.getContext(), "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                            if (volunteerIDs.isEmpty()) {
                                feedbackDoneListener.showDoneTextView();
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return listVolunteer.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        TextView nameVolunteer, expVolunteer, cityVolunteer, ageVolunteer, phoneVolunteer, emailVolunteer;
        RelativeLayout item;
        LinearLayout expandableItem;
        Button giveFeedback;

        EventViewHolder(View itemView) {
            super(itemView);

            item = (RelativeLayout) itemView.findViewById(R.id.item_view);
            nameVolunteer = (TextView) itemView.findViewById(R.id.name_volunteer_element);

            expandableItem = (LinearLayout) itemView.findViewById(R.id.expandable_item);
            expVolunteer = (TextView) itemView.findViewById(R.id.volunteer_experience);
            cityVolunteer = (TextView) itemView.findViewById(R.id.volunteer_city);
            ageVolunteer = (TextView) itemView.findViewById(R.id.volunteer_age);
            phoneVolunteer = (TextView) itemView.findViewById(R.id.volunteer_phone);
            emailVolunteer = (TextView) itemView.findViewById(R.id.volunteer_email);
            giveFeedback = (Button) itemView.findViewById(R.id.give_feedback);
        }
    }
}
