package com.volunteer.thc.volunteerapp.presentation;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
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
import com.volunteer.thc.volunteerapp.model.Volunteer;
import com.volunteer.thc.volunteerapp.model.type.UserType;
import com.volunteer.thc.volunteerapp.notification.FirebaseNewsService;
import com.volunteer.thc.volunteerapp.presentation.chat.ChatFragment;
import com.volunteer.thc.volunteerapp.presentation.organiser.OrganiserEventsFragment;
import com.volunteer.thc.volunteerapp.presentation.organiser.OrganiserProfileFragment;
import com.volunteer.thc.volunteerapp.presentation.organiser.OrganiserScoreboardFragment;
import com.volunteer.thc.volunteerapp.presentation.volunteer.VolunteerEventsFragment;
import com.volunteer.thc.volunteerapp.presentation.volunteer.VolunteerMyEventsFragment;
import com.volunteer.thc.volunteerapp.presentation.volunteer.VolunteerProfileFragment;
import com.volunteer.thc.volunteerapp.presentation.volunteer.VolunteerRewardsFragment;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private TextView mUserStatus, mUserName;
    private SharedPreferences mPrefs;
    private CircleImageView mImage;
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private Intent serviceIntent;
    private MenuItem eventsItem, selectedItem;
    private UserType mUserType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string
                .navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        serviceIntent = new Intent(this, FirebaseNewsService.class);
        startService(serviceIntent);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);
        eventsItem = navigationView.getMenu().findItem(R.id.nav_events);
        eventsItem.setChecked(true);

        View header = navigationView.getHeaderView(0);
        mImage = header.findViewById(R.id.photo);
        mUserName = header.findViewById(R.id.nav_header_name);
        mUserStatus = header.findViewById(R.id.nav_header_status);

        storageRef.child("Photos").child("User").child(mUser.getUid()).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.with(getApplicationContext()).load(uri).fit().centerCrop().into
                                (mImage);
                    }
                });

        mPrefs = getApplicationContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        ValueEventListener statusListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SharedPreferences.Editor editor = mPrefs.edit();
                if (dataSnapshot.hasChildren()) {
                    mUserType = UserType.VOLUNTEER;

                    Volunteer volunteer = dataSnapshot.getValue(Volunteer.class);
                    if (volunteer != null) {
                        String gender = volunteer.getGender();
                        String firstName = volunteer.getFirstname();
                        String lastName = volunteer.getLastname();
                        editor.putString("name", firstName + " " + lastName);
                        mUserName.setText(firstName + " " + lastName);

                        if (TextUtils.equals(gender, "Male")) {
                            editor.putString("gender", "Male");
                        } else {
                            editor.putString("gender", "Female");
                        }
                    } else {
                        // an error occurred: data is missing from volunteer
                        // TODO throw an exception or handle it
                        final String errorMsg = "Volunteer logged in, but the data was missing " +
                                "for user with id " + mUser.getUid();
                        Log.e(TAG, errorMsg);
                        Crashlytics.logException(new Exception(errorMsg));
                        return;
                    }
                } else {
                    mUserType = UserType.ORGANISER;
                    editor.putString("gender", mUserType.getPrefsValue());
                    mUserName.setText(mUser.getEmail());
                }

                editor.putString("user_status", mUserType.getPrefsValue());
                editor.putString("user_id", mUser.getUid());
                editor.apply();

                mUserStatus.setText(mUserType == UserType.VOLUNTEER ? getResources().getString(R
                        .string.volunteer) : getResources().getString(R.string.organiser));
                showEvents();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getDetails());
                Answers.getInstance().logCustom(
                        new CustomEvent("Error")
                                .putCustomAttribute("Error passing data for User", mUser.getUid()));
            }
        };

        String userType = mPrefs.getString("user_status", null);
        mUserType = UserType.lookupFromPrefsValue(userType);

        if (mUserType == null) {
            mDatabase.child("users").child("volunteers").child(mUser.getUid())
                    .addListenerForSingleValueEvent(statusListener);
        } else {
            if (mUserType == UserType.VOLUNTEER) {
                mUserName.setText(mPrefs.getString("name", null));
            } else {
                mUserName.setText(mUser.getEmail());
            }

            mUserStatus.setText(mUserType == UserType.VOLUNTEER ? getResources().getString(R
                    .string.volunteer) : getResources().getString(R.string.organiser));
            showEvents();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Events");
        }

        drawer.closeDrawers();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
            if (fragment instanceof OrganiserEventsFragment || fragment instanceof
                    VolunteerEventsFragment) {
                super.onBackPressed();
            } else {
                final String userStatus = mPrefs.getString("user_status", null);
                if (TextUtils.equals(userStatus, "Volunteer")) {
                    replaceFragmentByClass(new VolunteerEventsFragment());
                } else {
                    replaceFragmentByClass(new OrganiserEventsFragment());
                }

                if (selectedItem != null) {
                    selectedItem.setChecked(false);
                }
                eventsItem.setChecked(true);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Events");
                }
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        selectedItem = item;
        if (isNetworkAvailable()) {
            String actionBarTitle = getActionBar() == null ? "" : String.valueOf(getActionBar()
                    .getTitle());
            final String userStatus = mPrefs.getString("user_status", null);
            final String volunteer = "Volunteer";
            switch (id) {
                case R.id.nav_events: {
                    if (mUserType == UserType.VOLUNTEER) {
                        replaceFragmentByClass(new VolunteerEventsFragment());
                    } else {
                        replaceFragmentByClass(new OrganiserEventsFragment());
                    }
                    actionBarTitle = "Events";
                    break;
                }
                case R.id.user_events: {
                    replaceFragmentByClass(new VolunteerMyEventsFragment());
                    actionBarTitle = "My Events";
                    break;
                }
                case R.id.nav_profile: {
                    if (TextUtils.equals(userStatus, volunteer)) {
                        replaceFragmentByClass(new VolunteerProfileFragment());
                    } else {
                        replaceFragmentByClass(new OrganiserProfileFragment());
                    }
                    actionBarTitle = "Profile";
                    break;
                }
                case R.id.nav_settings: {
                    replaceFragmentByClass(new SettingsFragment());
                    actionBarTitle = "Settings";
                    break;
                }
                case R.id.nav_news: {
                    replaceFragmentByClass(new NewsFragment());
                    actionBarTitle = "News";
                    break;
                }
                case R.id.nav_chat: {
                    replaceFragmentByClass(new ChatFragment());
                    actionBarTitle = "Interview";
                    break;
                }
                case R.id.nav_scoreboard: {
                    if (TextUtils.equals(userStatus, volunteer)) {
                        replaceFragmentByClass(new VolunteerRewardsFragment());
                    } else {
                        replaceFragmentByClass(new OrganiserScoreboardFragment());
                    }
                    actionBarTitle = "Scoreboard";
                    break;
                }
                case R.id.nav_logout: {
                    AlertDialog logoutAlertDialog = new AlertDialog.Builder(this)
                            .setTitle("Are you sure?")
                            .setMessage("Are you sure you want to log out?")
                            .setCancelable(true)
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mAuth.signOut();
                                    SharedPreferences.Editor editor = mPrefs.edit();
                                    editor.putString("user_status", null);
                                    editor.putString("name", null);
                                    editor.putString("gender", null);
                                    editor.apply();
                                    stopService(serviceIntent);
                                    startActivity(new Intent(MainActivity.this, LoginActivity
                                            .class));
                                    finish();
                                }
                            })
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .create();
                    logoutAlertDialog.show();
                    break;
                }
            }

            if (eventsItem.isChecked()) {
                eventsItem.setChecked(false);
            }

            item.setChecked(true);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(actionBarTitle);
            }
        } else {
            Toast.makeText(MainActivity.this, "No internet connection.", Toast.LENGTH_LONG).show();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showEvents() {
        switch (mUserType) {
            case VOLUNTEER:
                NavigationView navView = findViewById(R.id.nav_view);
                Menu navMenu = navView.getMenu();
                navMenu.findItem(R.id.user_events).setVisible(true);
                replaceFragmentByClass(new VolunteerEventsFragment());
                break;
            case ORGANISER:
                replaceFragmentByClass(new OrganiserEventsFragment());
                break;
            default:
                Toast.makeText(MainActivity.this, "An error occurred", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void replaceFragmentByClass(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private void setCheckedFalse(MenuItem item) {
        if (item.isChecked()) {
            item.setChecked(false);
        }
    }
}
