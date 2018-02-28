package com.volunteer.thc.volunteerapp.presentation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.snatik.storage.Storage;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.util.VolteemConstants;

/**
 * Created by poppa on 14.08.2017.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);

        Storage storage = new Storage(getApplicationContext());
        storage.createDirectory(VolteemConstants.VOLTEEM_DIRECTORY_NAME);

        if (!sharedPreferences.contains(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            sharedPreferences.edit().putInt(VolteemConstants.STORAGE_PERMISSION_OPENED, VolteemConstants.STORAGE_REQUEST_CODE).apply();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, VolteemConstants.STORAGE_REQUEST_CODE);
        } else if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivityByClass(MainActivity.class);
        } else {
            startActivityByClass(LoginActivity.class);
        }
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

    void startActivityByClass(Class activityClass) {
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
        startActivity(new Intent(this, activityClass), bundle);
        finish();
    }
}
