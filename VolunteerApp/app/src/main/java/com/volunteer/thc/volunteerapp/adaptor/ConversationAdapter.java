package com.volunteer.thc.volunteerapp.adaptor;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.Chat;
import com.volunteer.thc.volunteerapp.util.CalendarUtil;

import java.util.ArrayList;



/**
 * Created by poppa on 25.08.2017.
 */

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.EventViewHolder> {

    private ArrayList<Chat> data = new ArrayList<>();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private int contClick = 1;
    private Context context;


    public ConversationAdapter(ArrayList<Chat> data, Context context) {
        this.data = data;
        this.context=context;
    }


    @Override
    public ConversationAdapter.EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversation_item, parent, false);


        return new EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ConversationAdapter.EventViewHolder holder, int position) {
        if (data.get(position).getSentBy().equals(user.getUid())) {

            holder.relativeSent.setVisibility(View.VISIBLE);
            holder.relativeReceive.setVisibility(View.GONE);
            holder.textSent.setText(data.get(position).getContent());
            holder.hourSent.setText(CalendarUtil.getHourFromLong(data.get(position).getHour()));

        } else {
            holder.relativeSent.setVisibility(View.GONE);
            holder.relativeReceive.setVisibility(View.VISIBLE);
            holder.textReceive.setText(data.get(position).getContent());
            holder.hourReceive.setText(CalendarUtil.getHourFromLong(data.get(position).getHour()));



        }


        holder.relativeReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contClick % 2 == 1) {
                    contClick++;
                    holder.hourReceive.setVisibility(View.VISIBLE);
                    holder.hourReceive.setVisibility(View.VISIBLE);
                    holder.textReceive.setPaddingRelative(15, 30, 15, 50);
                } else {
                    contClick++;
                    holder.hourReceive.setVisibility(View.GONE);
                    holder.hourReceive.setVisibility(View.GONE);
                    holder.textReceive.setPaddingRelative(15, 30, 15, 30);
                }
            }
        });


        holder.relativeSent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contClick % 2 == 1) {
                    contClick++;
                    holder.hourSent.setVisibility(View.VISIBLE);
                    holder.hourSent.setVisibility(View.VISIBLE);
                    holder.textSent.setPaddingRelative(7, 15, 7, 25);
                } else {
                    contClick++;
                    holder.hourSent.setVisibility(View.GONE);
                    holder.hourSent.setVisibility(View.GONE);
                    holder.textSent.setPaddingRelative(7, 15, 7, 15);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    class EventViewHolder extends RecyclerView.ViewHolder {
        TextView textSent;
        TextView textReceive;
        TextView hourSent;
        TextView hourReceive;
        RelativeLayout relativeSent;
        RelativeLayout relativeReceive;

        EventViewHolder(View v) {
            super(v);
            textSent = (TextView) v.findViewById(R.id.conversation_text_sent);
            textReceive = (TextView) v.findViewById(R.id.conversation_text_receive);
            hourSent = (TextView) v.findViewById(R.id.conversation_hour_sent);
            hourReceive = (TextView) v.findViewById(R.id.conversation_hour_receive);
            relativeSent = (RelativeLayout) v.findViewById(R.id.layout_sent);
            relativeReceive = (RelativeLayout) v.findViewById(R.id.layout_receive);


        }
    }

    public void addElement(Chat chat) {
        data.add(chat);
        notifyDataSetChanged();
    }


}
