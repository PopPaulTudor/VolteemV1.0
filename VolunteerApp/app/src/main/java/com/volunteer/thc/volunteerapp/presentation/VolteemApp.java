package com.volunteer.thc.volunteerapp.presentation;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.volunteer.thc.volunteerapp.BuildConfig;

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

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(BuildConfig.DEBUG);
        built.setLoggingEnabled(BuildConfig.DEBUG);
        Picasso.setSingletonInstance(built);
    }
}
