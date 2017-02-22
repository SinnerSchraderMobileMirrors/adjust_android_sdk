package com.adjust.example;

import android.os.*;
import android.util.*;

import com.adjust.analyzertest.*;

import java.util.*;

/**
 * Created by ab on 20/02/2017.
 */

public final class AnalyzerDictionary {
    private static final String TAG = "AnalyzerDictionary";

    public static void executeCommand(final AdjustAnalyzer.Command[] commands) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                for (AdjustAnalyzer.Command command : commands) {
                    String callingClass = command.getCallingClass();
                    String funcName = command.getFuncName();
                    List<AdjustAnalyzer.Param> params = command.getParams();

                    Log.d(TAG, "run: Running command: " + command.toString());

                    switch (callingClass) {
                        case "AdjustAnalyzer":
                            Log.d(TAG, "executeCommand: calling class: AdjustAnalyzer");
                            Dictionary_AdjustAnalyzer.receiveCommand(funcName, params);
                            break;
                        case "Adjust":
                            Log.d(TAG, "executeCommand: calling class: Adjust");
                            Dictionary_Adjust.receiveCommand(funcName, params);
                            break;
                        case "":
                            Log.d(TAG, "executeCommand: calling class: none");
                            Dictionary_NoClass.receiveCommand(funcName, params);
                            break;
                    }
                }
            }
        });
    }

    private static class Dictionary_AdjustAnalyzer {
        public static void receiveCommand(String funcName, List<AdjustAnalyzer.Param> params) {
            switch (funcName) {
                case "reportState":
                    String callSite = Dictionary_Util.getParam_String(params, "callSite");
                    AdjustAnalyzer.reportState(callSite);
                    break;
                case "terminate":
                    AdjustAnalyzer.terminate();
                    break;
            }
        }
    }

    private static class Dictionary_NoClass {
        public static void receiveCommand(String funcName, List<AdjustAnalyzer.Param> params) {
            switch (funcName) {
                case "sleep":
                    long mills = Dictionary_Util.getParam_Long(params, "mills");

                    try {
                        Thread.sleep(mills);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    private static class Dictionary_Adjust {
        public static void receiveCommand(String funcName, List<AdjustAnalyzer.Param> params) {
            switch (funcName) {
                case "reportState":
                    String callSite = Dictionary_Util.getParam_String(params, "callSite");
                    AdjustAnalyzer.reportState(callSite);
                    break;
                case "terminate":
                    AdjustAnalyzer.terminate();
                    break;
            }
        }
    }

    private static class Dictionary_Util {
        public static String getParam_String(List<AdjustAnalyzer.Param> params, String name) {
            for (AdjustAnalyzer.Param param : params) {
                if (param.getName().equals(name)) {
                    return param.getValue();
                }
            }

            throw new WrongDictionaryNameSupplied();
        }

        public static int getParam_Int(List<AdjustAnalyzer.Param> params, String name) {
            for (AdjustAnalyzer.Param param : params) {
                if (param.getName().equals(name)) {
                    try {
                        return Integer.parseInt(param.getValue());
                    } catch (NumberFormatException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            throw new WrongDictionaryNameSupplied();
        }

        public static long getParam_Long(List<AdjustAnalyzer.Param> params, String name) {
            for (AdjustAnalyzer.Param param : params) {
                if (param.getName().equals(name)) {
                    try {
                        return Long.parseLong(param.getValue());
                    } catch (NumberFormatException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            throw new WrongDictionaryNameSupplied();
        }
    }

    public static class WrongDictionaryNameSupplied extends RuntimeException {
        public WrongDictionaryNameSupplied() {
            super("Wrong name supplied to dictionary");
        }
    }
}
