package com.volunteer.thc.volunteerapp.presentation;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

/**
 * Class used for getting Context outside of Activity and Fragment classes.
 * Created by Vlad on 30.12.2017.
 */
public class VolteemApp extends Application {

    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        // TODO IMPORTANT!! on app release, remove debug from Fabric
        final Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics())
                .debuggable(true)
                .build();
        Fabric.with(fabric);
    }
}
