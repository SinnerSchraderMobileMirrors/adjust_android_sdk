package com.adjust.sdk;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by nonelse on 31.03.17.
 */

public class UtilNetworking {
    private static String userAgent;
    private static ActivityPackage errorPackage;

    private static ILogger getLogger() {
        return AdjustFactory.getLogger();
    }

    public static void setUserAgent(String userAgent) {
        UtilNetworking.userAgent = userAgent;
    }

    public static void setErrorPackage(ActivityPackage activityPackage) {
        UtilNetworking.errorPackage = activityPackage;
    }

    public static HttpsURLConnection createPOSTHttpsURLConnection(
            String urlString,
            String clientSdk,
            Map<String, String> parameters,
            int queueSize) throws IOException {
        DataOutputStream wr = null;
        HttpsURLConnection connection;

        try {
            URL url = new URL(urlString);
            connection = AdjustFactory.getHttpsURLConnection(url);

            setDefaultHttpsUrlConnectionProperties(connection, clientSdk, parameters);

            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(getPostDataString(parameters, queueSize));

            return connection;
        } catch (IOException e) {
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

    private static String getPostDataString(Map<String, String> body, int queueSize) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        for(Map.Entry<String, String> entry : body.entrySet()) {
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

    public static AdjustFactory.URLGetConnection createGETHttpsURLConnection(
            Uri.Builder uriBuilder,
            String clientSdk,
            Map<String, String> parameters) throws IOException {
        HttpsURLConnection connection;

        try {
            URL url = new URL(buildUriI(uriBuilder, parameters).toString());
            AdjustFactory.URLGetConnection urlGetConnection = AdjustFactory.getHttpsURLGetConnection(url);
            connection = urlGetConnection.httpsURLConnection;

            setDefaultHttpsUrlConnectionProperties(connection, clientSdk, parameters);

            connection.setRequestMethod("GET");

            return urlGetConnection;
        } catch (IOException e) {
            throw e;
        }
    }

    public static void setDefaultHttpsUrlConnectionProperties(HttpsURLConnection connection, String clientSdk, Map<String, String> parameters) {
        if (clientSdk != null) {
            connection.setRequestProperty("Client-SDK", clientSdk);
        }
        connection.setConnectTimeout(Constants.ONE_MINUTE);
        connection.setReadTimeout(Constants.ONE_MINUTE);
        if (userAgent != null) {
            connection.setRequestProperty("User-Agent", userAgent);
        }

        String authorizationHeader = buildAuthorizationHeader(parameters, clientSdk);
        connection.setRequestProperty("Authorization", authorizationHeader);
    }

    private static String buildAuthorizationHeader(Map<String, String> parameters, String clientSdk) {
        List<String> fieldsList = new ArrayList<String>(Arrays.asList(
                "app_version",
                "activity_kind",
                "created_at",
                "gps_adid",
                "app_secret"));

        String signature = buildSignature(fieldsList, parameters, clientSdk);
        String algorithm = "sha1";
        fieldsList.add(0, "sdk_version");
        String fields = android.text.TextUtils.join(" ", fieldsList);

        parameters.remove("app_secret");

        String signatureHeader = String.format("signature=\"%s\"", signature);
        String algorithmHeader = String.format("algorithm=\"%s\"", algorithm);
        String fieldsHeader = String.format("header=\"%s\"", fields);

        String authorizationHeader = String.format("Signature %s,%s,%s", signatureHeader, algorithmHeader, fieldsHeader);

        getLogger().verbose("authorizationHeader clear: %s", authorizationHeader);
        return authorizationHeader;
    }

    private static String buildSignature(List<String> fieldsList, Map<String, String> parameters, String clientSdk) {
        StringBuilder signatureBuilder = new StringBuilder();
        signatureBuilder.append(clientSdk);
        for (String fieldName : fieldsList) {
            String fieldValue = parameters.get(fieldName);
            if (fieldValue == null) {
                fieldValue = "";
            }
            signatureBuilder.append(fieldValue);
        }
        return Util.sha1(signatureBuilder.toString());
    }

    private static Uri buildUriI(Uri.Builder uriBuilder, Map<String, String> parameters) {
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            uriBuilder.appendQueryParameter(entry.getKey(), entry.getValue());
        }

        long now = System.currentTimeMillis();
        String dateString = Util.dateFormatter.format(now);

        uriBuilder.appendQueryParameter("sent_at", dateString);

        return uriBuilder.build();
    }


    public static ResponseData readHttpResponse(HttpsURLConnection connection, ActivityPackage activityPackage) throws Exception {
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
}
