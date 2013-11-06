package net.sitecore.android.mediauploader;

import android.app.Application;
import android.content.Context;

import com.squareup.picasso.Picasso;

import net.sitecore.android.sdk.api.LogUtils;

import butterknife.Views;

public class UploaderApp extends Application {

    private Picasso mImageLoader;

    public static UploaderApp from(Context context) {
        return (UploaderApp) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        LogUtils.setLogEnabled(true);
        Views.setDebug(true);
    }

    public Picasso getImageLoader() {
        if (mImageLoader == null) {
            mImageLoader = Picasso.with(this);
            mImageLoader.setDebugging(true);
        }
        return mImageLoader;
    }
}
