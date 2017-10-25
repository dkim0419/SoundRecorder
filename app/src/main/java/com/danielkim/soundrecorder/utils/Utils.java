/*
 * Copyright (c) 2017. This code was written by iClaude. All rights reserved.
 */

package com.danielkim.soundrecorder.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Common utility methods.
 */

public class Utils {

    /*
        Get, or create if necessary, the path of the directory where to save recordings.
     */
    public static String getDirectoryPath(Context context) {
        String directoryPath;
        if (isExternalStorageWritable()) {
            directoryPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SoundRecorder";
            new File(directoryPath).mkdirs();
        } else {
            directoryPath = context.getFilesDir().getAbsolutePath();
        }

        return directoryPath;
    }

    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
