/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Build;
import android.os.Build.VERSION;
import android.util.ArrayMap;
import android.util.LongSparseArray;

import java.lang.reflect.Field;
import java.util.Map;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * This class flushes all the entries of ThemedResourceCache, instead of only mUnthemedEntries as in
 * AppCompat.
 *
 * @see androidx.appcompat.app.ResourcesFlusher
 */
public class ResourcesFlusher {

    private ResourcesFlusher() {}

    @Nullable
    private static Field sDrawableCacheField;
    private static boolean sDrawableCacheFieldInitialized;
    @Nullable
    private static Class sThemedResourceCacheClass;
    private static boolean sThemedResourceCacheClassInitialized;
    @Nullable
    private static Field sThemedResourceCacheMThemedEntriesField;
    private static boolean sThemedResourceCacheMThemedEntriesFieldInitialized;
    @Nullable
    private static Field sThemedResourceCacheMUnthemedEntriesField;
    private static boolean sThemedResourceCacheMUnthemedEntriesFieldInitialized;
    @Nullable
    private static Field sThemedResourceCacheMNullThemedEntriesField;
    private static boolean sThemedResourceCacheMNullThemedEntriesFieldInitialized;
    @Nullable
    private static Field sResourcesImplField;
    private static boolean sResourcesImplFieldInitialized;

    @MainThread
    public static void flush(@NonNull Resources resources) {
        if (VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Do nothing.
        } else if (VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            flushNougat(resources);
        } else if (VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flushMarshmallow(resources);
        } else if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            flushLollipop(resources);
        }
    }

    @MainThread
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private static void flushLollipop(@NonNull Resources resources) {

        if (!sDrawableCacheFieldInitialized) {
            try {
                //noinspection JavaReflectionMemberAccess
                sDrawableCacheField = Resources.class.getDeclaredField("mDrawableCache");
                sDrawableCacheField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            sDrawableCacheFieldInitialized = true;
        }
        if (sDrawableCacheField == null) {
            return;
        }

        Map drawableCache = null;
        try {
            drawableCache = (Map)sDrawableCacheField.get(resources);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (drawableCache == null) {
            return;
        }

        drawableCache.clear();
    }

    @MainThread
    @RequiresApi(Build.VERSION_CODES.M)
    private static void flushMarshmallow(@NonNull Resources resources) {

        if (!sDrawableCacheFieldInitialized) {
            try {
                //noinspection JavaReflectionMemberAccess
                sDrawableCacheField = Resources.class.getDeclaredField("mDrawableCache");
                sDrawableCacheField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            sDrawableCacheFieldInitialized = true;
        }
        if (sDrawableCacheField == null) {
            return;
        }

        Object drawableCache = null;
        try {
            drawableCache = sDrawableCacheField.get(resources);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (drawableCache == null) {
            return;
        }

        flushThemedResourceCache(drawableCache);
    }

    @MainThread
    @RequiresApi(Build.VERSION_CODES.N)
    private static void flushNougat(@NonNull Resources resources) {

        if (!sResourcesImplFieldInitialized) {
            try {
                //noinspection JavaReflectionMemberAccess
                sResourcesImplField = Resources.class.getDeclaredField("mResourcesImpl");
                sResourcesImplField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            sResourcesImplFieldInitialized = true;
        }
        if (sResourcesImplField == null) {
            return;
        }

        Object resourcesImpl = null;
        try {
            resourcesImpl = sResourcesImplField.get(resources);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (resourcesImpl == null) {
            return;
        }

        if (!sDrawableCacheFieldInitialized) {
            try {
                sDrawableCacheField = resourcesImpl.getClass().getDeclaredField("mDrawableCache");
                sDrawableCacheField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            sDrawableCacheFieldInitialized = true;
        }
        if (sDrawableCacheField == null) {
            return;
        }

        Object drawableCache = null;
        try {
            drawableCache = sDrawableCacheField.get(resourcesImpl);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (drawableCache == null) {
            return;
        }

        flushThemedResourceCache(drawableCache);
    }

    @MainThread
    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("PrivateApi")
    private static void flushThemedResourceCache(@NonNull Object themedResourceCache) {

        if (!sThemedResourceCacheClassInitialized) {
            try {
                sThemedResourceCacheClass = Class.forName(
                        "android.content.res.ThemedResourceCache");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            sThemedResourceCacheClassInitialized = true;
        }
        if (sThemedResourceCacheClass == null) {
            return;
        }

        if (!sThemedResourceCacheMThemedEntriesFieldInitialized) {
            try {
                sThemedResourceCacheMThemedEntriesField =
                        sThemedResourceCacheClass.getDeclaredField("mThemedEntries");
                sThemedResourceCacheMThemedEntriesField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            sThemedResourceCacheMThemedEntriesFieldInitialized = true;
        }
        if (sThemedResourceCacheMThemedEntriesField != null) {
            ArrayMap themedEntries = null;
            try {
                themedEntries = (ArrayMap) sThemedResourceCacheMThemedEntriesField.get(
                        themedResourceCache);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (themedEntries != null) {
                themedEntries.clear();
            }
        }

        if (!sThemedResourceCacheMUnthemedEntriesFieldInitialized) {
            try {
                sThemedResourceCacheMUnthemedEntriesField =
                        sThemedResourceCacheClass.getDeclaredField("mUnthemedEntries");
                sThemedResourceCacheMUnthemedEntriesField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            sThemedResourceCacheMUnthemedEntriesFieldInitialized = true;
        }
        if (sThemedResourceCacheMUnthemedEntriesField != null) {
            LongSparseArray unthemedEntries = null;
            try {
                unthemedEntries = (LongSparseArray) sThemedResourceCacheMUnthemedEntriesField.get(
                        themedResourceCache);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (unthemedEntries != null) {
                unthemedEntries.clear();
            }
        }

        if (!sThemedResourceCacheMNullThemedEntriesFieldInitialized) {
            try {
                sThemedResourceCacheMNullThemedEntriesField =
                        sThemedResourceCacheClass.getDeclaredField("mNullThemedEntries");
                sThemedResourceCacheMNullThemedEntriesField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            sThemedResourceCacheMNullThemedEntriesFieldInitialized = true;
        }
        if (sThemedResourceCacheMNullThemedEntriesField != null) {
            LongSparseArray nullThemedEntries = null;
            try {
                nullThemedEntries = (LongSparseArray)
                        sThemedResourceCacheMNullThemedEntriesField.get(themedResourceCache);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (nullThemedEntries != null) {
                nullThemedEntries.clear();
            }
        }
    }
}
