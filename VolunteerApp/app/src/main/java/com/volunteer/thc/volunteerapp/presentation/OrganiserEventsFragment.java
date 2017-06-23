package com.volunteer.thc.volunteerapp.presentation;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.volunteer.thc.volunteerapp.R;

/**
 * Created by Cristi on 6/17/2017.
 */

public class OrganiserEventsFragment extends Fragment{

    private FloatingActionButton mAddEvent;
    private FragmentTransaction mFragmentTransaction;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                    Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organiserevents, container, false);

        mAddEvent = (FloatingActionButton) view.findViewById(R.id.add_event);

        mAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mFragmentTransaction = getFragmentManager().beginTransaction();
                mFragmentTransaction.replace(R.id.main_container, new CreateEventFragment());
                mFragmentTransaction.commit();
            }
        });

        //TODO: retrieve current organiser's events

        return view;
    }

}
