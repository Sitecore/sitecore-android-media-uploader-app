package net.sitecore.android.mediauploader;

import android.app.Application;
import android.content.Context;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import com.squareup.picasso.Picasso;

import net.sitecore.android.mediauploader.util.Prefs;
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

    public void getSession(final Listener<ScApiSession> sessionListener, ErrorListener errorListener) {
        if (mSession == null) {
            Prefs prefs = Prefs.from(this);

            String url = prefs.getString(R.string.key_instance_url);
            String username = prefs.getString(R.string.key_instance_login);
            String password = prefs.getString(R.string.key_instance_password);

            ScApiSession.getSession(this, url, username, password, new Listener<ScApiSession>() {
                @Override
                public void onResponse(ScApiSession scApiSession) {
                    mSession = scApiSession;
                    mSession.setShouldCache(true);
                    sessionListener.onResponse(scApiSession);
                }
            }, errorListener);
        } else {
            sessionListener.onResponse(mSession);
        }
    }

    public void setSession(ScApiSession session) {
        mSession = session;
    }
}
