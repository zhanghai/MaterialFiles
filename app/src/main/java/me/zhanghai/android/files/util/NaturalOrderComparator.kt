/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import java.util.Comparator

object NaturalOrderComparator : Comparator<String> {
    private const val CHAR_DOT = '.'
    private const val DIGIT_RADIX = 10

    override fun compare(string1: String, string2: String): Int {
        var start1 = 0
        var start2 = 0
        var leadingZeroCompareResult = 0
        while (start1 < string1.length && start2 < string2.length) {
            val codePoint1 = string1.codePointAt(start1)
            val codePoint2 = string2.codePointAt(start2)
            if (!Character.isDigit(codePoint1) || !Character.isDigit(codePoint2)) {
                if (!codePointEqualsIgnoreCase(codePoint1, codePoint2)) {
                    return when {
                        codePoint1 == CHAR_DOT.toInt() -> -1
                        codePoint2 == CHAR_DOT.toInt() -> 1
                        else -> codePointCompareToIgnoreCase(codePoint1, codePoint2)
                    }
                }
                start1 = string1.offsetByCodePoints(start1, 1)
                start2 = string2.offsetByCodePoints(start2, 1)
                continue
            }
            var end1 = start1
            do {
                end1 = string1.offsetByCodePoints(end1, 1)
            } while (end1 < string1.length && Character.isDigit(string1.codePointAt(end1)))
            var end2 = start2
            do {
                end2 = string2.offsetByCodePoints(end2, 1)
            } while (end2 < string2.length && Character.isDigit(string2.codePointAt(end2)))
            var noLeadingZeroStart1 = start1
            while (noLeadingZeroStart1 < end1
                && Character.digit(string1.codePointAt(noLeadingZeroStart1), DIGIT_RADIX) == 0) {
                noLeadingZeroStart1 = string1.offsetByCodePoints(noLeadingZeroStart1, 1)
            }
            var noLeadingZeroStart2 = start2
            while (noLeadingZeroStart2 < end2
                && Character.digit(string2.codePointAt(noLeadingZeroStart2), DIGIT_RADIX) == 0) {
                noLeadingZeroStart2 = string2.offsetByCodePoints(noLeadingZeroStart2, 1)
            }
            val noLeadingZeroLength1 = string1.codePointCount(noLeadingZeroStart1, end1)
            val noLeadingZeroLength2 = string2.codePointCount(noLeadingZeroStart2, end2)
            if (noLeadingZeroLength1 != noLeadingZeroLength2) {
                return noLeadingZeroLength1 - noLeadingZeroLength2
            }
            var digitIndex1 = noLeadingZeroStart1
            var digitIndex2 = noLeadingZeroStart2
            while (digitIndex1 < end1) {
                val digit1 = Character.digit(string1.codePointAt(digitIndex1), DIGIT_RADIX)
                val digit2 = Character.digit(string2.codePointAt(digitIndex2), DIGIT_RADIX)
                if (digit1 != digit2) {
                    return digit1 - digit2
                }
                digitIndex1 = string1.offsetByCodePoints(digitIndex1, 1)
                digitIndex2 = string2.offsetByCodePoints(digitIndex2, 1)
            }
            val leadingZeroLength1 = string1.codePointCount(start1, noLeadingZeroStart1)
            val leadingZeroLength2 = string2.codePointCount(start2, noLeadingZeroStart2)
            if (leadingZeroLength1 != leadingZeroLength2) {
                if (leadingZeroCompareResult == 0) {
                    leadingZeroCompareResult = leadingZeroLength1 - leadingZeroLength2
                }
            }
            start1 = end1
            start2 = end2
        }
        val remainingLength1 = string1.codePointCount(start1, string1.length)
        val remainingLength2 = string2.codePointCount(start2, string2.length)
        if (remainingLength1 != remainingLength2) {
            return remainingLength1 - remainingLength2
        }
        if (leadingZeroCompareResult != 0) {
            return leadingZeroCompareResult
        }
        return string1.compareTo(string2)
    }

    // @see String#regionMatches(boolean, int, String, int, int)
    private fun codePointEqualsIgnoreCase(codePoint1: Int, codePoint2: Int): Boolean {
        var codePoint1 = codePoint1
        var codePoint2 = codePoint2
        codePoint1 = Character.toUpperCase(codePoint1)
        codePoint2 = Character.toUpperCase(codePoint2)
        if (codePoint1 == codePoint2) {
            return true
        }
        codePoint1 = Character.toLowerCase(codePoint1)
        codePoint2 = Character.toLowerCase(codePoint2)
        return codePoint1 == codePoint2
    }

    // @see String.CaseInsensitiveComparator#compare(String, String)
    private fun codePointCompareToIgnoreCase(codePoint1: Int, codePoint2: Int): Int {
        var codePoint1 = codePoint1
        var codePoint2 = codePoint2
        if (codePoint1 != codePoint2) {
            codePoint1 = Character.toUpperCase(codePoint1)
            codePoint2 = Character.toUpperCase(codePoint2)
            if (codePoint1 != codePoint2) {
                codePoint1 = Character.toUpperCase(codePoint1)
                codePoint2 = Character.toUpperCase(codePoint2)
                if (codePoint1 != codePoint2) {
                    return codePoint1 - codePoint2
                }
            }
        }
        return 0
    }
}
