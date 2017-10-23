package com.volunteer.thc.volunteerapp.adaptor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;
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
import com.volunteer.thc.volunteerapp.interrface.ActionListener;
import com.volunteer.thc.volunteerapp.model.NewsMessage;
import com.volunteer.thc.volunteerapp.presentation.MainActivity;
import com.volunteer.thc.volunteerapp.presentation.organiser.OrganiserSingleEventActivity;
import com.volunteer.thc.volunteerapp.presentation.volunteer.VolunteerSingleEventActivity;
import com.volunteer.thc.volunteerapp.util.CalendarUtil;

import java.util.ArrayList;

/**
 * Created by Cristi on 8/25/2017.
 */

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private ArrayList<NewsMessage> newsList;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private Context context;
    private ActionListener.NewsDeletedListener newsDeletedListener;
    private boolean itemWasLongClicked = false;

    public NewsAdapter(ArrayList<NewsMessage> newsList, Context context, ActionListener.NewsDeletedListener newsDeletedListener) {
        this.newsList = newsList;
        this.context = context;
        this.newsDeletedListener = newsDeletedListener;
    }

    @Override
    public NewsAdapter.NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_element, parent, false);

        return new NewsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final NewsAdapter.NewsViewHolder holder, final int position) {

        final SharedPreferences prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);

        if (newsList.get(position).isStarred()) {
            holder.starredIcon.setVisibility(View.VISIBLE);
        }

        switch (newsList.get(position).getType()) {
            case NewsMessage.ACCEPT:
                holder.typeIcon.setImageResource(R.drawable.ic_checked);
                break;
            case NewsMessage.FEEDBACK:
                holder.typeIcon.setImageResource(R.drawable.ic_feedback_news);
                break;
            case NewsMessage.VOLUNTEER_LEFT:
                holder.typeIcon.setImageResource(R.drawable.ic_delete);
                break;
        }

        holder.content.setText(newsList.get(position).getContent());
        holder.time.setText(CalendarUtil.getNewsStringDateFromMM(newsList.get(position).getExpireDate()));
        holder.item.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                itemWasLongClicked = true;
                if (newsList.get(position).isStarred()) {
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
        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                switch (newsList.get(position).getType()) {
                    case NewsMessage.ACCEPT:
                        prefs.edit().putInt("cameFrom", 2).apply();
                        intent = new Intent(context, VolunteerSingleEventActivity.class);
                        intent.putExtra("newsEventID", newsList.get(position).getEventID());
                        break;
                    case NewsMessage.REGISTERED:
                        intent = new Intent(context, OrganiserSingleEventActivity.class);
                        intent.putExtra("newsEventID", newsList.get(position).getEventID());
                        break;
                    default:
                        intent = new Intent(context, MainActivity.class);
                }
                if (!itemWasLongClicked) {
                    context.startActivity(intent);
                }
                itemWasLongClicked = false;
            }
        });
    }

    public ItemTouchHelper getItemTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
               // mDatabase.child("news/" + newsList.get(position).getNewsID()).setValue(null);
                newsList.remove(position);
                notifyItemRemoved(position);
                if(newsList.isEmpty()) {
                    newsDeletedListener.onNewsDeleted();
                }
            }
        };
        return new ItemTouchHelper(simpleCallback);
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    class NewsViewHolder extends RecyclerView.ViewHolder {

        TextView content, time;
        RelativeLayout item;
        ImageView typeIcon, starredIcon;

        NewsViewHolder(View v) {
            super(v);
            item = (RelativeLayout) v.findViewById(R.id.item_view);
            content = (TextView) v.findViewById(R.id.content);
            time = (TextView) v.findViewById(R.id.time);
            typeIcon = (ImageView) v.findViewById(R.id.news_icon);
            starredIcon = (ImageView) v.findViewById(R.id.starred_icon);
        }
    }
}
