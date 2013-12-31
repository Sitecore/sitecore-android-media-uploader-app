package net.sitecore.android.mediauploader;

import android.app.Application;
import android.content.AsyncQueryHandler;
import android.content.Context;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyLog;

import com.crashlytics.android.Crashlytics;
import com.squareup.picasso.Picasso;

import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.util.EmptyErrorListener;
import net.sitecore.android.mediauploader.util.Prefs;
import net.sitecore.android.mediauploader.util.UploaderPrefs;
import net.sitecore.android.sdk.api.LogUtils;
import net.sitecore.android.sdk.api.RequestQueueProvider;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.ScApiSessionFactory;
import net.sitecore.android.sdk.api.ScPublicKey;
import net.sitecore.android.sdk.api.provider.ScItemsContract.Items;

import butterknife.ButterKnife;

import static net.sitecore.android.sdk.api.LogUtils.LOGE;

public class UploaderApp extends Application {

    private Picasso mImageLoader;
    private Prefs mPrefs;

    public static UploaderApp from(Context context) {
        return (UploaderApp) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPrefs = Prefs.from(this);
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
        try {
            String keyValue = mPrefs.getString(R.string.key_public_key_value);
            ScPublicKey key = new ScPublicKey(keyValue);

            String url = mPrefs.getString(R.string.key_instance_url);
            String login = mPrefs.getString(R.string.key_instance_login);
            String password = mPrefs.getString(R.string.key_instance_password);
            return ScApiSessionFactory.newSession(url, key, login, password);
        } catch (InvalidKeySpecException e) {
            LOGE(e);
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            LOGE(e);
            throw new RuntimeException(e);
        }
    }

    public void cleanInstanceCacheAsync() {
        new AsyncQueryHandler(getContentResolver()) {
        }.startDelete(0, null, Items.CONTENT_URI, null, null);
    }

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
        RequestQueueProvider.getRequestQueue(this).add(request);
    }

    public void switchInstance(Instance newInstance) {
        UploaderPrefs.from(this).setDefaultInstance(newInstance);
        UploaderApp.from(this).updateInstancePublicKeyAsync();
        UploaderApp.from(this).cleanInstanceCacheAsync();
    }
}
