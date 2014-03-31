package net.sitecore.android.mediauploader.util;

import android.content.Context;

import net.sitecore.android.mediauploader.R;
import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.sdk.api.ScPublicKey;

public class UploaderPrefs {
    private final Prefs mPrefs;

    private UploaderPrefs(Context context) {
        mPrefs = Prefs.from(context);
    }

    public static UploaderPrefs from(Context context) {
        return new UploaderPrefs(context);
    }

    public void setSelectedInstance(Instance instance) {
        mPrefs.put(R.string.key_instance_url, instance.getUrl());
        mPrefs.put(R.string.key_instance_login, instance.getLogin());
        mPrefs.put(R.string.key_instance_password, instance.getPassword());
        mPrefs.put(R.string.key_instance_root_folder, instance.getRootFolder());
        mPrefs.put(R.string.key_instance_database, instance.getDatabase());
    }

    public void savePublicKey(ScPublicKey key) {
        String keyValue = key.getRawValue();
        mPrefs.put(R.string.key_public_key_value, keyValue);
    }

    public String getPublicKey() {
        return mPrefs.getString(R.string.key_instance_id);
    }

    public Instance getCurrentInstance() {
        Instance instance = new Instance();
        instance.setUrl(mPrefs.getString(R.string.key_instance_url));
        instance.setLogin(mPrefs.getString(R.string.key_instance_login));
        instance.setPassword(mPrefs.getString(R.string.key_instance_password));
        instance.setRootFolder(mPrefs.getString(R.string.key_instance_root_folder));
        instance.setDatabase(mPrefs.getString(R.string.key_instance_root_folder));
        instance.setSelected(true);
        return instance;
    }
}
