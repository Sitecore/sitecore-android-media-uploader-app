package net.sitecore.android.mediauploader.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import net.sitecore.android.mediauploader.R;

public class ScFragment extends Fragment {

    private Animation mAnimationFadeOut;
    private Animation mAnimationFadeIn;

    private ViewGroup mProgressContainer;
    private ViewGroup mContentContainer;
    private ViewGroup mEmptyContainer;

    private boolean mContentShown = false;
    private boolean mIsEmpty = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final FrameLayout v = (FrameLayout) inflater.inflate(R.layout.fragment_sitecore, container, false);

        mContentContainer = (ViewGroup) v.findViewById(R.id.container_content);
        mProgressContainer = (ViewGroup) v.findViewById(R.id.container_progress);
        mEmptyContainer = (ViewGroup) v.findViewById(R.id.container_empty);

        mContentContainer.addView(onCreateContentView(inflater));
        mProgressContainer.addView(onCreateProgressView(inflater));
        mEmptyContainer.addView(onCreateEmptyView(inflater));

        return v;
    }

    @Override
    public void onDestroyView() {
        mProgressContainer = mContentContainer = mEmptyContainer = null;
        mContentShown = false;
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAnimationFadeOut = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
        mAnimationFadeIn = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
    }

    protected View onCreateContentView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.fragment_sitecore_list, null);
    }

    protected View onCreateProgressView(LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.fragment_sitecore_progress, null);
        return v;
    }

    protected View onCreateEmptyView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.fragment_sitecore_empty, null);
    }

    public void setContentShown(boolean shown) {
        if (mContentShown == shown) {
            return;
        }
        mContentShown = shown;
        if (shown) {
            mProgressContainer.startAnimation(mAnimationFadeOut);
            mContentContainer.startAnimation(mAnimationFadeIn);

            mProgressContainer.setVisibility(View.GONE);
            mContentContainer.setVisibility(View.VISIBLE);
            mEmptyContainer.setVisibility(View.GONE);
        } else {
            mProgressContainer.startAnimation(mAnimationFadeIn);
            mContentContainer.startAnimation(mAnimationFadeOut);

            mProgressContainer.setVisibility(View.VISIBLE);
            mContentContainer.setVisibility(View.GONE);
            mEmptyContainer.setVisibility(View.GONE);
        }
    }

    public void setEmpty(boolean isEmpty) {
        if (mIsEmpty == isEmpty) return;

        mIsEmpty = isEmpty;
        if (mIsEmpty) {
            mProgressContainer.startAnimation(mAnimationFadeOut);
            mEmptyContainer.startAnimation(mAnimationFadeIn);

            mProgressContainer.setVisibility(View.GONE);
            mEmptyContainer.setVisibility(View.VISIBLE);
        } else {
            mProgressContainer.startAnimation(mAnimationFadeOut);
            mEmptyContainer.startAnimation(mAnimationFadeIn);

            mProgressContainer.setVisibility(View.GONE);
            mEmptyContainer.setVisibility(View.VISIBLE);
        }

    }

    public void setContentState() {
    }

    public void setLoadingState() {
    }

    public void setEmptyState() {
    }

}
