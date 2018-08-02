package com.volunteer.thc.volunteerapp.presentation.volunteer;


import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.volunteer.thc.volunteerapp.R;


public class VolunteerProfileFragment extends Fragment {

    private static final int NUM_PAGES = 3;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_volunteerprofile, container, false);
        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        TextView mUserName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_header_name);

        mPager = (ViewPager) view.findViewById(R.id.view_pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager());
        mPager.setPageTransformer(true, new ZoomOutPageTransformer());
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(1);
        
        setHasOptionsMenu(true);
        return view;
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new VolunteerProfilePastEventsFragment();
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
