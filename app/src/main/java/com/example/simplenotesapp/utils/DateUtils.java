package com.example.simplenotesapp.utils;

import java.text.DateFormat;
import java.util.Date;

public class DateUtils {
    public static String formatTimestamp(long timestamp) {
        try {
            return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                    .format(new Date(timestamp));
        } catch (Exception e) {
            return "";
        }
    }
}