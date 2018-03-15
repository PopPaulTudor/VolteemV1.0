package com.volunteer.thc.volunteerapp.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.interrface.ActionListener;
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.presentation.organiser.OrganiserSingleEventActivity;
import com.volunteer.thc.volunteerapp.util.CalendarUtil;
import com.volunteer.thc.volunteerapp.util.VolteemConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by poppa on 12.07.2017.
 */

public class OrganiserEventsAdapter extends RecyclerView.Adapter<OrganiserEventsAdapter
        .EventViewHolder> {

    public static final int ALL_EVENTS = 1, MY_EVENTS = 2;
    protected Context context;
    List<Event> eventsList;
    private Resources resources;
    private ArrayList<Uri> imageUris = new ArrayList<>();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private ArrayList<String> typeList = new ArrayList<>();
    private int flag;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private ActionListener.EventPicturesLoadingListener eventPicturesLoadingListener;
    private boolean wasUIActivated = false;

    public OrganiserEventsAdapter(List<Event> list, Context context, Resources resources, final int
            FLAG, ActionListener.EventPicturesLoadingListener eventPicturesLoadingListener) {
        eventsList = list;
        this.context = context;
        this.resources = resources;
        this.flag = FLAG;
        this.eventPicturesLoadingListener = eventPicturesLoadingListener;
    }

    @Override
    public OrganiserEventsAdapter.EventViewHolder onCreateViewHolder(ViewGroup parent, int
            viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_element, parent,
                false);

        return new EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final OrganiserEventsAdapter.EventViewHolder holder, final int
            position) {

        holder.cardName.setText(eventsList.get(position).getName());
        holder.cardLocation.setText(eventsList.get(position).getLocation());

        if (flag == ALL_EVENTS) {
            holder.cardDate.setText(CalendarUtil.getStringDateFromMM(eventsList.get(position)
                    .getDeadline()));
        } else {
            holder.cardDate.setText(CalendarUtil.getStringDateFromMM(eventsList.get(position)
                    .getStartDate()));
        }
        SharedPreferences prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        if (prefs.getString("user_status", "").equals("Volunteer")) {
            if (flag == ALL_EVENTS) {
                holder.cardChecked.setVisibility(View.GONE);
            } else {
                holder.cardChecked.setVisibility(View.VISIBLE);

                mDatabase.child("events").child(eventsList.get(position).getEventID()).child
                        ("users").child(user.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (TextUtils.equals(String.valueOf(dataSnapshot.child("status")
                                        .getValue()), "accepted")) {
                                    holder.cardChecked.setImageResource(R.drawable.ic_checked);
                                } else {
                                    holder.cardChecked.setImageResource(R.drawable.ic_watch);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

        } else {
            holder.cardChecked.setVisibility(View.GONE);
        }

        populateTypeList();
        populateUriList();

        Picasso.with(context).load(imageUris.get(typeList.indexOf(eventsList.get(position)
                .getType()))).fit().centerCrop().into(holder.cardImage);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        storageRef.child("Photos").child("Event").child(eventsList.get(position).getEventID())
                .getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Log.w(eventsList.get(position).getName(), " has an image.");
                    Picasso.with(context).load(task.getResult()).fit().centerCrop().into(holder
                            .cardImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            if (!wasUIActivated && (position == 2 || position ==
                                    eventsList.size() - 1)) {
                                if (eventPicturesLoadingListener != null) {
                                    wasUIActivated = true;
                                    eventPicturesLoadingListener.onPicturesLoaded();
                                    //TODO aici se apeleaza metoda pt animatie daca ultimul event
                                    // are poza
                                }
                            }
                        }

                        @Override
                        public void onError() {

                        }
                    });
                } else {
                    Log.w(eventsList.get(position).getName(), " doesn't have an image.");
                    if (!wasUIActivated && (position == 2 || position == eventsList
                            .size() - 1)) {
                        if (eventPicturesLoadingListener != null) {
                            wasUIActivated = true;
                            eventPicturesLoadingListener.onPicturesLoaded();
                            //TODO aici se apeleaza metoda pt animatie daca ultimul event nu are
                            // poza
                        }
                    }
                }
            }
        });

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSingleEventActivity(position);
            }
        });
    }

    protected void startSingleEventActivity(int position) {
        Intent intent = new Intent(context.getApplicationContext(), OrganiserSingleEventActivity
                .class);
        intent.putExtra(VolteemConstants.INTENT_EXTRA_SINGLE_EVENT, eventsList.get(position));
        context.startActivity(intent);

    }

    @Override
    public int getItemCount() {
        return eventsList.size();
    }

    private Uri parseUri(int ID) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + resources.getResourcePackageName(ID)
                + '/' + resources.getResourceTypeName(ID) + '/' + resources.getResourceEntryName
                (ID));

    }

    private void populateUriList() {
        imageUris.add(parseUri(R.drawable.image_sports));
        imageUris.add(parseUri(R.drawable.image_music));
        imageUris.add(parseUri(R.drawable.image_festival));
        imageUris.add(parseUri(R.drawable.image_charity));
        imageUris.add(parseUri(R.drawable.image_training));
        imageUris.add(parseUri(R.drawable.image_other));
    }

    private void populateTypeList() {
        typeList.add("Sports");
        typeList.add("Music");
        typeList.add("Festival");
        typeList.add("Charity");
        typeList.add("Training");
        typeList.add("Other");
    }

    class EventViewHolder extends RecyclerView.ViewHolder {

        TextView cardName;
        TextView cardDate;
        TextView cardLocation;
        ImageView cardImage;
        ImageView cardChecked;
        CardView cardView;

        EventViewHolder(View v) {
            super(v);

            cardName = v.findViewById(R.id.NameCardElement);
            cardDate = v.findViewById(R.id.DateCardElement);
            cardLocation = v.findViewById(R.id.LocationCardElement);
            cardView = v.findViewById(R.id.CardElement);
            cardImage = v.findViewById(R.id.ImageCardElement);
            cardChecked = v.findViewById(R.id.CardCheck);
        }
    }
}
