package com.adjust.sdk;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by uerceg on 03/04/2017.
 */

public class UtilNetworking {
    private static String userAgent;

    private static ILogger getLogger() {
        return AdjustFactory.getLogger();
    }

    public static void setUserAgent(String userAgent) {
        UtilNetworking.userAgent = userAgent;
    }

    public static ResponseData createPOSTHttpsURLConnection(String urlString, ActivityPackage activityPackage, int queueSize) throws Exception {
        DataOutputStream wr = null;

        try {
            URL url = new URL(urlString);
            HttpsURLConnection connection = AdjustFactory.getHttpsURLConnection(url);
            Map<String, String> parameters = new HashMap<String, String>(activityPackage.getParameters());

            setDefaultHttpsUrlConnectionProperties(connection, activityPackage.getClientSdk());
            String authorizationHeader = buildAuthorizationHeader(parameters, activityPackage.getClientSdk(), activityPackage.getActivityKind().toString());
            if (authorizationHeader != null) {
                connection.setRequestProperty("Authorization", authorizationHeader);
            }

            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(getPostDataString(parameters, queueSize));

            ResponseData responseData = readHttpResponse(connection, activityPackage);

            return responseData;
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (wr != null) {
                    wr.flush();
                    wr.close();
                }
            } catch (Exception e) {}
        }
    }

    public static ResponseData createGETHttpsURLConnection(ActivityPackage activityPackage) throws Exception {
        try {
            Map<String, String> parameters = new HashMap<String, String>(activityPackage.getParameters());
            Uri uri = buildUri(activityPackage.getPath(), parameters);
            URL url = new URL(uri.toString());
            HttpsURLConnection connection = AdjustFactory.getHttpsURLConnection(url);

            setDefaultHttpsUrlConnectionProperties(connection, activityPackage.getClientSdk());
            String authorizationHeader = buildAuthorizationHeader(parameters, activityPackage.getClientSdk(), activityPackage.getActivityKind().toString());
            if (authorizationHeader != null) {
                connection.setRequestProperty("Authorization", authorizationHeader);
            }

            connection.setRequestMethod("GET");

            ResponseData responseData = readHttpResponse(connection, activityPackage);

            return responseData;
        } catch (Exception e) {
            throw e;
        }
    }

    private static ResponseData readHttpResponse(HttpsURLConnection connection, ActivityPackage activityPackage) throws Exception {
        StringBuffer sb = new StringBuffer();
        ILogger logger = getLogger();
        Integer responseCode = null;

        ResponseData responseData = ResponseData.buildResponseData(activityPackage);

        try {
            connection.connect();

            responseCode = connection.getResponseCode();
            InputStream inputStream;

            if (responseCode >= 400) {
                inputStream = connection.getErrorStream();
            } else {
                inputStream = connection.getInputStream();
            }

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
        }
        catch (Exception e) {
            logger.error("Failed to read response. (%s)", e.getMessage());
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        String stringResponse = sb.toString();
        logger.verbose("Response: %s", stringResponse);

        if (stringResponse == null || stringResponse.length() == 0) {
            return responseData;
        }

        JSONObject jsonResponse = null;

        try {
            jsonResponse = new JSONObject(stringResponse);
        } catch (JSONException e) {
            String message = String.format("Failed to parse json response. (%s)", e.getMessage());
            logger.error(message);
            responseData.message = message;
        }

        if (jsonResponse == null) {
            return responseData;
        }

        responseData.jsonResponse = jsonResponse;

        String message = jsonResponse.optString("message", null);

        responseData.message = message;
        responseData.timestamp = jsonResponse.optString("timestamp", null);
        responseData.adid = jsonResponse.optString("adid", null);

        if (message == null) {
            message = "No message found";
        }

        if (responseCode != null && responseCode == HttpsURLConnection.HTTP_OK) {
            logger.info("%s", message);
            responseData.success = true;
        } else {
            logger.error("%s", message);
        }

        return responseData;
    }

    private static String getPostDataString(Map<String, String> body, int queueSize) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : body.entrySet()) {
            String encodedName = URLEncoder.encode(entry.getKey(), Constants.ENCODING);
            String value = entry.getValue();
            String encodedValue = value != null ? URLEncoder.encode(value, Constants.ENCODING) : "";

            if (result.length() > 0) {
                result.append("&");
            }

            result.append(encodedName);
            result.append("=");
            result.append(encodedValue);
        }

        long now = System.currentTimeMillis();
        String dateString = Util.dateFormatter.format(now);

        result.append("&");
        result.append(URLEncoder.encode("sent_at", Constants.ENCODING));
        result.append("=");
        result.append(URLEncoder.encode(dateString, Constants.ENCODING));

        if (queueSize > 0) {
            result.append("&");
            result.append(URLEncoder.encode("queue_size", Constants.ENCODING));
            result.append("=");
            result.append(URLEncoder.encode("" + queueSize, Constants.ENCODING));
        }

        return result.toString();
    }

    private static void setDefaultHttpsUrlConnectionProperties(HttpsURLConnection connection, String clientSdk) {
        connection.setRequestProperty("Client-SDK", clientSdk);
        connection.setConnectTimeout(Constants.ONE_MINUTE);
        connection.setReadTimeout(Constants.ONE_MINUTE);

        if (userAgent != null) {
            connection.setRequestProperty("User-Agent", userAgent);
        }
    }

    private static String buildAuthorizationHeader(Map<String, String> parameters, String clientSdk, String activityKind) {
        String appSecretName = "app_secret";
        String appSecret = parameters.get(appSecretName);

        if (appSecret == null || appSecret.length() == 0) {
            return null;
        }

        Map<String, String> signatureDetails = getSignature(parameters, clientSdk, activityKind, appSecret);

        String algorithm = "md5";
        String signature = Util.md5(signatureDetails.get("clear_signature"));
        String fields = signatureDetails.get("fields");

        parameters.remove("app_secret");

        String signatureHeader = String.format("signature=\"%s\"", signature);
        String algorithmHeader = String.format("algorithm=\"%s\"", algorithm);
        String fieldsHeader = String.format("headers=\"%s\"", fields);

        String authorizationHeader = String.format("Signature %s,%s,%s", signatureHeader, algorithmHeader, fieldsHeader);
        getLogger().verbose("authorizationHeader clear: %s", authorizationHeader);

        return authorizationHeader;
    }

    private static Map<String, String> getSignature(
            final Map<String, String> parameters,
            final String clientSdk,
            final String activityKind,
            final String appSecret) {
        String sdkVersionName = "sdk_version";
        String sdkVersion = clientSdk != null ? clientSdk : "";

        String appVersionName = "app_version";
        String appVersion = parameters.get(appVersionName) != null ? parameters.get(appVersionName) : "";

        String activityKindName = "activity_kind";
        String activityKindValue = activityKind != null ? activityKind : "";

        String createdAtName = "created_at";
        String createdAt = ""; // parameters.get(createdAtName) != null ? parameters.get(createdAtName) : "";

        String googleAdIdName = "gps_adid";
        String googleAdId = null; // parameters.get(googleAdIdName);

        String fireAdIdName = "fire_adid";
        String fireAdId = parameters.get(fireAdIdName);

        String androidIdName = "android_id";
        String androidId = parameters.get(androidIdName);

        String macSha1Name = "mac_sha1";
        String macSha1 = parameters.get(macSha1Name);

        String macMd5Name = "mac_md5";
        String macMd5 = parameters.get(macMd5Name);

        String deviceIdentifier = "";
        String deviceIdentifierName = null;

        if (googleAdId != null) {
            deviceIdentifier = googleAdId;
            deviceIdentifierName = googleAdIdName;
        } else if (fireAdId != null) {
            deviceIdentifier = fireAdId;
            deviceIdentifierName = fireAdIdName;
        } else if (androidId != null) {
            deviceIdentifier = androidId;
            deviceIdentifierName = androidIdName;
        } else if (macSha1 != null) {
            deviceIdentifier = macSha1;
            deviceIdentifierName = macSha1Name;
        } else if (macMd5 != null) {
            deviceIdentifier = macMd5;
            deviceIdentifierName = macMd5Name;
        } else {
            deviceIdentifier = "";
            deviceIdentifierName = "gps_adid";
        }

        Map<String, String> signatureParams = new HashMap<String, String>();

        signatureParams.put("app_secret", appSecret);
        signatureParams.put(sdkVersionName, sdkVersion);
        signatureParams.put(appVersionName, appVersion);
        signatureParams.put(createdAtName, createdAt);
        signatureParams.put(activityKindName, activityKindValue);
        signatureParams.put(deviceIdentifierName, deviceIdentifier);

        String clearSignature = "";
        String fields = "";

        for (Map.Entry<String, String> entry : signatureParams.entrySet())  {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                clearSignature += entry.getValue();
                fields += entry.getKey() + " ";
            }
        }

        // Remove last empty space.
        fields = fields.substring(0, fields.length() - 1);

        HashMap<String, String> signature = new HashMap<String, String>();

        signature.put("clear_signature", clearSignature);
        signature.put("fields", fields);

        return signature;
    }

    private static Uri buildUri(String path, Map<String, String> parameters) {
        Uri.Builder uriBuilder = new Uri.Builder();

        uriBuilder.scheme(Constants.SCHEME);
        uriBuilder.authority(Constants.AUTHORITY);
        uriBuilder.appendPath(path);

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            uriBuilder.appendQueryParameter(entry.getKey(), entry.getValue());
        }

        long now = System.currentTimeMillis();
        String dateString = Util.dateFormatter.format(now);

        uriBuilder.appendQueryParameter("sent_at", dateString);

        return uriBuilder.build();
    }
}
