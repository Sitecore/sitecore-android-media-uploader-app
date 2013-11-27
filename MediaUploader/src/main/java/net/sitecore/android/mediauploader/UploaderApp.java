package net.sitecore.android.mediauploader;

import android.app.Application;
import android.content.AsyncQueryHandler;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyLog;

import com.crashlytics.android.Crashlytics;
import com.squareup.picasso.Picasso;

import net.sitecore.android.mediauploader.util.EmptyErrorListener;
import net.sitecore.android.mediauploader.util.Prefs;
import net.sitecore.android.mediauploader.util.Utils;
import net.sitecore.android.sdk.api.LogUtils;
import net.sitecore.android.sdk.api.RequestQueueProvider;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.ScApiSessionFactory;
import net.sitecore.android.sdk.api.ScPublicKey;
import net.sitecore.android.sdk.api.provider.ScItemsContract.Items;

import butterknife.ButterKnife;

public class UploaderApp extends Application {

    private Picasso mImageLoader;

    public static UploaderApp from(Context context) {
        return (UploaderApp) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mImageLoader = Picasso.with(this);

        if (!BuildConfig.DEBUG) {
            Crashlytics.start(this);
        }

        setUpLogging(BuildConfig.DEBUG);
    }

    public Picasso getImageLoader() {
        return mImageLoader;
    }

    private void setUpLogging(boolean isEnabled) {
        mImageLoader.setDebugging(isEnabled);
        LogUtils.setLogEnabled(isEnabled);
        VolleyLog.DEBUG = isEnabled;
        ButterKnife.setDebug(isEnabled);
    }

    public ScApiSession getSession() {
        ScPublicKey key = Utils.getPublicKey(this);
        if (key != null) {
            Prefs prefs = Prefs.from(this);
            String url = prefs.getString(R.string.key_instance_url);
            String login = prefs.getString(R.string.key_instance_login);
            String password = prefs.getString(R.string.key_instance_password);
            return ScApiSessionFactory.newSession(url, key, login, password);
        } else {
            return null;
        }
    }

    public void cleanInstanceCache() {
        new AsyncQueryHandler(getContentResolver()) {
        }.startDelete(0, null, Items.CONTENT_URI, null, null);
    }

    public void updateInstancePublicKey() {
        String url = Utils.getCurrentInstance(this).url;
        Listener<ScPublicKey> onSuccess = new Listener<ScPublicKey>() {
            @Override
            public void onResponse(ScPublicKey key) {
                Utils.saveKeyToPrefs(getApplicationContext(), key);
            }
        };
        ErrorListener onError = new EmptyErrorListener();

        Request request = ScApiSessionFactory.buildPublicKeyRequest(url, onSuccess, onError);
        RequestQueueProvider.getRequestQueue(this).add(request);
    }
}
