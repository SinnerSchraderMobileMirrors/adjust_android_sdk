package com.adjust.example;

import android.content.*;
import android.support.test.*;
import android.support.test.filters.*;
import android.support.test.rule.*;
import android.support.test.runner.*;

import com.adjust.analyzertest.*;
import com.adjust.sdk.*;

import org.junit.*;
import org.junit.runner.*;

import static junit.framework.Assert.*;

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
 * - This process would go back and forth for 10 bounces
 * - SDK would sent last attribution package when backend sends a response with NO "ask_in"
 * - End
 */
public class BasicTimeoutTest {
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
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        AdjustAnalyzer.reportState("AAA");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
