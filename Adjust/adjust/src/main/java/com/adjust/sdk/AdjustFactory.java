package com.adjust.sdk;

import android.content.Context;

import java.io.IOException;
import java.net.*;

import javax.net.ssl.HttpsURLConnection;

public class AdjustFactory {
    private static IPackageHandler packageHandler = null;
    private static IRequestHandler requestHandler = null;
    private static IAttributionHandler attributionHandler = null;
    private static IActivityHandler activityHandler = null;
    private static ILogger logger = null;
    private static HttpsURLConnection httpURLConnection = null;
    private static ISdkClickHandler sdkClickHandler = null;

    private static long timerInterval = -1;
    private static long timerStart = -1;
    private static long sessionInterval = -1;
    private static long subsessionInterval = -1;
    private static BackoffStrategy sdkClickBackoffStrategy = null;
    private static BackoffStrategy packageHandlerBackoffStrategy = null;
    private static long maxDelayStart = -1;

    public static class URLGetConnection {
        HttpURLConnection httpURLConnection;
        URL url;
        URLGetConnection(HttpURLConnection httpURLConnection, URL url) {
            this.httpURLConnection = httpURLConnection;
            this.url = url;
        }
    }

    public static IPackageHandler getPackageHandler(ActivityHandler activityHandler,
                                                    Context context,
                                                    boolean startsSending) {
        if (packageHandler == null) {
            return new PackageHandler(activityHandler, context, startsSending);
        }
        packageHandler.init(activityHandler, context, startsSending);
        return packageHandler;
    }

    public static IRequestHandler getRequestHandler(IPackageHandler packageHandler) {
        if (requestHandler == null) {
            return new RequestHandler(packageHandler);
        }
        requestHandler.init(packageHandler);
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
            return BackoffStrategy.TEST_WAIT;
        }
        return packageHandlerBackoffStrategy;
    }

    public static IActivityHandler getActivityHandler(AdjustConfig config) {
        if (activityHandler == null) {
            return ActivityHandler.getInstance(config);
        }
        activityHandler.init(config);
        return activityHandler;
    }

    public static IAttributionHandler getAttributionHandler(IActivityHandler activityHandler,
                                                            ActivityPackage attributionPackage,
                                                            boolean startsSending) {
        if (attributionHandler == null) {
            return new AttributionHandler(activityHandler, attributionPackage, startsSending);
        }
        attributionHandler.init(activityHandler, attributionPackage, startsSending);
        return attributionHandler;
    }

    public static HttpURLConnection getHttpURLConnection(URL url) throws IOException {
        if (AdjustFactory.httpURLConnection == null) {
            return (HttpURLConnection)url.openConnection();
        }

        return AdjustFactory.httpURLConnection;
    }

    public static URLGetConnection getHttpURLGetConnection(URL url) throws IOException {
        if (AdjustFactory.httpURLConnection == null) {
            return new URLGetConnection((HttpURLConnection)url.openConnection(), url);
        }

        return new URLGetConnection(AdjustFactory.httpURLConnection, url);
    }

    public static ISdkClickHandler getSdkClickHandler(boolean startsSending) {
        if (sdkClickHandler == null) {
            return new SdkClickHandler(startsSending);
        }

        sdkClickHandler.init(startsSending);
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

    public static void setHttpURLConnection(HttpsURLConnection httpURLConnection) {
        AdjustFactory.httpURLConnection = httpURLConnection;
    }

    public static void setSdkClickHandler(ISdkClickHandler sdkClickHandler) {
        AdjustFactory.sdkClickHandler = sdkClickHandler;
    }
}
