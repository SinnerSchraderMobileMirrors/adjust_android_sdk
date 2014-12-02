//
//  Util.java
//  Adjust
//
//  Created by Christian Wellenbrock on 2012-10-11.
//  Copyright (c) 2012-2014 adjust GmbH. All rights reserved.
//  See the file MIT-LICENSE for copying permission.
//

package com.adjust.sdk;

import static com.adjust.sdk.Constants.ENCODING;
import static com.adjust.sdk.Constants.HIGH;
import static com.adjust.sdk.Constants.LARGE;
import static com.adjust.sdk.Constants.LONG;
import static com.adjust.sdk.Constants.LOW;
import static com.adjust.sdk.Constants.MD5;
import static com.adjust.sdk.Constants.MEDIUM;
import static com.adjust.sdk.Constants.NORMAL;
import static com.adjust.sdk.Constants.PLUGINS;
import static com.adjust.sdk.Constants.SHA1;
import static com.adjust.sdk.Constants.SMALL;
import static com.adjust.sdk.Constants.UNKNOWN;
import static com.adjust.sdk.Constants.XLARGE;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.adjust.sdk.plugin.Plugin;

/**
 * Collects utility functions used by Adjust.
 */
public class Util {

    private static SimpleDateFormat dateFormat;
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'Z";

    protected static UserAgent getUserAgent(final Context context) {
        final Resources resources = context.getResources();
        final DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        final Configuration configuration = resources.getConfiguration();
        final Locale locale = configuration.locale;
        final int screenLayout = configuration.screenLayout;

        UserAgent userAgent = new UserAgent();
        userAgent.packageName = getPackageName(context);
        userAgent.appVersion = getAppVersion(context);
        userAgent.deviceType = getDeviceType(screenLayout);
        userAgent.deviceName = getDeviceName();
        userAgent.osName = getOsName();
        userAgent.osVersion = getOsVersion();
        userAgent.language = getLanguage(locale);
        userAgent.country = getCountry(locale);
        userAgent.screenSize = getScreenSize(screenLayout);
        userAgent.screenFormat = getScreenFormat(screenLayout);
        userAgent.screenDensity = getScreenDensity(displayMetrics);
        userAgent.displayWidth = getDisplayWidth(displayMetrics);
        userAgent.displayHeight = getDisplayHeight(displayMetrics);

        return userAgent;
    }

    private static String getPackageName(final Context context) {
        final String packageName = context.getPackageName();
        return sanitizeString(packageName);
    }

    private static String getAppVersion(final Context context) {
        try {
            final PackageManager packageManager = context.getPackageManager();
            final String name = context.getPackageName();
            final PackageInfo info = packageManager.getPackageInfo(name, 0);
            final String versionName = info.versionName;
            return sanitizeString(versionName);
        } catch (NameNotFoundException e) {
            return UNKNOWN;
        }
    }

    private static String getDeviceType(final int screenLayout) {
        int screenSize = screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return "phone";
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
            case 4:
                return "tablet";
            default:
                return UNKNOWN;
        }
    }

    private static String getDeviceName() {
        final String deviceName = Build.MODEL;
        return sanitizeString(deviceName);
    }

    private static String getOsName() {
        return "android";
    }

    private static String getOsVersion() {
        final String osVersion = "" + Build.VERSION.SDK_INT;
        return sanitizeString(osVersion);
    }

    private static String getLanguage(final Locale locale) {
        final String language = locale.getLanguage();
        return sanitizeStringShort(language);
    }

    private static String getCountry(final Locale locale) {
        final String country = locale.getCountry();
        return sanitizeStringShort(country);
    }

    private static String getScreenSize(final int screenLayout) {
        final int screenSize = screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return SMALL;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return NORMAL;
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                return LARGE;
            case 4:
                return XLARGE;
            default:
                return UNKNOWN;
        }
    }

    private static String getScreenFormat(final int screenLayout) {
        final int screenFormat = screenLayout & Configuration.SCREENLAYOUT_LONG_MASK;

        switch (screenFormat) {
            case Configuration.SCREENLAYOUT_LONG_YES:
                return LONG;
            case Configuration.SCREENLAYOUT_LONG_NO:
                return NORMAL;
            default:
                return UNKNOWN;
        }
    }

    private static String getScreenDensity(final DisplayMetrics displayMetrics) {
        final int density = displayMetrics.densityDpi;
        final int low = (DisplayMetrics.DENSITY_MEDIUM + DisplayMetrics.DENSITY_LOW) / 2;
        final int high = (DisplayMetrics.DENSITY_MEDIUM + DisplayMetrics.DENSITY_HIGH) / 2;

        if (0 == density) {
            return UNKNOWN;
        } else if (density < low) {
            return LOW;
        } else if (density > high) {
            return HIGH;
        }
        return MEDIUM;
    }

    private static String getDisplayWidth(DisplayMetrics displayMetrics) {
        final String displayWidth = String.valueOf(displayMetrics.widthPixels);
        return sanitizeString(displayWidth);
    }

    private static String getDisplayHeight(DisplayMetrics displayMetrics) {
        final String displayHeight = String.valueOf(displayMetrics.heightPixels);
        return sanitizeString(displayHeight);
    }

    protected static String createUuid() {
        return UUID.randomUUID().toString();
    }

    // removes spaces and replaces empty string with "unknown"
    private static String sanitizeString(final String string) {
        return sanitizeString(string, UNKNOWN);
    }

    private static String sanitizeStringShort(final String string) {
        return sanitizeString(string, "zz");
    }

    private static String sanitizeString(final String string, final String defaultString) {
        String result = string;
        if (TextUtils.isEmpty(result)) {
            result = defaultString;
        }

        result = result.replaceAll("\\s", "");
        if (TextUtils.isEmpty(result)) {
            result = defaultString;
        }

        return result;
    }

    protected static String getAttributionId(final Context context) {
        try {
            final ContentResolver contentResolver = context.getContentResolver();
            final Uri uri = Uri.parse("content://com.facebook.katana.provider.AttributionIdProvider");
            final String columnName = "aid";
            final String[] projection = {columnName};
            final Cursor cursor = contentResolver.query(uri, projection, null, null, null);

            if (null == cursor) {
                return null;
            }
            if (!cursor.moveToFirst()) {
                cursor.close();
                return null;
            }

            final String attributionId = cursor.getString(cursor.getColumnIndex(columnName));
            cursor.close();
            return attributionId;
        } catch (Exception e) {
            return null;
        }
    }

    public static String quote(String string) {
        if (string == null) {
            return null;
        }

        Pattern pattern = Pattern.compile("\\s");
        Matcher matcher = pattern.matcher(string);
        if (!matcher.find()) {
            return string;
        }

        return String.format("'%s'", string);
    }

    public static String dateFormat(long date) {
        if (null == dateFormat) {
            dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        }
        return dateFormat.format(date);
    }

    public static String getPlayAdId(Context context) {
        return Reflection.getPlayAdId(context);
    }

    public static Boolean isPlayTrackingEnabled(Context context) {
        return Reflection.isPlayTrackingEnabled(context);
    }

    public static boolean isGooglePlayServicesAvailable(Context context) {
        return Reflection.isGooglePlayServicesAvailable(context);
    }

    public static String getMacAddress(Context context) {
        return Reflection.getMacAddress(context);
    }

    public static String getMacSha1(String macAddress) {
        if (macAddress == null) {
            return null;
        }
        String macSha1 = sha1(macAddress);

        return macSha1;
    }

    public static String getMacShortMd5(String macAddress) {
        if (macAddress == null) {
            return null;
        }
        String macShort = macAddress.replaceAll(":", "");
        String macShortMd5 = md5(macShort);

        return macShortMd5;
    }

    public static String getAndroidId(Context context) {
        return Reflection.getAndroidId(context);
    }

    private static String sha1(final String text) {
        return hash(text, SHA1);
    }

    private static String md5(final String text) {
        return hash(text, MD5);
    }

    private static String hash(final String text, final String method) {
        try {
            final byte[] bytes = text.getBytes(ENCODING);
            final MessageDigest mesd = MessageDigest.getInstance(method);
            mesd.update(bytes, 0, bytes.length);
            final byte[] hash = mesd.digest();
            return convertToHex(hash);
        } catch (Exception e) {
            return null;
        }
    }

    private static String convertToHex(final byte[] bytes) {
        final BigInteger bigInt = new BigInteger(1, bytes);
        final String formatString = "%0" + (bytes.length << 1) + "x";
        return String.format(formatString, bigInt);
    }

    public static Map<String, String> getPluginKeys(Context context) {
        Map<String, String> pluginKeys = new HashMap<String, String>();

        for (Plugin plugin : getPlugins()) {
            Map.Entry<String, String> pluginEntry = plugin.getParameter(context);
            if (pluginEntry != null) {
                pluginKeys.put(pluginEntry.getKey(), pluginEntry.getValue());
            }
        }

        if (pluginKeys.size() == 0) {
            return null;
        } else {
            return pluginKeys;
        }
    }

    private static List<Plugin> getPlugins() {
        List<Plugin> plugins = new ArrayList<Plugin>(PLUGINS.size());

        for (String pluginName : PLUGINS) {
            Object pluginObject = Reflection.createDefaultInstance(pluginName);
            if (pluginObject != null && pluginObject instanceof Plugin) {
                plugins.add((Plugin) pluginObject);
            }
        }

        return plugins;
    }

    public static <T> T readObject(Context context, String filename, String objectName) {
        Logger logger = AdjustFactory.getLogger();
        try {
            FileInputStream inputStream = context.openFileInput(filename);
            BufferedInputStream bufferedStream = new BufferedInputStream(inputStream);
            ObjectInputStream objectStream = new ObjectInputStream(bufferedStream);

            try {
                T t = (T) objectStream.readObject();
                logger.debug("Read %s: %s uuid:%s", objectName, t);
                return t;
            } catch (ClassNotFoundException e) {
                logger.error("Failed to find activity state class");
            } catch (OptionalDataException e) {
                /* no-op */
            } catch (IOException e) {
                logger.error("Failed to read %s object", objectName);
            } catch (ClassCastException e) {
                logger.error("Failed to cast %s object", objectName);
            } finally {
                objectStream.close();
            }

        } catch (FileNotFoundException e) {
            logger.verbose("%s file not found", objectName);
        } catch (Exception e) {
            logger.error("Failed to open %s file for reading (%s)", objectName, e);
        }

        return null;
    }

    public static <T> void writeObject(T object, Context context, String filename, String objectName) {
        Logger logger = AdjustFactory.getLogger();
        try {
            FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            BufferedOutputStream bufferedStream = new BufferedOutputStream(outputStream);
            ObjectOutputStream objectStream = new ObjectOutputStream(bufferedStream);

            try {
                objectStream.writeObject(object);
                logger.debug("Wrote %s: %s", objectName, object);
            } catch (NotSerializableException e) {
                logger.error("Failed to serialize %s", objectName);
            } finally {
                objectStream.close();
            }

        } catch (Exception e) {
            logger.error("Failed to open %s for writing (%s)", objectName, e);
        }
    }

    public static String parseResponse(HttpResponse httpResponse, Logger logger) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            httpResponse.getEntity().writeTo(out);
            out.close();
            String response =  out.toString().trim();
            logger.verbose("Response: %s", response);
            return response;
        } catch (Exception e) {
            logger.error("Failed to parse response (%s)", e);
            return null;
        }
    }

    public static JSONObject buildJsonObject(String jsonString) {
        if (jsonString == null) return null;

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return jsonObject;
        } catch (JSONException e){
            Logger logger = AdjustFactory.getLogger();
            logger.error("Failed to parse json response: %s (%s)", jsonString, e.getMessage());
        }

        return null;
    }


}
