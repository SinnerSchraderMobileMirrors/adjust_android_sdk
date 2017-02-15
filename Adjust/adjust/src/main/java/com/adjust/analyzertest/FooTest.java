package com.adjust.analyzertest;

import android.os.*;
import android.util.*;

/**
 * Created by ab on 13/02/2017.
 */

public class FooTest {
    private static final String TAG = "FooTest";

    public static void foo() {
        Log.d(TAG, "foo: >>>>>>>>>>");
        AdjustAnalyzer.reportState("callsite A");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "foo: aaaa");
                AdjustAnalyzer.terminate();
            }
        }, 3000);
    }
}
