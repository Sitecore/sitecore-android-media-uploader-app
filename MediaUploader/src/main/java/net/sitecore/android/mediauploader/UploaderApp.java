package net.sitecore.android.mediauploader;

import android.app.Application;
import android.content.Context;

import com.android.volley.toolbox.ImageLoader;

import net.sitecore.android.mediauploader.util.BitmapLruCache;
import net.sitecore.android.sdk.api.LogUtils;
import net.sitecore.android.sdk.api.RequestQueueProvider;

import butterknife.Views;

public class UploaderApp extends Application {

    private ImageLoader mImageLoader;

    public static UploaderApp from(Context context) {
        return (UploaderApp) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        LogUtils.setLogEnabled(true);
        Views.setDebug(true);
    }

    public ImageLoader getImageLoader() {
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(RequestQueueProvider.getRequestQueue(this), new BitmapLruCache());
        }
        return mImageLoader;
    }
}
