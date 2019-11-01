package by.naxa.soundrecorder.util;

import android.os.Environment;

import java.io.File;

public class Paths {

    public static final String SOUND_RECORDER_FOLDER = "/SoundRecorder.by";

    public static String combine(String parent, String... children) {
        return combine(new File(parent), children);
    }

    public static String combine(File parent, String... children) {
        File path = parent;
        for (String child : children) {
            path = new File(path, child);
        }
        return path.toString();
    }

    /**
     * Checks if external storage is available for read and write.
     */
    public static boolean isExternalStorageWritable() {
        return (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()));
    }

    /**
     * Checks if external storage is available to at least read.
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static double getFreeStorageSpacePercent() {
        long freespace = Environment.getExternalStorageDirectory().getFreeSpace();
        long totalspace = Environment.getExternalStorageDirectory().getTotalSpace();
        return (freespace * 100.0) / (double) totalspace;
    }

    public static void createDirectory(File parent, String... children){
        String cacheFileString = combine(parent, children);
        File cacheFile = new File(cacheFileString);
        if(!cacheFile.exists()){
            cacheFile.mkdirs();
        }
    }
}
