package com.adjust.sdk.test;

import android.net.Uri;

import com.adjust.sdk.ActivityPackage;
import com.adjust.sdk.AdjustAttribution;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustEvent;
import com.adjust.sdk.IActivityHandler;
import com.adjust.sdk.ResponseData;
import com.adjust.sdk.EventResponseData;
import com.adjust.sdk.SessionResponseData;
import com.adjust.sdk.AttributionResponseData;


/**
 * Created by pfms on 09/01/15.
 */
public class MockActivityHandler implements IActivityHandler {
    private MockLogger testLogger;
    private String prefix = "ActivityHandler ";
    private AdjustConfig config;
    private ResponseData lastResponseData;

    public MockActivityHandler(MockLogger testLogger) {
        this.testLogger = testLogger;
    }

    @Override
    public void init(AdjustConfig config) {
        testLogger.test(prefix + "init");
        this.config = config;
    }

    @Override
    public void onResume() {
        testLogger.test(prefix + "onResume");
    }

    @Override
    public void onPause() {
        testLogger.test(prefix + "onPause");
    }

    @Override
    public void trackEvent(AdjustEvent event) {
        testLogger.test(prefix + "trackEvent, " + event);
    }

    @Override
    public void finishedTrackingActivity(ResponseData responseData) {
        testLogger.test(prefix + "finishedTrackingActivity, " + responseData);
        this.lastResponseData = responseData;
    }

    @Override
    public void setEnabled(boolean enabled) {
        testLogger.test(prefix + "setEnabled, " + enabled);
    }

    @Override
    public boolean isEnabled() {
        testLogger.test(prefix + "isEnabled");
        return false;
    }

    @Override
    public void readOpenUrl(Uri url, long clickTime) {
        testLogger.test(prefix + "readOpenUrl, " + url + ". ClickTime, " + clickTime);
    }

    @Override
    public boolean updateAttribution(AdjustAttribution attribution) {
        testLogger.test(prefix + "updateAttribution, " + attribution);
        return false;
    }

    @Override
    public void launchEventResponseTasks(EventResponseData eventResponseData) {
        testLogger.test(prefix + "launchEventResponseTasks, " + eventResponseData);
        this.lastResponseData = eventResponseData;
    }

    @Override
    public void launchSessionResponseTasks(SessionResponseData sessionResponseData) {
        testLogger.test(prefix + "launchSessionResponseTasks, " + sessionResponseData);
        this.lastResponseData = sessionResponseData;
    }

    @Override
    public void launchAttributionResponseTasks(AttributionResponseData attributionResponseData) {
        testLogger.test(prefix + "launchAttributionResponseTasks, " + attributionResponseData);
        this.lastResponseData = attributionResponseData;
    }

    @Override
    public void sendReferrer(String referrer, long clickTime) {
        testLogger.test(prefix + "sendReferrer, " + referrer + ". ClickTime, " + clickTime);
    }

    @Override
    public void setOfflineMode(boolean enabled) {
        testLogger.test(prefix + "setOfflineMode, " + enabled);
    }

    @Override
    public void setAskingAttribution(boolean askingAttribution) {
        testLogger.test(prefix + "setAskingAttribution, " + askingAttribution);
    }
}
