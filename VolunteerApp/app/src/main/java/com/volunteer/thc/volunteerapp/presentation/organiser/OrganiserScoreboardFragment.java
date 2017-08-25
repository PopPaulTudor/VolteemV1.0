package com.volunteer.thc.volunteerapp.presentation.organiser;

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
import com.volunteer.thc.volunteerapp.model.Organiser;
import com.volunteer.thc.volunteerapp.model.OrganiserRating;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Cristi on 8/23/2017.
 */

public class OrganiserScoreboardFragment extends Fragment {

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private TextView mRating, mRank, mExperience, mCongratsText;
    private Button showHideLeaderboard;
    private RecyclerView leaderboardRecView;
    private ProgressBar mProgressBar;
    private int experience, ranking;
    private OrganiserRating organiserRating;
    private ArrayList<Organiser> leaderboard;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organiser_scoreboard, container, false);

        mRating = (TextView) view.findViewById(R.id.rating);
        mRank = (TextView) view.findViewById(R.id.leaderboardPosition);
        mExperience = (TextView) view.findViewById(R.id.experienceText);
        mCongratsText = (TextView) view.findViewById(R.id.congratsText);
        showHideLeaderboard = (Button) view.findViewById(R.id.leaderboard_show_hide);
        mProgressBar = (ProgressBar) view.findViewById(R.id.indeterminateBar);
        leaderboardRecView = (RecyclerView) view.findViewById(R.id.leaderboardRecView);
        leaderboardRecView.setHasFixedSize(true);

        mDatabase.child("users/organisers/" + user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                experience = dataSnapshot.child("experience").getValue(Integer.class);
                organiserRating = dataSnapshot.child("org_rating").getValue(OrganiserRating.class);
                mExperience.setText("XP: " + experience);
                mRating.setText("Rating: " + organiserRating.getRating() + "/5");
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.child("users/organisers").orderByChild("experience").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ranking = (int) dataSnapshot.getChildrenCount();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    if (TextUtils.equals(dataSnapshot1.getKey(), user.getUid())) {
                        if (ranking <= 10) {
                            mCongratsText.setVisibility(View.VISIBLE);
                        }
                        mRank.setText("Rank: " + ranking);
                    } else {
                        --ranking;
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
                    showHideLeaderboard.setText("HIDE RANKING");
                    leaderboard = new ArrayList<>();
                    mProgressBar.setVisibility(View.VISIBLE);
                    mDatabase.child("users/organisers").orderByChild("experience").limitToLast(10).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                leaderboard.add(dataSnapshot1.getValue(Organiser.class));
                            }
                            Collections.reverse(leaderboard);
                            mProgressBar.setVisibility(View.GONE);
                            LeaderboardAdapter adapter = new LeaderboardAdapter(leaderboard, LeaderboardAdapter.ORGANISER);
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
