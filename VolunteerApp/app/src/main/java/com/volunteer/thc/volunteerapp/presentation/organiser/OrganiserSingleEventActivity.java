package com.volunteer.thc.volunteerapp.presentation.organiser;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.adaptor.OrganiserSingleEventViewAdapter;
import com.volunteer.thc.volunteerapp.model.Event;

import java.util.ArrayList;

/**
 * Created by poppa on 13.07.2017.
 */

public class OrganiserSingleEventActivity extends AppCompatActivity {

    private Event mCurrentEvent;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private OrganiserSingleEventViewAdapter mViewPagerAdapter;
    private ViewPager mViewPager;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organisersingleevent);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager = (ViewPager) findViewById(R.id.container);

        mCurrentEvent = (Event) getIntent().getSerializableExtra("SingleEvent");

        if (mCurrentEvent == null) {
            String eventID = getIntent().getStringExtra("eventID");
            mDatabase.child("events/" + eventID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Event currentEvent = dataSnapshot.getValue(Event.class);
                    ArrayList<String> reg_users = new ArrayList<>();
                    ArrayList<String> acc_users = new ArrayList<>();

                    for (DataSnapshot registered_users : dataSnapshot.child("users").getChildren()) {
                        if (TextUtils.equals(registered_users.child("status").getValue().toString(), "pending")) {
                            reg_users.add(registered_users.child("id").getValue().toString());
                        } else {
                            acc_users.add(registered_users.child("id").getValue().toString());
                        }
                    }
                    currentEvent.setRegistered_volunteers(reg_users);
                    currentEvent.setAccepted_volunteers(acc_users);

                    mCurrentEvent = currentEvent;
                    setUpUI();
                    tabLayout.getTabAt(1).select();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("OrgSingleEvA", databaseError.getMessage());
                }
            });
        } else {
            setUpUI();
        }
    }

    private void setUpUI() {
        getSupportActionBar().setTitle(mCurrentEvent.getName());

        Bundle bundle = new Bundle();
        bundle.putSerializable("currentEvent", mCurrentEvent);
        OrganiserSingleEventInfoFragment fragmentInfo = new OrganiserSingleEventInfoFragment();
        fragmentInfo.setArguments(bundle);

        OrganiserSingleEventRegisteredUsersFragment fragmentRegistered = new OrganiserSingleEventRegisteredUsersFragment();
        fragmentRegistered.setArguments(bundle);

        OrganiserSingleEventAcceptedUsersFragment fragmentAccepted = new OrganiserSingleEventAcceptedUsersFragment();
        fragmentAccepted.setArguments(bundle);

        mViewPagerAdapter = new OrganiserSingleEventViewAdapter(getSupportFragmentManager(), fragmentInfo, fragmentRegistered, fragmentAccepted);
        mViewPager.setAdapter(mViewPagerAdapter);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
