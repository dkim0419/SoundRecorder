package com.danielkim.soundrecorder.fragments;

import android.content.Intent;
import android.text.format.DateUtils;

import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.activities.MainActivity;

import junit.framework.TestCase;

import org.junit.Rule;

import java.util.concurrent.TimeUnit;

import androidx.test.espresso.IdlingPolicies;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class RecordFragmentChronometerTest extends TestCase {
    @Rule
    private final ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class, false, false);

    @Override
    public void setUp() {
        activityTestRule.launchActivity(new Intent());
    }

    @Override
    public void tearDown() {
        activityTestRule.finishActivity();
    }

    public void testRecordFor5Seconds() {
        recordFor(DateUtils.SECOND_IN_MILLIS * 5, "00:05");
    }

    public void testRecordFor30Seconds() {
        recordFor(DateUtils.SECOND_IN_MILLIS * 30, "00:30");
    }

    public void testRecordFor75Seconds() {
        recordFor(DateUtils.SECOND_IN_MILLIS * 75, "01:15");
    }

    private static void recordFor(long waitingTime, String displayedTime) {
        onView(withId(R.id.btnRecord)).perform(click());

        IdlingPolicies.setMasterPolicyTimeout(waitingTime * 2, TimeUnit.MILLISECONDS);
        IdlingPolicies.setIdlingResourceTimeout(waitingTime * 2, TimeUnit.MILLISECONDS);

        IdlingResource idlingResource = new ChronometerIdlingResource(waitingTime);
        IdlingRegistry.getInstance().register(idlingResource);

        onView(withId(R.id.chronometer)).check(matches(withText(displayedTime)));
        onView(withId(R.id.btnRecord)).perform(click());

        IdlingRegistry.getInstance().unregister(idlingResource);
    }
}