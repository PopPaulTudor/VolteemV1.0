package com.volunteer.thc.volunteerapp.presentation;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.snatik.storage.Storage;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.AppConfig;
import com.volunteer.thc.volunteerapp.util.VolteemConstants;

/**
 * Created by poppa on 14.08.2017.
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        Storage storage = new Storage(getApplicationContext());
        storage.createDirectory(VolteemConstants.VOLTEEM_DIRECTORY_NAME);

        checkAppVersion();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case VolteemConstants.STORAGE_REQUEST_CODE: {
                if (grantResults.length <= 0 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, getString(R.string.storage_permission_denied), Toast.LENGTH_LONG).show();
                }
                startActivityByClass(LoginActivity.class);
            }
        }
    }

    private void startActivityByClass(Class activityClass) {
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
        startActivity(new Intent(this, activityClass), bundle);
        finish();
    }

    private void setupAndContinue() {
        SharedPreferences sharedPreferences = SplashActivity.this.getPreferences(Context.MODE_PRIVATE);
        if (!sharedPreferences.contains(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            sharedPreferences.edit().putInt(VolteemConstants.STORAGE_PERMISSION_OPENED, VolteemConstants.STORAGE_REQUEST_CODE)
                    .apply();
            ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    VolteemConstants.STORAGE_REQUEST_CODE);
        } else if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivityByClass(MainActivity.class);
        } else {
            startActivityByClass(LoginActivity.class);
        }
    }

    private void checkAppVersion() {
        FirebaseDatabase.getInstance().getReference().child("config").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                AppConfig latestConfiguration = null;
                // TODO search all version from current one to last one and if one is not backwards compatible, then the result will be not compatible
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    AppConfig config = snapshot.getValue(AppConfig.class);
                    if (config != null) {
                        if (latestConfiguration == null || latestConfiguration.getReleaseDate() < config.getReleaseDate()) {
                            latestConfiguration = config;
                        }
                    }
                }

                if (latestConfiguration != null) {
                    try {
                        PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
                        String currentVersion = String.valueOf(info.versionCode);

                        if (!currentVersion.equals(latestConfiguration.getAppVersion()) && !latestConfiguration.isBackwardsCompatible()) {
                            AlertDialog.Builder builder;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                builder = new AlertDialog.Builder(SplashActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                            } else {
                                builder = new AlertDialog.Builder(SplashActivity.this);
                            }
                            builder.setTitle(getString(R.string.old_version_title))
                                    .setMessage(getString(R.string.old_version_message))
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            SplashActivity.this.finish();
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        } else {
                            setupAndContinue();
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.e(TAG, e.getMessage());
                        setupAndContinue();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
                setupAndContinue();
            }
        });
    }
}
