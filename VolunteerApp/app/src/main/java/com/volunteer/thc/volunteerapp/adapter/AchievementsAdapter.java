package com.volunteer.thc.volunteerapp.adapter;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.volunteer.thc.volunteerapp.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by poppa on 23.01.2018.
 */

public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.EventViewHolder> {

    private ArrayList<String> checkList;
    private Context context;
    private StorageReference mStorage = FirebaseStorage.getInstance().getReference();


    public AchievementsAdapter(ArrayList<String> checkList, Context context) {
        this.checkList = checkList;
        this.context = context;


    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.achivements_element, parent, false);

        return new EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final EventViewHolder holder, int position) {
        int orderNumber = position + 1;
        String adress = orderNumber + ".png";

        mStorage.child("Photos").child("Achivements").child(adress).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(context).load(uri).fit().centerCrop().into(holder.circleImageView);
            }
        });

        if(orderNumber%2==0){
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            holder.circleImageView.setColorFilter(filter);
            holder.circleImageView.setAlpha(100);
            holder.cardView.setBackgroundResource(R.color.lightGrey);
        }

    }

    @Override
    public int getItemCount() {
        return checkList.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageView;
        CardView cardView;

        EventViewHolder(View itemView) {
            super(itemView);
            circleImageView = (CircleImageView) itemView.findViewById(R.id.achivements_image);
            cardView=(CardView) itemView.findViewById(R.id.achivements_card);

        }
    }
}
