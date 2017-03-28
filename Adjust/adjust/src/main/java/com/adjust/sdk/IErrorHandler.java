package com.adjust.sdk;

/**
 * Created by uerceg on 28/03/2017.
 */

public interface IErrorHandler {
    void init(boolean startsSending);
    void pauseSending();
    void resumeSending();
    void sendErrorPackage(ActivityPackage errorPackage);
    void teardown();
}
