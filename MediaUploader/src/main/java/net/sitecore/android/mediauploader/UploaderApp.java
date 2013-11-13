package net.sitecore.android.mediauploader;

import android.app.Application;
import android.content.AsyncQueryHandler;
import android.content.Context;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyLog;

import com.crashlytics.android.Crashlytics;
import com.squareup.picasso.Picasso;

import net.sitecore.android.mediauploader.util.Prefs;
import net.sitecore.android.sdk.api.LogUtils;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.provider.ScItemsContract.Items;

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
        Crashlytics.start(this);
        mImageLoader = Picasso.with(this);
        setUpLogging(BuildConfig.DEBUG);
    }

    public Picasso getImageLoader() {
        return mImageLoader;
    }

    private void setUpLogging(boolean isEnabled) {
        mImageLoader.setDebugging(isEnabled);
        LogUtils.setLogEnabled(isEnabled);
        VolleyLog.DEBUG = isEnabled;
        Views.setDebug(isEnabled);
    }

    public void getSession(final Listener<ScApiSession> sessionListener, ErrorListener errorListener) {
        if (mSession == null) {
            Prefs prefs = Prefs.from(this);

            String url = prefs.getString(R.string.key_instance_url);
            String username = prefs.getString(R.string.key_instance_login);
            String password = prefs.getString(R.string.key_instance_password);

            Listener<ScApiSession> onSuccess = new Listener<ScApiSession>() {
                @Override
                public void onResponse(ScApiSession scApiSession) {
                    mSession = scApiSession;
                    mSession.setShouldCache(true);
                    sessionListener.onResponse(scApiSession);
                }
            };

            ScApiSession.getSession(this, url, username, password, onSuccess, errorListener);
        } else {
            sessionListener.onResponse(mSession);
        }
    }

    public void cleanInstanceCache() {
        mSession = null;
        new AsyncQueryHandler(getContentResolver()) {
        }.startDelete(0, null, Items.CONTENT_URI, null, null);
    }

    public void setSession(ScApiSession session) {
        mSession = session;
    }
}
