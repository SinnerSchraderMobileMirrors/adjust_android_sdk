package com.adjust.sdk;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by uerceg on 28/03/2017.
 */

public class ErrorHandler implements IErrorHandler {
    private CustomScheduledExecutor scheduledExecutor;
    private ILogger logger;
    private boolean paused;
    private List<ActivityPackage> packageQueue;
    private BackoffStrategy backoffStrategy;

    @Override
    public void teardown() {
        logger.verbose("ErrorHandler teardown");
        if (scheduledExecutor != null) {
            try {
                scheduledExecutor.shutdownNow();
            } catch(SecurityException se) {}
        }
        if (packageQueue != null) {
            packageQueue.clear();
        }

        scheduledExecutor = null;
        logger = null;
        packageQueue = null;
        backoffStrategy = null;
    }

    public ErrorHandler(boolean startsSending) {
        init(startsSending);
        this.logger = AdjustFactory.getLogger();
        this.scheduledExecutor = new CustomScheduledExecutor("ErrorHandler", false);
        this.backoffStrategy = AdjustFactory.getErrorBackoffStrategy();
    }

    @Override
    public void init(boolean startsSending) {
        this.paused = !startsSending;
        this.packageQueue = new ArrayList<ActivityPackage>();
    }

    @Override
    public void pauseSending() {
        paused = true;
    }

    @Override
    public void resumeSending() {
        paused = false;

        sendNextErrorPackage();
    }

    @Override
    public void sendErrorPackage(final ActivityPackage errorPackage) {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                packageQueue.add(errorPackage);
                logger.debug("Added error_package %d", packageQueue.size());
                logger.verbose("%s", errorPackage.getExtendedString());
                sendNextErrorPackage();
            }
        });
    }

    private void sendNextErrorPackage() {
        scheduledExecutor.submit(new Runnable() {
            @Override
            public void run() {
                sendNextErrorPackageI();
            }
        });
    }

    private void sendNextErrorPackageI() {
        if (paused) {
            return;
        }

        if (packageQueue.isEmpty()) {
            return;
        }

        final ActivityPackage errorPackage = packageQueue.remove(0);
        int retries = errorPackage.getRetries();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                sendErrorPackageI(errorPackage);
                sendNextErrorPackage();
            }
        };

        if (retries <= 0) {
            runnable.run();
            return;
        }

        long waitTimeMilliSeconds = Util.getWaitingTime(retries, backoffStrategy);

        double waitTimeSeconds = waitTimeMilliSeconds / 1000.0;
        String secondsString = Util.SecondsDisplayFormat.format(waitTimeSeconds);

        logger.verbose("Waiting for %s seconds before retrying error_package for the %d time", secondsString, retries);
        scheduledExecutor.schedule(runnable, waitTimeMilliSeconds, TimeUnit.MILLISECONDS);
    }

    private void sendErrorPackageI(ActivityPackage errorPackage) {
        String targetURL = Constants.BASE_URL + errorPackage.getPath();

        try {
            HttpsURLConnection connection = Util.createPOSTHttpsURLConnection(
                    targetURL,
                    errorPackage.getClientSdk(),
                    errorPackage.getParameters(),
                    packageQueue.size() - 1);

            ResponseData responseData = Util.readHttpResponse(connection, errorPackage);

            if (responseData.jsonResponse == null) {
                retrySendingI(errorPackage);
            }
        } catch (UnsupportedEncodingException e) {
            logErrorMessageI(errorPackage, "error_package failed to encode parameters", e);
        } catch (SocketTimeoutException e) {
            logErrorMessageI(errorPackage, "error_package request timed out. Will retry later", e);
            retrySendingI(errorPackage);
        } catch (IOException e) {
            logErrorMessageI(errorPackage, "error_package request failed. Will retry later", e);
            retrySendingI(errorPackage);
        } catch (Throwable e) {
            logErrorMessageI(errorPackage, "error_package runtime exception", e);
        }
    }

    private void retrySendingI(ActivityPackage errorPackage) {
        int retries = errorPackage.increaseRetries();

        logger.error("Retrying error_package for the %d time", retries);
        sendErrorPackage(errorPackage);
    }

    private void logErrorMessageI(ActivityPackage errorPackage, String message, Throwable throwable) {
        final String packageMessage = errorPackage.getFailureMessage();
        final String reasonString = Util.getReasonString(message, throwable);
        String finalMessage = String.format("%s. (%s)", packageMessage, reasonString);
        logger.error(finalMessage);
    }
}
