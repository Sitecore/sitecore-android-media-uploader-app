package net.sitecore.android.mediauploader;

import android.app.Application;
import android.content.Context;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import com.android.volley.VolleyLog;

import com.squareup.picasso.Picasso;

import net.sitecore.android.mediauploader.util.Prefs;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.ScApiSessionFactory;
import net.sitecore.android.sdk.api.ScPublicKey;
import net.sitecore.android.sdk.api.internal.LogUtils;

import butterknife.ButterKnife;
import dagger.ObjectGraph;

import static net.sitecore.android.sdk.api.internal.LogUtils.LOGE;

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

    @Deprecated
    public ScApiSession getSession() {
        try {
            String keyValue = mPrefs.getString(R.string.key_public_key_value);
            ScPublicKey key = new ScPublicKey(keyValue);

            String url = mPrefs.getString(R.string.key_instance_url);
            String login = mPrefs.getString(R.string.key_instance_login);
            String password = mPrefs.getString(R.string.key_instance_password);
            return ScApiSessionFactory.newSession(url, key, login, password);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            LOGE(e);
            throw new RuntimeException(e);
        }
    }

    /*
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
        new ScRequestQueue(getContentResolver()).add(request);
    }

    public void switchInstance(Instance newInstance) {
        UploaderPrefs.from(this).setSelectedInstance(newInstance);
        UploaderApp.from(this).updateInstancePublicKeyAsync();
        UploaderApp.from(this).cleanInstanceCacheAsync();
    }*/
}
