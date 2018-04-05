package com.labs.vic.labspeedometer;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.PositionAssertions.isBottomAlignedWith;
import static android.support.test.espresso.assertion.PositionAssertions.isCompletelyAbove;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testNavigationView_Units() {
        onView(withId(R.id.navigation_units)).perform(click());
        onView(withId(R.id.units_menu)).check(isCompletelyAbove(withId(R.id.navigation_units)));
        onView(withId(R.id.units_kmh)).perform(click());
        onView(withId(R.id.units_menu)).check(isBottomAlignedWith(withId(R.id.container)));

        onView(withId(R.id.display_large)).check(matches(withText("0.00 km/h")));
        onView(withId(R.id.display_small)).check(matches(withText("0.00 km/h")));

        onView(withId(R.id.navigation_units)).perform(click());
        onView(withId(R.id.units_menu)).check(isCompletelyAbove(withId(R.id.navigation_units)));
        onView(withId(R.id.units_ms)).perform(click());

        onView(withId(R.id.display_large)).check(matches(withText("0.00 m/s")));
        onView(withId(R.id.display_small)).check(matches(withText("0.00 m/s")));

        onView(withId(R.id.navigation_units)).perform(click());
        onView(withId(R.id.navigation_home)).perform(click());
        onView(withId(R.id.units_menu)).check(isBottomAlignedWith(withId(R.id.container)));
    }
}