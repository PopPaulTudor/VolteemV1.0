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
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.volunteer.thc.volunteerapp.R;


/**
 * Created by poppa on 19.08.2017.
 */

public class DisplayPhotoFragment extends Fragment {

    ImageView imageView;
    TextView textView;
    ImageView button;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.photo_detailed, container, false);
        imageView = (ImageView) view.findViewById(R.id.photo_display);
        textView = (TextView) view.findViewById(R.id.photo_name);
        button = (ImageView) view.findViewById(R.id.photo_button);

        String userID = getArguments().getString("userID");
        String userName = getArguments().getString("userName");
        textView.setText(userName);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();


        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        storageRef.child("Photos").child("User").child(userID).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(getContext()).load(uri).fit().centerCrop().into(imageView);
            }
        });


        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    }
}
