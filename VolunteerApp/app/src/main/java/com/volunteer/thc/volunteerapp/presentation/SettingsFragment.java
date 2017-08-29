package com.volunteer.thc.volunteerapp.presentation;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.volunteer.thc.volunteerapp.R;

import de.cketti.library.changelog.ChangeLog;

/**
 * Created by Cristi on 6/17/2017.
 */

public class SettingsFragment extends Fragment {

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private EditText mPassword, mOldPassword, mNewPassword;
    private SwitchCompat notificationsSwitch;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private SharedPreferences prefs;
    private boolean notifications;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        setAppVersionName(view);
        notificationsSwitch = (SwitchCompat) view.findViewById(R.id.notification_button);
        prefs = getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        notifications = prefs.getBoolean("notifications", true);
        notificationsSwitch.setChecked(notifications);
        notificationsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(notificationsSwitch.isChecked()) {
                    if(!notifications) {
                        prefs.edit().putBoolean("notifications", true).apply();
                    }
                } else {
                    if(notifications) {
                        AlertDialog muteNotificationsAlert = new AlertDialog.Builder(getActivity())
                                .setTitle("Mute notifications")
                                .setMessage("Are you sure you want to mute all notifications?")
                                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        prefs.edit().putBoolean("notifications", false).apply();
                                    }
                                })
                                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        notificationsSwitch.setChecked(true);
                                    }
                                })
                                .create();
                        muteNotificationsAlert.show();
                    }
                }
            }
        });

        view.findViewById(R.id.change_pass_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(getActivity());
                View parentView = inflater.inflate(R.layout.change_password_bottom_sheet_design, null);
                mBottomSheetDialog.setContentView(parentView);
                BottomSheetBehavior mBottomSheetBehavior = BottomSheetBehavior.from((View) parentView.getParent());
                mBottomSheetBehavior.setPeekHeight((int) TypedValue.applyDimension
                        (TypedValue.COMPLEX_UNIT_DIP, 250, getResources().getDisplayMetrics()));
                mBottomSheetDialog.show();

                mOldPassword = (EditText) parentView.findViewById(R.id.oldPassword);
                mNewPassword = (EditText) parentView.findViewById(R.id.newPassword);

                parentView.findViewById(R.id.change_password).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String old_password = mOldPassword.getText().toString();
                        final String new_password = mNewPassword.getText().toString();
                        if (!TextUtils.isEmpty(old_password) && valid(new_password)) {
                            Toast.makeText(getActivity(), "Changing password...", Toast.LENGTH_SHORT).show();
                            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), old_password);
                            user.reauthenticate(credential)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                user.updatePassword(new_password).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        mBottomSheetDialog.dismiss();
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(getActivity(), "Password changed!", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            } else {
                                                mBottomSheetDialog.dismiss();
                                                Toast.makeText(getActivity(), "Authentication failed. Check if your password is correct and try again.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
            }
        });

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
                        if (!TextUtils.isEmpty(password)) {
                            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
                            user.reauthenticate(credential)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                SharedPreferences prefs = getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
                                                String userstatus = prefs.getString("user_status", null);

                                                Activity activity = getActivity();

                                                if (TextUtils.equals(userstatus, "Volunteer")) {
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

        view.findViewById(R.id.settings_feedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Email = new Intent(Intent.ACTION_SEND);
                Email.setType("text/email");
                // TODO we should add a second account just for app feedback (for security reasons)
                Email.putExtra(Intent.EXTRA_EMAIL, new String[] { "thcvolunteerapp@gmail.com" });
                Email.putExtra(Intent.EXTRA_SUBJECT, "App Feedback");
                Email.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(Email, "Send Feedback:"));
            }
        });

        view.findViewById(R.id.settings_changelog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangeLog changelog = new ChangeLog(getActivity());
                changelog.getLogDialog().show();
            }
        });

        return view;
    }

    private void setAppVersionName(View view) {
        String version = "";
        try {
            PackageInfo info = getActivity().getPackageManager().getPackageInfo(
                    getActivity().getPackageName(), 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("SettingsFragment", e.getMessage());
        }

        TextView appNameView = (TextView) view.findViewById(R.id.text_settings);
        appNameView.setText("- App version: " + version + " -");
    }

    private boolean valid(String password) {
        if (password.length() < 6) {
            return false;
        }
        return true;
    }
}
