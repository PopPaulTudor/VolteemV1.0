package com.volunteer.thc.volunteerapp.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.volunteer.thc.volunteerapp.presentation.organiser.OrganiserRegisterFragment;
import com.volunteer.thc.volunteerapp.presentation.volunteer.VolunteerRegisterFragment;

/**
 * Created by Cristi on 6/18/2017.
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private Fragment mVolunteerFragment = new VolunteerRegisterFragment();
    private Fragment mOrganiserFragment = new OrganiserRegisterFragment();

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return mVolunteerFragment;
            case 1:
                return mOrganiserFragment;
        }
        return null;
    }

    @Override
    public int getCount() {

        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Volunteer";
            case 1:
                return "Organiser";
        }
        return null;
    }
}

