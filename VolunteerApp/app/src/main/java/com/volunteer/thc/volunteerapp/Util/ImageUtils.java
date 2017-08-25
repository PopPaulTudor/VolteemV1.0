package com.volunteer.thc.volunteerapp.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by poppa on 19.08.2017.
 */

public final class ImageUtils {

    private static final String TAG = "ImageUtils";


    public static byte[] compressImage(Uri fileUri, Activity activity) {
        byte[] byteArray = null;
        InputStream imageStream = null;
        try {
            imageStream = activity.getContentResolver().openInputStream(fileUri);
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }

        Bitmap bmp = BitmapFactory.decodeStream(imageStream);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        bmp.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        byteArray = stream.toByteArray();
        try {
            stream.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        return byteArray;
    }

}





