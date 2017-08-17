package com.volunteer.thc.volunteerapp.presentation.volunteer;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.Util.PermissionUtil;
import com.volunteer.thc.volunteerapp.model.Volunteer;
import com.volunteer.thc.volunteerapp.presentation.LoginActivity;
import com.volunteer.thc.volunteerapp.presentation.MainActivity;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.ContentValues.TAG;


/**
 * Created by Cristi on 6/18/2017.
 */

public class VolunteerRegisterFragment extends Fragment {

    private static final int GALLERY_INTENT = 1;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private EditText mEmail, mPassword, mPhone, mCity, mAge, mFirstname, mLastname;
    private Button mRegister, mBack;
    private Intent intent;
    private ProgressDialog mProgressDialog;
    private Spinner spinner;
    private List<String> gender = new ArrayList<>();
    private String mGender;
    private CircleImageView mImage;
    private Uri uri = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_volunteerregister, container, false);

        spinner = (Spinner) view.findViewById(R.id.gender);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mEmail = (EditText) view.findViewById(R.id.email);
        mPassword = (EditText) view.findViewById(R.id.password);
        mPhone = (EditText) view.findViewById(R.id.user_phone);
        mCity = (EditText) view.findViewById(R.id.user_city);
        mAge = (EditText) view.findViewById(R.id.user_age);
        mFirstname = (EditText) view.findViewById(R.id.first_name);
        mLastname = (EditText) view.findViewById(R.id.last_name);
        mImage = (CircleImageView) view.findViewById(R.id.photo);
        mRegister = (Button) view.findViewById(R.id.register_user);
        mBack = (Button) view.findViewById(R.id.back);
        intent = new Intent(getActivity(), MainActivity.class);

        gender.add("Gender");
        gender.add("Male");
        gender.add("Female");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, gender);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selected = spinner.getSelectedItem().toString();
                if (!TextUtils.equals(selected, "Gender")) {
                    mGender = selected;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // do nothing
            }
        });

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateForm()) {
                    mProgressDialog = ProgressDialog.show(getActivity(), "Registering", "", true);
                }
                createAccount(mEmail.getText().toString(), mPassword.getText().toString());
            }
        });

        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (PermissionUtil.isStoragePermissionGranted(getContext())) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, GALLERY_INTENT);

                } else {
                    Snackbar.make(view, "Please allow storage permission", Snackbar.LENGTH_LONG).setAction("Set Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                        }
                    }).show();
                }
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        });

        return view;
    }

    private void createAccount(final String email, String password) {

        Log.d("TAG", "CreateAccount: " + email);
        if (!validateForm()) {
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserwithEmail:Succes");


                            mPhone.setVisibility(View.GONE);
                            mFirstname.setVisibility(View.GONE);
                            mLastname.setVisibility(View.GONE);
                            mCity.setVisibility(View.GONE);
                            mAge.setVisibility(View.GONE);
                            mRegister.setVisibility(View.VISIBLE);
                            StorageReference mStorage = FirebaseStorage.getInstance().getReference();

                            FirebaseUser user = mAuth.getCurrentUser();

                            String userID = user.getUid();
                            String user_firstname = mFirstname.getText().toString();
                            String user_lastname = mLastname.getText().toString();
                            int user_age = Integer.parseInt(mAge.getText().toString());
                            String user_city = mCity.getText().toString();
                            String user_phone = mPhone.getText().toString();

                            Volunteer volunteer1 = new Volunteer(user_firstname, user_lastname, email, user_age, user_city, user_phone, mGender);

                            mDatabase.child("users").child("volunteers").child(userID).setValue(volunteer1);
                            StorageReference filePath = mStorage.child("Photos").child("User").child(userID);
                            filePath.putFile(uri);

                            UserProfileChangeRequest mProfileUpdate = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(user_firstname)
                                    .build();

                            user.updateProfile(mProfileUpdate);
                            user.sendEmailVerification();
                            Toast.makeText(getActivity(), "Account successfully created. A verification email has been sent to your email address.", Toast.LENGTH_LONG).show();

                            startActivity(intent);
                            getActivity().finish();

                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                mEmail.setError("Email address is already in use.");
                                mEmail.requestFocus();
                            } else {
                                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    mEmail.setError("Please enter a valid email address.");
                                    mEmail.requestFocus();
                                } else {
                                    Log.w("Error registering ", task.getException());
                                }
                            }
                        }
                        mProgressDialog.dismiss();
                    }

                });
    }

    private boolean validateForm() {

        boolean valid;
        valid = (editTextIsValid(mEmail) && editTextIsValid(mPassword) && editTextIsValid(mFirstname) &&
                editTextIsValid(mLastname) && editTextIsValid(mAge) && editTextIsValid(mPhone) && editTextIsValid(mCity));
        valid &= !TextUtils.isEmpty(mGender);
        return valid;
    }

    private boolean editTextIsValid(EditText mEditText) {
        String text = mEditText.getText().toString();
        if (TextUtils.isEmpty(text)) {
            mEditText.setError("This field can not be empty.");
            mEditText.requestFocus();
            return false;
        } else {
            if (mEditText == mEmail && !text.contains("@") && !text.contains(".")) {
                mEditText.setError("Please enter a valid email address.");

            } else {
                if (mEditText == mPassword && text.length() < 6) {
                    mEditText.setError("Your password must be at least 6 characters long.");
                    mEditText.requestFocus();
                    return false;
                } else {
                    mEditText.setError(null);
                }
            }
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_INTENT && (data != null)) {
            uri = data.getData();
            Picasso.with(getContext()).load(uri).fit().centerCrop().into(mImage);


        }
    }
}
