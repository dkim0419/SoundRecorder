/*
 * Copyright (c) 2017. This code was written by iClaude. All rights reserved.
 */

package com.danielkim.soundrecorder;

import android.Manifest;
import android.app.Activity;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;

import com.danielkim.soundrecorder.activities.AddScheduledRecordingActivity;
import com.danielkim.soundrecorder.activities.MainActivity;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.runner.lifecycle.Stage.RESUMED;
import static junit.framework.Assert.assertTrue;

/**
 * Created by iClaude on 20/10/2017.
 */

public class EspressoScheduledRecordingsFragment {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE);

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
        GregorianCalendar today = new GregorianCalendar();
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH);
        int day = today.get(Calendar.DAY_OF_MONTH);
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
        onView(withText("OK")).perform(click());

        // Check that the scheduled recording is no longer in the list.
        onView(withText("23:00")).check(doesNotExist());
    }

    /*
        Add a new scheduled recording with a duration of 3 minutes.
        Check that the recording is added to the list with a duration of 5 minutes.
        Delete the recording.
        Check that the recording is no longer in the list.
    */
    @Test
    public void addScheduledRecording3Minutes() {
        String save = mActivityRule.getActivity().getResources().getString(R.string.action_save);

        onView(withId(R.id.pager)).perform(swipeLeft());
        onView(withId(R.id.pager)).perform(swipeLeft()); // go to ScheduledRecordingsFragment
        onView(withId(R.id.fab_add)).perform(click()); // click on add new scheduled recording button

        // Set date and time for a scheduled recording in AddScheduledRecordingActivity.
        GregorianCalendar today = new GregorianCalendar();
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH);
        int day = today.get(Calendar.DAY_OF_MONTH);
        AddScheduledRecordingActivity activity = (AddScheduledRecordingActivity) getActivityInstance();
        activity.setDatesAndTimesForTesting(year, month, day, 23, 0, year, month, day, 23, 3);

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
        onView(withText("OK")).perform(click());

        // Check that the scheduled recording is no longer in the list.
        onView(withText("23:00")).check(doesNotExist());
    }

    /*
       Try to add a scheduled recording in the past.
       It should not be added (it should stay in the same Activity).
   */
    @Test
    public void addScheduledRecordingPast() {
        String save = mActivityRule.getActivity().getResources().getString(R.string.action_save);

        onView(withId(R.id.pager)).perform(swipeLeft());
        onView(withId(R.id.pager)).perform(swipeLeft()); // go to ScheduledRecordingsFragment
        onView(withId(R.id.fab_add)).perform(click()); // click on add new scheduled recording button

        // Set date and time for a scheduled recording in AddScheduledRecordingActivity.
        GregorianCalendar today = new GregorianCalendar();
        today.add(Calendar.DAY_OF_MONTH, -2);
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH);
        int day = today.get(Calendar.DAY_OF_MONTH);
        AddScheduledRecordingActivity activity = (AddScheduledRecordingActivity) getActivityInstance();
        activity.setDatesAndTimesForTesting(year, month, day, 23, 0, year, month, day, 23, 10);

        // Click on action save.
        ViewInteraction actionMenuItemView = onView(
                Matchers.allOf(withId(R.id.action_save), withText(save), isDisplayed()));
        actionMenuItemView.perform(click());

        // Check that we are still in the same Activity (scheduled recording not added).
        Activity currentActivity = getActivityInstance();
        boolean b = currentActivity instanceof AddScheduledRecordingActivity;
        assertTrue("We should be in AddScheduledRecordingActivity", b);
    }

    /*
       Try to add a scheduled recording with end time before start time.
       It should not be added (it should stay in the same Activity).
    */
    @Test
    public void addScheduledRecordingTimesMismatch() {
        String save = mActivityRule.getActivity().getResources().getString(R.string.action_save);

        onView(withId(R.id.pager)).perform(swipeLeft());
        onView(withId(R.id.pager)).perform(swipeLeft()); // go to ScheduledRecordingsFragment
        onView(withId(R.id.fab_add)).perform(click()); // click on add new scheduled recording button

        // Set date and time for a scheduled recording in AddScheduledRecordingActivity.
        GregorianCalendar today = new GregorianCalendar();
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH);
        int day = today.get(Calendar.DAY_OF_MONTH);
        AddScheduledRecordingActivity activity = (AddScheduledRecordingActivity) getActivityInstance();
        activity.setDatesAndTimesForTesting(year, month, day, 23, 10, year, month, day, 23, 0);

        // Click on action save.
        ViewInteraction actionMenuItemView = onView(
                Matchers.allOf(withId(R.id.action_save), withText(save), isDisplayed()));
        actionMenuItemView.perform(click());

        // Check that we are still in the same Activity (scheduled recording not added).
        Activity currentActivity = getActivityInstance();
        boolean b = currentActivity instanceof AddScheduledRecordingActivity;
        assertTrue("We should be in AddScheduledRecordingActivity", b);
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
