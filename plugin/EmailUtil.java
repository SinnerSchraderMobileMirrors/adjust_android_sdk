package com.adjust.sdk.plugin;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Patterns;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustFactory;
import com.adjust.sdk.Logger;
import com.adjust.sdk.Util;

import java.util.AbstractMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by pfms on 17/09/14.
 */
public class EmailUtil implements Extension {
    @Override
    public Map.Entry<String, String> getParameter(Context context) {
        if (context == null) {
            return null;
        }

        Logger logger = AdjustFactory.getLogger();

        if (!(context.checkCallingOrSelfPermission(Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED)) {
            logger.error("Permission needed to get email: GET_ACCOUNTS");
        }

        Pattern emailPattern = Patterns.EMAIL_ADDRESS;
        Account[] accounts = AccountManager.get(context).getAccounts();
        String sha1_email = null;
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                String email = account.name;
                String salt = getSalt(context);
                sha1_email = Util.sha1(email + salt);
                break;
            }
        }
        if (sha1_email == null) {
            return null;
        }
        MapEntry<String, String> mapEntry = new MapEntry<String, String>("email_macAddress", sha1_email);
        return mapEntry;
    }

    private String getSalt(Context context) {
        String defaultSalt = "";
        Logger logger = AdjustFactory.getLogger();
        Bundle bundle = Util.getApplicationBundle(context, logger);
        if (null == bundle) {
            return defaultSalt;
        }
        String readSalt = bundle.getString("AdjustSalt");

        if (null == readSalt) {
            return defaultSalt;
        } else {
            return readSalt;
        }
    }
}
