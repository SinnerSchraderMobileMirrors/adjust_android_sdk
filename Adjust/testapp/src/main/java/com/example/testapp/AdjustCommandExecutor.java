package com.example.testapp;

import android.content.Context;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.LogLevel;

import java.util.Map;

/**
 * Created by nonelse on 10.03.17.
 */

public class AdjustCommandExecutor extends ICommandExecutor {
    Context context;
    String basePath;

    public AdjustCommandExecutor(Context context) {
        this.context = context;
    }

    @Override
    public void executeCommand(Command command) {
        super.executeCommand(command);

        switch (command.methodName) {
            case "onCreate": onCreate(); break;
            case "teardown": teardown(); break;
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

    private void teardown() {
        String deleteStateString = command.getFirstParameterValue("deleteState");
        boolean deleteState = Boolean.parseBoolean(deleteStateString);

        AdjustFactory.teardown(deleteState);
    }
}
