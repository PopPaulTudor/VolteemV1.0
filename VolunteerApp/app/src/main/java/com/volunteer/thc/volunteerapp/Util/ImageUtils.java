package com.volunteer.thc.volunteerapp.util;

import android.app.Activity;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.DisplayMetrics;
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


    public static byte[] compressImage(Uri fileUri, Activity activity,Resources resources) {
        byte[] byteArray = null;
        InputStream imageStream = null;
        try {
            imageStream = activity.getContentResolver().openInputStream(fileUri);
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }

        Bitmap bmp = BitmapFactory.decodeStream(imageStream);

        int width= (int)(bmp.getWidth()*getImageFactor(resources));
        int height=(int)(bmp.getHeight()*getImageFactor(resources));

        if(width>1024) width=1024;
        if(height>1024) height=1024;
        bmp=Bitmap.createScaledBitmap(bmp,width,height,false);

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

    public static Bitmap getCroppedBitmap(Bitmap bitmap,Resources resources) {

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth()/2, bitmap.getHeight()/2 ,
                bitmap.getWidth()/2 , paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        int width= (int)(output.getWidth()*getImageFactor(resources));
        int height=(int)(output.getHeight()*getImageFactor(resources));

        return Bitmap.createScaledBitmap(output, width, height, false);
    }

    private static float getImageFactor(Resources resources){
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float multiplier=metrics.density/3f;
        return multiplier;
    }


    public static String getFileName(Uri uri,Activity activity) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }







}





