package com.danielkim.soundrecorder.fragments;

import android.content.Intent;

import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.activities.MainActivity;

import junit.framework.TestCase;

import org.junit.Rule;

import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

public class RecordFragmentTest extends TestCase {
    @Rule
    private final ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class, false, false);

    private MainActivity activity;

    @Override
    public void setUp() {
        activityTestRule.launchActivity(new Intent());
        activity = activityTestRule.getActivity();
    }

    @Override
    public void tearDown() {
        activityTestRule.finishActivity();
    }

    public void testStartNotificationDisplayedWhenRecordButtonTapped() {
        onView(withId(R.id.btnRecord)).perform(click());
        onView(withText(R.string.toast_recording_start)).inRoot(withDecorView(not(is(activity.getWindow().getDecorView())))).check(matches(isDisplayed()));
    }
}