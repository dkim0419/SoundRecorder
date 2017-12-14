/*
 * Year: 2017. This class was added by iClaude.
 */

package com.danielkim.soundrecorder;

import android.Manifest;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.danielkim.soundrecorder.activities.MainActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/**
 * Tests on RecordFragment.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspressoRecordFragment {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE);

    /*
        Checks that:
        - when I click on the start/stop record button the UI changes correctly
        - when I stop recording the new recording is added to the file viewer Fragment
     */
    @Test
    public void startAndStopRecording() {
        String recording = mActivityRule.getActivity().getResources().getString(R.string.record_in_progress);
        String prompt = mActivityRule.getActivity().getResources().getString(R.string.record_prompt);

        // Start recording.
        onView(withId(R.id.btnRecord)).perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.recording_status_text)).check(matches(withText(containsString(recording))));
        onView(withId(R.id.tvChronometer)).check(matches(withText(not(containsString("00:00")))));

        // Stop recording.
        onView(withId(R.id.btnRecord)).perform(click());
        onView(withId(R.id.recording_status_text)).check(matches(withText(containsString(prompt))));
        onView(withId(R.id.tvChronometer)).check(matches(withText(containsString("00:00"))));

        // Check that the recording is added to FileViewerFragment.
        String myRecordings = mActivityRule.getActivity().getResources().getString(R.string.default_file_name);
        onView(withId(R.id.pager)).perform(swipeLeft());
        onView(withId(R.id.recyclerView)).perform(actionOnItemAtPosition(0, click()));
        onView(withId(R.id.file_name_text_view)).check(matches(withText(containsString(myRecordings))));
        pressBack();

        // Delete the recording.
        String deleteFile = mActivityRule.getActivity().getResources().getString(R.string.dialog_file_delete);
        String yes = mActivityRule.getActivity().getResources().getString(R.string.dialog_action_yes);

        onView(withText(containsString(myRecordings))).perform(longClick());
        onView(withText(containsString(deleteFile))).perform(click());
        onView(withText(containsString(yes))).perform(click());

        // Check that the recording is no longer in the list.
        onView(withText(containsString(myRecordings))).check(doesNotExist());
    }

    /*
        Checks that:
        - when I stop the Activity while recording the recording continues
        - when I start the Activity again the UI shows the ongoing recording correctly
     */
    @Test
    public void stopActivityWhileRecording() {
        String recording = mActivityRule.getActivity().getResources().getString(R.string.record_in_progress);
        String prompt = mActivityRule.getActivity().getResources().getString(R.string.record_prompt);

        onView(withId(R.id.btnRecord)).perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        MainActivity activity = mActivityRule.getActivity();
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                getInstrumentation().callActivityOnPause(activity);
                getInstrumentation().callActivityOnStop(activity);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getInstrumentation().callActivityOnRestart(activity);
                getInstrumentation().callActivityOnStart(activity);
                getInstrumentation().callActivityOnResume(activity);
            }
        });

        onView(withId(R.id.recording_status_text)).check(matches(withText(containsString(recording))));
        onView(withId(R.id.tvChronometer)).check(matches(withText(not(containsString("00:00")))));

        onView(withId(R.id.btnRecord)).perform(click());
        onView(withId(R.id.recording_status_text)).check(matches(withText(containsString(prompt))));
        onView(withId(R.id.tvChronometer)).check(matches(withText(containsString("00:00"))));

        // Delete the recording.
        String myRecordings = mActivityRule.getActivity().getResources().getString(R.string.default_file_name);
        String deleteFile = mActivityRule.getActivity().getResources().getString(R.string.dialog_file_delete);
        String yes = mActivityRule.getActivity().getResources().getString(R.string.dialog_action_yes);

        onView(withId(R.id.pager)).perform(swipeLeft());
        onView(withText(containsString(myRecordings))).perform(longClick());
        onView(withText(containsString(deleteFile))).perform(click());
        onView(withText(containsString(yes))).perform(click());

        // Check that the recording is no longer in the list.
        onView(withText(containsString(myRecordings))).check(doesNotExist());

    }

    /*
        Checks that:
        - when I destroy the Activity while recording the recording continues
        - when I start the Activity again the UI shows the ongoing recording correctly
    */
    @Test
    public void destroyActivityWhileRecording() {
        String recording = mActivityRule.getActivity().getResources().getString(R.string.record_in_progress);
        String prompt = mActivityRule.getActivity().getResources().getString(R.string.record_prompt);

        onView(withId(R.id.btnRecord)).perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        MainActivity activity = mActivityRule.getActivity();
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                activity.finish();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Intent intent = new Intent(InstrumentationRegistry.getTargetContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getInstrumentation().startActivitySync(intent);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.recording_status_text)).check(matches(withText(containsString(recording))));
        onView(withId(R.id.tvChronometer)).check(matches(withText(not(containsString("00:00")))));

        onView(withId(R.id.btnRecord)).perform(click());
        onView(withId(R.id.recording_status_text)).check(matches(withText(containsString(prompt))));
        onView(withId(R.id.tvChronometer)).check(matches(withText(containsString("00:00"))));

        // Delete the recording.
        String myRecordings = mActivityRule.getActivity().getResources().getString(R.string.default_file_name);
        String deleteFile = mActivityRule.getActivity().getResources().getString(R.string.dialog_file_delete);
        String yes = mActivityRule.getActivity().getResources().getString(R.string.dialog_action_yes);

        onView(withId(R.id.pager)).perform(swipeLeft());
        onView(withText(containsString(myRecordings))).perform(longClick());
        onView(withText(containsString(deleteFile))).perform(click());
        onView(withText(containsString(yes))).perform(click());

        // Check that the recording is no longer in the list.
        onView(withText(containsString(myRecordings))).check(doesNotExist());
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

}
