package com.volunteer.thc.volunteerapp.presentation.volunteer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.volunteer.thc.volunteerapp.R;

/**
 * Created by poppa on 17.01.2018.
 */

public class VolunteerProfileQuestionFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_volunteer_profile_question, container, false);


        return v;

    }
}
