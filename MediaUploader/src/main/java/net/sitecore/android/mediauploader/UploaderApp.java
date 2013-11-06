package net.sitecore.android.mediauploader;

import android.app.Application;
import android.content.Context;

import com.squareup.picasso.Picasso;

import net.sitecore.android.sdk.api.LogUtils;
import net.sitecore.android.sdk.api.ScApiSession;

import butterknife.Views;

public class UploaderApp extends Application {

    private Picasso mImageLoader;
    private ScApiSession mSession;

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

    public ScApiSession getSession() {
        return mSession;
    }

    public void setSession(ScApiSession session) {
        mSession = session;
    }
}
