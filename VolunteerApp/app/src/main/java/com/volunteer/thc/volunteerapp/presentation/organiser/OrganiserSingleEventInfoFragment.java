package com.volunteer.thc.volunteerapp.presentation.organiser;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.model.NewsMessage;
import com.volunteer.thc.volunteerapp.presentation.DisplayPhotoFragment;
import com.volunteer.thc.volunteerapp.util.CalendarUtil;
import com.volunteer.thc.volunteerapp.util.DatabaseUtils;
import com.volunteer.thc.volunteerapp.util.ImageUtils;
import com.volunteer.thc.volunteerapp.util.PermissionUtil;
import com.volunteer.thc.volunteerapp.util.VolteemConstants;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Cristi on 7/27/2017.
 */

public class OrganiserSingleEventInfoFragment extends Fragment {

    private static final int GALLERY_INTENT = 1;
    private static final int PICK_PDF = 2;
    private Event mCurrentEvent = new Event();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private EditText mName, mLocation, mStartDate, mDescription, mDeadline, mSize, mFinishDate;
    private MenuItem mEdit, mSave, mCancel, mDelete;
    private long currentStartDate, currentFinishDate, currentDeadline;
    private Spinner mType;
    private ImageView mImage;
    private ArrayList<String> typeList = new ArrayList<>();
    private boolean hasSelectedPDF = false;
    private Uri uriPicture = null;
    private boolean hasUserSelectedPicture = false;
    private Button changeContract, saveChanges, cancelChanges;
    private Uri uriPDF;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organiser_single_event_info, container, false);

        mCurrentEvent = (Event) getArguments().getSerializable(VolteemConstants.INTENT_CURRENT_EVENT);
        populateSpinnerArray();

        saveChanges = (Button) view.findViewById(R.id.save_changes);
        cancelChanges = (Button) view.findViewById(R.id.cancel_changes);
        mName = (EditText) view.findViewById(R.id.event_deadline);
        mLocation = (EditText) view.findViewById(R.id.event_location);
        mStartDate = (EditText) view.findViewById(R.id.event_date_start);
        mFinishDate = (EditText) view.findViewById(R.id.event_date_finish);
        mDeadline = (EditText) view.findViewById(R.id.event_name);
        mType = (Spinner) view.findViewById(R.id.event_type);
        mDescription = (EditText) view.findViewById(R.id.event_description);
        mSize = (EditText) view.findViewById(R.id.event_size);
        mImage = (ImageView) view.findViewById(R.id.event_org_image);
        changeContract = (Button) view.findViewById(R.id.event_contract);

        changeContract.setClickable(false);
        mImage.setClickable(false);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, typeList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mType.setAdapter(adapter);

        mName.setText(mCurrentEvent.getName());
        mLocation.setText(mCurrentEvent.getLocation());
        mType.setSelection(typeList.indexOf(mCurrentEvent.getType()));
        mDescription.setText(mCurrentEvent.getDescription());
        mDeadline.setText(CalendarUtil.getStringDateFromMM(mCurrentEvent.getDeadline()));
        mStartDate.setText(CalendarUtil.getStringDateFromMM(mCurrentEvent.getStartDate()));
        mFinishDate.setText(CalendarUtil.getStringDateFromMM(mCurrentEvent.getFinishDate()));

        mSize.setText(mCurrentEvent.getSize() + "");
        currentStartDate = mCurrentEvent.getStartDate();
        currentFinishDate = mCurrentEvent.getFinishDate();
        currentDeadline = mCurrentEvent.getDeadline();

        mStartDate.setOnClickListener(setonClickListenerCalendar(mStartDate));
        mFinishDate.setOnClickListener(setonClickListenerCalendar(mFinishDate));
        mDeadline.setOnClickListener(setonClickListenerCalendar(mDeadline));

        StorageReference mStorage = FirebaseStorage.getInstance().getReference();
        StorageReference filePath = mStorage.child("Photos").child("Event").child(mCurrentEvent.getEventID());

        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(getContext()).load(uri).fit().centerCrop().into(mImage);
            }
        });

        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSaveItemPressed();
            }
        });

        cancelChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCancelItemPressed();
            }
        });

        mImage.setOnClickListener(new View.OnClickListener() {
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
                                Snackbar.make(getView(), "Please allow storage permission", Snackbar.LENGTH_LONG).setAction("Set Permission", new
                                        View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission
                                                        .READ_EXTERNAL_STORAGE}, 1);
                                            }
                                        }).show();
                            }

                        } else {

                            DisplayPhotoFragment displayPhotoFragment = new DisplayPhotoFragment();
                            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                            Bundle bundle = new Bundle();
                            bundle.putString("type", "event");
                            bundle.putString(VolteemConstants.INTENT_EVENT_ID, mCurrentEvent.getEventID());
                            displayPhotoFragment.setArguments(bundle);
                            fragmentTransaction.add(R.id.event_detailed_photo, displayPhotoFragment).addToBackStack("showImage");
                            fragmentTransaction.commit();
                        }
                    }
                });
                builderSingle.show();
            }
        });

        changeContract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionUtil.isStorageReadPermissionGranted(getActivity())) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/pdf");
                    startActivityForResult(intent, PICK_PDF);

                } else {
                    Snackbar.make(v, "Please allow storage permission", Snackbar.LENGTH_LONG).setAction("Set Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                        }
                    }).show();
                }
            }
        });

        toggleEdit(false);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_event_edit, menu);

        mEdit = menu.findItem(R.id.action_edit);
        mDelete = menu.findItem(R.id.action_delete);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result;
        switch (item.getItemId()) {
            case R.id.action_edit:
                onEditItemPressed();
                result = true;
                break;
            case R.id.action_delete:
                result = super.onOptionsItemSelected(item);
                break;
            default:
                result = super.onOptionsItemSelected(item);
                break;
        }
        return result;
    }

    private void onEditItemPressed() {
        toggleEdit(true);

        saveChanges.setVisibility(View.VISIBLE);
        cancelChanges.setVisibility(View.VISIBLE);
        mImage.setClickable(true);
        changeContract.setClickable(true);
    }

    private void onSaveItemPressed() {
        String currentName, currentLocation, currentType, currentDescription, currentSize;
        currentName = mName.getText().toString();
        currentLocation = mLocation.getText().toString();
        currentType = mType.getSelectedItem().toString();
        currentDescription = mDescription.getText().toString();
        currentSize = mSize.getText().toString();

        mImage.setClickable(false);
        changeContract.setClickable(false);

        if (validateForm()) {
            if (!currentName.equals(mCurrentEvent.getName())) {
                mDatabase.child("events").child(mCurrentEvent.getEventID()).child("name").setValue(currentName);
                mCurrentEvent.setName(currentName);
            }

            if (!currentLocation.equals(mCurrentEvent.getLocation())) {
                mDatabase.child("events").child(mCurrentEvent.getEventID()).child("location").setValue(currentLocation);
                mCurrentEvent.setLocation(currentLocation);
            }

            if (currentStartDate != mCurrentEvent.getStartDate()) {
                mDatabase.child("events").child(mCurrentEvent.getEventID()).child("startDate").setValue(currentStartDate);
                mCurrentEvent.setStartDate(currentStartDate);
            }
            if (currentFinishDate != mCurrentEvent.getFinishDate()) {
                mDatabase.child("events").child(mCurrentEvent.getEventID()).child("finishDate").setValue(currentFinishDate);
                mCurrentEvent.setFinishDate(currentFinishDate);
            }

            if (!currentType.equals(mCurrentEvent.getType())) {
                mDatabase.child("events").child(mCurrentEvent.getEventID()).child("type").setValue(currentType);
                mCurrentEvent.setType(currentType);
            }

            if (!currentDescription.equals(mCurrentEvent.getDescription())) {
                mDatabase.child("events").child(mCurrentEvent.getEventID()).child("description").setValue(currentDescription);
                mCurrentEvent.setDescription(currentDescription);
            }

            if (currentDeadline != mCurrentEvent.getDeadline()) {
                mDatabase.child("events").child(mCurrentEvent.getEventID()).child("deadline").setValue(currentDeadline);
                mCurrentEvent.setDeadline(currentDeadline);
            }

            if (!currentSize.equals(mCurrentEvent.getSize())) {
                mDatabase.child("events").child(mCurrentEvent.getEventID()).child("size").setValue(Integer.parseInt(currentSize));
                mCurrentEvent.setSize(Integer.parseInt(currentSize));
            }

            if (hasUserSelectedPicture) {
                StorageReference mStorage = FirebaseStorage.getInstance().getReference();
                StorageReference filePath = mStorage.child("Photos").child("Event").child(mCurrentEvent.getEventID());
                filePath.putBytes(ImageUtils.compressImage(uriPicture, getActivity(), getResources()));
            }

            if (hasSelectedPDF) {
                StorageReference mStorage = FirebaseStorage.getInstance().getReference();
                StorageReference filePath = mStorage.child("Contracts").child("Event").child(mCurrentEvent.getEventID());
                filePath.putFile(uriPDF);


                for (String id : mCurrentEvent.getAccepted_volunteers()) {

                    String newsID = mDatabase.child("news").push().getKey();
                    mDatabase.child("news").child(newsID).setValue(new NewsMessage(CalendarUtil.getCurrentTimeInMillis(), newsID, "soon",
                            DatabaseUtils.getUserID(), id,
                            "A new contract has been uploaded for" + mCurrentEvent.getName(), NewsMessage.FEEDBACK, false, false));
                }
            }

            Toast.makeText(getActivity(), "Event updated!", Toast.LENGTH_LONG).show();

            saveChanges.setVisibility(View.GONE);
            cancelChanges.setVisibility(View.GONE);
            toggleEdit(false);
            hideKeyboardFrom(getActivity(), getView());
        }
    }

    private void onCancelItemPressed() {

        mName.setText(mCurrentEvent.getName());
        mLocation.setText(mCurrentEvent.getLocation());
        mType.setSelection(typeList.indexOf(mCurrentEvent.getType()));
        mDescription.setText(mCurrentEvent.getDescription());
        mSize.setText(mCurrentEvent.getSize() + "");
        mDeadline.setText(CalendarUtil.getStringDateFromMM(mCurrentEvent.getDeadline()));
        mStartDate.setText(CalendarUtil.getStringDateFromMM(mCurrentEvent.getStartDate()));
        mFinishDate.setText(CalendarUtil.getStringDateFromMM(mCurrentEvent.getFinishDate()));

        mName.setError(null);
        mLocation.setError(null);
        mDescription.setError(null);
        mDeadline.setError(null);
        mStartDate.setError(null);
        mSize.setError(null);
        mFinishDate.setError(null);

        toggleEdit(false);
        saveChanges.setVisibility(View.GONE);
        cancelChanges.setVisibility(View.GONE);
        hideKeyboardFrom(getActivity(), getView());

        mImage.setClickable(false);
        changeContract.setClickable(false);
    }

    private void onDeleteItemPressed() {
        final AlertDialog deleteDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Are you sure?")
                .setMessage("Are you sure you want to delete this event? You will not be able to undo this action.")
                .setCancelable(false)
                .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        for (String volunteer_id : mCurrentEvent.getRegistered_volunteers()) {
                            String newsID = mDatabase.child("news").push().getKey();
                            NewsMessage newsMessage = new NewsMessage(CalendarUtil.getCurrentTimeInMillis(), newsID, mCurrentEvent.getEventID(),
                                    DatabaseUtils.getUserID(),
                                    volunteer_id, mCurrentEvent.getName() + " has been deleted by its organiser.", NewsMessage.EVENT_DELETED,
                                    false, false);
                            mDatabase.child("news/" + newsID).setValue(newsMessage);
                        }
                        for (String volunteer_id : mCurrentEvent.getAccepted_volunteers()) {
                            String newsID = mDatabase.child("news").push().getKey();
                            NewsMessage newsMessage = new NewsMessage(CalendarUtil.getCurrentTimeInMillis(), newsID, mCurrentEvent.getEventID(),
                                    DatabaseUtils.getUserID(),
                                    volunteer_id, mCurrentEvent.getName() + " has been deleted by its organiser.", NewsMessage.EVENT_DELETED,
                                    false, false);
                            mDatabase.child("news/" + newsID).setValue(newsMessage);
                        }
                        mDatabase.child("events").child(mCurrentEvent.getEventID()).setValue(null);
                        Toast.makeText(getActivity(), "Event deleted.", Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create();
        deleteDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {

            if (requestCode == GALLERY_INTENT) {
                uriPicture = data.getData();
                hasUserSelectedPicture = true;
                Picasso.with(getActivity()).load(uriPicture).fit().centerCrop().into(mImage);
            } else {
                if (requestCode == PICK_PDF) {
                    uriPDF = data.getData();
                    hasSelectedPDF = true;
                    changeContract.setText(ImageUtils.getFileName(uriPDF, getActivity()));
                }
            }
        }
    }

    public void toggleEdit(boolean bool) {

        mName.setEnabled(bool);
        mLocation.setEnabled(bool);
        mType.setEnabled(bool);
        mDescription.setEnabled(bool);
        mSize.setEnabled(bool);
        mStartDate.setEnabled(bool);
        mFinishDate.setEnabled(bool);
        mDeadline.setEnabled(bool);
    }

    public boolean validateForm() {

        boolean valid;
        valid = (editTextIsValid(mName) && editTextIsValid(mLocation) &&
                editTextIsValid(mDescription) && editTextIsValid(mDeadline) && editTextIsValid(mSize));
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

    private void populateSpinnerArray() {
        typeList.add("Sports");
        typeList.add("Music");
        typeList.add("Festival");
        typeList.add("Charity");
        typeList.add("Training");
        typeList.add("Other");
    }

    View.OnClickListener setonClickListenerCalendar(final EditText editText) {
        return new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final Calendar myCalendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                        month++;
                        editText.setText(dayOfMonth + "/" + month + "/" + year);
                        month--;
                        myCalendar.set(year, month, dayOfMonth);
                        if (editText.equals(mStartDate))
                            currentStartDate = myCalendar.getTimeInMillis();
                        else if (editText.equals(mFinishDate))
                            currentFinishDate = myCalendar.getTimeInMillis();
                        else if (editText.equals(mDeadline))
                            currentDeadline = myCalendar.getTimeInMillis();

                    }
                }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        };
    }
}
