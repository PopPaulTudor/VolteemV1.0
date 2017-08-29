package com.volunteer.thc.volunteerapp.adaptor;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.Chat;

import java.util.ArrayList;


/**
 * Created by poppa on 25.08.2017.
 */

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.EventViewHolder> {

    private ArrayList<Chat> data = new ArrayList<>();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();



    public ConversationAdapter(ArrayList<Chat> data) {
        this.data = data;
    }


    @Override
    public ConversationAdapter.EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversation_item, parent, false);


        return new EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ConversationAdapter.EventViewHolder holder, int position) {
        if(data.get(position).getSentBy().equals(user.getUid())) {
            holder.sent.setVisibility(View.VISIBLE);
            holder.sent.setText(data.get(position).getContent());
            holder.receive.setVisibility(View.GONE);

        }else {
            holder.receive.setVisibility(View.VISIBLE);
            holder.receive.setText(data.get(position).getContent());
            holder.sent.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    class EventViewHolder extends RecyclerView.ViewHolder {
        TextView sent;
        TextView receive;

        EventViewHolder(View v) {
            super(v);
                sent = (TextView) v.findViewById(R.id.conversation_text_sent);
                receive = (TextView) v.findViewById(R.id.conversation_text_receive);

        }
    }

    public void addElement(Chat chat) {
        data.add(chat);
        notifyDataSetChanged();
    }


}
