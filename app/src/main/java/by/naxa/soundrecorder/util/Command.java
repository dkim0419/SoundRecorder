package by.naxa.soundrecorder.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

// Command enumeration
// more info - https://blog.shamanland.com/2016/02/int-string-enum.html
@IntDef({Command.INVALID, Command.START, Command.PAUSE, Command.STOP})
@Retention(RetentionPolicy.SOURCE)
public @interface Command {
    int INVALID = -1;
    int START = 0;
    int PAUSE = 1;
    int STOP = 2;
}
