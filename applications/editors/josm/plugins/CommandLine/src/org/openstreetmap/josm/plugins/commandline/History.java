// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.commandline;

import java.util.LinkedList;

public class History {
    private final LinkedList<String> historyList;
    private final int maxLen;
    private int num;

    public History(int len) {
        num = 0;
        maxLen = len;
        historyList = new LinkedList<>();
    }

    public void addItem(String item) {
        if (!item.equals("")) {
            String prevItem = historyList.peekFirst();
            if (prevItem == null) {
                historyList.addFirst(item);
            } else {
                if (!prevItem.equalsIgnoreCase(item))
                    historyList.addFirst(item);
            }
            if (historyList.size() > maxLen) {
                historyList.removeLast();
            }
        }
        num = -1;
    }

    public String getPrevItem() {
        num += 1;
        if (num >= historyList.size()) {
            num = historyList.size() - 1;
        }
        if (num < 0) {
            num = -1;
            return "";
        }
        return historyList.get(num);
    }

    public String getLastItem() {
        if (historyList.size() > 0)
            return historyList.get(0);
        return "";
    }

    public String getNextItem() {
        num -= 1;
        if (num < 0) {
            num = -1;
            return "";
        }
        return historyList.get(num);
    }
}

