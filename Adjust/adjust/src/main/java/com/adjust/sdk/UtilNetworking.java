package com.adjust.sdk;

import android.net.Uri;
import android.util.Base64;

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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
        return createPOSTHttpsURLConnection(urlString, clientSdk, parameters, queueSize, true);
    }

    private static HttpsURLConnection createPOSTHttpsURLConnection(
            String urlString,
            String clientSdk,
            Map<String, String> parameters,
            int queueSize,
            boolean checkCerts) throws IOException {
        DataOutputStream wr = null;
        HttpsURLConnection connection;

        try {
            URL url = new URL(urlString);
            connection = AdjustFactory.getHttpsURLConnection(url);

            if (checkCerts) {
                setAdjustTrustManager(connection);
            }

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
            Map<String, String> parameters) throws IOException
    {
        return createGETHttpsURLConnection(uriBuilder, clientSdk, parameters, true);
    }

    private static AdjustFactory.URLGetConnection createGETHttpsURLConnection(
            Uri.Builder uriBuilder,
            String clientSdk,
            Map<String, String> parameters,
            boolean checkCerts) throws IOException {
        HttpsURLConnection connection;

        try {
            URL url = new URL(buildUriI(uriBuilder, parameters).toString());
            AdjustFactory.URLGetConnection urlGetConnection = AdjustFactory.getHttpsURLGetConnection(url);
            connection = urlGetConnection.httpsURLConnection;

            if (checkCerts) {
                setAdjustTrustManager(connection);
            }

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

        String authorizationHeader = buildAuthorizationHeader(parameters);
        connection.setRequestProperty("Authorization", authorizationHeader);
    }

    private static String buildAuthorizationHeader(Map<String, String> parameters) {
        List<String> fieldsList = Arrays.asList(
                "sdk_version",
                "app_version",
                "activity_kind",
                "created_at",
                "gps_adid",
                "app_token");

        String signature = buildSignature(fieldsList, parameters);
        String algorithm = "sha1";
        String fields = android.text.TextUtils.join(",", fieldsList);

        String signatureHeader = String.format("signature=\"%s\"", signature);
        String algorithmHeader = String.format("algorithm=\"%s\"", algorithm);
        String fieldsHeader = String.format("header=\"%s\"", fields);

        String authorizationHeader = String.format("Signature %s,%s,%s", signatureHeader, algorithmHeader, fieldsHeader);

        byte[] base64AuthHeaderBytes = authorizationHeader.getBytes();
        String base64AuthHeader = Base64.encodeToString(base64AuthHeaderBytes, Base64.NO_WRAP);

        return base64AuthHeader;
    }

    private static String buildSignature(List<String> fieldsList, Map<String, String> parameters) {
        StringBuilder signatureBuilder = new StringBuilder();
        for (String fieldName : fieldsList) {
            signatureBuilder.append(parameters.get(fieldName));
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
            if (e instanceof UtilNetworking.UntrustedCAException) {
                sendErrorRequest();
                responseData.skipPackage = true;
                return responseData;
            }
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

    private static void sendErrorRequest() {
        ActivityPackage activityPackage = UtilNetworking.errorPackage;
        String targetURL = Constants.BASE_URL + activityPackage.getPath();

        try {
            HttpsURLConnection connection = UtilNetworking.createPOSTHttpsURLConnection(
                    targetURL,
                    null,
                    activityPackage.getParameters(),
                    0,
                    false);

            UtilNetworking.readHttpResponse(connection, activityPackage);
        } catch (Exception e) {}
    }

    private static TrustManager[] getTrustManager() {
        TrustManager[] trustManager = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
            }

            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
                boolean foundTrustedCertificate = false;

                String trustedThumbprints[] = {
                        // DigiCert High Assurance EV Root CA
                        "5FB7EE0633E259DBAD0C4C9AE6D38F1A61C7DC25",
                        // DigiCert SHA2 Extended Validation Server CA
                        "7E2F3A4F8FE8FA8A5730AECA029696637E986F3F"
                };

                for (X509Certificate certificate : chain) {
                    try {
                        MessageDigest md = MessageDigest.getInstance("SHA1");
                        byte[] publicKey = md.digest(certificate.getEncoded());
                        String hexString = Util.byte2HexFormatted(publicKey);

                        for (String thumbprint : trustedThumbprints) {
                            if (hexString.equalsIgnoreCase(thumbprint)) {
                                foundTrustedCertificate = true;

                                break;
                            }
                        }
                    } catch (NoSuchAlgorithmException ex) {}

                    if (foundTrustedCertificate) {
                        break;
                    }
                }

                if (!foundTrustedCertificate) {
                    throw new UntrustedCAException();
                }
            }
        }};

        return trustManager;
    }

    private static class UntrustedCAException extends CertificateException {
    }

    private static void setAdjustTrustManager(HttpsURLConnection connection) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, getTrustManager(), new java.security.SecureRandom());

            connection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (Exception e) {}
    }

}
