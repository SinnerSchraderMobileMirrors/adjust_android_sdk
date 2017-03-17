package com.example.testlibrary;

import android.util.Base64;
import android.util.Log;

import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.CookieManager;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.net.ssl.HttpsURLConnection;

import static com.example.testlibrary.Constants.COOKIES_HEADER;
import static com.example.testlibrary.Constants.COOKIE_NAME;
import static com.example.testlibrary.Constants.LOGTAG;
import static com.example.testlibrary.Utils.debug;
import static com.example.testlibrary.Utils.gson;
import static com.example.testlibrary.Utils.sendPostI;
import static com.example.testlibrary.Utils.stringStringMap;

/**
 * Created by nonelse on 09.03.17.
 */

public class TestLibrary {
    private static final String TEST_LIBRARY = "TestLibrary";

    String baseUrl;
    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    ICommandListener commandListener;
    ICommandJsonListener commandJsonListener;
    CookieManager cookieManager = new CookieManager();
    String testSessionId;
    TestLibraryCommandExecutor testLibraryCommandExecutor = new TestLibraryCommandExecutor(this);

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

        registerEndPoint();
    }

    public void registerEndPoint() {
        String baseUrl = this.baseUrl;
        if (this.testSessionId != null && this.testSessionId.length() > 0) {
            baseUrl = baseUrl + "/" + this.testSessionId;
        }
        AdjustFactory.setBaseUrl(baseUrl);
        debug("base url: %s", AdjustFactory.getBaseUrl());
    }

    public void initTestSession(final String clientSdk) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                sendTestSessionI(clientSdk);
            }
        });
    }

    private void sendTestSessionI(String clientSdk) {
        this.testSessionId = null;
        debug("testSessionId : %s", testSessionId);
        // change path
        registerEndPoint();

        Util.HttpResponse httpResponse = sendPostI("/init_session", clientSdk);
        if (httpResponse == null) {
            return;
        }

        List<String> cookiesHeader = httpResponse.headerFields.get(COOKIES_HEADER);
        debug("Cookies: %s", cookiesHeader);

        for (String cookie : cookiesHeader) {
            //debug("Cookie: %s", cookie);
            parseExpressCookie(cookie);
        }

        List<TestCommand> testCommands = Arrays.asList(gson.fromJson(httpResponse.response, TestCommand[].class));
        execTestCommands(testCommands);
    }

    private void parseExpressCookie(String expressCookie) {
        List<String> options = Arrays.asList(expressCookie.split("[; ]."));
        for (String option : options){
            //debug("Cookie option: %s", option);
            List<String> keyAndValue= Arrays.asList(option.split("="));
            //debug("Cookie keyAndValue: %s", keyAndValue);
            if (keyAndValue == null || keyAndValue.size() == 0) {
                continue;
            }
            String key = keyAndValue.get(0);
            //debug("Cookie key: %s", key);
            if (COOKIE_NAME.equals(key)) {
                try {
                    String value = keyAndValue.get(1);
                    //debug("Cookie session: %s", value);

                    byte[] data = Base64.decode(value, Base64.DEFAULT);
                    String text = new String(data, "UTF-8");
                    debug("Cookie session decoded: %s", text);

                    Map<String, String> json = gson.fromJson(text, stringStringMap);
                    debug("json : %s", json);

                    this.testSessionId = json.get("testSessionId");
                    debug("testSessionId : %s", testSessionId);
                    // change path
                    registerEndPoint();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void execTestCommands(List<TestCommand> testCommands) {
        debug("testCommands: %s", testCommands);

        for (TestCommand testCommand: testCommands) {
            debug("ClassName: %s", testCommand.className);
            debug("FunctionName: %s", testCommand.functionName);
            debug("Params:");
            if (testCommand.params != null && testCommand.params.size() > 0) {
                for(Map.Entry<String, List<String>> entry : testCommand.params.entrySet()) {
                    debug("\t%s: %s", entry.getKey(), entry.getValue());
                }
            }
            if (TEST_LIBRARY.equals(testCommand.className)) {
                testLibraryCommandExecutor.executeCommand(testCommand);
                continue;
            }
            if (commandListener != null) {
                commandListener.executeCommand(testCommand.className, testCommand.functionName, testCommand.params);
            } else {
                commandJsonListener.executeCommand(testCommand.className, testCommand.functionName, gson.toJson(testCommand.params));
            }
        }
    }
}
