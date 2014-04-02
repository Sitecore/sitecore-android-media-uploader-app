package net.sitecore.android.mediauploader;

import android.app.Application;
import android.content.AsyncQueryHandler;
import android.content.Context;

import com.android.volley.VolleyLog;

import com.squareup.picasso.Picasso;

import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.util.Prefs;
import net.sitecore.android.mediauploader.util.UploaderPrefs;
import net.sitecore.android.sdk.api.internal.LogUtils;
import net.sitecore.android.sdk.api.provider.ScItemsContract.Items;

import butterknife.ButterKnife;
import dagger.ObjectGraph;

public class UploaderApp extends Application {

    private Picasso mImageLoader;
    private Prefs mPrefs;

    private ObjectGraph mObjectGraph;

    public static UploaderApp from(Context context) {
        return (UploaderApp) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPrefs = Prefs.from(this);
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

    @Deprecated
    public Picasso getImageLoader() {
        return mImageLoader;
    }

    public void cleanInstanceCacheAsync() {
        new AsyncQueryHandler(getContentResolver()) {
        }.startDelete(0, null, Items.CONTENT_URI, null, null);
    }

    /*
        public void updateInstancePublicKeyAsync() {
            String url = UploaderPrefs.from(this).getCurrentInstance().url;
            Listener<ScPublicKey> onSuccess = new Listener<ScPublicKey>() {
                @Override
                public void onResponse(ScPublicKey key) {
                    UploaderPrefs.from(getApplicationContext()).saveKeyToPrefs(key);
                }
            };
            ErrorListener onError = new EmptyErrorListener();

            Request request = ScApiSessionFactory.buildPublicKeyRequest(url, onSuccess, onError);
            new ScRequestQueue(getContentResolver()).add(request);
        }
    */
    public void switchInstance(Instance newInstance) {
        UploaderPrefs.from(this).setSelectedInstance(newInstance);
        UploaderApp.from(this).cleanInstanceCacheAsync();
    }
}
