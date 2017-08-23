package com.volunteer.thc.volunteerapp.presentation.volunteer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.adaptor.LeaderboardAdapter;
import com.volunteer.thc.volunteerapp.model.Volunteer;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Cristi on 8/23/2017.
 */

public class VolunteerScoreboardFragment extends Fragment {

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private int experience;
    private TextView experienceText, congratsText, leaderboardPosition;
    private ProgressBar mProgressBar;
    private int position;
    private Button showHideLeaderboard;
    private ArrayList<Volunteer> leaderboard;
    private RecyclerView leaderboardRecView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_volunteer_scoreboard, container, false);
        experienceText = (TextView) view.findViewById(R.id.experienceText);
        mProgressBar = (ProgressBar) view.findViewById(R.id.indeterminateBar);
        leaderboardPosition = (TextView) view.findViewById(R.id.leaderboardPosition);
        congratsText = (TextView) view.findViewById(R.id.congratsText);
        showHideLeaderboard = (Button) view.findViewById(R.id.leaderboard_show_hide);
        leaderboardRecView = (RecyclerView) view.findViewById(R.id.leaderboardRecView);
        leaderboardRecView.setHasFixedSize(true);

        mDatabase.child("users/volunteers/" + user.getUid() + "/experience").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                experience = dataSnapshot.getValue(Integer.class);
                experienceText.setText("XP: " + experience);
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.child("users/volunteers").orderByChild("experience").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                position = (int) dataSnapshot.getChildrenCount();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    if (TextUtils.equals(dataSnapshot1.getKey(), user.getUid())) {
                        ///We've found the volunteer's position in the leaderboard
                        if (position <= 10) {
                            congratsText.setVisibility(View.VISIBLE);
                        }
                        leaderboardPosition.setText("Rank: " + position);
                    } else {
                        --position;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        showHideLeaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.equals(showHideLeaderboard.getText().toString(), "SHOW RANKING")) {
                    leaderboard = new ArrayList<>();
                    showHideLeaderboard.setText("HIDE RANKING");
                    mProgressBar.setVisibility(View.VISIBLE);
                    mDatabase.child("users/volunteers").orderByChild("experience").limitToLast(10).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                leaderboard.add(dataSnapshot1.getValue(Volunteer.class));
                            }
                            Collections.reverse(leaderboard);
                            mProgressBar.setVisibility(View.GONE);
                            LeaderboardAdapter adapter = new LeaderboardAdapter(leaderboard);
                            leaderboardRecView.setAdapter(adapter);
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                            leaderboardRecView.setLayoutManager(linearLayoutManager);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    showHideLeaderboard.setText("SHOW RANKING");
                    leaderboardRecView.setAdapter(null);
                }
            }
        });
        return view;
    }
}
