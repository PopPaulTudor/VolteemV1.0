package com.volunteer.thc.volunteerapp.presentation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
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
import com.volunteer.thc.volunteerapp.adapter.NewsAdapter;
import com.volunteer.thc.volunteerapp.callback.ActionListener;
import com.volunteer.thc.volunteerapp.model.NewsMessage;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by poppa on 25.08.2017.
 */

public class NewsFragment extends Fragment implements ActionListener.NewsDeletedListener {

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private ArrayList<NewsMessage> news = new ArrayList<>();
    private RecyclerView newsRecView;
    private TextView noNewsTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        noNewsTextView = view.findViewById(R.id.no_news_text);
        newsRecView = view.findViewById(R.id.newsRecView);
        newsRecView.setHasFixedSize(true);
        mDatabase.child("news").orderByChild("receivedBy").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    news.add(dataSnapshot1.getValue(NewsMessage.class));
                }

                if(news.isEmpty()){
                    noNewsTextView.setVisibility(View.VISIBLE);
                }

                Collections.reverse(news);
                NewsAdapter adapter = new NewsAdapter(news, getActivity(), NewsFragment.this);
                ItemTouchHelper itemTouchHelper = adapter.getItemTouchHelper();
                newsRecView.setAdapter(adapter);
                itemTouchHelper.attachToRecyclerView(newsRecView);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                newsRecView.setLayoutManager(linearLayoutManager);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("NewsFragment", databaseError.getMessage());
            }
        });

        return view;
    }

    @Override
    public void onNewsDeleted() {
        noNewsTextView.setVisibility(View.VISIBLE);
    }
}
