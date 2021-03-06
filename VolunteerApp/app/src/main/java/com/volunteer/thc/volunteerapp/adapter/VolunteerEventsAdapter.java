package com.volunteer.thc.volunteerapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import com.volunteer.thc.volunteerapp.callback.ActionListener;
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.presentation.volunteer.VolunteerSingleEventActivity;
import com.volunteer.thc.volunteerapp.util.VolteemConstants;

import java.util.List;

/**
 * Created by Vlad on 12.02.2018.
 */
public class VolunteerEventsAdapter extends OrganiserEventsAdapter {

    private int mCameFrom;

    public VolunteerEventsAdapter(List<Event> list, Context context, Resources resources, final int
            FLAG, ActionListener.EventPicturesLoadingListener eventPicturesLoadingListener, int
                                          cameFrom) {
        super(list, context, resources, FLAG, eventPicturesLoadingListener);
        this.mCameFrom = cameFrom;
    }

    @Override
    protected void startSingleEventActivity(int position) {
        Intent intent = new Intent(context.getApplicationContext(), VolunteerSingleEventActivity
                .class);
        intent.putExtra(VolteemConstants.INTENT_EXTRA_SINGLE_EVENT, eventsList.get(position));
        intent.putExtra(VolteemConstants.VOLUNTEER_SINGLE_ACTIVITY_CAME_FROM_KEY, mCameFrom);
        context.startActivity(intent);
    }
}
