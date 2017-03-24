package com.example.testapp;

import android.content.Context;
import android.util.Log;

import com.adjust.testlibrary.ICommandListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by nonelse on 09.03.17.
 */

public class CommandListener implements ICommandListener {
    Map<String, ICommandExecutor> classMap;
    AdjustCommandExecutor adjustCommandExecutor;

    public CommandListener(Context context) {
        adjustCommandExecutor = new AdjustCommandExecutor(context);
        classMap = new HashMap<String, ICommandExecutor>();
        classMap.put ("Adjust", adjustCommandExecutor);
        classMap.put ("System", new SystemCommandExecutor());
    }

    @Override
    public void executeCommand(String className, String methodName, Map<String, List<String>> parameters) {
        ICommandExecutor commandExecutor = this.classMap.get(className);
        if (commandExecutor == null) {
            debug("Could not find %s class to execute", className);
            return;
        }
        commandExecutor.executeCommand(new Command(className, methodName, parameters));
    }

    @Override
    public void setBasePath(String basePath) {
        adjustCommandExecutor.setBasePath(basePath);
    }

    static void debug(String message, Object... parameters) {
        try {
            Log.d("TestApp", String.format(Locale.US, message, parameters));
        } catch (Exception e) {
            Log.e("TestApp", String.format(Locale.US, "Error formating log message: %s, with params: %s"
                    , message, Arrays.toString(parameters)));
        }
    }
}
