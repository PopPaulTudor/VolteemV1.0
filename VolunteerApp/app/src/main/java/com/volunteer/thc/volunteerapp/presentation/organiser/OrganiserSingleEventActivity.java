package com.volunteer.thc.volunteerapp.presentation.organiser;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
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
import com.volunteer.thc.volunteerapp.presentation.MainActivity;

import java.util.ArrayList;

/**
 * Created by poppa on 13.07.2017.
 */

public class OrganiserSingleEventActivity extends AppCompatActivity {

    private Event mCurrentEvent;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager mViewPager;
    private Resources resources;
    private AppBarLayout appBarLayout;
    private TextView acceptedText, regText;

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private StorageReference mStorage = FirebaseStorage.getInstance().getReference();
    private ImageView squareImageView;
    private ArrayList<Uri> imageUris = new ArrayList<>();
    private ArrayList<String> typeList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organisersingleevent);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        resources = getResources();
        populateUriList();
        populateTypeList();

        squareImageView = (ImageView) findViewById(R.id.collapsing_toolbar_image);
        acceptedText = (TextView) findViewById(R.id.accept_number);
        regText = (TextView) findViewById(R.id.reg_number);
        mCurrentEvent = (Event) getIntent().getSerializableExtra("SingleEvent");
        appBarLayout = (AppBarLayout) findViewById(R.id.appbar);


        if (mCurrentEvent.getAccepted_volunteers().size() == 1)
            acceptedText.setText("         " + mCurrentEvent.getAccepted_volunteers().size() + "\nvolunteer");
        else
            acceptedText.setText("         " + mCurrentEvent.getAccepted_volunteers().size() + acceptedText.getText());

        if (mCurrentEvent.getRegistered_volunteers().size() == 1)
            regText.setText("         " + mCurrentEvent.getRegistered_volunteers().size() + "\nvolunteer");
        else
            regText.setText("         " + mCurrentEvent.getRegistered_volunteers().size() + regText.getText());


        mStorage.child("Photos").child("Event").child(mCurrentEvent.getEventID()).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {

            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Picasso.with(getApplicationContext()).load(task.getResult()).fit().centerInside().into(squareImageView);

                } else {
                    Picasso.with(getApplicationContext()).load(imageUris.get(typeList.indexOf(mCurrentEvent.getType()))).fit().centerCrop().into(squareImageView);
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
                    toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                } else {
                    toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.transparent));
                }
            }
        });

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager = (ViewPager) findViewById(R.id.container);


        if (mCurrentEvent == null) {
            String eventID = getIntent().getStringExtra("eventID");
            if (eventID == null) {
                eventID = getIntent().getStringExtra("newsEventID");
            }
            getEvent(eventID);

        } else {
            setUpUI();
        }
    }

    private void setUpUI() {

        Bundle bundle = new Bundle();
        bundle.putSerializable("currentEvent", mCurrentEvent);
        OrganiserSingleEventInfoFragment fragmentInfo = new OrganiserSingleEventInfoFragment();
        fragmentInfo.setArguments(bundle);

        OrganiserSingleEventRegisteredUsersFragment fragmentRegistered = new OrganiserSingleEventRegisteredUsersFragment();
        fragmentRegistered.setArguments(bundle);

        OrganiserSingleEventAcceptedUsersFragment fragmentAccepted = new OrganiserSingleEventAcceptedUsersFragment();
        fragmentAccepted.setArguments(bundle);

        OrganiserSingleEventViewAdapter mViewPagerAdapter = new OrganiserSingleEventViewAdapter(getSupportFragmentManager(), fragmentInfo, fragmentRegistered, fragmentAccepted);
        mViewPager.setAdapter(mViewPagerAdapter);
        tabLayout.setupWithViewPager(mViewPager);
    }

    private void getEvent(String eventID) {
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
    }

    @Override
    public void onBackPressed() {
        if (getIntent().getStringExtra("eventID") != null) {
            startActivity(new Intent(this, MainActivity.class));
        }
        super.onBackPressed();
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


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
