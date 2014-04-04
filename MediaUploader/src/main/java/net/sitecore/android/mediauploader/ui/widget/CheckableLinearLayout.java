package net.sitecore.android.mediauploader.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.LinearLayout;

public class CheckableLinearLayout extends LinearLayout implements Checkable {
    private Checkable mCheckable;

    public CheckableLinearLayout(Context context) {
        super(context);
    }

    public CheckableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckableLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        for (int i = 0; i < getChildCount(); i++) {
            final View v = getChildAt(i);
            if (v instanceof Checkable) {
                mCheckable = (Checkable) v;
                break;
            }
        }
    }

    @Override
    public void setChecked(boolean checked) {
        mCheckable.setChecked(checked);
    }

    @Override
    public boolean isChecked() {
        return mCheckable.isChecked();
    }

    @Override
    public void toggle() {
        mCheckable.setChecked(!mCheckable.isChecked());
    }
}
