package com.volunteer.thc.volunteerapp.presentation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.volunteer.thc.volunteerapp.R;

/**
 * Created by Cristi on 6/17/2017.
 */

public class SettingsFragment extends Fragment {

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private EditText mPassword;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        view.findViewById(R.id.button_delete_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(getActivity());
                View parentView = inflater.inflate(R.layout.delete_account_bottom_sheet_design, null);
                mBottomSheetDialog.setContentView(parentView);
                BottomSheetBehavior mBottomSheetBehavior = BottomSheetBehavior.from((View) parentView.getParent());
                mBottomSheetBehavior.setPeekHeight((int) TypedValue.applyDimension
                        (TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics()));
                mBottomSheetDialog.show();

                mPassword = (EditText) parentView.findViewById(R.id.password);

                parentView.findViewById(R.id.delete_account).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String password = mPassword.getText().toString();
                        if(!TextUtils.isEmpty(password)) {
                            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
                            user.reauthenticate(credential)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                SharedPreferences prefs = getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
                                                String userstatus = prefs.getString("user_status", null);

                                                Activity activity = getActivity();

                                                if(TextUtils.equals(userstatus, "Volunteer")) {
                                                    mDatabase.child("users").child("volunteers").child(user.getUid()).setValue(null);
                                                } else {
                                                    mDatabase.child("users").child("organisers").child(user.getUid()).setValue(null);
                                                }
                                                user.delete();
                                                FirebaseAuth.getInstance().signOut();
                                                prefs.edit().putString("user_status", null).commit();
                                                Intent intent = new Intent(activity, LoginActivity.class);
                                                startActivity(intent);
                                                activity.finish();
                                            }
                                        }
                                    });
                        }
                    }
                });
            }
        });

        return view;
    }

}
