package com.example.testlibrary;

import android.util.Base64;
import android.util.Log;

import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.Constants;
import com.adjust.sdk.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static com.example.testlibrary.Constants.COOKIES_HEADER;
import static com.example.testlibrary.Constants.COOKIE_NAME;
import static com.example.testlibrary.Constants.LOGTAG;

/**
 * Created by nonelse on 09.03.17.
 */

public class TestLibrary {
    private static String formatErrorMessage = "Error formating log message: %s, with params: %s";

    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    String schema;
    String authority;
    ICommandListener commandListener;
    ICommandJsonListener commandJsonListener;
    Util.IConnectionOptions connectionOptions;
    TrustManager[] trustAllCerts;
    HostnameVerifier hostnameVerifier;
    CookieManager cookieManager = new CookieManager();
    Gson gson = new Gson();
    Type stringStringMap = new TypeToken<Map<String, String>>(){}.getType();
    String testSessionId;

    public TestLibrary(String schema, String authority, ICommandJsonListener commandJsonListener) {
        this(schema, authority);
        this.commandJsonListener = commandJsonListener;
    }

    public TestLibrary(String schema, String authority, ICommandListener commandListener) {
        this(schema, authority);
        this.commandListener = commandListener;
    }

    private TestLibrary(String schema, String authority) {
        this.schema = schema;
        this.authority = authority;

        this.trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        debug("getAcceptedIssuers");

                        return null;
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                        debug("checkClientTrusted");
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                        debug("checkServerTrusted");
                    }
                }
        };

        this.hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        this.connectionOptions = new Util.IConnectionOptions() {
            @Override
            public void applyConnectionOptions(HttpsURLConnection connection) {
                connection.setConnectTimeout(Constants.ONE_MINUTE);
                connection.setReadTimeout(Constants.ONE_MINUTE);
                try {
                    SSLContext sc = SSLContext.getInstance("SSL");
                    sc.init(null, trustAllCerts, new java.security.SecureRandom());
                    connection.setSSLSocketFactory(sc.getSocketFactory());

                    connection.setHostnameVerifier(hostnameVerifier);
                    debug("applyConnectionOptions");
                } catch (Exception e) {
                    debug("applyConnectionOptions %s", e.getMessage());
                }
            }
        };

        registerEndPoint();
    }

    private String getBaseUrl() {
        String baseUrl = String.format(Locale.US, "%s://%s", this.schema, this.authority);
        if (testSessionId != null && testSessionId.length() > 0) {
            baseUrl = baseUrl + "/" + testSessionId;
        }
        return baseUrl;
    }

    public void registerEndPoint() {
        AdjustFactory.setBaseUrl(getBaseUrl());
        AdjustFactory.setScheme(schema);
        AdjustFactory.setAuthority(authority);
    }

    public void initTestSession() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                sendTestSessionI();
            }
        });
    }

    private void sendTestSessionI() {
        String targetURL = AdjustFactory.getBaseUrl() + "/init_session";
        debug("targetURL: %s", targetURL);

        try {
            HttpsURLConnection connection = Util.createPOSTHttpsURLConnection(
                    targetURL, null, this.connectionOptions);
            Util.HttpResponse httpResponse = Util.readHttpResponse(connection);
            debug("Response: %s", httpResponse.response);

            Map<String, List<String>> headerFields = connection.getHeaderFields();
            debug("Header: %s", headerFields);

            List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);
            debug("Cookies: %s", cookiesHeader);

            for (String cookie : cookiesHeader) {
                debug("Cookie: %s", cookie);
                parseExpressCookie(cookie);
            }

            List<TestCommand> testCommands = Arrays.asList(gson.fromJson(httpResponse.response, TestCommand[].class));
            execTestCommands(testCommands);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseExpressCookie(String expressCookie) {
        List<String> options = Arrays.asList(expressCookie.split("[; ]."));
        for (String option : options){
            debug("Cookie option: %s", option);
            List<String> keyAndValue= Arrays.asList(option.split("="));
            debug("Cookie keyAndValue: %s", keyAndValue);
            if (keyAndValue == null || keyAndValue.size() == 0) {
                continue;
            }
            String key = keyAndValue.get(0);
            debug("Cookie key: %s", key);
            if (COOKIE_NAME.equals(key)) {
                try {
                    String value = keyAndValue.get(1);
                    debug("Cookie session: %s", value);

                    byte[] data = Base64.decode(value, Base64.DEFAULT);
                    String text = new String(data, "UTF-8");
                    debug("Cookie session decoded: %s", text);

                    Map<String, String> json = gson.fromJson(text, stringStringMap);
                    debug("json : %s", json);

                    testSessionId = json.get("testSession");
                    // change path
                    registerEndPoint();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void execTestCommands(List<TestCommand> testCommands) {
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
            if (commandListener != null) {
                commandListener.executeCommand(testCommand.className, testCommand.functionName, testCommand.params);
            } else {
                commandJsonListener.executeCommand(testCommand.className, testCommand.functionName, this.gson.toJson(testCommand.params));
            }
        }
    }

    public void debug(String message, Object... parameters) {
        try {
            Log.d(LOGTAG, String.format(Locale.US, message, parameters));
        } catch (Exception e) {
            Log.e(LOGTAG, String.format(Locale.US, formatErrorMessage, message, Arrays.toString(parameters)));
        }
    }
}
