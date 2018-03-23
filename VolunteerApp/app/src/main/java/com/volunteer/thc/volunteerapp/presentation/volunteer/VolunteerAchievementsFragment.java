package com.volunteer.thc.volunteerapp.presentation.volunteer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.adapter.AchievementsAdapter;

import java.util.ArrayList;

/**
 * Created by poppa on 23.01.2018.
 */

public class VolunteerAchievementsFragment extends Fragment{

    private ArrayList<String> checkList = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_volunteer_achivements, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.volunteer_achivements_recycler_view);
        recyclerView.setHasFixedSize(true);

        checkList.add("Da");
        checkList.add("Da");
        checkList.add("Da");
        checkList.add("Da");
        checkList.add("Da");
        checkList.add("Da");
        checkList.add("Da");
        checkList.add("Da");
        checkList.add("Da");

        AchievementsAdapter adapter = new AchievementsAdapter(checkList,getContext());
        recyclerView.setAdapter(adapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(),3);
        recyclerView.setLayoutManager(gridLayoutManager);




        return view;
    }
}
