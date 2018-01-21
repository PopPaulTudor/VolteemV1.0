package com.volunteer.thc.volunteerapp.presentation.volunteer;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by poppa on 16.01.2018.
 */

class ZoomOutPageTransformer implements ViewPager.PageTransformer {

    private static final float MIN_SCALE = 0.85f;
    private static final float MIN_ALPHA = 0.85f;

    @Override
    public void transformPage(View page, float position) {
        int pageWidth = page.getWidth();
        int pageHeight = page.getHeight();

        if (position < -1) page.setAlpha(0);
        else if (position <= 1) {
            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
            float vertMargin = pageHeight * (1 - scaleFactor) / 2;
            float horzMargin = pageWidth * (1 - scaleFactor) / 2;

            if (position < 0) page.setTranslationX(horzMargin - vertMargin / 2);
            else page.setTranslationX(-horzMargin + vertMargin / 2);

            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);

            page.setAlpha(MIN_ALPHA + (scaleFactor - MIN_ALPHA) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));

        } else page.setAlpha(0);
    }

}

