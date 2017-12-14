/*
 * Copyright (c) 2017 Claudio "iClaude" Agostini <agostini.claudio1@gmail.com>
 * Licensed under the Apache License, Version 2.0
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
            File f = new File(directoryPath);
            boolean available = f.mkdirs() || f.isDirectory();
            if (available)
                return directoryPath;
        }

        return context.getFilesDir().getAbsolutePath();
    }

    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }
}
