package com.volunteer.thc.volunteerapp.presentation.volunteer;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.Volunteer;
import com.volunteer.thc.volunteerapp.presentation.DisplayPhotoFragment;
import com.volunteer.thc.volunteerapp.util.ImageUtils;
import com.volunteer.thc.volunteerapp.util.PermissionUtil;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by poppa on 17.01.2018.
 */

public class VolunteerProfileInformationFragment extends Fragment {

    public static final int GALLERY_INTENT = 1;
    private boolean statusEdit = false;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private StorageReference mStorage = FirebaseStorage.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private EditText  mAge, mCity, mPhone;
    private TextView mEmail;
    private Volunteer volunteer1;
    private TextView mVolunteerName;
    private SharedPreferences prefs;
    private TextView mUserName;
    private CircleImageView circleImageView;
    private CircleImageView circleImageViewMenu;
    private FloatingActionButton mEditFloating, mCancelFloating;
    private ImageView mImagePhone,mImageAge,mImageLocation;
    private Uri uri;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_volunteer_profile_information, container, false);

        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);


        prefs = getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        mUserName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_header_name);

        volunteer1 = new Volunteer();
        mVolunteerName = (TextView) view.findViewById(R.id.ProfileVolunteerName);
        mEmail = (TextView) view.findViewById(R.id.ProfileVolunteerEmail);
        mAge = (EditText) view.findViewById(R.id.ProfileVolunteerDate);
        mCity = (EditText) view.findViewById(R.id.ProfileVolunteerCity);
        mPhone = (EditText) view.findViewById(R.id.ProfileVolunteerPhone);
        circleImageView = (CircleImageView) view.findViewById(R.id.ProfileVolunteerImage);
        mEditFloating = (FloatingActionButton) view.findViewById(R.id.volunteer_profile_edit);
        mCancelFloating = (FloatingActionButton) view.findViewById(R.id.volunteer_profile_cancel);

        mImageAge=(ImageView)view.findViewById(R.id.icon_edit_age);
        mImageLocation=(ImageView) view.findViewById(R.id.icon_edit_city) ;
        mImagePhone=(ImageView) view.findViewById(R.id.icon_edit_phone);


        mEmail.setTag(mEmail.getKeyListener());
        mAge.setTag(mAge.getKeyListener());
        mCity.setTag(mCity.getKeyListener());
        mPhone.setTag(mPhone.getKeyListener());
        mEmail.setKeyListener(null);

        toggleEditOff();
        mCancelFloating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelItemPressed();

            }
        });

        mEditFloating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (statusEdit) {
                    onSaveItemPressed();
                    mCancelFloating.setVisibility(View.GONE);
                    mEditFloating.setImageResource(R.drawable.ic_edit);
                    statusEdit = false;
                    visibilityEditIcon(false);
                } else {
                    onEditItemPressed();
                    mEditFloating.setImageResource(R.drawable.ic_save);
                    mCancelFloating.setVisibility(View.VISIBLE);
                    statusEdit = true;
                    visibilityEditIcon(true);
                }
            }
        });

        circleImageView.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_item);
                arrayAdapter.add("Change Image");
                arrayAdapter.add("View Image");

                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String choice = arrayAdapter.getItem(which);


                        if (choice.contains("Change")) {
                            if (PermissionUtil.isStorageReadPermissionGranted(getContext())) {
                                Intent intent = new Intent(Intent.ACTION_PICK);
                                intent.setType("image/*");
                                startActivityForResult(intent, GALLERY_INTENT);

                            } else {
                                Snackbar.make(getView(), "Please allow storage permission", Snackbar.LENGTH_LONG).setAction("Set Permission", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                                    }
                                }).show();
                            }
                        } else {

                            DisplayPhotoFragment displayPhotoFragment = new DisplayPhotoFragment();
                            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                            Bundle bundle = new Bundle();
                            bundle.putString("type", "user");
                            bundle.putString("userID", user.getUid());
                            displayPhotoFragment.setArguments(bundle);
                            fragmentTransaction.add(R.id.volunteer_profile_container, displayPhotoFragment).addToBackStack("showImage");
                            fragmentTransaction.commit();

                        }

                    }
                });
                builderSingle.show();
            }
        });


        ValueEventListener mVolunteerProfileListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                volunteer1 = dataSnapshot.getValue(Volunteer.class);
                mEmail.setText(volunteer1.getEmail());
                mPhone.setText(volunteer1.getPhone());
                mCity.setText(volunteer1.getCity());
                mVolunteerName.setText(volunteer1.getFirstname() + " " + volunteer1.getLastname());
                mAge.setText(volunteer1.getAge() + "");

                mStorage.child("Photos").child("User").child(user.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.with(getContext()).load(uri).fit().centerCrop().into(circleImageView);
                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.w("ProfileReadCanceled: ", databaseError.toException());
            }
        };
        mDatabase.child("users").child("volunteers").child(user.getUid()).addListenerForSingleValueEvent(mVolunteerProfileListener);
        mDatabase.removeEventListener(mVolunteerProfileListener);

        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_INTENT && (data != null)) {
            uri = data.getData();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference filePathProfile = storageRef.child("Photos").child("User").child(user.getUid());
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.show();

            filePathProfile.putBytes(ImageUtils.compressImage(uri, getActivity(), getResources())).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Picasso.with(getContext()).load(uri).fit().centerCrop().into(circleImageView);
                    Picasso.with(getContext()).load(uri).fit().centerCrop().into(circleImageViewMenu);
                }
            });


        }
    }


    private void onEditItemPressed() {
        toggleEditOn();
        toggleFocusOn();

    }

    private void onSaveItemPressed() {

        String currentFirstName, currentLastName, currentCity = null, currentPhone = null, fullName = null;
        int currentAge = 0;
        boolean changedName = false;
        if (mAge.getText().length() != 0) {
            currentAge = Integer.parseInt(mAge.getText().toString());
        }
        if (mCity.getText().length() != 0) {
            currentCity = mCity.getText().toString();
        }
        if (mPhone.getText().length() != 0) {
            currentPhone = mPhone.getText().toString();
        }

        if (validateForm()) {

            if (currentAge != volunteer1.getAge() && currentAge != 0) {
                mDatabase.child("users").child("volunteers").child(user.getUid()).child("age").setValue(currentAge);
                volunteer1.setAge(currentAge);
            }

            if (!currentCity.equals(volunteer1.getCity()) && !currentCity.isEmpty()) {
                mDatabase.child("users").child("volunteers").child(user.getUid()).child("city").setValue(currentCity);
                volunteer1.setCity(currentCity);
            }

            if (!currentPhone.equals(volunteer1.getPhone()) && currentPhone.isEmpty()) {
                mDatabase.child("users").child("volunteers").child(user.getUid()).child("phone").setValue(currentPhone);
                volunteer1.setPhone(currentPhone);
            }

            Toast.makeText(getActivity(), "Saved!", Toast.LENGTH_SHORT).show();

            hideKeyboardFrom(getActivity(), getView());
            toggleEditOff();
            toggleFocusOff();
        }

        if (changedName) {
            mUserName.setText(fullName);
            prefs.edit().putString("name", fullName).apply();
        }
        mCancelFloating.setVisibility(View.GONE);
        mEditFloating.setImageResource(R.drawable.ic_edit);
        statusEdit = false;
        visibilityEditIcon(false);

    }

    private void onCancelItemPressed() {
        mPhone.setText(volunteer1.getPhone());
        mCity.setText(volunteer1.getCity());
        mAge.setText(volunteer1.getAge() + "");
        mPhone.setError(null);
        mCity.setError(null);
        mAge.setError(null);
        toggleEditOff();
        toggleFocusOff();
        hideKeyboardFrom(getActivity(), getView());

        mCancelFloating.setVisibility(View.GONE);
        mEditFloating.setImageResource(R.drawable.ic_edit);
        statusEdit = false;
        visibilityEditIcon(false);

    }

    private void toggleEditOn() {

        mPhone.setKeyListener((KeyListener) mPhone.getTag());
        mAge.setKeyListener((KeyListener) mAge.getTag());
        mCity.setKeyListener((KeyListener) mCity.getTag());
    }

    private void toggleEditOff() {


        mPhone.setKeyListener(null);
        mCity.setKeyListener(null);
        mAge.setKeyListener(null);
    }

    private void toggleFocusOn() {

        mEmail.setFocusableInTouchMode(true);
        mEmail.setFocusable(true);
        mAge.setFocusableInTouchMode(true);
        mAge.setFocusable(true);
        mPhone.setFocusableInTouchMode(true);
        mPhone.setFocusable(true);
        mCity.setFocusableInTouchMode(true);
        mCity.setFocusable(true);
    }

    private void toggleFocusOff() {

        mEmail.setFocusableInTouchMode(false);
        mEmail.setFocusable(false);
        mAge.setFocusableInTouchMode(false);
        mAge.setFocusable(false);
        mPhone.setFocusableInTouchMode(false);
        mPhone.setFocusable(false);
        mCity.setFocusableInTouchMode(false);
        mCity.setFocusable(false);
    }

    private boolean validateForm() {

        boolean valid;
        valid = (editTextIsValid(mAge) && editTextIsValid(mPhone) && editTextIsValid(mCity));
        return valid;
    }

    private boolean editTextIsValid(EditText mEditText) {

        String text = mEditText.getText().toString();
        if (TextUtils.isEmpty(text)) {
            mEditText.setError("This field can not be empty.");
            mEditText.requestFocus();
            return false;
        } else {
            mEditText.setError(null);
        }
        return true;
    }

    private void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void visibilityEditIcon(boolean visible)
    {
        if(visible)
        {
            mImagePhone.setVisibility(View.VISIBLE);
            mImageLocation.setVisibility(View.VISIBLE);
            mImageAge.setVisibility(View.VISIBLE);
        }else
        {
            mImagePhone.setVisibility(View.INVISIBLE);
            mImageLocation.setVisibility(View.INVISIBLE);
            mImageAge.setVisibility(View.INVISIBLE);

        }
    }


}
