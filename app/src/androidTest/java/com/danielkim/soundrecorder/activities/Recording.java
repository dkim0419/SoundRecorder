package com.danielkim.soundrecorder.activities;


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.danielkim.soundrecorder.R;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class Recording {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    // clean all files from storage dir
    @BeforeClass
    public static void setUp(){
        File dir = new File("/storage/self/primary/SoundRecorder");
        if(dir.exists() && dir.isDirectory()) {
            try {
                FileUtils.cleanDirectory(dir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /** Test for sequence: s0
     *  - 1. play stop
     *  - 2. play pause stop
     *  - 3. play pause play stop*/
    @Test
    public void recording() {
        ViewInteraction floatingActionButton = onView(
                allOf(withId(R.id.btnRecord),
                        childAtPosition(
                                allOf(withId(R.id.fragment_record),
                                        childAtPosition(
                                                withClassName(is("android.support.v4.app.NoSaveStateFrameLayout")),
                                                0)),
                                0),
                        isDisplayed()));
        floatingActionButton.perform(click());

        ViewInteraction floatingActionButton2 = onView(
                allOf(withId(R.id.btnRecord),
                        childAtPosition(
                                allOf(withId(R.id.fragment_record),
                                        childAtPosition(
                                                withClassName(is("android.support.v4.app.NoSaveStateFrameLayout")),
                                                0)),
                                0),
                        isDisplayed()));
        floatingActionButton2.perform(click());
    }

    @Test
    public void recording2(){
        ViewInteraction floatingActionButton3 = onView(
                allOf(withId(R.id.btnRecord),
                        childAtPosition(
                                allOf(withId(R.id.fragment_record),
                                        childAtPosition(
                                                withClassName(is("android.support.v4.app.NoSaveStateFrameLayout")),
                                                0)),
                                0),
                        isDisplayed()));
        floatingActionButton3.perform(click());

        ViewInteraction button = onView(
                allOf(withId(R.id.btnPause), withText("Pause"),
                        childAtPosition(
                                allOf(withId(R.id.fragment_record),
                                        childAtPosition(
                                                withClassName(is("android.support.v4.app.NoSaveStateFrameLayout")),
                                                0)),
                                3),
                        isDisplayed()));
        button.perform(click());

        ViewInteraction floatingActionButton4 = onView(
                allOf(withId(R.id.btnRecord),
                        childAtPosition(
                                allOf(withId(R.id.fragment_record),
                                        childAtPosition(
                                                withClassName(is("android.support.v4.app.NoSaveStateFrameLayout")),
                                                0)),
                                0),
                        isDisplayed()));
        floatingActionButton4.perform(click());

        ViewInteraction floatingActionButton5 = onView(
                allOf(withId(R.id.btnRecord),
                        childAtPosition(
                                allOf(withId(R.id.fragment_record),
                                        childAtPosition(
                                                withClassName(is("android.support.v4.app.NoSaveStateFrameLayout")),
                                                0)),
                                0),
                        isDisplayed()));
        floatingActionButton5.perform(click());

        ViewInteraction button2 = onView(
                allOf(withId(R.id.btnPause), withText("Pause"),
                        childAtPosition(
                                allOf(withId(R.id.fragment_record),
                                        childAtPosition(
                                                withClassName(is("android.support.v4.app.NoSaveStateFrameLayout")),
                                                0)),
                                3),
                        isDisplayed()));
        button2.perform(click());

        ViewInteraction button3 = onView(
                allOf(withId(R.id.btnPause), withText("Pause"),
                        childAtPosition(
                                allOf(withId(R.id.fragment_record),
                                        childAtPosition(
                                                withClassName(is("android.support.v4.app.NoSaveStateFrameLayout")),
                                                0)),
                                3),
                        isDisplayed()));
        button3.perform(click());

        ViewInteraction floatingActionButton6 = onView(
                allOf(withId(R.id.btnRecord),
                        childAtPosition(
                                allOf(withId(R.id.fragment_record),
                                        childAtPosition(
                                                withClassName(is("android.support.v4.app.NoSaveStateFrameLayout")),
                                                0)),
                                0),
                        isDisplayed()));
        floatingActionButton6.perform(click());
    }

    @Test
    public void recording3(){
        ViewInteraction floatingActionButton5 = onView(
                allOf(withId(R.id.btnRecord),
                        childAtPosition(
                                allOf(withId(R.id.fragment_record),
                                        childAtPosition(
                                                withClassName(is("android.support.v4.app.NoSaveStateFrameLayout")),
                                                0)),
                                0),
                        isDisplayed()));
        floatingActionButton5.perform(click());

        ViewInteraction button2 = onView(
                allOf(withId(R.id.btnPause), withText("Pause"),
                        childAtPosition(
                                allOf(withId(R.id.fragment_record),
                                        childAtPosition(
                                                withClassName(is("android.support.v4.app.NoSaveStateFrameLayout")),
                                                0)),
                                3),
                        isDisplayed()));
        button2.perform(click());

        ViewInteraction button3 = onView(
                allOf(withId(R.id.btnPause), withText("Pause"),
                        childAtPosition(
                                allOf(withId(R.id.fragment_record),
                                        childAtPosition(
                                                withClassName(is("android.support.v4.app.NoSaveStateFrameLayout")),
                                                0)),
                                3),
                        isDisplayed()));
        button3.perform(click());

        ViewInteraction floatingActionButton6 = onView(
                allOf(withId(R.id.btnRecord),
                        childAtPosition(
                                allOf(withId(R.id.fragment_record),
                                        childAtPosition(
                                                withClassName(is("android.support.v4.app.NoSaveStateFrameLayout")),
                                                0)),
                                0),
                        isDisplayed()));
        floatingActionButton6.perform(click());
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
