package net.sitecore.android.mediauploader.util;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {

    private static final String PREFS_NAME = "media_uploader.preferences";

    private final SharedPreferences mPreferences;
    private final Context mContext;

    private Prefs(Context context) {
        mPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mContext = context;
    }

    public static Prefs from(Context context) {
        return new Prefs(context);
    }

    public String getString(int keyResourceId) {
        return getString(keyResourceId, "");
    }

    public String getString(int keyResourceId, String defaultText) {
        final String key = mContext.getString(keyResourceId);
        return mPreferences.getString(key, defaultText);
    }

    public boolean getBool(int keyResourceId, boolean defaultValue) {
        final String key = mContext.getString(keyResourceId);
        return mPreferences.getBoolean(key, defaultValue);
    }

    public int getInt(int keyResourceId, int defaultValue) {
        final String key = mContext.getString(keyResourceId);
        return mPreferences.getInt(key, defaultValue);
    }

    public void put(int stringResourceId, String value) {
        String key = mContext.getString(stringResourceId);
        mPreferences.edit().putString(key, value).commit();
    }

    public void put(int stringResourceId, int value) {
        String key = mContext.getString(stringResourceId);
        mPreferences.edit().putInt(key, value).commit();
    }

    public void put(int stringResourceId, boolean value) {
        String key = mContext.getString(stringResourceId);
        mPreferences.edit().putBoolean(key, value).commit();
    }
}
