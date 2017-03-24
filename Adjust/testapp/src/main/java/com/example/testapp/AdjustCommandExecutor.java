package com.example.testapp;

import android.content.Context;
import android.net.*;
import android.util.*;

import com.adjust.sdk.*;

/**
 * Created by nonelse on 10.03.17.
 */

public class AdjustCommandExecutor extends ICommandExecutor {
    Context context;
    String basePath;
    private static final String TAG = "AdjustCommandExecutor";

    public AdjustCommandExecutor(Context context) {
        this.context = context;
    }

    @Override
    public void executeCommand(Command command) {
        super.executeCommand(command);

        try {
            switch (command.methodName) {
                case "onCreate": onCreate(); break;
                case "trackEvent": trackEvent(); break;
                case "onResume": onResume(); break;
                case "onPause": onPause(); break;
                case "setEnabled": setEnabled(); break;
                case "setReferrer": setReferrer(); break;
                case "setOfflineMode": setOfflineMode(); break;
                case "sendFirstPackages": sendFirstPackages(); break;
                case "addSessionCallbackParameter": addSessionCallbackParameter(); break;
                case "addSessionPartnerParameter": addSessionPartnerParameter(); break;
                case "removeSessionCallbackParameter": removeSessionCallbackParameter(); break;
                case "removeSessionPartnerParameter": removeSessionPartnerParameter(); break;
                case "resetSessionCallbackParameters": resetSessionCallbackParameters(); break;
                case "resetSessionPartnerParameters": resetSessionPartnerParameters(); break;
                case "setPushToken": setPushToken(); break;
                case "teardown": teardown(); break;
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            Log.e(TAG, "executeCommand: failed to parse command. Check commands' syntax");
        }
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    private void onCreate() {
        String environment = command.getFirstParameterValue("environment");
        String appToken = command.getFirstParameterValue("appToken");

        AdjustConfig config = new AdjustConfig(this.context, appToken, environment);
        config.setBasePath(basePath);

        config.setLogLevel(LogLevel.VERBOSE);

        Adjust.onCreate(config);

        Adjust.onResume();
    }

    private void trackEvent() throws NullPointerException {
        String eventToken = command.getFirstParameterValue("eventToken");

        AdjustEvent event = new AdjustEvent(eventToken);

        String temp;
        if((temp = command.getFirstParameterValue("currency")) != null) {
            Double revenue = Double.valueOf(command.getFirstParameterValue("revenue"));
            event.setRevenue(revenue, temp);
        } else if((temp = command.getFirstParameterValue("callbackParams")) != null) {
            String[] pairs = temp.split(",,,");
            for (String pair : pairs) {
                String[] keyAndValue = pair.split("@");
                event.addCallbackParameter(keyAndValue[0], keyAndValue[1]);
            }
        } else if((temp = command.getFirstParameterValue("partnerParams")) != null) {
            String[] pairs = temp.split(",,,");
            for (String pair : pairs) {
                String[] keyAndValue = pair.split("@");
                event.addPartnerParameter(keyAndValue[0], keyAndValue[1]);
            }
        }

        Adjust.trackEvent(event);
    }

    private void setReferrer() throws NullPointerException {
        String referrer = command.getFirstParameterValue("referrer");
        Adjust.setReferrer(referrer);
    }

    private void onPause() throws NullPointerException {
        Adjust.onPause();
    }

    private void onResume() throws NullPointerException {
        Adjust.onResume();
    }

    private void setEnabled() throws NullPointerException {
        Boolean enabled = Boolean.valueOf(command.getFirstParameterValue("enabled"));
        Adjust.setEnabled(enabled);
    }

    private void setOfflineMode() throws NullPointerException {
        Boolean enabled = Boolean.valueOf(command.getFirstParameterValue("enabled"));
        Adjust.setOfflineMode(enabled);
    }

    private void sendFirstPackages() throws NullPointerException {
        Adjust.sendFirstPackages();
    }

    private void addSessionCallbackParameter() throws NullPointerException {
        String key = command.getFirstParameterValue("key");
        String value = command.getFirstParameterValue("value");

        Adjust.addSessionCallbackParameter(key, value);
    }

    private void addSessionPartnerParameter() throws NullPointerException {
        String key = command.getFirstParameterValue("key");
        String value = command.getFirstParameterValue("value");

        Adjust.addSessionPartnerParameter(key, value);
    }

    private void removeSessionCallbackParameter() throws NullPointerException {
        String key = command.getFirstParameterValue("key");

        Adjust.removeSessionCallbackParameter(key);
    }

    private void removeSessionPartnerParameter() throws NullPointerException {
        String key = command.getFirstParameterValue("key");

        Adjust.removeSessionCallbackParameter(key);
    }

    private void resetSessionCallbackParameters() throws NullPointerException {
        Adjust.resetSessionCallbackParameters();
    }

    private void resetSessionPartnerParameters() throws NullPointerException {
        Adjust.resetSessionPartnerParameters();
    }

    private void setPushToken() throws NullPointerException {
        String token = command.getFirstParameterValue("token");

        Adjust.setPushToken(token);
    }

    private void teardown() throws NullPointerException {
        String deleteStateString = command.getFirstParameterValue("deleteState");
        boolean deleteState = Boolean.parseBoolean(deleteStateString);

        AdjustFactory.teardown(deleteState);
    }

}
