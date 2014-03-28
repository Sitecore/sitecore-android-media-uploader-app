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

    public void setDefaultInstance(Instance instance, int instanceID) {
        mPrefs.put(R.string.key_instance_url, instance.getUrl());
        mPrefs.put(R.string.key_instance_login, instance.getLogin());
        mPrefs.put(R.string.key_instance_password, instance.getPassword());
        mPrefs.put(R.string.key_instance_root_folder, instance.getRootFolder());
        mPrefs.put(R.string.key_instance_id, instanceID);
    }

    public void saveKeyToPrefs(ScPublicKey key) {
        String keyValue = key.getRawValue();
        mPrefs.put(R.string.key_public_key_value, keyValue);
    }

    public int getCurrentInstanceID() {
        return mPrefs.getInt(R.string.key_instance_id, -1);
    }

    public Instance getCurrentInstance() {
        String url = mPrefs.getString(R.string.key_instance_url);
        String login = mPrefs.getString(R.string.key_instance_login);
        String password = mPrefs.getString(R.string.key_instance_password);
        String rootFolder = mPrefs.getString(R.string.key_instance_root_folder);
        String database = mPrefs.getString(R.string.key_instance_root_folder);
        return new Instance(url, login, password, rootFolder, database);
    }

    public boolean isDefaultInstance(String instanceName) {
        return instanceName.equals(mPrefs.getString(R.string.key_instance_name));
    }
}
