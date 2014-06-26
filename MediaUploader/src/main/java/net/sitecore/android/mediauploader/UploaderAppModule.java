package net.sitecore.android.mediauploader;

import android.content.res.Resources;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import javax.inject.Singleton;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import net.sitecore.android.mediauploader.model.Instance;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances;
import net.sitecore.android.mediauploader.provider.UploadMediaContract.Instances.Query;
import net.sitecore.android.mediauploader.ui.MainActivity;
import net.sitecore.android.mediauploader.ui.browser.BrowserActivity;
import net.sitecore.android.mediauploader.ui.browser.BrowserItemViewBinder;
import net.sitecore.android.mediauploader.ui.browser.PreviewActivity;
import net.sitecore.android.mediauploader.ui.location.LocationActivity;
import net.sitecore.android.mediauploader.ui.settings.CreateEditInstanceActivity;
import net.sitecore.android.mediauploader.ui.settings.MediaFolderSelectionActivity;
import net.sitecore.android.mediauploader.ui.settings.SettingsActivity;
import net.sitecore.android.mediauploader.service.MediaUploadResponseHandler;
import net.sitecore.android.mediauploader.ui.upload.UploadActivity;
import net.sitecore.android.mediauploader.ui.upload.UploadsListFragment;
import net.sitecore.android.mediauploader.service.UploadHelper;
import net.sitecore.android.mediauploader.util.Prefs;
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
                UploadsListFragment.class,
                MediaFolderSelectionActivity.class,
                SettingsActivity.class,
                PreviewActivity.class,
                UploadHelper.class,
                LocationActivity.class,
                MediaUploadResponseHandler.class
        }
)
public final class UploaderAppModule {

    private final UploaderApp mApp;

    public UploaderAppModule(UploaderApp app) {
        mApp = app;
    }

    @Provides @Singleton Picasso providePicasso() {
        OkHttpDownloader downloader = new OkHttpDownloader(mApp, 10*1024*1024);
        return new Picasso.Builder(mApp)
                .downloader(downloader)
                .build();
    }

    @Provides @Singleton Prefs providePrefs() {
        return Prefs.from(mApp);
    }

    @Provides @Singleton ScRequestQueue provideRequestQueue() {
        return new ScRequestQueue(mApp.getContentResolver());
    }

    @Provides @Singleton Resources provideResources() {
        return mApp.getResources();
    }

    @Provides Instance provideCurrentInstance() {
        Cursor c = mApp.getContentResolver().query(Instances.CONTENT_URI, Query.PROJECTION, Instances.SELECTED + "=1",
                null, null);
        Instance currentInstance;
        if (c.moveToFirst()) {
            currentInstance = Instance.fromInstanceCursor(c);
        } else {
            currentInstance = null;
        }
        c.close();
        return currentInstance;
    }

    // not @Singleton - may change after settings are changed.
    @Provides ScApiSession provideApiSession(@Nullable Instance instance) {
        try {
            if (instance == null) {
                return null;
            }

            String url = instance.getUrl();
            String login = instance.getLogin();
            String password = instance.getPassword();
            ScPublicKey key = new ScPublicKey(instance.getPublicKey());

            ScApiSession session = ScApiSessionFactory.newSession(url, key, login, password);
            if (!TextUtils.isEmpty(instance.getSite())) session.setDefaultSite(instance.getSite());
            session.setDefaultDatabase(instance.getDatabase());

            return session;
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            LOGE(e);
            return null;
        }
    }

}
