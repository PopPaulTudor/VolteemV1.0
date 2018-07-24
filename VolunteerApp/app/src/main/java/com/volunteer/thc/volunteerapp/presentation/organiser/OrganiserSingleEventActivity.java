package com.volunteer.thc.volunteerapp.presentation.organiser;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.adapter.OrganiserSingleEventViewAdapter;
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.model.type.EventType;
import com.volunteer.thc.volunteerapp.presentation.MainActivity;
import com.volunteer.thc.volunteerapp.util.VolteemConstants;

import java.util.ArrayList;

/**
 * Created by poppa on 13.07.2017.
 */
public class OrganiserSingleEventActivity extends AppCompatActivity {

    private static final String TAG = "OrgSingleEvA";
    private Event mCurrentEvent;
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private TextView acceptedTextView, registeredTextView;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private StorageReference mStorage = FirebaseStorage.getInstance().getReference();
    private ImageView mSquareImageView;
    private ArrayList<Uri> mImageUris = new ArrayList<>();
    private ArrayList<String> mTypeList = new ArrayList<>();
    private AppBarLayout appBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organisersingleevent);
        mCurrentEvent = (Event) getIntent().getSerializableExtra(VolteemConstants.INTENT_EXTRA_SINGLE_EVENT);

        mTabLayout = findViewById(R.id.tabs);
        mViewPager = findViewById(R.id.container);
        mToolbar = findViewById(R.id.toolbar);
        mSquareImageView = findViewById(R.id.collapsing_toolbar_image);
        acceptedTextView = findViewById(R.id.accept_number);
        registeredTextView = findViewById(R.id.reg_number);
        appBarLayout = findViewById(R.id.appbar);

        populateUriList();
        populateTypeList();

        if (mCurrentEvent == null) {
            String eventID = getIntent().getStringExtra(VolteemConstants.INTENT_EVENT_ID);
            if (eventID == null) {
                eventID = getIntent().getStringExtra(VolteemConstants.INTENT_NEWS_EVENT_ID);
            }
            getEvent(eventID);
        } else {
            setUpUi();
        }

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setUpUi() {

        // TODO refactor this (fix warning)
        if (mCurrentEvent.getAccepted_volunteers().size() == 1) {
            acceptedTextView.setText("         " + mCurrentEvent.getAccepted_volunteers().size() +
                    "\nvolunteer");
        } else {
            acceptedTextView.setText("         " + mCurrentEvent.getAccepted_volunteers().size() +
                    acceptedTextView.getText());
        }

        if (mCurrentEvent.getRegistered_volunteers().size() == 1) {
            registeredTextView.setText("         " + mCurrentEvent.getRegistered_volunteers().size() +
                    "\nvolunteer");
        } else {
            registeredTextView.setText("         " + mCurrentEvent.getRegistered_volunteers().size() +
                    registeredTextView.getText());
        }

        mStorage.child("Photos").child("Event").child(mCurrentEvent.getEventID()).getDownloadUrl
                ().addOnCompleteListener(new OnCompleteListener<Uri>() {

            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Picasso.get().load(task.getResult()).fit()
                            .centerInside().into(mSquareImageView);
                } else {
                    Picasso.get().load(mImageUris.get(mTypeList.indexOf
                            (mCurrentEvent.getType()))).fit().centerCrop().into(mSquareImageView);
                }
            }
        });

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(final AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    mToolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R
                            .color.colorPrimary));
                } else {
                    mToolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R
                            .color.transparent));
                }
            }
        });
        Bundle bundle = new Bundle();
        bundle.putSerializable(VolteemConstants.INTENT_CURRENT_EVENT, mCurrentEvent);
        OrganiserSingleEventInfoFragment fragmentInfo = new OrganiserSingleEventInfoFragment();
        fragmentInfo.setArguments(bundle);

        OrganiserSingleEventRegisteredUsersFragment fragmentRegistered = new
                OrganiserSingleEventRegisteredUsersFragment();
        fragmentRegistered.setArguments(bundle);

        OrganiserSingleEventAcceptedUsersFragment fragmentAccepted = new
                OrganiserSingleEventAcceptedUsersFragment();
        fragmentAccepted.setArguments(bundle);

        OrganiserSingleEventViewAdapter mViewPagerAdapter = new OrganiserSingleEventViewAdapter
                (getSupportFragmentManager(), fragmentInfo, fragmentRegistered, fragmentAccepted);
        mViewPager.setAdapter(mViewPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void getEvent(String eventID) {
        mDatabase.child("events/" + eventID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Event currentEvent = dataSnapshot.getValue(Event.class);
                ArrayList<String> reg_users = new ArrayList<>();
                ArrayList<String> acc_users = new ArrayList<>();

                for (DataSnapshot registered_users : dataSnapshot.child("users").getChildren()) {
                    if (TextUtils.equals(String.valueOf(registered_users.child("status").getValue()), "pending")) {
                        reg_users.add(String.valueOf(registered_users.child("id").getValue()));
                    } else {
                        acc_users.add(String.valueOf(registered_users.child("id").getValue()));
                    }
                }

                if (currentEvent != null) {
                    currentEvent.setRegistered_volunteers(reg_users);
                    currentEvent.setAccepted_volunteers(acc_users);
                }

                mCurrentEvent = currentEvent;
                setUpUi();

                // TODO use viewPager.setCurrentItem(int position)
                mTabLayout.getTabAt(1).select();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (getIntent().getStringExtra(VolteemConstants.INTENT_EVENT_ID) != null) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            super.onBackPressed();
        }
    }

    private Uri parseUri(int ID) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(ID)
                + '/' + getResources().getResourceTypeName(ID) + '/' + getResources().getResourceEntryName
                (ID));

    }

    private void populateUriList() {
        // TODO refactor with custom adapter
        mImageUris.add(parseUri(R.drawable.image_sports));
        mImageUris.add(parseUri(R.drawable.image_music));
        mImageUris.add(parseUri(R.drawable.image_festival));
        mImageUris.add(parseUri(R.drawable.image_charity));
        mImageUris.add(parseUri(R.drawable.image_training));
        mImageUris.add(parseUri(R.drawable.image_other));
    }

    private void populateTypeList() {
        mTypeList.addAll(EventType.getAllAsList());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
