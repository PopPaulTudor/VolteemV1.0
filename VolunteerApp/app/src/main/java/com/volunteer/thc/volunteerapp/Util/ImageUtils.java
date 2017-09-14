package com.volunteer.thc.volunteerapp.util;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
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

        return Bitmap.createScaledBitmap(output, (int)(output.getWidth()*getImageFactor(resources)), (int)(output.getHeight()*getImageFactor(resources)), false);
    }


    public static float getImageFactor(Resources resources){
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float multiplier=metrics.density/3f;
        return multiplier;
    }



}





