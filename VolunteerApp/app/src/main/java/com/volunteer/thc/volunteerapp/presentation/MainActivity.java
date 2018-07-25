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
import com.volunteer.thc.volunteerapp.util.VolteemConstants;

import de.hdodenhof.circleimageview.CircleImageView;
import me.leolin.shortcutbadger.ShortcutBadger;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private TextView mUserTypeView, mUserName;
    private SharedPreferences mPrefs;
    private CircleImageView mImage;
    private Intent serviceIntent;
    private MenuItem eventsItem, selectedItem, profileItem;
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
        selectedItem = eventsItem;
        profileItem = navigationView.getMenu().findItem(R.id.nav_profile);

        View header = navigationView.getHeaderView(0);
        mImage = header.findViewById(R.id.photo);
        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = mUserType == UserType.VOLUNTEER ? new VolunteerProfileFragment() :
                        new OrganiserProfileFragment();

                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);

                eventsItem.setChecked(false);
                if (selectedItem != null) {
                    selectedItem.setChecked(false);
                    selectedItem = profileItem;
                }
                profileItem.setChecked(true);

                replaceFragmentByClass(fragment);

                String actionBarTitle = getString(R.string.profile);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(actionBarTitle);
                }
            }
        });


        mUserName = header.findViewById(R.id.nav_header_name);
        mUserTypeView = header.findViewById(R.id.nav_header_status);

        storageRef.child("Photos").child("User").child(mUser.getUid()).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).fit().centerCrop().into
                                (mImage);
                    }
                });

        mPrefs = getApplicationContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        String userType = mPrefs.getString("user_status", null);
        mUserType = UserType.lookupFromPrefsValue(userType);

        if (mUserType == null) {
            mDatabase.child("users").child("volunteers").child(mUser.getUid())
                    .addListenerForSingleValueEvent(createVolunteerEventListener());
        } else {
            mUserName.setText(mUserType == UserType.VOLUNTEER ? mPrefs.getString("name", null) :
                    mUser.getEmail());
            mUserTypeView.setText(mUserType == UserType.VOLUNTEER ? getString(R
                    .string.volunteer) : getString(R.string.organiser));
            showEvents();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.events));
        }

        drawer.closeDrawers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // deselect all menu items
        deselectAllMenuItems();

        mPrefs.edit().putInt("badgeCount", 0).apply();
        ShortcutBadger.applyCount(this, 0);

        if (selectedItem != null) {
            selectedItem.setChecked(true);
        }
    }

    private void deselectAllMenuItems() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        for (int i = 0; i < navigationView.getMenu().size(); i++) {
            MenuItem menuItem = navigationView.getMenu().getItem(i);
            menuItem.setChecked(false);

            if (menuItem.getSubMenu() != null) {
                for (int j = 0; j < menuItem.getSubMenu().size(); j++) {
                    MenuItem menuItem2 = menuItem.getSubMenu().getItem(j);
                    menuItem2.setChecked(false);
                }
            }
        }
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
                if (mUserType == UserType.VOLUNTEER) {
                    replaceFragmentByClass(new VolunteerEventsFragment());
                } else {
                    replaceFragmentByClass(new OrganiserEventsFragment());
                }

                if (selectedItem != null) {
                    selectedItem.setChecked(false);
                }
                eventsItem.setChecked(true);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(getString(R
                            .string.events));
                }
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        boolean shouldShowAsSelected = true;
        selectedItem = item;

        if (isNetworkAvailable()) {
            Fragment fragment = null;
            String actionBarTitle = getActionBar() == null ? "" : String.valueOf(getActionBar()
                    .getTitle());
            switch (id) {
                case R.id.nav_events: {
                    fragment = mUserType == UserType.VOLUNTEER ? new VolunteerEventsFragment() :
                            new OrganiserEventsFragment();
                    actionBarTitle = getString(R
                            .string.events);
                    break;
                }
                case R.id.user_events: {
                    fragment = new VolunteerMyEventsFragment();
                    actionBarTitle = getString(R
                            .string.my_events);
                    break;
                }
                case R.id.nav_profile: {
                    fragment = mUserType == UserType.VOLUNTEER ? new VolunteerProfileFragment() :
                            new OrganiserProfileFragment();
                    actionBarTitle = getString(R
                            .string.profile);
                    break;
                }
                case R.id.nav_settings: {
                    fragment = new SettingsFragment();
                    actionBarTitle = getString(R
                            .string.settings);
                    break;
                }
                case R.id.nav_news: {
                    fragment = new NewsFragment();
                    actionBarTitle = getString(R
                            .string.news);
                    break;
                }
                case R.id.nav_chat: {
                    fragment = new ChatFragment();
                    actionBarTitle = getString(R
                            .string.interview);
                    break;
                }
                case R.id.nav_scoreboard: {
                    fragment = mUserType == UserType.VOLUNTEER ? new VolunteerRewardsFragment() :
                            new OrganiserScoreboardFragment();
                    actionBarTitle = getString(R.string.scoreboard);
                    break;
                }
                case R.id.nav_logout: {
                    shouldShowAsSelected = false;
                    final AlertDialog logoutAlertDialog = new AlertDialog.Builder(this)
                            .setTitle(getString(R
                                    .string.logout_message_title))
                            .setMessage(getString(R
                                    .string.logout_message))
                            .setCancelable(true)
                            .setPositiveButton(getString(R
                                    .string.logout_yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mAuth.signOut();
                                    SharedPreferences.Editor editor = mPrefs.edit();
                                    editor.clear();
                                    editor.apply();
                                    stopService(serviceIntent);
                                    startActivity(new Intent(MainActivity.this, LoginActivity
                                            .class));
                                    finish();
                                }
                            })
                            .setNegativeButton(getString(R
                                    .string.logout_no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // do nothing, just dismiss dialog
                                }
                            })
                            .create();
                    logoutAlertDialog.show();
                    break;
                }
                default:
                    Log.wtf(TAG, "Menu item not found.");
                    break;
            }

            profileItem.setChecked(false);
            if (eventsItem.isChecked()) {
                eventsItem.setChecked(false);
            }

            item.setChecked(shouldShowAsSelected);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(actionBarTitle);
            }

            if (fragment != null) {
                replaceFragmentByClass(fragment);
            }
        } else {
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_LONG).show();
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
                Log.e(TAG, "Error showing events for user " + mUser.getUid());
                Answers.getInstance().logCustom(new CustomEvent(VolteemConstants.USER_CUSTOM_ERROR_CRASHLYTICS)
                        .putCustomAttribute("Error showing events for user", mUser.getUid()));
                break;
        }
    }

    private void replaceFragmentByClass(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private ValueEventListener createVolunteerEventListener() {
        return new ValueEventListener() {
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
                        editor.putString("gender", gender);
                        mUserName.setText(firstName + " " + lastName);
                    } else {
                        // TODO throw an exception or handle the error
                        final String errorMsg = "Volunteer logged in, but the  firestore data was" +
                                " missing for user with id " + mUser.getUid();
                        Log.e(TAG, errorMsg);
                        Crashlytics.logException(new Exception(errorMsg));
                        return;
                    }
                } else {
                    mUserName.setText(mUser.getEmail());
                    mUserType = UserType.ORGANISER;
                    editor.putString("gender", mUserType.getPrefsValue());
                }

                editor.putString("user_status", mUserType.getPrefsValue());
                editor.putString("user_id", mUser.getUid());
                editor.apply();

                mUserTypeView.setText(mUserType == UserType.VOLUNTEER ? getString(R
                        .string.volunteer) : getString(R.string.organiser));
                showEvents();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getDetails());
                Answers.getInstance().logCustom(new CustomEvent(VolteemConstants.USER_CUSTOM_ERROR_CRASHLYTICS)
                        .putCustomAttribute("Error passing data for user", mUser.getUid()));
            }
        };
    }
}
