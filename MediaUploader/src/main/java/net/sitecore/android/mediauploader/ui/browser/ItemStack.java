package net.sitecore.android.mediauploader.ui.browser;

import java.util.Iterator;
import java.util.LinkedList;

public class ItemStack {

    private LinkedList<String> mItemIds;
    private LinkedList<String> mItemNames;
    private LinkedList<String> mItemPaths;

    public ItemStack(String id, String name, String path) {
        mItemIds = new LinkedList<String>();
        mItemNames = new LinkedList<String>();
        mItemPaths = new LinkedList<String>();
        goInside(id, name, path);
    }

    public String getCurrentPath() {
        StringBuilder builder = new StringBuilder();

        Iterator<String> iterator = mItemNames.descendingIterator();
        while (iterator.hasNext()) {
            builder.append("/").append(iterator.next());
        }

        return builder.toString();
    }

    boolean canGoUp() {
        return mItemIds.size() > 1;
    }

    String getCurrentItemId() {
        return mItemIds.peek();
    }

    void goUp() {
        mItemIds.pop();
        mItemNames.pop();
        mItemPaths.pop();
    }

    void goInside(String id, String name, String path) {
        mItemIds.push(id);
        mItemNames.push(name);
        mItemPaths.push(path);
    }

}
