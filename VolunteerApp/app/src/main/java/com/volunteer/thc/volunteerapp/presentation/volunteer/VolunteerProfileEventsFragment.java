package com.volunteer.thc.volunteerapp.presentation.volunteer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.adaptor.VolunteerEventsProfileAdapter;
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.util.CalculateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by poppa on 17.01.2018.
 */

public class VolunteerProfileEventsFragment extends Fragment {


    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_volunteer_profile_events, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_events_profile);
        loadEvents();

        return view;
    }


    private void loadEvents() {
        mDatabase.child("events").orderByChild("users/" + user.getUid() + "/status").equalTo("accepted").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Event> mEventsList = new ArrayList<>();
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    final Event currentEvent = eventSnapshot.getValue(Event.class);
                    if (currentEvent.getFinishDate() < Calendar.getInstance().getTimeInMillis()) {
                        mEventsList.add(currentEvent);
                    }
                }

                for (int i = 0; i < mEventsList.size(); ++i) {
                    for (int j = i; j < mEventsList.size(); ++j) {

                        long nrOfDays = TimeUnit.MILLISECONDS.toDays(mEventsList.get(i).getFinishDate() - mEventsList.get(i).getStartDate());
                        long experience = CalculateUtils.calculateVolunteerExperience(mEventsList.get(i).getSize(), nrOfDays);

                        long nrOfDays1 = TimeUnit.MILLISECONDS.toDays(mEventsList.get(j).getFinishDate() - mEventsList.get(j).getStartDate());
                        long experience1 = CalculateUtils.calculateVolunteerExperience(mEventsList.get(j).getSize(), nrOfDays1);

                        if (experience1 > experience) {
                            Event aux = mEventsList.get(i);
                            mEventsList.set(i, mEventsList.get(j));
                            mEventsList.set(j, aux);
                        }

                    }
                }


                if (isFragmentActive()) {
                    VolunteerEventsProfileAdapter adapter = new VolunteerEventsProfileAdapter(mEventsList, getContext());
                    recyclerView.setAdapter(adapter);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                    recyclerView.setLayoutManager(linearLayoutManager);
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
