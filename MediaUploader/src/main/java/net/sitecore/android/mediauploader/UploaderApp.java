package net.sitecore.android.mediauploader;

import android.app.Application;
import android.content.Context;

import com.android.volley.VolleyLog;

import com.squareup.picasso.Picasso;

import net.sitecore.android.mediauploader.provider.ItemsAsyncHandler;
import net.sitecore.android.sdk.api.internal.LogUtils;

import butterknife.ButterKnife;
import dagger.ObjectGraph;

public class UploaderApp extends Application {

    private Picasso mImageLoader;

    private ObjectGraph mObjectGraph;

    public static UploaderApp from(Context context) {
        return (UploaderApp) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mImageLoader = Picasso.with(this);

        if (!BuildConfig.DEBUG) {
            //TODO: add crashlytics
            // Crashlytics.start(this);
        }

        setUpLogging(BuildConfig.DEBUG);

        mObjectGraph = ObjectGraph.create(new UploaderAppModule(this));
    }

    private void setUpLogging(boolean isEnabled) {
        mImageLoader.setDebugging(isEnabled);
        LogUtils.setLogEnabled(isEnabled);
        VolleyLog.DEBUG = isEnabled;
        ButterKnife.setDebug(isEnabled);
    }

    public void inject(Object o) {
        mObjectGraph.inject(o);
    }

    public void cleanInstanceCacheAsync() {
        new ItemsAsyncHandler(getContentResolver()).deleteItemsBrowserCache();
    }

}
