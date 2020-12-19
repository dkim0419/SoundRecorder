package com.danielkim.soundrecorder.activities;


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.danielkim.soundrecorder.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class Playback {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void playback() {
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
        cardView.perform(click());

        ViewInteraction floatingActionButton3 = onView(
                allOf(withId(R.id.fab_play),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        2),
                                1),
                        isDisplayed()));
        floatingActionButton3.perform(click());

        ViewInteraction floatingActionButton4 = onView(
                allOf(withId(R.id.fab_play),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        2),
                                1),
                        isDisplayed()));
        floatingActionButton4.perform(click());

        ViewInteraction floatingActionButton5 = onView(
                allOf(withId(R.id.fab_play),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        2),
                                1),
                        isDisplayed()));
        floatingActionButton5.perform(click());
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
