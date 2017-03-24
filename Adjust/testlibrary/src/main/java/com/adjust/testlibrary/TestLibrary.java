package com.adjust.testlibrary;

import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.Util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.adjust.testlibrary.Constants.BASE_PATH_HEADER;
import static com.adjust.testlibrary.Constants.TEST_SCRIPT_HEADER;
import static com.adjust.testlibrary.Constants.TEST_SESSION_END_HEADER;
import static com.adjust.testlibrary.Utils.debug;
import static com.adjust.testlibrary.Utils.gson;
import static com.adjust.testlibrary.Utils.sendPostI;


/**
 * Created by nonelse on 09.03.17.
 */

public class TestLibrary {
    private static final String TEST_LIBRARY = "TestLibrary";

    String baseUrl;
    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    ICommandListener commandListener;
    ICommandJsonListener commandJsonListener;
    ControlChannel controlChannel = new ControlChannel(this);
    String currentTest;
    Future<?> lastFuture;
    public String currentBasePath;

    public TestLibrary(String baseUrl, ICommandJsonListener commandJsonListener) {
        this(baseUrl);
        this.commandJsonListener = commandJsonListener;
    }

    public TestLibrary(String baseUrl, ICommandListener commandListener) {
        this(baseUrl);
        this.commandListener = commandListener;
    }

    private TestLibrary(String baseUrl) {
        this.baseUrl = baseUrl;

        AdjustFactory.setBaseUrl(baseUrl);
        debug("base url: %s", AdjustFactory.getBaseUrl());
    }

    public void initTestSession(final String clientSdk) {
        lastFuture = executor.submit(new Runnable() {
            @Override
            public void run() {
                sendTestSessionI(clientSdk);
            }
        });
    }

    public void readHeaders(final Util.HttpResponse httpResponse) {
        lastFuture = executor.submit(new Runnable() {
            @Override
            public void run() {
                readHeadersI(httpResponse);
            }
        });
    }

    public void flushExecution() {
        debug("flushExecution");
        if (lastFuture != null && !lastFuture.isDone()) {
            debug("lastFuture.cancel");
            lastFuture.cancel(true);
        }
        executor.shutdownNow();
        executor = new ScheduledThreadPoolExecutor(1);
    }

    private void sendTestSessionI(String clientSdk) {
        Util.HttpResponse httpResponse = sendPostI("/init_session", clientSdk);
        if (httpResponse == null) {
            return;
        }

        readHeadersI(httpResponse);
    }

    public void readHeadersI(Util.HttpResponse httpResponse) {
        if (httpResponse.headerFields.containsKey(TEST_SESSION_END_HEADER)) {
            controlChannel.endControlChannel();
            debug("TestSessionEnd received");
            return;
        }

        if (httpResponse.headerFields.containsKey(BASE_PATH_HEADER)) {
            currentBasePath = httpResponse.headerFields.get(BASE_PATH_HEADER).get(0);
        }

        if (httpResponse.headerFields.containsKey(TEST_SCRIPT_HEADER)) {
            currentTest = httpResponse.headerFields.get(TEST_SCRIPT_HEADER).get(0);
            controlChannel.startControlChannel();

            List<TestCommand> testCommands = Arrays.asList(gson.fromJson(httpResponse.response, TestCommand[].class));
            execTestCommandsI(testCommands);
            return;
        }
    }

    private void execTestCommandsI(List<TestCommand> testCommands) {
        debug("testCommands: %s", testCommands);

        // set the base path for the start of the test
        if (commandListener != null) {
            commandListener.setBasePath(currentBasePath);
        } else {
            commandJsonListener.setBasePath(currentBasePath);
        }

        for (TestCommand testCommand : testCommands) {
            debug("ClassName: %s", testCommand.className);
            debug("FunctionName: %s", testCommand.functionName);
            debug("Params:");
            if (testCommand.params != null && testCommand.params.size() > 0) {
                for (Map.Entry<String, List<String>> entry : testCommand.params.entrySet()) {
                    debug("\t%s: %s", entry.getKey(), entry.getValue());
                }
            }
            long timeBefore = System.nanoTime();
            debug("time before %s %s: %d", testCommand.className, testCommand.functionName, timeBefore);

            if (TEST_LIBRARY.equals(testCommand.className)) {
                executeTestLibraryCommandI(testCommand);
                long timeAfter = System.nanoTime();
                long timeElapsedMillis = TimeUnit.NANOSECONDS.toMillis(timeAfter - timeBefore);
                debug("time after %s %s: %d", testCommand.className, testCommand.functionName, timeAfter);
                debug("time elapsed %s %s in milli seconds: %d", testCommand.className, testCommand.functionName, timeElapsedMillis);

                continue;
            }
            if (commandListener != null) {
                commandListener.executeCommand(testCommand.className, testCommand.functionName, testCommand.params);
            } else {
                commandJsonListener.executeCommand(testCommand.className, testCommand.functionName, gson.toJson(testCommand.params));
            }
            long timeAfter = System.nanoTime();
            long timeElapsedMillis = TimeUnit.NANOSECONDS.toMillis(timeAfter - timeBefore);
            debug("time after %s.%s: %d", testCommand.className, testCommand.functionName, timeAfter);
            debug("time elapsed %s.%s in milli seconds: %d", testCommand.className, testCommand.functionName, timeElapsedMillis);
        }
    }

    private void executeTestLibraryCommandI(TestCommand testCommand) {
        switch (testCommand.functionName) {
            case "end_test": endTestI(); break;
        }
    }

    private void endTestI() {
        Util.HttpResponse httpResponse = sendPostI(Utils.appendBasePath(currentBasePath, "/end_test"));
        this.currentTest = null;
        if (httpResponse == null) {
            return;
        }

        readHeadersI(httpResponse);
    }
}
