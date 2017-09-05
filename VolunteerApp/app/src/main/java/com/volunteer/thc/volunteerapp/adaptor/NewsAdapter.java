package com.volunteer.thc.volunteerapp.adaptor;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.NewsMessage;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Cristi on 8/25/2017.
 */

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<NewsMessage> newsList;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private Context context;
    private Calendar date = Calendar.getInstance();

    public NewsAdapter(List<NewsMessage> list, Context context) {
        newsList = list;
        this.context = context;
    }

    @Override
    public NewsAdapter.NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_element, parent, false);

        return new NewsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final NewsAdapter.NewsViewHolder holder, final int position) {

        if(newsList.get(position).isStarred()) {
            holder.starredIcon.setVisibility(View.VISIBLE);
        }
        //TODO: display news for organisers too

        switch (newsList.get(position).getType()) {
            case 1:
                holder.typeIcon.setImageResource(R.drawable.ic_checked);
                break;
            case 2:
                holder.typeIcon.setImageResource(R.drawable.ic_feedback_news);
                break;
        }
        holder.content.setText(newsList.get(position).getContent());
        holder.item.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(newsList.get(position).isStarred()) {
                    Toast.makeText(context, "News unstarred.", Toast.LENGTH_SHORT).show();
                    mDatabase.child("news/" + newsList.get(position).getNewsID() + "/starred").setValue(false);
                    holder.starredIcon.setVisibility(View.GONE);
                    newsList.get(position).setStarred(false);
                } else {
                    Toast.makeText(context, "News starred.", Toast.LENGTH_SHORT).show();
                    mDatabase.child("news/" + newsList.get(position).getNewsID() + "/starred").setValue(true);
                    holder.starredIcon.setVisibility(View.VISIBLE);
                    newsList.get(position).setStarred(true);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    class NewsViewHolder extends RecyclerView.ViewHolder {

        TextView content;
        RelativeLayout item;
        ImageView typeIcon, starredIcon;

        NewsViewHolder(View v) {
            super(v);
            item = (RelativeLayout) v.findViewById(R.id.item_view);
            content = (TextView) v.findViewById(R.id.content);
            typeIcon = (ImageView) v.findViewById(R.id.news_icon);
            starredIcon = (ImageView) v.findViewById(R.id.starred_icon);
        }
    }
}
