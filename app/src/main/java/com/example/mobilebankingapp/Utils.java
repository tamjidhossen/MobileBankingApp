package com.example.mobilebankingapp;

import android.content.Context;
import android.widget.Toast;

public class Utils {
    private static final String TAG = "UTILS_TAG";

    /** A Function to show Toast
     @param context the context of activity/fragment from where this function will be called
     @param message the message to be shown in the Toast */
    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static long getTimestamp() {
        return System.currentTimeMillis();
    }
















}
