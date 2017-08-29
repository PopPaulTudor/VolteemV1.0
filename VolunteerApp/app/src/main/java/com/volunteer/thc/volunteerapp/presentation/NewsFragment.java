package com.volunteer.thc.volunteerapp.presentation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.adaptor.NewsAdapter;
import com.volunteer.thc.volunteerapp.model.NewsMessage;

import java.util.ArrayList;

/**
 * Created by poppa on 25.08.2017.
 */

public class NewsFragment extends Fragment {

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private ArrayList<NewsMessage> news = new ArrayList<>();
    private RecyclerView newsRecView;
    private TextView noNewsText;
    private ImageView noNewsImage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        noNewsText = (TextView) view.findViewById(R.id.no_news_text);
        noNewsImage = (ImageView) view.findViewById(R.id.no_news_image);
        newsRecView = (RecyclerView) view.findViewById(R.id.newsRecView);
        newsRecView.setHasFixedSize(true);
        mDatabase.child("news").orderByChild("receivedBy").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    news.add(dataSnapshot1.getValue(NewsMessage.class));
                }

                if(news.isEmpty()){
                    noNewsImage.setVisibility(View.VISIBLE);
                    noNewsText.setVisibility(View.VISIBLE);
                }
                NewsAdapter adapter = new NewsAdapter(news, getActivity());
                newsRecView.setAdapter(adapter);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                newsRecView.setLayoutManager(linearLayoutManager);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }
}
