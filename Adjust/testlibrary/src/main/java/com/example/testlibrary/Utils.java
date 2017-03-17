package com.example.testlibrary;

import android.util.Log;

import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static com.example.testlibrary.Constants.LOGTAG;

/**
 * Created by nonelse on 11.03.17.
 */

public class Utils {
    static ConnectionOptions connectionOptions;
    static TrustManager[] trustAllCerts;
    static HostnameVerifier hostnameVerifier;

    static Gson gson;
    static Type stringStringMap;

    static {
        trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        debug("getAcceptedIssuers");

                        return null;
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                        debug("checkClientTrusted");
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                        debug("checkServerTrusted");
                    }
                }
        };
        hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        connectionOptions = new ConnectionOptions();
        gson = new Gson();
        stringStringMap = new TypeToken<Map<String, String>>(){}.getType();
    }

    static class ConnectionOptions implements Util.IConnectionOptions {
        public String clientSdk;

        @Override
        public void applyConnectionOptions(HttpsURLConnection connection) {
            if (this.clientSdk != null) {
                connection.setRequestProperty("Client-SDK", clientSdk);
            }
            connection.setConnectTimeout(com.adjust.sdk.Constants.ONE_MINUTE);
            connection.setReadTimeout(com.adjust.sdk.Constants.ONE_MINUTE);
            try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                connection.setSSLSocketFactory(sc.getSocketFactory());

                connection.setHostnameVerifier(hostnameVerifier);
                debug("applyConnectionOptions");
            } catch (Exception e) {
                debug("applyConnectionOptions %s", e.getMessage());
            }
        }
    }
    static Util.HttpResponse sendPostI(String path) {
        return sendPostI(path, null);
    }
    static Util.HttpResponse sendPostI(String path, String clientSdk) {
        String targetURL = AdjustFactory.getBaseUrl() + path;
        debug("targetURL: %s", targetURL);

        try {
            if (clientSdk != null) {
                connectionOptions.clientSdk = clientSdk;
            }
            HttpsURLConnection connection = Util.createPOSTHttpsURLConnection(
                    targetURL, null, connectionOptions);
            Util.HttpResponse httpResponse = Util.readHttpResponse(connection);
            debug("Response: %s", httpResponse.response);

            httpResponse.headerFields= connection.getHeaderFields();
            debug("Headers: %s", httpResponse.headerFields);

            return httpResponse;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static void debug(String message, Object... parameters) {
        try {
            Log.d(LOGTAG, String.format(Locale.US, message, parameters));
        } catch (Exception e) {
            Log.e(LOGTAG, String.format(Locale.US, "Error formating log message: %s, with params: %s"
                    , message, Arrays.toString(parameters)));
        }
    }
}
