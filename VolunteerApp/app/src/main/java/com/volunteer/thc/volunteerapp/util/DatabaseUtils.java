package com.volunteer.thc.volunteerapp.util;

import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Cristi on 8/20/2017.
 */

public final class DatabaseUtils {

    /**
     * Writes new data to a given path in the database
     *
     * @param path The path to witch the new data will be written
     * @param data The object which contains the new data
     */
    public static void writeData(String path, @Nullable Object data) {
        FirebaseDatabase.getInstance().getReference().child(path).setValue(data);
    }

    public static String getUserID() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user.getUid();
    }
}
