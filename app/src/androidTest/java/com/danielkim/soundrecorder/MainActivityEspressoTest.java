/*
 * Copyright (c) 2017. This code was written by iClaude. All rights reserved.
 */

package com.danielkim.soundrecorder;

import android.app.Activity;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;

import com.danielkim.soundrecorder.activities.AddScheduledRecordingActivity;
import com.danielkim.soundrecorder.activities.MainActivity;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.runner.lifecycle.Stage.RESUMED;
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
        String defaultFileName = mActivityRule.getActivity().getResources().getString(R.string.default_file_name);
        onView(withId(R.id.pager)).perform(swipeLeft());
        onView(withId(R.id.recyclerView)).perform(actionOnItemAtPosition(0, click()));
        onView(withId(R.id.file_name_text_view)).check(matches(withText(containsString(defaultFileName))));
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

        getInstrumentation().startActivitySync(new Intent(InstrumentationRegistry.getTargetContext(), MainActivity.class));
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
    }

    /*
        Add a new scheduled recording with correct data.
        Check that the recording is added to the list.
        Delete the recording.
        Check that the recording is no longer in the list.
     */
    @Test
    public void addScheduledRecordingCorrect() {
        String save = mActivityRule.getActivity().getResources().getString(R.string.action_save);

        onView(withId(R.id.pager)).perform(swipeLeft());
        onView(withId(R.id.pager)).perform(swipeLeft()); // go to ScheduledRecordingsFragment
        onView(withId(R.id.fab_add)).perform(click()); // click on add new scheduled recording button

        // Set date and time for a scheduled recording in AddScheduledRecordingActivity.
        GregorianCalendar tomorrow = new GregorianCalendar();
        int year = tomorrow.get(Calendar.YEAR);
        int month = tomorrow.get(Calendar.MONTH);
        int day = tomorrow.get(Calendar.DAY_OF_MONTH);
        AddScheduledRecordingActivity activity = (AddScheduledRecordingActivity) getActivityInstance();
        activity.setDatesAndTimesForTesting(year, month, day, 23, 0, year, month, day, 23, 5);

        // Click on action save.
        ViewInteraction actionMenuItemView = onView(
                Matchers.allOf(withId(R.id.action_save), withText(save), isDisplayed()));
        actionMenuItemView.perform(click());

        // Check that the new scheduled recording is added to the list.
        String scheduledRecording = mActivityRule.getActivity().getResources().getString(R.string.frag_sched_scheduled_recording);
        onView(withId(R.id.rvRecordings)).perform(scrollToPosition(0));
        onView(withText(scheduledRecording)).check(matches(isDisplayed()));
        onView(withText("23:00")).check(matches(isDisplayed()));
        onView(withText("23:05")).check(matches(isDisplayed()));

        // Delete the scheduled recording.
        onView(withText("23:00")).perform(longClick());
        ViewInteraction appCompatButton3 = onView(
                Matchers.allOf(withId(android.R.id.button1), withText("OK")));
        appCompatButton3.perform(scrollTo(), click());

        // Check that the scheduled recording is no longer in the list.
        onView(withText("23:00")).check(doesNotExist());
    }

    public Activity getActivityInstance() {
        final Activity[] activity = new Activity[1];
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                Activity currentActivity = null;
                Collection resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(RESUMED);
                if (resumedActivities.iterator().hasNext()) {
                    currentActivity = (Activity) resumedActivities.iterator().next();
                    activity[0] = currentActivity;
                }
            }
        });

        return activity[0];
    }

}
