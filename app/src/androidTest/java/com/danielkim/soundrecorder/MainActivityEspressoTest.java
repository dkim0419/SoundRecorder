/*
 * Copyright (c) 2017. This code was written by iClaude. All rights reserved.
 */

package com.danielkim.soundrecorder;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.danielkim.soundrecorder.activities.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/**
 * Created by iClaude on 11/10/2017.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityEspressoTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

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
        String defaultFileName = mActivityRule.getActivity().getResources().getString(R.string.default_file_name);
        onView(withId(R.id.recyclerView)).perform(scrollToPosition(0));
        onView(withText(containsString(defaultFileName))).check(matches(isDisplayed()));
    }
}
