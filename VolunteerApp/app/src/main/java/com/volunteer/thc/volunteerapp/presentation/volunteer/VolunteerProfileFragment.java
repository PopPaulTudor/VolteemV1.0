package com.volunteer.thc.volunteerapp.presentation.volunteer;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.volunteer.thc.volunteerapp.R;


public class VolunteerProfileFragment extends Fragment {

    private static final int NUM_PAGES = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_volunteerprofile, container, false);
        ViewPager pager = view.findViewById(R.id.view_pager);
        PagerAdapter pagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager());
        pager.setPageTransformer(true, new ZoomOutPageTransformer());
        pager.setAdapter(pagerAdapter);
        pager.setCurrentItem(1);


        setHasOptionsMenu(true);
        return view;
    }


    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new VolunteerProfileEventsFragment();
                case 1:
                    return new VolunteerProfileInformationFragment();
                case 2:
                    return new VolunteerProfileQuestionFragment();
                default:
                    return null;
            }

        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }


}
