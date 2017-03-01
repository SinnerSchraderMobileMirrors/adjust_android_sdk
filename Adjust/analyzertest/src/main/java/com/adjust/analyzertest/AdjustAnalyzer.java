package com.adjust.analyzertest;

import android.os.*;
import android.util.*;

import com.adjust.sdk.*;

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

    private AdjustAnalyzer() {

    }

    public static void init(String baseUrl, final AnalyzerCallback_OnPostInit callback) {
        AdjustFactory.setBaseUrl(baseUrl);

        //Send test scenario details
        final String targetURL = AdjustFactory.getBaseUrl() + "/init";

        final Map<String, String> map = new HashMap<>();
        map.put("scenario_type", "basic_attribution");
        final String json = new JSONObject(map).toString();

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                URL url = null;
                StringBuilder output = new StringBuilder();
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

                    String line;
                    while ((line = br.readLine()) != null) {
                        output.append(line);
                    }

                    conn.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return output.toString();
            }

            @Override
            protected void onPostExecute(String jsonString) {
                if (jsonString == null || jsonString.isEmpty()) {
                    Log.e(TAG, "onPostExecute: Couldn't retrieve JSON string");
                    return;
                }

                AdjustAnalyzer.didInit = true;
                callback.onPostInit(jsonString);
            }
        }.execute();
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

//    public static void executeCommands(final AnalyzerCallback_OnPostGetCommands analyzerCallback) {
//        if (!didInit) {
//            AdjustFactory.getLogger().error("Init not called. Please call init before running Adjust.onCreate()");
//            return;
//        }
//
//        final String targetURL = AdjustFactory.getBaseUrl() + "/commands";
//
//        new AsyncTask<String, Void, String>() {
//            HttpURLConnection conn = null;
//
//            @Override
//            protected String doInBackground(String... params) {
//                URL url = null;
//                StringBuilder output = new StringBuilder();
//                try {
//                    url = new URL(targetURL);
//                    conn = (HttpURLConnection) url.openConnection();
//                    InputStream in = new BufferedInputStream(conn.getInputStream());
//                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
//
//                    String line;
//                    while ((line = br.readLine()) != null) {
//                        output.append(line);
//                    }
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } finally {
//                    conn.disconnect();
//                }
//
//                return output.toString();
//            }
//
//            @Override
//            protected void onPostExecute(String jsonString) {
//                if (jsonString == null || jsonString.isEmpty()) {
//                    Log.e(TAG, "onPostExecute: Couldn't retrieve JSON string");
//                    return;
//                }
//
//                analyzerCallback.onPostGetCommands(jsonString);
//            }
//        }.execute();
//    }

    public interface AnalyzerCallback_OnPostInit {
        void onPostInit(String commands);
    }

}
