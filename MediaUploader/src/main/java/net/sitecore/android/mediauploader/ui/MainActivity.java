package net.sitecore.android.mediauploader.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import net.sitecore.android.mediauploader.R;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
