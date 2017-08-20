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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
import com.volunteer.thc.volunteerapp.util.ImageUtils;
import com.volunteer.thc.volunteerapp.util.PermissionUtil;
import com.volunteer.thc.volunteerapp.model.Volunteer;
import com.volunteer.thc.volunteerapp.presentation.DisplayPhotoFragment;

import de.hdodenhof.circleimageview.CircleImageView;


public class VolunteerProfileFragment extends Fragment {

    public static final int GALLERY_INTENT = 1;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private EditText mFirstnameEdit, mLastname, mEmail, mAge, mCity, mPhone;
    private Volunteer volunteer1;
    private ProgressBar mProgressBar;
    private SharedPreferences prefs;
    private TextView mUserName;
    private MenuItem mEdit, mSave, mCancel;
    private CircleImageView circleImageView;
    private CircleImageView circleImageViewMenu;
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private Uri uri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_volunteerprofile, container, false);
        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);

        mProgressBar = (ProgressBar) view.findViewById(R.id.indeterminateBar);
        mProgressBar.setVisibility(View.VISIBLE);
        prefs = getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        mUserName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_header_name);

        volunteer1 = new Volunteer();
        mFirstnameEdit = (EditText) view.findViewById(R.id.edit_firstname);
        mLastname = (EditText) view.findViewById(R.id.edit_lastname);
        mEmail = (EditText) view.findViewById(R.id.edit_email);
        mAge = (EditText) view.findViewById(R.id.edit_age);
        mCity = (EditText) view.findViewById(R.id.edit_city);
        mPhone = (EditText) view.findViewById(R.id.edit_phone);
        circleImageView = (CircleImageView) view.findViewById(R.id.photo);
        circleImageViewMenu = (CircleImageView) navigationView.findViewById(R.id.photo);

        mFirstnameEdit.setTag(mFirstnameEdit.getKeyListener());
        mLastname.setTag(mLastname.getKeyListener());
        mEmail.setTag(mEmail.getKeyListener());
        mAge.setTag(mAge.getKeyListener());
        mCity.setTag(mCity.getKeyListener());
        mPhone.setTag(mPhone.getKeyListener());
        mEmail.setKeyListener(null);

        toggleEditOff();


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
                            if (PermissionUtil.isStoragePermissionGranted(getContext())) {
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
                            bundle.putString("userID", user.getUid());
                            bundle.putString("userName", user.getDisplayName());
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
                mFirstnameEdit.setText(volunteer1.getFirstname());
                mEmail.setText(volunteer1.getEmail());
                mLastname.setText(volunteer1.getLastname());
                mPhone.setText(volunteer1.getPhone());
                mCity.setText(volunteer1.getCity());
                mAge.setText(volunteer1.getAge() + "");


                storageRef.child("Photos").child("User").child(user.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.with(getContext()).load(uri).fit().centerCrop().into(circleImageView);
                    }
                });

                mProgressBar.setVisibility(View.GONE);
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
            StorageReference filePath = storageRef.child("Photos").child("User").child(user.getUid());
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.show();
            filePath.putBytes(ImageUtils.compressImage(uri, getActivity())).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    Picasso.with(getContext()).load(uri).fit().centerCrop().into(circleImageView);
                    Picasso.with(getContext()).load(uri).fit().centerCrop().into(circleImageViewMenu);
                }
            });


        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_profile_edit, menu);
        mEdit = menu.findItem(R.id.action_edit);
        mSave = menu.findItem(R.id.action_save);
        mCancel = menu.findItem(R.id.action_cancel);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.action_edit:
                onEditItemPressed();
                mEdit.setVisible(false);
                mSave.setVisible(true);
                mCancel.setVisible(true);
                return true;
            case R.id.action_save:
                onSaveItemPressed();
                return true;
            case R.id.action_cancel:
                onCancelItemPressed();
                mEdit.setVisible(true);
                mSave.setVisible(false);
                mCancel.setVisible(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onEditItemPressed() {
        toggleEditOn();
        toggleFocusOn();
    }

    private void onSaveItemPressed() {

        String currentFirstName, currentLastName, currentCity, currentPhone, fullName = null;
        int currentAge;
        boolean changedName = false;
        currentFirstName = mFirstnameEdit.getText().toString();
        currentLastName = mLastname.getText().toString();
        currentAge = Integer.parseInt(mAge.getText().toString());
        currentCity = mCity.getText().toString();
        currentPhone = mPhone.getText().toString();

        if (validateForm()) {

            fullName = currentFirstName + " " + currentLastName;

            if (!currentFirstName.equals(volunteer1.getFirstname())) {
                mDatabase.child("users").child("volunteers").child(user.getUid()).child("firstname").setValue(currentFirstName);
                volunteer1.setFirstname(currentFirstName);
                changedName = true;
            }

            if (!currentLastName.equals(volunteer1.getLastname())) {
                mDatabase.child("users").child("volunteers").child(user.getUid()).child("lastname").setValue(currentLastName);
                volunteer1.setLastname(currentLastName);
                changedName = true;
            }

            if (currentAge != volunteer1.getAge()) {
                mDatabase.child("users").child("volunteers").child(user.getUid()).child("age").setValue(currentAge);
                volunteer1.setAge(currentAge);
            }

            if (!currentCity.equals(volunteer1.getCity())) {
                mDatabase.child("users").child("volunteers").child(user.getUid()).child("city").setValue(currentCity);
                volunteer1.setCity(currentCity);
            }

            if (!currentPhone.equals(volunteer1.getPhone())) {
                mDatabase.child("users").child("volunteers").child(user.getUid()).child("phone").setValue(currentPhone);
                volunteer1.setPhone(currentPhone);
            }

            Toast.makeText(getActivity(), "Saved!", Toast.LENGTH_SHORT).show();

            hideKeyboardFrom(getActivity(), getView());
            mEdit.setVisible(true);
            mSave.setVisible(false);
            mCancel.setVisible(false);
            toggleEditOff();
            toggleFocusOff();
        }

        if (changedName) {
            mUserName.setText(fullName);
            prefs.edit().putString("name", fullName).commit();
        }

    }

    private void onCancelItemPressed() {
        mFirstnameEdit.setText(volunteer1.getFirstname());
        mLastname.setText(volunteer1.getLastname());
        mPhone.setText(volunteer1.getPhone());
        mCity.setText(volunteer1.getCity());
        mAge.setText(volunteer1.getAge() + "");
        mFirstnameEdit.setError(null);
        mLastname.setError(null);
        mPhone.setError(null);
        mCity.setError(null);
        mAge.setError(null);
        toggleEditOff();
        toggleFocusOff();
        hideKeyboardFrom(getActivity(), getView());
    }

    private void toggleEditOn() {

        mFirstnameEdit.setKeyListener((KeyListener) mFirstnameEdit.getTag());
        mLastname.setKeyListener((KeyListener) mLastname.getTag());
        mPhone.setKeyListener((KeyListener) mPhone.getTag());
        mAge.setKeyListener((KeyListener) mAge.getTag());
        mCity.setKeyListener((KeyListener) mCity.getTag());
    }

    private void toggleEditOff() {

        mFirstnameEdit.setKeyListener(null);
        mLastname.setKeyListener(null);
        mPhone.setKeyListener(null);
        mCity.setKeyListener(null);
        mAge.setKeyListener(null);
    }

    private void toggleFocusOn() {

        mEmail.setFocusableInTouchMode(true);
        mEmail.setFocusable(true);
        mFirstnameEdit.setFocusableInTouchMode(true);
        mFirstnameEdit.setFocusable(true);
        mLastname.setFocusableInTouchMode(true);
        mLastname.setFocusable(true);
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
        mFirstnameEdit.setFocusableInTouchMode(false);
        mFirstnameEdit.setFocusable(false);
        mLastname.setFocusableInTouchMode(false);
        mLastname.setFocusable(false);
        mAge.setFocusableInTouchMode(false);
        mAge.setFocusable(false);
        mPhone.setFocusableInTouchMode(false);
        mPhone.setFocusable(false);
        mCity.setFocusableInTouchMode(false);
        mCity.setFocusable(false);
    }

    private boolean validateForm() {

        boolean valid;
        valid = (editTextIsValid(mFirstnameEdit) && editTextIsValid(mLastname) && editTextIsValid(mAge) &&
                editTextIsValid(mPhone) && editTextIsValid(mCity));
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

}
