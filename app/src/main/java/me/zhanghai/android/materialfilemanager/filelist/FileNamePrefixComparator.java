/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import java.util.Comparator;

public class FileNamePrefixComparator implements Comparator<String> {

    // We sort file names with these prefixes last.
    private static final String[] PREFIXES = { ".", "#" };

    @Override
    public int compare(String fileName1, String fileName2) {
        boolean hasPrefix1 = false;
        for (String prefix : PREFIXES) {
            if (fileName1.startsWith(prefix)) {
                hasPrefix1 = true;
                break;
            }
        }
        boolean hasPrefix2 = false;
        for (String prefix : PREFIXES) {
            if (fileName2.startsWith(prefix)) {
                hasPrefix2 = true;
                break;
            }
        }
        if (!hasPrefix1 && hasPrefix2) {
            return -1;
        } else if (hasPrefix1 && !hasPrefix2) {
            return 1;
        } else {
            return 0;
        }
    }
}
