package by.naxa.soundrecorder;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import by.naxa.soundrecorder.activities.MainActivity;

/**
 * <a href="https://d.android.com/studio/test/">Test your app</a>
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTests {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testGreet() {

    }

}