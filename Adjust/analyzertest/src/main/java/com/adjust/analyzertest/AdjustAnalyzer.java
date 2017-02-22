package com.adjust.analyzertest;

import android.os.*;
import android.util.*;

import com.adjust.sdk.*;
import com.google.gson.*;
import com.google.gson.annotations.*;

import org.json.*;

import java.io.*;
import java.net.*;
import java.util.*;

import static android.content.ContentValues.TAG;

/**
 * Created by ab on 01/12/2016.
 */

public final class AdjustAnalyzer {
    private static boolean didInit = false;
    private static Command[] myCommands;

    private AdjustAnalyzer() {

    }

    public static void init(String baseUrl) {
        AdjustFactory.setBaseUrl(baseUrl);
        AdjustAnalyzer.didInit = true;

        AdjustAnalyzer.getCommands();
    }

    public static void reportState(String callsite) {
        if (!didInit) {
            AdjustFactory.getLogger().error("Init not called. Please call init before running Adjust.onCreate()");
            return;
        }

        final String targetURL = AdjustFactory.getBaseUrl() + "/state";

        //Tree map to have it organized (not sure why this is necessary. A regular map is also good)
        Map<String, Object> map = new TreeMap<>();

        //append callsite as first parameter
        map.put("call_site", callsite);

        //Activity handler's state
        ActivityHandler activityHandler = (ActivityHandler) AdjustFactory.getActivityHandler();
        if (activityHandler == null) {
            AdjustFactory.getLogger().error("activity handler is null. Couldn't report");
            return;
        }
        map.putAll(activityHandler.getState());

        //Package handler's state
        PackageHandler packageHandler = (PackageHandler) AdjustFactory.getPackageHandler();
        if (packageHandler == null) {
            AdjustFactory.getLogger().error("package handler is null. Couldn't report");
            return;
        }
        map.putAll(packageHandler.getState());

        final String json = new JSONObject(map).toString();

        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                URL url = null;
                try {
                    url = new URL(targetURL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setInstanceFollowRedirects(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");

                    OutputStream os = conn.getOutputStream();
                    os.write(json.getBytes());
                    os.flush();

                    Log.d("ADJUST", String.valueOf(conn.getResponseCode()));

                    BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

                    String output;
                    Log.d("ADJUST", "Output from Server .... \n");
                    while ((output = br.readLine()) != null) {
                        Log.d("ADJUST", output);
                    }

                    conn.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    public static void terminate() {
        if (!didInit) {
            AdjustFactory.getLogger().error("Init not called. Please call init before running Adjust.onCreate()");
            return;
        }

        final String targetURL = AdjustFactory.getBaseUrl() + "/terminate";

        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                URL url = null;
                try {
                    url = new URL(targetURL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setInstanceFollowRedirects(true);
                    conn.setRequestMethod("POST");
//                    conn.setRequestProperty("Content-Type", "application/json");

//                    OutputStream os = conn.getOutputStream();
//                    os.write(json.getBytes());
//                    os.flush();

                    Log.d("ADJUST", String.valueOf(conn.getResponseCode()));

//                    BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
//
//                    String output;
//                    Log.d("ADJUST", "Output from Server .... \n");
//                    while ((output = br.readLine()) != null) {
//                        Log.d("ADJUST", output);
//                    }

                    conn.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    private static void getCommands() {
    }

    public static void executeCommands(final AnalyzerCallback analyzerCallback) {
        if (!didInit) {
            AdjustFactory.getLogger().error("Init not called. Please call init before running Adjust.onCreate()");
            return;
        }

        final String targetURL = "http://pastebin.com/raw/CVwjpuaW";

        new AsyncTask<String, Void, String>() {
            HttpURLConnection conn = null;

            @Override
            protected String doInBackground(String... params) {
                URL url = null;
                StringBuilder output = new StringBuilder();
                try {
                    url = new URL(targetURL);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setInstanceFollowRedirects(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");

                    BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

                    Log.d("ADJUST", "Output from Server .... \n");
                    String line;
                    while ((line = br.readLine()) != null) {
                        output.append(line);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    conn.disconnect();
                }

                return output.toString();
            }

            @Override
            protected void onPostExecute(String jsonString) {
                //parse json string
                myCommands = new Gson().fromJson(jsonString, Command[].class);

//                = new Gson().fromJson(jsonString,
//                        new TypeToken<List<Command>>() {
//                        }.getType());

                Log.d(TAG, "onPostExecute: num Of Commands: " + myCommands.length);
                analyzerCallback.onPostGetCommands(myCommands);
            }
        }.execute();
    }

    public interface AnalyzerCallback {
        void onPostGetCommands(Command[] myCommands);
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
