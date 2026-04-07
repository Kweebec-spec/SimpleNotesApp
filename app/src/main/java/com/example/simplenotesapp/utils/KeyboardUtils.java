package com.example.simplenotesapp.utils;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class KeyboardUtils {
    public static boolean handleDispatchTouchEvent(MotionEvent event, Activity activity) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View focusedView = activity.getCurrentFocus();
            if (focusedView instanceof EditText) {
                int[] location = new int[2];
                focusedView.getLocationOnScreen(location);
                float x = event.getRawX();
                float y = event.getRawY();
                boolean outsideX = x < location[0] || x > location[0] + focusedView.getWidth();
                boolean outsideY = y < location[1] || y > location[1] + focusedView.getHeight();
                if (outsideX || outsideY) {
                    focusedView.clearFocus();
                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
                    }
                }
            }
        }
        return false; // caller should still call super.dispatchTouchEvent(event)
    }
    // Add to KeyboardUtils.java
    public static void setupScrollViewKeyboardDismiss(View scrollView, Activity activity) {
        scrollView.setOnTouchListener((v, event) -> {
            handleDispatchTouchEvent(event, activity);
            return false; // don't consume — let scroll still work
        });
    }
}