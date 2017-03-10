package com.example.testapp;

import android.content.Context;

import com.example.testlibrary.ICommandListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nonelse on 09.03.17.
 */

public class CommandListener implements ICommandListener {
    Map<String, ICommandExecutor> classMap;

    public CommandListener(Context context) {
        classMap = new HashMap<String, ICommandExecutor>();
        classMap.put ("Adjust", new AdjustCommandExecutor(context));
        classMap.put ("System", new SystemCommandExecutor());
    }

    @Override
    public void executeCommand(String className, String methodName, Map<String, List<String>> parameters) {
        ICommandExecutor commandExecutor = this.classMap.get(className);
        if (commandExecutor == null) {
            // XXX
            return;
        }
        commandExecutor.executeCommand(new Command(className, methodName, parameters));
    }
}
