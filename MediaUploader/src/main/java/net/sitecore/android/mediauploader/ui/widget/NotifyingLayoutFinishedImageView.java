package net.sitecore.android.mediauploader.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;

public class NotifyingLayoutFinishedImageView extends ImageView {

    public interface OnLayoutFinishedListener {
        public void onLayoutFinished();
    }

    private OnLayoutFinishedListener mOnLayoutFinishedListener;

    public NotifyingLayoutFinishedImageView(Context context) {
        super(context);
    }

    public NotifyingLayoutFinishedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NotifyingLayoutFinishedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();
        getViewTreeObserver().addOnGlobalLayoutListener(VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN
                ? new V16OnGlobalLayoutListener()
                : new LegacyOnGlobalLayoutListener());
    }

    /**
     * This adds ability to get real ImageView width and height.
     */
    public void setOnLayoutFinishedListener(OnLayoutFinishedListener onLayoutFinishedListener) {
        mOnLayoutFinishedListener = onLayoutFinishedListener;
    }

    @TargetApi(VERSION_CODES.JELLY_BEAN)
    private class V16OnGlobalLayoutListener implements OnGlobalLayoutListener {
        @Override public void onGlobalLayout() {
            getViewTreeObserver().removeOnGlobalLayoutListener(this);
            if (mOnLayoutFinishedListener != null) mOnLayoutFinishedListener.onLayoutFinished();
        }
    }

    private class LegacyOnGlobalLayoutListener implements OnGlobalLayoutListener {
        @Override public void onGlobalLayout() {
            getViewTreeObserver().removeGlobalOnLayoutListener(this);
            if (mOnLayoutFinishedListener != null) mOnLayoutFinishedListener.onLayoutFinished();
        }
    }

}
