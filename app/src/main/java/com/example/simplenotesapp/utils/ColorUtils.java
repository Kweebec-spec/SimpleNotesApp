package com.example.simplenotesapp.utils;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

public class ColorUtils {
    public static void setColor(View view, String colorHex) {
        if (view == null || colorHex == null) return;
        Drawable backgroundDrawable = view.getBackground();
        if (backgroundDrawable instanceof GradientDrawable) {
            GradientDrawable shape = (GradientDrawable) backgroundDrawable.mutate();
            try {
                shape.setColor(android.graphics.Color.parseColor(colorHex));
            } catch (Exception e) {
                shape.setColor(android.graphics.Color.TRANSPARENT);
            }
        }
    }

    public static boolean isValidHex(String hex) {
        return hex.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3}|[A-Fa-f0-9]{8})$");
    }
}