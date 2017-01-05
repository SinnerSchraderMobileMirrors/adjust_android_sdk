package com.adjust.example;

import android.content.*;
import android.net.*;
import android.support.test.*;
import android.support.test.filters.*;
import android.support.test.filters.LargeTest;
import android.support.test.rule.*;
import android.support.test.runner.*;
import android.test.suitebuilder.annotation.*;
import android.util.*;

import com.adjust.sdk.*;

import org.junit.*;
import org.junit.runner.*;

import dalvik.annotation.*;

import static android.support.test.espresso.Espresso.getIdlingResources;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;

/**
 * Created by ab on 01/12/2016.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
/**
 * - Start
 * - SDK would send first `Session` package to backend
 * - Backend would send back response with an `ask_in` parameter
 * - SDK would send an `Attribution` package to backend.
 * - End
 */
public class BasicAttributionTest {
    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.adjust.example", appContext.getPackageName());
    }

    @Test
    public void foo() {
        AdjustAnalyzer.reportFooToRestApi("AAA");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
