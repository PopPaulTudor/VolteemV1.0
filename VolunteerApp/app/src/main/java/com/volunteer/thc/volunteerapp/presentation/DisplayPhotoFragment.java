package com.volunteer.thc.volunteerapp.presentation;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.util.VolteemConstants;


/**
 * Created by poppa on 19.08.2017.
 */

public class DisplayPhotoFragment extends Fragment {

    ImageView imageView;
    ImageView button;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.photo_detailed, container, false);
        imageView = view.findViewById(R.id.photo_display);
        button = view.findViewById(R.id.photo_button);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        if (getArguments().getString("type").equals("user")) {
            String userID = getArguments().getString("userID");


            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            if (userID != null) {
                storageRef.child("Photos").child("User").child(userID).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.with(getContext()).load(uri).fit().centerCrop().into(imageView);
                    }
                });

            }else{
                Toast.makeText(getContext(),"You don't have a photo",Toast.LENGTH_SHORT).show();
            }
        } else {
            String eventID = getArguments().getString(VolteemConstants.INTENT_EVENT_ID);

            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            storageRef.child("Photos").child("Event").child(eventID).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(getContext()).load(uri).fit().centerCrop().into(imageView);
                }
            });
        }


        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    }
}
