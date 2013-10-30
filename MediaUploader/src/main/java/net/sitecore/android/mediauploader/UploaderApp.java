package net.sitecore.android.mediauploader;

import android.app.Application;

import net.sitecore.android.sdk.api.LogUtils;

import butterknife.Views;

public class UploaderApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        LogUtils.setLogEnabled(true);
        Views.setDebug(true);
    }
}
