package com.obaied.commandsexample;

import android.os.*;
import android.provider.*;
import android.util.*;

import com.adjust.analyzertest.*;
import com.adjust.sdk.*;
import com.google.gson.*;
import com.google.gson.annotations.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Created by ab on 20/02/2017.
 */

public final class AnalyzerDictionary {
    private static final String TAG = "AnalyzerDictionary";

    public static void executeCommand(final String jsonString) {
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... paramz) {
                Command[] commands = new Gson().fromJson(jsonString, Command[].class);
                for (Command command : commands) {
                    String callingClass = command.getCallingClass();
                    String funcName = command.getFuncName();
                    List<Param> params = command.getParams();

                    Log.d(TAG, "run: Running command: " + command.toString());

                    switch (callingClass) {
                        case "AdjustAnalyzer":
                            Log.d(TAG, "executeCommand: calling class: AdjustAnalyzer");
                            Dictionary_AdjustAnalyzer.receiveCommand(funcName, params);
                            break;
                        case "AdjustFactory":
                            Log.d(TAG, "executeCommand: calling class: AdjustFactory");
                            Dictionary_AdjustFactory.receiveCommand(funcName, params);
                            break;
                        case "Adjust":
                            Log.d(TAG, "executeCommand: calling class: Adjust");
                            Dictionary_Adjust.receiveCommand(funcName, params);
                            break;
                        case "System":
                            Log.d(TAG, "executeCommand: calling class: System");
                            Dictionary_System.receiveCommand(funcName, params);
                            break;
                    }
                }
                return null;
            }
        }.execute();
    }

    private static class Dictionary_AdjustAnalyzer {
        public static void receiveCommand(String funcName, List<Param> params) {
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

    private static class Dictionary_System {
        public static void receiveCommand(String funcName, List<Param> params) {
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
        public static void receiveCommand(String funcName, List<Param> params) {
            switch (funcName) {
                case "onCreate":
                    String appToken = Dictionary_Util.getParam_String(params, "appToken");
                    String environment = Dictionary_Util.getParam_String(params, "environment");
                    Boolean allowSuppressLogLevel = Dictionary_Util.getParam_Boolean(params, "allowSuppressLogLevel");
                    AdjustConfig adjustConfig = new AdjustConfig(GlobalApplication.getAppContext(),
                            appToken, environment, allowSuppressLogLevel);
                    Adjust.onCreate(adjustConfig);
                    break;
                case "onResume":
                    Adjust.onResume();
                    break;
                case "onPause":
                    Adjust.onPause();
                    break;
            }
        }
    }

    private static class Dictionary_AdjustFactory {
        public static void receiveCommand(String funcName, List<Param> params) {
            switch (funcName) {
                case "tearDown":
                    AdjustFactory.teardown();
                    break;
            }
        }
    }

    private static class Dictionary_Util {
        public static String getParam_String(List<Param> params, String name) {
            for (Param param : params) {
                if (param.getName().equals(name)) {
                    return param.getValue();
                }
            }

            throw new WrongDictionaryNameSupplied();
        }

        public static int getParam_Int(List<Param> params, String name) {
            for (Param param : params) {
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

        public static long getParam_Long(List<Param> params, String name) {
            for (Param param : params) {
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

        public static Boolean getParam_Boolean(List<Param> params, String name) {
            for (Param param : params) {
                if (param.getName().equals(name)) {
                    try {
                        return Boolean.parseBoolean(param.getValue());
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


    public static class Command {
        @SerializedName("class")
        @Expose
        private String mClass;
        @SerializedName("funcName")
        @Expose
        private String mFuncName;
        @SerializedName("params")
        @Expose
        private List<Param> mParams;

        public Command(String mClass, String mFuncName, List<Param> mParams) {
            this.mClass = mClass;
            this.mFuncName = mFuncName;
            this.mParams = mParams;
        }

        public String getCallingClass() {
            return mClass;
        }

        public String getFuncName() {
            return mFuncName;
        }

        public List<Param> getParams() {
            return mParams;
        }

        @Override
        public String toString() {
            return "class: " + mClass + "\n"
                    + "funcName: " + mFuncName + "\n"
                    + "Param size: " + mParams.size() + "\n";
        }
    }

    public static class Param {
        @SerializedName("name")
        @Expose
        private String mName;
        @SerializedName("value")
        @Expose
        private String mValue;

        public Param(String mName, String mValue) {
            this.mName = mName;
            this.mValue = mValue;
        }

        public String getValue() {
            return mValue;
        }

        public String getName() {
            return mName;
        }
    }
}
