package net.sitecore.android.mediauploader.ui.browser;

import android.util.Pair;

import java.util.LinkedList;

public class ItemStack2 {

    private LinkedList<Pair<String, String>> mItems;

    public ItemStack2(Pair<String, String> currentItem) {
        mItems = new LinkedList<Pair<String, String>>();
    }

    public String getCurrentPath() {

        return null;
    }

    boolean canGoUp() {
        return mItems.size() > 1;
    }

    Pair<String, String> getCurrentItem() {
        return mItems.peek();
    }

    Pair<String, String> goUp() {
        return mItems.pop();
    }

    void goDown(Pair<String, String> item) {
        mItems.push(item);
    }

}
