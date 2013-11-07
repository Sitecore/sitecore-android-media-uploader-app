package net.sitecore.android.mediauploader.ui.browser;

import java.util.Iterator;
import java.util.LinkedList;

public class ItemStack {

    private LinkedList<String> mItemIds;
    private LinkedList<String> mItemNames;
    private LinkedList<String> mItemPaths;

    public ItemStack() {
        mItemIds = new LinkedList<String>();
        mItemNames = new LinkedList<String>();
        mItemPaths = new LinkedList<String>();
    }

    public void clear() {
        mItemIds.clear();
        mItemNames.clear();
        mItemPaths.clear();
    }

    public String getCurrentPath() {
        StringBuilder builder = new StringBuilder();

        Iterator<String> iterator = mItemNames.descendingIterator();
        while (iterator.hasNext()) {
            builder.append("/").append(iterator.next());
        }

        return builder.toString();
    }

    public boolean canGoUp() {
        return mItemIds.size() > 1;
    }

    public String getCurrentItemId() {
        return mItemIds.peek();
    }

    public String getCurrentFullPath() {
        return mItemPaths.peek();
    }

    public void goUp() {
        mItemIds.pop();
        mItemNames.pop();
        mItemPaths.pop();
    }

    public void goInside(String id, String name, String path) {
        mItemIds.push(id);
        mItemNames.push(name);
        mItemPaths.push(path);
    }

}
