package net.sitecore.android.mediauploader.ui;

import android.app.Activity;
import android.os.Bundle;

import net.sitecore.android.mediauploader.UploaderApp;

import butterknife.ButterKnife;

public class InjectingActivity extends Activity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inject dependencies
        UploaderApp.from(this).inject(this);
    }

    @Override public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        // Inject views
        ButterKnife.inject(this);
    }
}
