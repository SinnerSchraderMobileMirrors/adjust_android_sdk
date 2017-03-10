package com.example.testapp;

import android.content.Context;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustConfig;

/**
 * Created by nonelse on 10.03.17.
 */

public class AdjustCommandExecutor extends ICommandExecutor {
    Context context;
    private static final String ON_CREATE = "onCreate";

    public AdjustCommandExecutor(Context context) {
        this.context = context;
    }

    @Override
    public void executeCommand(Command command) {
        super.executeCommand(command);

        if (command.methodName.equals(ON_CREATE)) {
            onCreate();
        }
    }

    private void onCreate() {
        String environment = command.getFirstParameterValue("environment");
        String appToken = command.getFirstParameterValue("appToken");

        AdjustConfig config = new AdjustConfig(this.context, appToken, environment);
        Adjust.onCreate(config);

        Adjust.onResume();
    }
}
