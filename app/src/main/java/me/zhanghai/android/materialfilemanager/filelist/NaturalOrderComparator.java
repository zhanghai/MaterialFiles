/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import java.util.Comparator;

public class NaturalOrderComparator implements Comparator<String> {

    private static final int DIGIT_RADIX = 10;

    @Override
    public int compare(String string1, String string2) {
        int start1 = 0;
        int start2 = 0;
        int leadingZeroCompareResult = 0;
        while (start1 < string1.length() && start2 < string2.length()) {
            char char1 = string1.charAt(start1);
            char char2 = string2.charAt(start2);
            if (characterEqualsIgnoreCase(char1, char2)) {
                ++start1;
                ++start2;
                continue;
            }
            if (!Character.isDigit(char1) || !Character.isDigit(char2)) {
                return characterCompareToIgnoreCase(char1, char2);
            }
            int end1 = start1 + 1;
            while (end1 < string1.length() && Character.isDigit(string1.charAt(end1))) {
                ++end1;
            }
            int end2 = start2 + 1;
            while (end2 < string2.length() && Character.isDigit(string2.charAt(end2))) {
                ++end2;
            }
            int noLeadingZeroStart1 = start1;
            while (noLeadingZeroStart1 < end1
                    && Character.digit(string1.charAt(noLeadingZeroStart1), DIGIT_RADIX) == 0) {
                ++noLeadingZeroStart1;
            }
            int noLeadingZeroStart2 = start2;
            while (noLeadingZeroStart2 < end2
                    && Character.digit(string2.charAt(noLeadingZeroStart2), DIGIT_RADIX) == 0) {
                ++noLeadingZeroStart2;
            }
            int noLeadingZeroLength1 = end1 - noLeadingZeroStart1;
            int noLeadingZeroLength2 = end2 - noLeadingZeroStart2;
            if (noLeadingZeroLength1 != noLeadingZeroLength2) {
                return noLeadingZeroLength1 - noLeadingZeroLength2;
            }
            for (int i = 0; i < noLeadingZeroLength1; ++i) {
                int digit1 = Character.digit(string1.charAt(noLeadingZeroStart1 + i), DIGIT_RADIX);
                int digit2 = Character.digit(string2.charAt(noLeadingZeroStart2 + i), DIGIT_RADIX);
                if (digit1 != digit2) {
                    return digit1 - digit2;
                }
            }
            int leadingZeroLength1 = noLeadingZeroStart1 - start1;
            int leadingZeroLength2 = noLeadingZeroStart2 - start2;
            if (leadingZeroLength1 != leadingZeroLength2) {
                if (leadingZeroCompareResult == 0) {
                    leadingZeroCompareResult = leadingZeroLength1 - leadingZeroLength2;
                }
            }
            start1 = end1;
            start2 = end2;
        }
        int remainingLength1 = string1.length() - start1;
        int remainingLength2 = string2.length() - start2;
        if (remainingLength1 != remainingLength2) {
            return remainingLength1 - remainingLength2;
        }
        if (leadingZeroCompareResult != 0) {
            return leadingZeroCompareResult;
        }
        if (string1.length() != string2.length()) {
            return string1.length() - string2.length();
        }
        return string1.compareTo(string2);
    }

    // @see String#regionMatches(boolean, int, String, int, int)
    private static boolean characterEqualsIgnoreCase(char char1, char char2) {
        char upperCaseChar1 = Character.toUpperCase(char1);
        char upperCaseChar2 = Character.toUpperCase(char2);
        return upperCaseChar1 == upperCaseChar2
                || Character.toLowerCase(upperCaseChar1) == Character.toLowerCase(upperCaseChar2);
    }

    // @see String.CaseInsensitiveComparator#compare(String, String)
    private static int characterCompareToIgnoreCase(char char1, char char2) {
        if (char1 != char2) {
            char1 = Character.toUpperCase(char1);
            char2 = Character.toUpperCase(char2);
            if (char1 != char2) {
                char1 = Character.toUpperCase(char1);
                char2 = Character.toUpperCase(char2);
                if (char1 != char2) {
                    return char1 - char2;
                }
            }
        }
        return 0;
    }
}
