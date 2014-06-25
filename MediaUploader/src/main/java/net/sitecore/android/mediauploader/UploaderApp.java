package net.sitecore.android.mediauploader;

import android.app.Application;
import android.content.Context;

import com.android.volley.VolleyLog;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import net.sitecore.android.mediauploader.provider.ItemsAsyncHandler;
import net.sitecore.android.sdk.api.internal.LogUtils;

import dagger.ObjectGraph;

public class UploaderApp extends Application {

    private Picasso mImageLoader;

    private ObjectGraph mObjectGraph;
    private Tracker mTracker;

    public static UploaderApp from(Context context) {
        return (UploaderApp) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mImageLoader = Picasso.with(this);

        if (!BuildConfig.DEBUG) {
            Crashlytics.start(this);
            initAnalytics();
        }

        setUpLogging(BuildConfig.DEBUG);

        mObjectGraph = ObjectGraph.create(new UploaderAppModule(this));
    }

    private void initAnalytics() {
        final GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        mTracker = analytics.newTracker(R.xml.media_uploader_tracker);
        mTracker.enableAutoActivityTracking(true);
        analytics.enableAutoActivityReports(this);
    }

    public void trackEvent(int actionResId) {
        trackEvent(actionResId, "");
    }

    public void trackEvent(int actionResId, String message) {
        if (mTracker == null) {
            initAnalytics();
        }
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("event")
                .setAction(getString(actionResId))
                .setLabel(message)
                .build());
    }

    private void setUpLogging(boolean isEnabled) {
        mImageLoader.setIndicatorsEnabled(isEnabled);
        LogUtils.setLogEnabled(isEnabled);
        //VolleyLog.DEBUG = isEnabled;
    }

    public void inject(Object o) {
        mObjectGraph.inject(o);
    }

    public void cleanInstanceCacheAsync() {
        new ItemsAsyncHandler(getContentResolver()).deleteItemsBrowserCache();
    }

}
