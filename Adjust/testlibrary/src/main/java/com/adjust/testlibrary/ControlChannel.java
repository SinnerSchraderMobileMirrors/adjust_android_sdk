package com.adjust.testlibrary;

import android.os.SystemClock;

import com.adjust.sdk.Util;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.adjust.testlibrary.Constants.TEST_CANCEL;
import static com.adjust.testlibrary.Utils.debug;
import static com.adjust.testlibrary.Utils.sendPostI;

/**
 * Created by nonelse on 21.03.17.
 */

public class ControlChannel {
    ScheduledThreadPoolExecutor controlChannelExecutor = new ScheduledThreadPoolExecutor(1);
    Future<?> controlChannelFuture;
    TestLibrary testLibrary;

    public ControlChannel(TestLibrary testLibrary) {
        this.testLibrary = testLibrary;
    }

    public void startControlChannel() {
        endControlChannel();
        debug("startControlChannel");
        this.controlChannelFuture = controlChannelExecutor.submit(new Runnable() {
            @Override
            public void run() {
            long timeBefore = System.nanoTime();
            debug("time before wait: %d", timeBefore);

            Util.HttpResponse httpResponse = sendPostI(
                    Utils.appendBasePath(testLibrary.currentBasePath,"/control"));

            long timeAfter = System.nanoTime();
            long timeElapsedMillis = TimeUnit.NANOSECONDS.toMillis(timeAfter - timeBefore);
            debug("time after wait: %d", timeAfter);
            debug("time elapsed waiting in milli seconds: %d", timeElapsedMillis);

            readControlHeaders(httpResponse);
            }
        });
    }

    void readControlHeaders(Util.HttpResponse httpResponse) {
        if (httpResponse.headerFields.containsKey(TEST_CANCEL)) {
            debug("Test canceled due to %s", httpResponse.headerFields.get(TEST_CANCEL).get(0));
            testLibrary.flushExecution();
        }
        testLibrary.readHeaders(httpResponse);
    }

    public void endControlChannel() {
        if (controlChannelFuture != null) {
            // if the side channel connection is not done yet
            if (!controlChannelFuture.isDone()) {
                // wait one second before forcefully ending the connection
                SystemClock.sleep(com.adjust.sdk.Constants.ONE_SECOND);
            }
            // if the side channel connection is still not done
            if (!controlChannelFuture.isDone()) {
                debug("cancel control channel");
                // then cancel the connection
                controlChannelFuture.cancel(true);
                // and create a new executor
                controlChannelExecutor = new ScheduledThreadPoolExecutor(1);
            }
        }
        controlChannelFuture = null;
    }
}
