package by.naxa.soundrecorder;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * <a href="https://d.android.com/studio/test/">Test your app</a>
 */
@RunWith(AndroidJUnit4.class)
public class ApplicationTest {

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("by.naxa.soundrecorder", appContext.getPackageName());
    }

}