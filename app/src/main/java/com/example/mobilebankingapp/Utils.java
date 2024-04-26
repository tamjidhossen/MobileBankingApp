package com.example.mobilebankingapp;

import android.content.Context;
import android.text.format.DateFormat;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

public class Utils {
    private static final String TAG = "UTILS_TAG";

    /** A Function to show Toast
     @param context the context of activity/fragment from where this function will be called
     @param message the message to be shown in the Toast */
    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static final String[] categories = {
            "Transportation",
            "Entertainment",
            "Healthcare",
            "Educations",
            "Food",
            "Rent",
            "Others"
    };

    public static long getTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     @param timestamp the timestamp of type Long that we need to format to dd/MM/yyyy
     @return timestamp formatted to date dd/mm/yyyy*/
    public static String formatTimestampDate(Long timestamp){
        if (timestamp == null) {
            return "00/00/00"; // or some default value
        }
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);
        String date = DateFormat.format("dd/MM/yyyy", calendar).toString();
        return date;
    }















}
