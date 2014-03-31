package net.sitecore.android.mediauploader;

import android.content.res.Resources;
import android.text.TextUtils;

import javax.inject.Singleton;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import com.squareup.picasso.Picasso;

import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.ui.MainActivity;
import net.sitecore.android.mediauploader.ui.browser.BrowserActivity;
import net.sitecore.android.mediauploader.ui.browser.BrowserFragment.BrowserItemViewBinder;
import net.sitecore.android.mediauploader.ui.settings.ChooseMediaFolderActivity;
import net.sitecore.android.mediauploader.ui.settings.CreateEditInstanceActivity;
import net.sitecore.android.mediauploader.ui.settings.SettingsActivity;
import net.sitecore.android.mediauploader.ui.upload.UploadActivity;
import net.sitecore.android.mediauploader.util.UploaderPrefs;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.ScApiSessionFactory;
import net.sitecore.android.sdk.api.ScPublicKey;
import net.sitecore.android.sdk.api.ScRequestQueue;

import dagger.Module;
import dagger.Provides;

import static net.sitecore.android.sdk.api.internal.LogUtils.LOGE;

@Module(
        library = true,
        injects = {
                MainActivity.class,
                BrowserActivity.class,
                BrowserItemViewBinder.class,
                CreateEditInstanceActivity.class,
                UploadActivity.class,
                ChooseMediaFolderActivity.class,
                SettingsActivity.class
        }
)
public final class UploaderAppModule {

    private final UploaderApp mApp;

    public UploaderAppModule(UploaderApp app) {
        mApp = app;
    }

    @Provides @Singleton Picasso providePicasso() {
        return new Picasso.Builder(mApp)
                .build();
    }

    @Provides @Singleton ScRequestQueue provideRequestQueue() {
        return new ScRequestQueue(mApp.getContentResolver());
    }

    @Provides @Singleton UploaderPrefs providePrefs() {
        return UploaderPrefs.from(mApp);
    }

    @Provides @Singleton Resources provideResources() {
        return mApp.getResources();
    }

    // not @Singleton - may change after settings are changed.
    @Provides ScApiSession provideApiSession(UploaderPrefs prefs) {
        String keyValue = prefs.getPublicKey();
        if (TextUtils.isEmpty(keyValue)) {
            return null;
        }

        try {
            ScPublicKey key = new ScPublicKey(keyValue);
            Instance instance = prefs.getCurrentInstance();

            String url = instance.getUrl();
            String login = instance.getLogin();
            String password = instance.getPassword();

            return ScApiSessionFactory.newSession(url, key, login, password);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            LOGE(e);
            return null;
        }
    }

}
