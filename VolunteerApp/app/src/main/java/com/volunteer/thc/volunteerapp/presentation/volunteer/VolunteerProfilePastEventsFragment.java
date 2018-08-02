package com.volunteer.thc.volunteerapp.presentation.volunteer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.adapter.VolunteerEventsProfileAdapter;
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.util.DatabaseUtils;
import com.volunteer.thc.volunteerapp.util.VolteemConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by poppa on 17.01.2018.
 */

public class VolunteerProfilePastEventsFragment extends Fragment {

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private RecyclerView recyclerView;
    private ArrayList<Event> mPastEventsList = new ArrayList<>();
    private TextView noPastEventsText;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_volunteer_profile_past_events, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_events_profile);
        noPastEventsText = view.findViewById(R.id.no_past_events_text);
        progressBar = view.findViewById(R.id.indeterminateBar);
        loadEvents();

        return view;
    }

    private void loadEvents() {
        progressBar.setVisibility(View.VISIBLE);
        mDatabase.child("events").orderByChild("users/" + DatabaseUtils.getUserID() + "/flag").equalTo(VolteemConstants.VOLUNTEER_EVENT_FLAG_DONE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mPastEventsList = new ArrayList<>();
                        for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                            boolean isAccepted = TextUtils.equals(eventSnapshot.child("users/" + DatabaseUtils.getUserID() + "/status").getValue().toString(), VolteemConstants.VOLUNTEER_EVENT_STATUS_ACCEPTED);
                            if (isAccepted) {
                                mPastEventsList.add(eventSnapshot.getValue(Event.class));
                            }
                        }
                        progressBar.setVisibility(View.GONE);
                        if (mPastEventsList.isEmpty()) {
                            noPastEventsText.setVisibility(View.VISIBLE);
                        } else {
                            Collections.sort(mPastEventsList, new Comparator<Event>() {
                                @Override
                                public int compare(Event event, Event t1) {
                                    if (event.getStartDate() < t1.getStartDate())
                                        return -1;
                                    if (event.getStartDate() > t1.getStartDate())
                                        return 1;
                                    return 0;
                                }
                            });

                            if (isFragmentActive()) {
                                VolunteerEventsProfileAdapter adapter = new VolunteerEventsProfileAdapter(mPastEventsList, getContext());
                                recyclerView.setAdapter(adapter);
                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                                recyclerView.setLayoutManager(linearLayoutManager);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private boolean isFragmentActive() {
        return isAdded() && !isDetached() && !isRemoving();
    }
}
