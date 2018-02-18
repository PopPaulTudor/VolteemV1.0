package com.volunteer.thc.volunteerapp.presentation;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

/**
 * Class used for initializing different things.
 * Created by Vlad on 30.12.2017.
 */
public class VolteemApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // TODO IMPORTANT!! on app release, remove debug from Fabric
        final Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics())
                .debuggable(true)
                .build();
        Fabric.with(fabric);
    }
}
