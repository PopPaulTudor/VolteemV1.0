package com.volunteer.thc.volunteerapp.presentation.organiser;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.volunteer.thc.volunteerapp.model.Organiser;
import com.volunteer.thc.volunteerapp.presentation.DisplayPhotoFragment;
import com.volunteer.thc.volunteerapp.util.ImageUtils;
import com.volunteer.thc.volunteerapp.util.PermissionUtil;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Cristi on 6/19/2017.
 */

public class OrganiserProfileFragment extends Fragment {

    private static final int GALLERY_INTENT = 1;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private EditText mEmail, mCompany, mPhone, mCity;
    private ProgressBar mProgressBar;
    private Organiser organiser;
    private MenuItem mSave, mEdit, mCancel;
    private CircleImageView circleImageView;
    private CircleImageView circleImageViewMenu;
    private Uri uri;
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organiserprofile, container, false);
        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);


        organiser = new Organiser();
        mEmail = (EditText) view.findViewById(R.id.edit_email);
        mCompany = (EditText) view.findViewById(R.id.edit_company);
        mCity = (EditText) view.findViewById(R.id.edit_city);
        mPhone = (EditText) view.findViewById(R.id.edit_phone);
        circleImageView = (CircleImageView) view.findViewById(R.id.photo);
        circleImageViewMenu = (CircleImageView) navigationView.findViewById(R.id.photo);


        mCompany.setTag(mCompany.getKeyListener());
        mEmail.setTag(mEmail.getKeyListener());
        mCity.setTag(mCity.getKeyListener());
        mPhone.setTag(mPhone.getKeyListener());
        mEmail.setKeyListener(null);

        mProgressBar = (ProgressBar) view.findViewById(R.id.indeterminateBar);
        mProgressBar.setVisibility(View.VISIBLE);

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
                            fragmentTransaction.add(R.id.organiser_profile_container, displayPhotoFragment).addToBackStack("showImage");
                            fragmentTransaction.commit();

                        }

                    }
                });
                builderSingle.show();
            }
        });


        ValueEventListener mOrganiserProfileListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                organiser = dataSnapshot.getValue(Organiser.class);

                mCompany.setText(organiser.getCompany());
                mEmail.setText(organiser.getEmail());
                mPhone.setText(organiser.getPhone());
                mCity.setText(organiser.getCity());

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

        mDatabase.child("users").child("organisers").child(user.getUid()).addListenerForSingleValueEvent(mOrganiserProfileListener);
        mDatabase.removeEventListener(mOrganiserProfileListener);

        setHasOptionsMenu(true);
        return view;
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
                return true;
            case R.id.action_save:
                onSaveItemPressed();
                return true;
            case R.id.action_cancel:
                onCancelItemPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onEditItemPressed() {
        mEdit.setVisible(false);
        mSave.setVisible(true);
        mCancel.setVisible(true);
        toggleFocusOn();
        toggleEditOn();
    }

    private void onSaveItemPressed() {
        String currentCompany, currentCity, currentPhone;

        currentCompany = mCompany.getText().toString();
        currentCity = mCity.getText().toString();
        currentPhone = mPhone.getText().toString();

        if (validateForm()) {
            if (!currentCompany.equals(organiser.getCompany())&&!currentCompany.isEmpty()) {
                mDatabase.child("users").child("organisers").child(user.getUid()).child("company").setValue(currentCompany);
                organiser.setCompany(currentCompany);
            }

            if (!currentCity.equals(organiser.getCity())&&!currentCity.isEmpty()) {
                mDatabase.child("users").child("organisers").child(user.getUid()).child("city").setValue(currentCity);
                organiser.setCity(currentCity);
            }

            if (!currentPhone.equals(organiser.getPhone())&&!currentPhone.isEmpty()) {
                mDatabase.child("users").child("organisers").child(user.getUid()).child("phone").setValue(currentPhone);
                organiser.setPhone(currentPhone);
            }

            Toast.makeText(getActivity(), "Changes saved!", Toast.LENGTH_SHORT).show();
            hideKeyboardFrom(getActivity(), getView());
            toggleEditOff();
            toggleFocusOff();
            mEdit.setVisible(true);
            mSave.setVisible(false);
            mCancel.setVisible(false);
        }
    }

    private void onCancelItemPressed() {
        mEdit.setVisible(true);
        mSave.setVisible(false);
        mCancel.setVisible(false);
        mCompany.setText(organiser.getCompany());
        mEmail.setText(organiser.getEmail());
        mPhone.setText(organiser.getPhone());
        mCity.setText(organiser.getCity());
        mCompany.setError(null);
        mPhone.setError(null);
        mCity.setError(null);
        toggleEditOff();
        toggleFocusOff();
        hideKeyboardFrom(getActivity(), getView());
    }

    private void toggleEditOn() {

        mCompany.setKeyListener((KeyListener) mCompany.getTag());
        mPhone.setKeyListener((KeyListener) mPhone.getTag());
        mCity.setKeyListener((KeyListener) mCity.getTag());
    }

    private void toggleEditOff() {

        mCompany.setKeyListener(null);
        mPhone.setKeyListener(null);
        mCity.setKeyListener(null);
    }

    private void toggleFocusOn() {

        mEmail.setFocusableInTouchMode(true);
        mEmail.setFocusable(true);
        mCompany.setFocusableInTouchMode(true);
        mCompany.setFocusable(true);
        mPhone.setFocusableInTouchMode(true);
        mPhone.setFocusable(true);
        mCity.setFocusableInTouchMode(true);
        mCity.setFocusable(true);
    }

    private void toggleFocusOff() {

        mEmail.setFocusableInTouchMode(false);
        mEmail.setFocusable(false);
        mCompany.setFocusableInTouchMode(false);
        mCompany.setFocusable(false);
        mPhone.setFocusableInTouchMode(false);
        mPhone.setFocusable(false);
        mCity.setFocusableInTouchMode(false);
        mCity.setFocusable(false);
    }

    private boolean validateForm() {

        boolean valid;
        valid = (editTextIsValid(mCompany) && editTextIsValid(mCity) && editTextIsValid(mPhone));
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
}
