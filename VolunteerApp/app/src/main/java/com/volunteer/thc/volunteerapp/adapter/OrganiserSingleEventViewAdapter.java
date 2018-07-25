package com.volunteer.thc.volunteerapp.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Cristi on 7/27/2017.
 */

public class OrganiserSingleEventViewAdapter extends FragmentPagerAdapter {

    private Fragment mEventInfo;
    private Fragment mRegisteredUsers;
    private Fragment mAcceptedUsers;

    public OrganiserSingleEventViewAdapter(FragmentManager fm, Fragment mEventInfo, Fragment mRegisteredUsers, Fragment mAcceptedUsers) {
        super(fm);
        this.mEventInfo = mEventInfo;
        this.mRegisteredUsers = mRegisteredUsers;
        this.mAcceptedUsers = mAcceptedUsers;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return mEventInfo;
            case 1:
                return mRegisteredUsers;
            case 2:
                return mAcceptedUsers;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Event info";
            case 1:
                return "Registered";
            case 2:
                return "Accepted";
        }
        return null;
    }
}
