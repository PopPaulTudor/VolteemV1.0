package com.volunteer.thc.volunteerapp.presentation;

import android.content.Context;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import com.volunteer.thc.volunteerapp.notification.FirebaseNewsService;
import com.volunteer.thc.volunteerapp.presentation.organiser.OrganiserEventsFragment;
import com.volunteer.thc.volunteerapp.presentation.organiser.OrganiserProfileFragment;
import com.volunteer.thc.volunteerapp.presentation.organiser.OrganiserScoreboardFragment;
import com.volunteer.thc.volunteerapp.presentation.volunteer.VolunteerEventsFragment;
import com.volunteer.thc.volunteerapp.presentation.volunteer.VolunteerMyEventsFragment;
import com.volunteer.thc.volunteerapp.presentation.volunteer.VolunteerProfileFragment;
import com.volunteer.thc.volunteerapp.presentation.volunteer.VolunteerScoreboardFragment;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseAuth Auth = FirebaseAuth.getInstance();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private TextView mUserStatus, mUserName;
    private SharedPreferences prefs;
    private CircleImageView mImage;
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        serviceIntent = new Intent(this, FirebaseNewsService.class);
        startService(serviceIntent);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        View header = navigationView.getHeaderView(0);
        mImage = (CircleImageView) header.findViewById(R.id.photo);
        mUserName = (TextView) header.findViewById(R.id.nav_header_name);
        mUserStatus = (TextView) header.findViewById(R.id.nav_header_status);

        storageRef.child("Photos").child("User").child(user.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(getApplicationContext()).load(uri).fit().centerCrop().into(mImage);
            }
        });

        prefs = getApplicationContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        ValueEventListener statusListener = (new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SharedPreferences.Editor editor = prefs.edit();
                String userType;
                if (dataSnapshot.hasChildren()) {
                    userType = "Volunteer";
                    String gender = dataSnapshot.child("gender").getValue().toString();
                    String firstName = dataSnapshot.child("firstname").getValue().toString();
                    String lastName = dataSnapshot.child("lastname").getValue().toString();
                    editor.putString("name", firstName + " " + lastName);
                    mUserName.setText(firstName + " " + lastName);

                    if (TextUtils.equals(gender, "Male")) {
                        editor.putString("gender", "Male");
                    } else {
                        editor.putString("gender", "Female");
                    }
                } else {
                    userType = "Organiser";
                    editor.putString("gender", userType);
                    mUserName.setText(user.getEmail());
                }

                editor.putString("user_status", userType);
                editor.putString("user_id", user.getUid());
                editor.apply();

                mUserStatus.setText(userType);
                showEvents(userType);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Cancelled: ", "Cancelled \n" + databaseError.getDetails());
            }
        });

        String userStatus = prefs.getString("user_status", null);
        if (userStatus == null) {
            mDatabase.child("users").child("volunteers").child(user.getUid()).addListenerForSingleValueEvent(statusListener);
            mDatabase.removeEventListener(statusListener); // is this really needed?
        } else {
            String name = prefs.getString("name", null);

            if (TextUtils.equals(userStatus, "Volunteer")) {
                mUserName.setText(name);

            } else {
                mUserName.setText(user.getEmail());
            }


            mUserStatus.setText(userStatus);
            showEvents(userStatus);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Events");
        }
        drawer.closeDrawers();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            int count = getFragmentManager().getBackStackEntryCount();
            if (count == 0) {
                super.onBackPressed();
            } else {
                getFragmentManager().popBackStackImmediate();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (isNetworkAvailable()) {
            String actionBarTitle = getActionBar() == null ? "" : String.valueOf(getActionBar().getTitle());
            final String userStatus = prefs.getString("user_status", null);
            final String volunteer = "Volunteer";
            switch (id) {
                case R.id.nav_events: {
                    prefs.edit().putInt("cameFrom", 1).apply();
                    if (TextUtils.equals(userStatus, volunteer)) {
                        replaceFragmentByClass(new VolunteerEventsFragment());
                    } else {
                        replaceFragmentByClass(new OrganiserEventsFragment());
                    }
                    actionBarTitle = "Events";
                    break;
                }
                case R.id.user_events: {
                    prefs.edit().putInt("cameFrom", 2).apply();
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
                    actionBarTitle = "Chat";
                    break;
                }
                case R.id.nav_scoreboard: {
                    if (TextUtils.equals(userStatus, volunteer)) {
                        replaceFragmentByClass(new VolunteerScoreboardFragment());
                    } else {
                        replaceFragmentByClass(new OrganiserScoreboardFragment());
                    }
                    actionBarTitle = "Scoreboard";
                    break;
                }
                case R.id.nav_logout: {
                    Auth.signOut();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("user_status", null);
                    editor.putString("name", null);
                    editor.putString("gender", null);
                    editor.apply();
                    stopService(serviceIntent);
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                    break;
                }
            }

            item.setChecked(true);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(actionBarTitle);
            }
        } else {
            Toast.makeText(MainActivity.this, "No internet connection.", Toast.LENGTH_LONG).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showEvents(String userStatus) {
        prefs.edit().putInt("cameFrom", 1).apply();

        if (TextUtils.equals(userStatus, "Volunteer")) {
            NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
            Menu navMenu = navView.getMenu();
            navMenu.findItem(R.id.user_events).setVisible(true);
            replaceFragmentByClass(new VolunteerEventsFragment());
        } else {
            replaceFragmentByClass(new OrganiserEventsFragment());
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
}
