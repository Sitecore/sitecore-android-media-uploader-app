package net.sitecore.android.mediauploader;

import javax.inject.Singleton;

import com.squareup.picasso.Picasso;

import net.sitecore.android.mediauploader.ui.MainActivity;
import net.sitecore.android.sdk.api.ScApiSession;
import net.sitecore.android.sdk.api.ScRequestQueue;

import dagger.Module;
import dagger.Provides;

@Module(
        library = true,
        injects = MainActivity.class
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

    @Provides @Singleton ScApiSession provideApiSession() {
        return null;
    }

}
