package com.example.testapp;

import android.os.SystemClock;

/**
 * Created by nonelse on 10.03.17.
 */

public class SystemCommandExecutor extends ICommandExecutor {
    @Override
    public void executeCommand(Command command) {
        super.executeCommand(command);

        switch (command.methodName) {
            case "sleep": sleep(); break;
        }
    }

    private void sleep() {
        String millisString = command.getFirstParameterValue("millis");
        long millis = Long.parseLong(millisString);

        SystemClock.sleep(millis);
    }
}
