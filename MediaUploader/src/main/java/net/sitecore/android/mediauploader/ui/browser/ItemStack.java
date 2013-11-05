package net.sitecore.android.mediauploader.ui.browser;

import android.text.TextUtils;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

class ItemStack {

    private static final String MEDIA_LIBRARY_ID = "{3D6658D8-A0BF-4E75-B3E2-D050FABCF4E1}";
    private LinkedHashMap<String, String> map;

    ItemStack() {
        map = new LinkedHashMap<String, String>();
    }

    private void parse(String longID, String path) {
        String[] ids = longID.split("/");
        String[] names = path.split("/");
        for (int i = 0; i < ids.length; i++) {
            if (TextUtils.isEmpty(ids[i])) continue;
            map.put(ids[i], names[i]);
        }
    }

    public void init(String longID, String path) {
        parse(longID, path);
    }

    public void push(String parentId, String itemName) {
        map.put(parentId, itemName);
    }

    public String removeLastParent() {
        return map.remove(getLastKey());
    }

    private String getLastKey() {
        if (map.isEmpty()) return null;
        return new LinkedList<String>(map.keySet()).getLast();
    }

    public String getCurrentParentId() {
        return getLastKey();
    }

    public String getCurrentPath() {
        StringBuilder stack = new StringBuilder();

        Set<String> reverseIterator = map.keySet();
        for (String key : reverseIterator) {
            stack.append(map.get(key)).append("/");
        }
        return stack.toString();
    }

    public boolean canGoUp() {
        return !getLastKey().equals(MEDIA_LIBRARY_ID);
    }

    public boolean contains(String id) {
        return map.containsKey(id);
    }
}
