package net.sitecore.android.mediauploader.ui.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import net.sitecore.android.mediauploader.R;

public class CheckableRelativeLayout extends LinearLayout implements Checkable {
    private RadioButton radioButton;

    public CheckableRelativeLayout(Context context) {
        super(context);
    }

    public CheckableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckableRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        radioButton = (RadioButton) findViewById(R.id.radio_button_selected_instance);
    }

    @Override
    public void setChecked(boolean checked) {
        radioButton.setChecked(checked);
    }

    @Override
    public boolean isChecked() {
        return radioButton.isChecked();
    }

    @Override
    public void toggle() {
        radioButton.setChecked(!radioButton.isChecked());
    }
}
