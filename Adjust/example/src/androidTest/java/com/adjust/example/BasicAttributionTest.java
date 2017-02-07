package com.adjust.example;

import android.content.*;
import android.content.pm.*;
import android.support.test.*;
import android.support.test.filters.LargeTest;
import android.support.test.rule.*;
import android.support.test.runner.*;
import android.util.*;

import com.adjust.sdk.*;
import com.adjust.sdk.BuildConfig;

import org.junit.*;
import org.junit.runner.*;

import static android.support.test.espresso.action.ViewActions.click;
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
    private static final String TAG = "BasicAttributionTest";
    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("com.adjust.example", appContext.getPackageName());
//        assertEquals("http://192.168.2.71:8081", AdjustConfig.getBaseUrl());
//        assertEquals("http://172.16.150.242:8081", AdjustConfig.getBaseUrl());


        assertEquals("1.0", getAppVersion_fake(appContext));
        assertEquals("4.10.4", getAppVersion_good(appContext));

        Log.d(TAG, "useAppContext: " + com.adjust.sdk.BuildConfig.VERSION_NAME);
        Log.d(TAG, "useAppContext: " + com.adjust.sdk.BuildConfig.VERSION_CODE);
        Log.d(TAG, "useAppContext: -----------------------");

        Log.d(TAG, "useAppContext: " + com.adjust.example.BuildConfig.VERSION_NAME);
        Log.d(TAG, "useAppContext: " + com.adjust.example.BuildConfig.VERSION_CODE);
    }

    private String getAppVersion_fake(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            String name = "com.adjust.example";
            PackageInfo info = packageManager.getPackageInfo(name, 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private String getAppVersion_good(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            String name = "com.adjust.sdk";
            PackageInfo info = packageManager.getPackageInfo(name, 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Test
    public void foo() {
        AdjustAnalyzer.reportState("callsite A");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        AdjustAnalyzer.terminate();
    }
}
