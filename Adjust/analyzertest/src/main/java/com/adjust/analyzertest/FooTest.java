package com.adjust.analyzertest;

import android.os.*;
import android.util.*;

/**
 * Created by ab on 13/02/2017.
 */

public class FooTest {
    private static final String TAG = "FooTest";

    public static void foo() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: reporting state");
                AdjustAnalyzer.reportState("callsite A");

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "run: sending terminate signal");
                AdjustAnalyzer.terminate();

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "run: Shutting down");
                System.exit(0);
            }
        }, 3000);
    }
}
