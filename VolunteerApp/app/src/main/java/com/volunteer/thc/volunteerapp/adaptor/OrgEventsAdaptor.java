package com.volunteer.thc.volunteerapp.adaptor;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.presentation.organiser.OrganiserSingleEventActivity;
import com.volunteer.thc.volunteerapp.presentation.volunteer.VolunteerSingleEventActivity;
import com.volunteer.thc.volunteerapp.util.CalendarUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by poppa on 12.07.2017.
 */

public class OrgEventsAdaptor extends RecyclerView.Adapter<OrgEventsAdaptor.EventViewHolder> {

    private List<Event> eventsList;
    private Context context;
    private Resources resources;
    private ArrayList<Uri> imageUris = new ArrayList<>();
    private ArrayList<String> typeList = new ArrayList<>();
    private int flag;
    public static final int ALL_EVENTS = 1, MY_EVENTS = 2;

    public OrgEventsAdaptor(List<Event> list, Context context, Resources resources, final int FLAG) {
        eventsList = list;
        this.context = context;
        this.resources = resources;
        this.flag = FLAG;
    }

    @Override
    public OrgEventsAdaptor.EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_element, parent, false);

        return new EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final OrgEventsAdaptor.EventViewHolder holder, final int position) {

        holder.cardName.setText(eventsList.get(position).getName());
        holder.cardLocation.setText(eventsList.get(position).getLocation());

        if(flag == ALL_EVENTS) {
            holder.cardDate.setText(CalendarUtil.getStringDateFromMM(eventsList.get(position).getDeadline()));
        } else {
            holder.cardDate.setText(CalendarUtil.getStringDateFromMM(eventsList.get(position).getStartDate()));
        }

        populateTypeList();
        populateUriList();

        Picasso.with(context).load(imageUris.get(typeList.indexOf(eventsList.get(position).getType()))).fit().centerCrop().into(holder.cardImage);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        storageRef.child("Photos").child("Event").child(eventsList.get(position).getEventID()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(context).load(uri).fit().centerCrop().into(holder.cardImage);
            }
        });

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
                if (TextUtils.equals(prefs.getString("user_status", null), "Organiser")) {
                    Intent intent = new Intent(context.getApplicationContext(), OrganiserSingleEventActivity.class);
                    intent.putExtra("SingleEvent", eventsList.get(position));
                    context.startActivity(intent);
                } else {
                    Intent intent = new Intent(context.getApplicationContext(), VolunteerSingleEventActivity.class);
                    intent.putExtra("SingleEvent", eventsList.get(position));
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventsList.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {

        TextView cardName;
        TextView cardDate;
        TextView cardLocation;
        ImageView cardImage;
        CardView cardView;

        EventViewHolder(View v) {
            super(v);

            cardName = (TextView) v.findViewById(R.id.NameCardElement);
            cardDate = (TextView) v.findViewById(R.id.DateCardElement);
            cardLocation = (TextView) v.findViewById(R.id.LocationCardElement);
            cardView = (CardView) v.findViewById(R.id.CardElement);
            cardImage = (ImageView) v.findViewById(R.id.ImageCardElement);

        }
    }

    private Uri parseUri(int ID) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + resources.getResourcePackageName(ID)
                + '/' + resources.getResourceTypeName(ID) + '/' + resources.getResourceEntryName(ID));

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
}
