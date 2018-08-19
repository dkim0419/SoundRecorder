package by.naxa.soundrecorder;

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

}
