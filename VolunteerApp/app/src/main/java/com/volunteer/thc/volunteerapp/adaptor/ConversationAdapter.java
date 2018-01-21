package com.volunteer.thc.volunteerapp.adaptor;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.ChatGroup;
import com.volunteer.thc.volunteerapp.model.ChatSingle;
import com.volunteer.thc.volunteerapp.model.Message;
import com.volunteer.thc.volunteerapp.util.CalendarUtil;

import java.util.ArrayList;


/**
 * Created by poppa on 25.08.2017.
 */

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.EventViewHolder> {

    private ArrayList<Message> data = new ArrayList<>();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private int contClick = 1;
    private Context context;
    ChatSingle chatSingle = null;
    ChatGroup chatGroup = null;
    long hour;


    public ConversationAdapter(ArrayList<Message> data, Context context) {
        this.data = data;
        this.context = context;
    }


    @Override
    public ConversationAdapter.EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversation_item, parent, false);


        return new EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ConversationAdapter.EventViewHolder holder, int position) {

        if (data.get(position) instanceof ChatSingle) {
            chatSingle = (ChatSingle) data.get(position);
            hour = chatSingle.getHour();
        } else {
            chatGroup = (ChatGroup) data.get(position);
            hour = chatGroup.getHour();
        }
        if (data.get(position).getSentBy().equals(user.getUid())) {

            holder.relativeSent.setVisibility(View.VISIBLE);
            holder.relativeReceive.setVisibility(View.INVISIBLE);
            holder.textSent.setText(data.get(position).getContent());
            holder.hourSent.setText(CalendarUtil.getHourFromLong(hour));

        } else {
            holder.relativeSent.setVisibility(View.INVISIBLE);
            holder.relativeReceive.setVisibility(View.VISIBLE);
            holder.textReceive.setText(data.get(position).getContent());
            holder.hourReceive.setText(CalendarUtil.getHourFromLong(hour));


        }

        final int start = holder.textReceive.getPaddingStart();
        final int end = holder.textReceive.getPaddingEnd();
        final int top = holder.textReceive.getCompoundPaddingTop();
        final int bottom = holder.textReceive.getPaddingBottom();


        holder.relativeReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int start = holder.textReceive.getPaddingStart();
                int end = holder.textReceive.getPaddingEnd();
                int top = holder.textReceive.getCompoundPaddingTop();
                int bottom = holder.textReceive.getPaddingBottom();

                if (contClick % 2 == 1) {
                    contClick++;
                    holder.hourReceive.setVisibility(View.VISIBLE);
                    holder.hourReceive.setVisibility(View.VISIBLE);
                    holder.textReceive.setPaddingRelative(start, top, end, bottom + 7);
                } else {
                    contClick++;
                    holder.hourReceive.setVisibility(View.GONE);
                    holder.hourReceive.setVisibility(View.GONE);
                    holder.textReceive.setPaddingRelative(start, top, end, bottom);
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
                    holder.textSent.setPaddingRelative(start, top, end, bottom + 7);
                } else {
                    contClick++;
                    holder.hourSent.setVisibility(View.GONE);
                    holder.hourSent.setVisibility(View.GONE);
                    holder.textSent.setPaddingRelative(start, top, end, bottom);
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

    public void addElement(ChatSingle chatSingle) {
        data.add(chatSingle);
        notifyDataSetChanged();
    }


}
