package com.volunteer.thc.volunteerapp.presentation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private TextView mUserStatus;
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
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().getItem(0).setChecked(true);

        View header = navigationView.getHeaderView(0);
        mUserStatus = (TextView) header.findViewById(R.id.nav_header_status);

        prefs = getApplicationContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        ValueEventListener mStatusListener = (new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                SharedPreferences.Editor editor = prefs.edit();
                String userstatus;
                if (dataSnapshot.hasChildren()) {
                    mUserStatus.setText("Volunteer");
                    editor.putString("user_status", "Volunteer");
                    editor.apply();
                    userstatus = "Volunteer";

                } else {
                    mUserStatus.setText("Organiser");
                    editor.putString("user_status", "Organiser");
                    editor.apply();
                    userstatus = "Organiser";
                }
                showEvents(userstatus);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Cancelled: ", "Cancelled");
            }
        });

        String userstatus = prefs.getString("user_status", null);

        if(TextUtils.equals(userstatus, null)) {

            mDatabase.child("users").child("volunteers").child(user.getUid()).addListenerForSingleValueEvent(mStatusListener);
            mDatabase.removeEventListener(mStatusListener);
        } else {

            mUserStatus.setText(userstatus);
            showEvents(userstatus);
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
            int count = getFragmentManager().getBackStackEntryCount();
            if(count == 0) {
                super.onBackPressed();
            } else {
                getFragmentManager().popBackStack();
            }
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

        if(isNetworkAvailable()) {

            if (id == R.id.nav_events) {

                prefs.edit().putInt("cameFrom", 1).commit();

                String userstatus = prefs.getString("user_status", null);
                if (TextUtils.equals(userstatus, "Volunteer")) {
                    replaceFragmentByClass(new VolunteerEventsFragment());
                } else {
                    replaceFragmentByClass(new OrganiserEventsFragment());
                }

                getSupportActionBar().setTitle("Events");
                item.setChecked(true);

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawers();


            } else if (id == R.id.user_events) {

                prefs.edit().putInt("cameFrom", 2).commit();
                replaceFragmentByClass(new VolunteerMyEventsFragment());
                getSupportActionBar().setTitle("My Events");
                item.setChecked(true);

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawers();

            } else if (id == R.id.nav_profile) {

                String userstatus = prefs.getString("user_status", null);
                if (TextUtils.equals(userstatus, "Volunteer")) {

                    replaceFragmentByClass(new VolunteerProfileFragment());
                } else {

                    replaceFragmentByClass(new OrganiserProfileFragment());
                }
                getSupportActionBar().setTitle("Profile");
                item.setChecked(true);

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawers();

            } else if (id == R.id.nav_settings) {

                replaceFragmentByClass(new SettingsFragment());
                getSupportActionBar().setTitle("Settings");
                item.setChecked(true);
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawers();

            } else if (id == R.id.nav_logout) {

                Auth.signOut();
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("user_status", null);
                editor.apply();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
            Toast.makeText(MainActivity.this, "No internet connection.", Toast.LENGTH_LONG).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showEvents(String userstatus) {

        prefs.edit().putInt("cameFrom", 1).commit();

        if(TextUtils.equals(userstatus, "Volunteer")) {

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

    private boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
}
