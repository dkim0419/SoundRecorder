package com.danielkim.soundrecorder.activities;


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.DataInteraction;
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

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DeleteFile {

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


    /** Test for sequence: s0 s1 s2 s4 s1
     * - 1. for delete cancel
     * - 2. for delete ok*/
    @Test
    public void deleteFile() {
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

        ViewInteraction textView = onView(
                allOf(withId(R.id.tab_title), withText("Saved Recordings"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.tabs),
                                        0),
                                1),
                        isDisplayed()));
        textView.perform(click());

        ViewInteraction viewPager = onView(
                allOf(withId(R.id.pager),
                        childAtPosition(
                                allOf(withId(R.id.main_activity),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                2),
                        isDisplayed()));
        viewPager.perform(swipeLeft());

        ViewInteraction cardView = onView(
                allOf(withId(R.id.card_view),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.recyclerView),
                                        0),
                                0),
                        isDisplayed()));
        cardView.perform(longClick());

        DataInteraction textView2 = onData(anything())
                .inAdapterView(allOf(withClassName(is("com.android.internal.app.AlertController$RecycleListView")),
                        childAtPosition(
                                withClassName(is("android.widget.FrameLayout")),
                                0)))
                .atPosition(3);
        textView2.perform(click());

        ViewInteraction button = onView(
                allOf(withId(android.R.id.button2), withText("No"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                2)));
        button.perform(scrollTo(), click());

        ViewInteraction cardView2 = onView(
                allOf(withId(R.id.card_view),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.recyclerView),
                                        0),
                                0),
                        isDisplayed()));
        cardView2.perform(longClick());

        DataInteraction textView3 = onData(anything())
                .inAdapterView(allOf(withClassName(is("com.android.internal.app.AlertController$RecycleListView")),
                        childAtPosition(
                                withClassName(is("android.widget.FrameLayout")),
                                0)))
                .atPosition(3);
        textView3.perform(click());

        ViewInteraction button2 = onView(
                allOf(withId(android.R.id.button1), withText("Yes"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                3)));
        button2.perform(scrollTo(), click());
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
