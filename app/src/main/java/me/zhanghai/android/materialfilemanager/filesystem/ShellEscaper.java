/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.LookupTranslator;

import me.zhanghai.android.materialfilemanager.util.MapBuilder;

public class ShellEscaper {

    private ShellEscaper() {}

    /**
     * This translator escapes instead of strips newlines.
     * @see StringEscapeUtils#ESCAPE_XSI
     */
    private static final CharSequenceTranslator ESCAPER = new LookupTranslator(
            MapBuilder.<CharSequence, CharSequence>newHashMap()
                    .put("|", "\\|")
                    .put("&", "\\&")
                    .put(";", "\\;")
                    .put("<", "\\<")
                    .put(">", "\\>")
                    .put("(", "\\(")
                    .put(")", "\\)")
                    .put("$", "\\$")
                    .put("`", "\\`")
                    .put("\\", "\\\\")
                    .put("\"", "\\\"")
                    .put("'", "\\'")
                    .put(" ", "\\ ")
                    .put("\t", "\\\t")
                    .put("\n", "$'\\n'")
                    .put("*", "\\*")
                    .put("?", "\\?")
                    .put("[", "\\[")
                    .put("#", "\\#")
                    .put("~", "\\~")
                    .put("=", "\\=")
                    .put("%", "\\%")
                    .buildUnmodifiable()
    );

    public static String escape(String text) {
        return ESCAPER.translate(text);
    }
}
