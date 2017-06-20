package com.volunteer.thc.volunteerapp.presentation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.volunteer.thc.volunteerapp.R;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseAuth Auth = FirebaseAuth.getInstance();
    private FragmentTransaction mFragmentTransaction;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private TextView mUserStatus, mUserName;
    private ValueEventListener mStatusListener;
    private SharedPreferences prefs;


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

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        mUserStatus = (TextView) header.findViewById(R.id.nav_header_status);
        mUserName = (TextView) header.findViewById(R.id.nav_header_name);

        prefs = getApplicationContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        mStatusListener = (new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                SharedPreferences.Editor editor = prefs.edit();

                if (dataSnapshot.hasChildren()) {
                    mUserStatus.setText("Volunteer");
                    editor.putString("user_status", "Volunteer");
                    editor.apply();
                } else {
                    mUserStatus.setText("Organiser");
                    editor.putString("user_status", "Organiser");
                    editor.apply();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Cancelled: ", "Cancelled");
            }
        });

        String userstatus = prefs.getString("user_status", null);
        String username = user.getDisplayName();

        if(TextUtils.equals(userstatus, null)) {
            mDatabase.child("users").child("volunteers").child(user.getUid()).addListenerForSingleValueEvent(mStatusListener);
            mDatabase.removeEventListener(mStatusListener);
            userstatus = prefs.getString("user_status", null);
        }

        mUserName.setText(username);
        mUserStatus.setText(userstatus);

        if(TextUtils.equals(userstatus, "Volunteer")) {
            mFragmentTransaction = getSupportFragmentManager().beginTransaction();
            mFragmentTransaction.replace(R.id.main_container, new VolunteerEventsFragment());
            mFragmentTransaction.commit();
        } else {
            mFragmentTransaction = getSupportFragmentManager().beginTransaction();
            mFragmentTransaction.replace(R.id.main_container, new OrganiserEventsFragment());
            mFragmentTransaction.commit();
        }

        getSupportActionBar().setTitle("Events");

        drawer.closeDrawers();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_events) {

            String userstatus = prefs.getString("user_status", null);
            if(TextUtils.equals(userstatus, "Volunteer")) {
                mFragmentTransaction = getSupportFragmentManager().beginTransaction();
                mFragmentTransaction.replace(R.id.main_container, new VolunteerEventsFragment());
            } else {
                mFragmentTransaction = getSupportFragmentManager().beginTransaction();
                mFragmentTransaction.replace(R.id.main_container, new OrganiserEventsFragment());
            }

            mFragmentTransaction.commit();
            getSupportActionBar().setTitle("Events");
            item.setChecked(true);

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawers();


        } else if (id == R.id.nav_profile) {

            String userstatus = prefs.getString("user_status", null);
            if(TextUtils.equals(userstatus, "Volunteer")) {

                mFragmentTransaction = getSupportFragmentManager().beginTransaction();
                mFragmentTransaction.replace(R.id.main_container, new VolunteerProfileFragment());
                mFragmentTransaction.commit();
            } else {

                mFragmentTransaction = getSupportFragmentManager().beginTransaction();
                mFragmentTransaction.replace(R.id.main_container, new OrganiserProfileFragment());
                mFragmentTransaction.commit();
            }
            getSupportActionBar().setTitle("Profile");
            item.setChecked(true);

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawers();

        } else if (id == R.id.nav_settings) {

            mFragmentTransaction = getSupportFragmentManager().beginTransaction();
            mFragmentTransaction.replace(R.id.main_container, new SettingsFragment());

            mFragmentTransaction.commit();
            getSupportActionBar().setTitle("Settings");
            item.setChecked(true);
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawers();

        } else if (id == R.id.nav_logout){
            Auth.signOut();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("user_status", null);
            editor.apply();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
