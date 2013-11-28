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

    public void setDefaultInstance(Instance instance) {
        mPrefs.put(R.string.key_instance_name, instance.name);
        mPrefs.put(R.string.key_instance_url, instance.url);
        mPrefs.put(R.string.key_instance_login, instance.login);
        mPrefs.put(R.string.key_instance_password, instance.password);
        mPrefs.put(R.string.key_instance_root_folder, instance.rootFolder);
    }

    public void saveKeyToPrefs(ScPublicKey key) {
        String keyValue = key.getRawValue();
        mPrefs.put(R.string.key_public_key_value, keyValue);
    }

    public Instance getCurrentInstance() {
        String name = mPrefs.getString(R.string.key_instance_name);
        String url = mPrefs.getString(R.string.key_instance_url);
        String login = mPrefs.getString(R.string.key_instance_login);
        String password = mPrefs.getString(R.string.key_instance_password);
        String rootFolder = mPrefs.getString(R.string.key_instance_root_folder);
        return new Instance(name, url, login, password, rootFolder);
    }

    public boolean isDefaultInstance(String instanceName) {
        return instanceName.equals(mPrefs.getString(R.string.key_instance_name));
    }
}
