package com.volunteer.thc.volunteerapp.presentation;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.adaptor.OrganiserSingleEventViewAdapter;
import com.volunteer.thc.volunteerapp.model.Event;

/**
 * Created by poppa on 13.07.2017.
 */

public class OrganiserSingleEventActivity extends AppCompatActivity {

    private Event mCurrentEvent;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private OrganiserSingleEventViewAdapter mViewPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organisersingleevent);

        mCurrentEvent = (Event) getIntent().getSerializableExtra("SingleEvent");

        Bundle bundle = new Bundle();
        bundle.putSerializable("currentEvent", mCurrentEvent);
        OrganiserSingleEventInfoFragment fragmentInfo = new OrganiserSingleEventInfoFragment();
        fragmentInfo.setArguments(bundle);

        bundle.putString("eventID", mCurrentEvent.getEventID());
        bundle.putStringArrayList("registered_users", mCurrentEvent.getRegistered_volunteers());
        OrganiserSingleEventRegisteredUsersFragment fragmentRegistered = new OrganiserSingleEventRegisteredUsersFragment();
        fragmentRegistered.setArguments(bundle);

        bundle.putStringArrayList("accepted_users", mCurrentEvent.getAccepted_volunteers());
        OrganiserSingleEventAcceptedUsersFragment fragmentAccepted = new OrganiserSingleEventAcceptedUsersFragment();
        fragmentAccepted.setArguments(bundle);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPagerAdapter = new OrganiserSingleEventViewAdapter(getSupportFragmentManager(), fragmentInfo, fragmentRegistered, fragmentAccepted);
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mViewPagerAdapter);
        tabLayout.setupWithViewPager(mViewPager);
    }
}
