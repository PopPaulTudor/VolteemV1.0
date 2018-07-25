package com.volunteer.thc.volunteerapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.IdRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.callback.ActionListener;
import com.volunteer.thc.volunteerapp.model.ChatSingle;
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.model.NewsMessage;
import com.volunteer.thc.volunteerapp.model.OrganiserRating;
import com.volunteer.thc.volunteerapp.model.Volunteer;
import com.volunteer.thc.volunteerapp.presentation.chat.ConversationActivity;
import com.volunteer.thc.volunteerapp.presentation.organiser.OrganiserSingleEventRegisteredUsersFragment;
import com.volunteer.thc.volunteerapp.util.CalendarUtil;
import com.volunteer.thc.volunteerapp.util.VolteemConstants;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by poppa on 28.07.2017.
 */

public class EventVolunteersAdapter extends RecyclerView.Adapter<EventVolunteersAdapter.EventViewHolder> {

    private ArrayList<Volunteer> listVolunteer;
    private ArrayList<String> volunteerIDs;
    private String classParent;
    private Event event;
    private int mExpandedPosition = -1;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private StorageReference mStorage = FirebaseStorage.getInstance().getReference();
    private ViewGroup parent;
    private Context context;
    private Calendar date = Calendar.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private OrganiserSingleEventRegisteredUsersFragment fragment;
    private Activity activity;
    private int counter = 0, mShortAnimTime;
    private ActionListener.VolunteersRemovedListener volunteersRemovedListener;


    public EventVolunteersAdapter(ArrayList<Volunteer> list, ArrayList<String> volunteerIDs, String classParent, Event event, Context context,
                                  OrganiserSingleEventRegisteredUsersFragment fragment, Activity activity, ActionListener.VolunteersRemovedListener
                                          volunteersRemovedListener) {
        listVolunteer = list;
        this.classParent = classParent;
        this.volunteerIDs = volunteerIDs;
        this.event = event;
        this.context = context;
        this.fragment = fragment;
        this.activity = activity;
        this.volunteersRemovedListener = volunteersRemovedListener;
    }

    public EventVolunteersAdapter(ArrayList<Volunteer> list, ArrayList<String> volunteerIDs, String classParent, Event event, Context context,
                                  Activity activity, ActionListener.VolunteersRemovedListener volunteersRemovedListener) {
        listVolunteer = list;
        this.classParent = classParent;
        this.volunteerIDs = volunteerIDs;
        this.event = event;
        this.context = context;
        this.activity = activity;
        this.volunteersRemovedListener = volunteersRemovedListener;
    }

    @Override
    public EventVolunteersAdapter.EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.volunteer_element, parent, false);

        this.parent = parent;
        return new EventVolunteersAdapter.EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final EventVolunteersAdapter.EventViewHolder holder, final int position) {

        mShortAnimTime = context.getResources().getInteger(android.R.integer.config_shortAnimTime);

        holder.nameVolunteer.setText(listVolunteer.get(position).getFirstname() + " " + listVolunteer.get(position).getLastname());
        holder.cityVolunteer.setText("City: " + listVolunteer.get(position).getCity());
        holder.ageVolunteer.setText("Age: " + listVolunteer.get(position).getAge());
        holder.emailVolunteer.setText("Email: " + listVolunteer.get(position).getEmail());

        mStorage.child("Photos").child("User").child(volunteerIDs.get(position)).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).fit().centerCrop().into(holder.volunteerImage);
            }
        });


        if (classParent.contains("accept")) {
            holder.phoneVolunteer.setText("Experience: " + listVolunteer.get(position).getExperience());
            holder.acceptUser.setVisibility(View.GONE);
            holder.sendMessage.setVisibility(View.GONE);
            holder.viewFeedback.setVisibility(View.GONE);
            holder.kickVolunteer.setVisibility(View.VISIBLE);
            holder.sendMessageAccepted.setVisibility(View.VISIBLE);
            holder.expPhoneVolunteer.setText(listVolunteer.get(position).getPhone());
            holder.detailedText.setText("Phone:");


        } else {
            holder.phoneVolunteer.setText("Phone: " + listVolunteer.get(position).getPhone());
            holder.acceptUser.setVisibility(View.VISIBLE);
            holder.sendMessage.setVisibility(View.VISIBLE);
            holder.viewFeedback.setVisibility(View.VISIBLE);
            holder.kickVolunteer.setVisibility(View.GONE);
            holder.sendMessageAccepted.setVisibility(View.GONE);
            holder.expPhoneVolunteer.setText(listVolunteer.get(position).getExperience() + "");
            holder.detailedText.setText("Experience:");

        }

        final boolean isExpanded = position == mExpandedPosition;
        if (isExpanded) {
            holder.expandableItem.setAlpha(0f);
            holder.expandableItem.setVisibility(View.VISIBLE);
            holder.expandableItem.animate()
                    .alpha(1f)
                    .setDuration(mShortAnimTime)
                    .setListener(null);
        } else {
            holder.expandableItem.setVisibility(View.GONE);
        }
        holder.item.setActivated(isExpanded);
        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mExpandedPosition = isExpanded ? -1 : position;
                notifyDataSetChanged();
            }
        });


        holder.viewFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View parentView = activity.getLayoutInflater().inflate(R.layout.volunteer_feedback_alert_dialog, null);
                final ProgressBar progressBar = (ProgressBar) parentView.findViewById(R.id.progressBar);
                final ListView feedbackListView = (ListView) parentView.findViewById(R.id.feedback_list);
                final TextView noFeedbackText = (TextView) parentView.findViewById(R.id.no_feedback_text);
                final AlertDialog feedbackDialog = new AlertDialog.Builder(context)
                        .setView(parentView)
                        .setTitle("Feedback")
                        .setPositiveButton("DONE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .create();
                feedbackDialog.show();
                progressBar.setVisibility(View.VISIBLE);
                feedbackListView.setVisibility(View.GONE);
                noFeedbackText.setVisibility(View.GONE);
                final ArrayList<String> feedback = new ArrayList<>();
                mDatabase.child("users/volunteers/" + volunteerIDs.get(position) + "/feedback").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final long count = dataSnapshot.getChildrenCount();
                        if (count == 0) {
                            progressBar.setVisibility(View.GONE);
                            noFeedbackText.setVisibility(View.VISIBLE);
                        } else {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                final String feedbackText = dataSnapshot1.getValue().toString();
                                mDatabase.child("users/organisers/" + dataSnapshot1.getKey()).addListenerForSingleValueEvent(new ValueEventListener
                                        () {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot2) {
                                        ++counter;
                                        OrganiserRating rating = dataSnapshot2.child("org_rating").getValue(OrganiserRating.class);
                                        String company = dataSnapshot2.child("company").getValue().toString();
                                        feedback.add(company + ", " + new DecimalFormat("#.##").format(rating.getRating()) + "/5: " + feedbackText);
                                        if (counter == count) {
                                            progressBar.setVisibility(View.GONE);

                                            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, feedback);
                                            feedbackListView.setAdapter(adapter);
                                            feedbackListView.setVisibility(View.VISIBLE);

                                            counter = 0;
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.e("EvVolAdaptOrg", databaseError.getMessage());
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("EvVolAdapterFeedback", databaseError.getMessage());
                    }
                });
            }
        });

        holder.acceptUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                AlertDialog acceptUserDialog = new AlertDialog.Builder(context)
                        .setTitle("Accept volunteer")
                        .setMessage("Are you sure you want to accept this volunteer?")
                        .setCancelable(true)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String eventID = mDatabase.child("news").push().getKey();
                                mDatabase.child("news").child(eventID).setValue(new NewsMessage(date.getTimeInMillis(), eventID, event.getEventID()
                                        , event.getCreated_by(), volunteerIDs.get(position),
                                        "You have been accepted at " + event.getName() + "!", NewsMessage.ACCEPT, false, false));

                                mDatabase.child("events").child(event.getEventID()).child("users").child(volunteerIDs.get(position)).child
                                        ("status").setValue(VolteemConstants.VOLUNTEER_EVENT_STATUS_ACCEPTED);
                                Toast.makeText(parent.getContext(), "Accepted volunteer!", Toast.LENGTH_LONG).show();
                                ChatSingle chatSingle = new ChatSingle(event.getCreated_by(), volunteerIDs.get(position), "You have been accepted " +
                                        "to " + event.getName(), UUID.randomUUID().toString(), Calendar.getInstance().getTimeInMillis(), false);
                                mDatabase.child("conversation").child("single").push().setValue(chatSingle);


                                listVolunteer.remove(position);
                                volunteerIDs.remove(position);
                                notifyDataSetChanged();
                                if (listVolunteer.isEmpty()) {
                                    volunteersRemovedListener.onAllVolunteersRemoved();
                                }
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .create();
                acceptUserDialog.show();

            }
        });

        holder.sendMessage.setOnClickListener(sendMessage(position,"not_acc"));
        holder.sendMessageAccepted.setOnClickListener(sendMessage(position,"acc"));

        holder.kickVolunteer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View parentView = activity.getLayoutInflater().inflate(R.layout.kick_volunteer_alert_dialog, null);
                final RadioGroup radioGroup = (RadioGroup) parentView.findViewById(R.id.kick_volunteer_radio);
                final EditText otherText = (EditText) parentView.findViewById(R.id.feedback);
                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                        if (radioGroup.getCheckedRadioButtonId() == R.id.radio_other) {
                            otherText.setVisibility(View.VISIBLE);
                        } else {
                            otherText.setVisibility(View.GONE);
                        }
                    }
                });

                final AlertDialog kickVolunteerDialog = new AlertDialog.Builder(context)
                        .setView(parentView)
                        .setCancelable(true)
                        .setTitle("Remove volunteer?")
                        .setMessage("Please tell us the reason why you want to remove this volunteer:")
                        .setPositiveButton("REMOVE", null)
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .create();
                kickVolunteerDialog.show();
                kickVolunteerDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int selectedItemID = radioGroup.getCheckedRadioButtonId();
                        switch (selectedItemID) {
                            case R.id.not_respect_duties:
                                mDatabase.child("users/volunteers/" + volunteerIDs.get(position) + "/feedback" + user.getUid()).setValue("This" +
                                        " user has been kicked out from " + event.getName() + " for not respecting his duties.");
                                removeVolunteerFromEvent(position);
                                kickVolunteerDialog.dismiss();
                                break;
                            case R.id.inappropriate_behaviour:
                                mDatabase.child("users/volunteers/" + volunteerIDs.get(position) + "/feedback" + user.getUid()).setValue("This" +
                                        " user has been kicked out from " + event.getName() + " for having inappropriate behaviour.");
                                removeVolunteerFromEvent(position);
                                kickVolunteerDialog.dismiss();
                                break;
                            case R.id.cant_come:
                                removeVolunteerFromEvent(position);
                                kickVolunteerDialog.dismiss();
                                break;
                            case R.id.too_many_volunteer:
                                removeVolunteerFromEvent(position);
                                kickVolunteerDialog.dismiss();
                                break;
                            case R.id.radio_other:
                                String reason = otherText.getText().toString();
                                if (reason.isEmpty()) {
                                    Toast.makeText(context, "You've selected Other, please write the reason.", Toast.LENGTH_SHORT).show();
                                } else {
                                    mDatabase.child("users/volunteers/" + volunteerIDs.get(position) + "/feedback" + user.getUid()).setValue("This" +
                                            " user has been kicked out from " + event.getName() + " with the feedback: \"" + reason + "\".");
                                    removeVolunteerFromEvent(position);
                                    kickVolunteerDialog.dismiss();
                                }
                                break;
                            default:
                                Toast.makeText(context, "Please select an option", Toast.LENGTH_SHORT).show();
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

    public void acceptVolunteer(final String id, Activity activity) {
        mDatabase.child("events").child(event.getEventID()).child("users").child(id).child("status").setValue(VolteemConstants.VOLUNTEER_EVENT_STATUS_ACCEPTED);
        Toast.makeText(activity, "Accepted volunteer!", Toast.LENGTH_LONG).show();

        String eventID = mDatabase.child("news").push().getKey();
        mDatabase.child("news").child(eventID).setValue(new NewsMessage(CalendarUtil.getCurrentTimeInMillis(), eventID, event.getEventID(), event
                .getCreated_by(), id,
                "You have been accepted at " + event.getName() + "!", NewsMessage.ACCEPT, false, false));

        mDatabase.child("conversation").child("single").orderByChild("receivedBy").equalTo(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String uuid = null;
                boolean ifHasConv = false;
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        ChatSingle chatSingleData = data.getValue(ChatSingle.class);
                        if (chatSingleData.getSentBy().equals(user.getUid())) {
                            uuid = chatSingleData.getUuid();
                            ifHasConv = true;
                            break;
                        }
                    }
                }
                if (!ifHasConv) {
                    uuid = UUID.randomUUID().toString();
                }
                ChatSingle chatSingle = new ChatSingle(event.getCreated_by(), id, "You have been accepted to " + event.getName(), uuid, Calendar
                        .getInstance().getTimeInMillis(), false);
                mDatabase.child("conversation").push().setValue(chatSingle);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("EvVolAdaptChat", databaseError.getMessage());
            }
        });

        int position = volunteerIDs.indexOf(id);
        listVolunteer.remove(position);
        volunteerIDs.remove(position);
    }

    private void removeVolunteerFromEvent(int position) {
        mDatabase.child("events/" + event.getEventID() + "/users/" + volunteerIDs.get(position)).setValue(null);
        String newsID = mDatabase.child("news").push().getKey();
        mDatabase.child("news/" + newsID).setValue(new NewsMessage(CalendarUtil.getCurrentTimeInMillis(), newsID,
                event.getEventID(), user.getUid(), volunteerIDs.get(position), "You have been removed from the event " +
                event.getName(), NewsMessage.VOLUNTEER_LEFT, false, false));
        volunteerIDs.remove(position);
        listVolunteer.remove(position);
        if (volunteerIDs.isEmpty()) {
            volunteersRemovedListener.onAllVolunteersRemoved();
        }
        notifyDataSetChanged();
    }


    private View.OnClickListener sendMessage(final int position, final String accepted) {

        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Intent intent = new Intent(context, ConversationActivity.class);

                mDatabase.child("conversation").child("single").orderByChild("receivedBy").equalTo(volunteerIDs.get(position)).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override

                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean ifHasConv = false;
                        if (!dataSnapshot.hasChildren()) {
                            ChatSingle chatSingle = new ChatSingle(user.getUid(), volunteerIDs.get(position), "", UUID.randomUUID().toString(), 0, false);
                            intent.putExtra("chat", chatSingle);
                        } else {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                ChatSingle chatSingle = dataSnapshot1.getValue(ChatSingle.class);
                                if (TextUtils.equals(chatSingle.getSentBy(), user.getUid())) {
                                    intent.putExtra("chat", chatSingle);
                                    ifHasConv = true;
                                    break;
                                }
                            }

                            if (!ifHasConv) {
                                ChatSingle chatSingle = new ChatSingle(user.getUid(), volunteerIDs.get(position), "", UUID.randomUUID().toString(), 0, false);
                                intent.putExtra("chat", chatSingle);
                            }
                        }
                        ConversationActivity.nameChat = listVolunteer.get(position).getFirstname() + " " + listVolunteer.get(position).getLastname();
                        intent.putExtra("class", "adapter");
                        intent.putExtra("id", volunteerIDs.get(position));
                        intent.putExtra("accept",accepted);
                        ConversationActivity.fragment = fragment;
                        context.startActivity(intent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        final Intent intent = new Intent(context, ConversationActivity.class);

                        ConversationActivity.nameChat = listVolunteer.get(position).getFirstname() + " " + listVolunteer.get(position).getLastname();
                        intent.putExtra("class", "adapter");
                        ChatSingle chatSingle = new ChatSingle(user.getUid(), volunteerIDs.get(position), "", UUID.randomUUID().toString(), 0, false);
                        intent.putExtra("chat", chatSingle);
                        intent.putExtra("id", volunteerIDs.get(position));
                        ConversationActivity.fragment = fragment;
                        context.startActivity(intent);
                    }
                });

            }
        };
    }

    class EventViewHolder extends RecyclerView.ViewHolder {

        TextView nameVolunteer, expPhoneVolunteer, cityVolunteer, ageVolunteer, phoneVolunteer,
                emailVolunteer, detailedText;
        RelativeLayout item;
        RelativeLayout expandableItem;
        ImageView acceptUser, sendMessage, viewFeedback, kickVolunteer, sendMessageAccepted;
        CircleImageView volunteerImage;

        EventViewHolder(View itemView) {
            super(itemView);

            item = (RelativeLayout) itemView.findViewById(R.id.item_view);
            nameVolunteer = (TextView) itemView.findViewById(R.id.name_volunteer_element);
            expPhoneVolunteer = (TextView) itemView.findViewById(R.id.exp_phone_volunteer_element);
            expandableItem = (RelativeLayout) itemView.findViewById(R.id.expandable_item);
            cityVolunteer = (TextView) itemView.findViewById(R.id.volunteer_city);
            ageVolunteer = (TextView) itemView.findViewById(R.id.volunteer_age);
            phoneVolunteer = (TextView) itemView.findViewById(R.id.volunteer_phone);
            emailVolunteer = (TextView) itemView.findViewById(R.id.volunteer_email);
            acceptUser = (ImageView) itemView.findViewById(R.id.accept_volunteer);
            sendMessage = (ImageView) itemView.findViewById(R.id.send_volunteer);
            viewFeedback = (ImageView) itemView.findViewById(R.id.view_feedback);
            kickVolunteer = (ImageView) itemView.findViewById(R.id.kick_volunteer);
            detailedText = (TextView) itemView.findViewById(R.id.text_experience);
            volunteerImage = (CircleImageView) itemView.findViewById(R.id.photo_volunteer_element);
            sendMessageAccepted = (ImageView) itemView.findViewById(R.id.send_accepted_volunteer);
        }
    }
}
