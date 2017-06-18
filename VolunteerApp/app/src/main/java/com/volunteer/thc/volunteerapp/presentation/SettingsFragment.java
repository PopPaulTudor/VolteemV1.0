package com.volunteer.thc.volunteerapp.presentation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.volunteer.thc.volunteerapp.R;

/**
 * Created by Cristi on 6/17/2017.
 */

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        Button delete_account = (Button)view.findViewById(R.id.button_delete_account);

        //TODO: do something on settings page

        return view;
    }

}
