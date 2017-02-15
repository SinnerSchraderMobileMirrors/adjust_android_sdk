package com.adjust.sdk;

import android.content.Context;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.net.ssl.*;

public class AdjustFactory {
    private static IPackageHandler packageHandler = null;
    private static IRequestHandler requestHandler = null;
    private static IAttributionHandler attributionHandler = null;
    private static IActivityHandler activityHandler = null;
    private static ILogger logger = null;
    private static HttpURLConnection httpURLConnection = null;
    private static ISdkClickHandler sdkClickHandler = null;

    private static String baseUrl = "https://app.adjust.com";
    private static long timerInterval = -1;
    private static long timerStart = -1;
    private static long sessionInterval = -1;
    private static long subsessionInterval = -1;
    private static BackoffStrategy sdkClickBackoffStrategy = null;
    private static BackoffStrategy packageHandlerBackoffStrategy = null;
    private static long maxDelayStart = -1;

    public static void teardown() {
        packageHandler = null;
        requestHandler = null;
        attributionHandler = null;
        activityHandler = null;
        logger = null;
        httpURLConnection = null;
        sdkClickHandler = null;
    }

    public static class URLGetConnection {
        HttpURLConnection httpURLConnection;
        URL url;

        URLGetConnection(HttpURLConnection httpURLConnection, URL url) {
            this.httpURLConnection = httpURLConnection;
            this.url = url;
        }
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static void setBaseUrl(String baseUrl) {
        AdjustFactory.baseUrl = baseUrl;
    }

    public static IPackageHandler getPackageHandler(ActivityHandler activityHandler,
                                                    Context context,
                                                    boolean startsSending) {
        if (packageHandler == null) {
            packageHandler = new PackageHandler(activityHandler, context, startsSending);
        }

        return packageHandler;
    }

    public static IPackageHandler getPackageHandler() {
        if (packageHandler == null) {
            logger.error("Trying to retrieve PackageHandler without prior initialization. " +
                    "Call Adjust.onCreate() first before calling this function");
            return null;
        }

        return packageHandler;
    }

    public static IRequestHandler getRequestHandler(IPackageHandler packageHandler) {
        if (requestHandler == null) {
            requestHandler = new RequestHandler(packageHandler);
        }

        return requestHandler;
    }

    public static ILogger getLogger() {
        if (logger == null) {
            // Logger needs to be "static" to retain the configuration throughout the app
            logger = new Logger();

        }
        return logger;
    }

    public static long getTimerInterval() {
        if (timerInterval == -1) {
            return Constants.ONE_MINUTE;
        }
        return timerInterval;
    }

    public static long getTimerStart() {
        if (timerStart == -1) {
            return Constants.ONE_MINUTE;
        }
        return timerStart;
    }

    public static long getSessionInterval() {
        if (sessionInterval == -1) {
            return Constants.THIRTY_MINUTES;
        }
        return sessionInterval;
    }

    public static long getSubsessionInterval() {
        if (subsessionInterval == -1) {
            return Constants.ONE_SECOND;
        }
        return subsessionInterval;
    }

    public static BackoffStrategy getSdkClickBackoffStrategy() {
        if (sdkClickBackoffStrategy == null) {
            return BackoffStrategy.SHORT_WAIT;
        }
        return sdkClickBackoffStrategy;
    }

    public static BackoffStrategy getPackageHandlerBackoffStrategy() {
        if (packageHandlerBackoffStrategy == null) {
            return BackoffStrategy.LONG_WAIT;
        }
        return packageHandlerBackoffStrategy;
    }

    public static IActivityHandler getActivityHandler(AdjustConfig config) {
        if (activityHandler == null) {
            activityHandler = ActivityHandler.getInstance(config);
            if (activityHandler == null) {
                return null;
            }
        }

        return activityHandler;
    }

    public static IActivityHandler getActivityHandler() {
        if (activityHandler == null) {
            logger.error("Trying to retrieve ActivityHandler without AdjustConfig. " +
                    "Call Adjust.onCreate() first before calling this function");
            return null;
        }

        return activityHandler;
    }

    public static IAttributionHandler getAttributionHandler(IActivityHandler activityHandler,
                                                            ActivityPackage attributionPackage,
                                                            boolean startsSending) {
        if (attributionHandler == null) {
            attributionHandler = new AttributionHandler(activityHandler, attributionPackage, startsSending);
        }

        return attributionHandler;
    }

    public static HttpURLConnection getHttpURLConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

    public static URLGetConnection getHttpURLGetConnection(URL url) throws IOException {
        return new URLGetConnection((HttpURLConnection)url.openConnection(), url);
    }

    public static ISdkClickHandler getSdkClickHandler(boolean startsSending) {
        if (sdkClickHandler == null) {
            sdkClickHandler = new SdkClickHandler(startsSending);
        }

        return sdkClickHandler;
    }

    public static long getMaxDelayStart() {
        if (maxDelayStart == -1) {
            return Constants.ONE_SECOND * 10; // 10 seconds
        }
        return maxDelayStart;
    }

    public static void setPackageHandler(IPackageHandler packageHandler) {
        AdjustFactory.packageHandler = packageHandler;
    }

    public static void setRequestHandler(IRequestHandler requestHandler) {
        AdjustFactory.requestHandler = requestHandler;
    }

    public static void setLogger(ILogger logger) {
        AdjustFactory.logger = logger;
    }

    public static void setTimerInterval(long timerInterval) {
        AdjustFactory.timerInterval = timerInterval;
    }

    public static void setTimerStart(long timerStart) {
        AdjustFactory.timerStart = timerStart;
    }

    public static void setSessionInterval(long sessionInterval) {
        AdjustFactory.sessionInterval = sessionInterval;
    }

    public static void setSubsessionInterval(long subsessionInterval) {
        AdjustFactory.subsessionInterval = subsessionInterval;
    }

    public static void setSdkClickBackoffStrategy(BackoffStrategy sdkClickBackoffStrategy) {
        AdjustFactory.sdkClickBackoffStrategy = sdkClickBackoffStrategy;
    }

    public static void setPackageHandlerBackoffStrategy(BackoffStrategy packageHandlerBackoffStrategy) {
        AdjustFactory.packageHandlerBackoffStrategy = packageHandlerBackoffStrategy;
    }

    public static void setActivityHandler(IActivityHandler activityHandler) {
        AdjustFactory.activityHandler = activityHandler;
    }

    public static void setAttributionHandler(IAttributionHandler attributionHandler) {
        AdjustFactory.attributionHandler = attributionHandler;
    }

    public static void setHttpURLConnection(HttpURLConnection httpURLConnection) {
        AdjustFactory.httpURLConnection = httpURLConnection;
    }

    public static void setSdkClickHandler(ISdkClickHandler sdkClickHandler) {
        AdjustFactory.sdkClickHandler = sdkClickHandler;
    }

}

