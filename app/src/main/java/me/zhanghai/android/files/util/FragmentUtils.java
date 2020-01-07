/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public class FragmentUtils {

    private FragmentUtils() {}

    @NonNull
    public static BundleBuilder getArgumentsBuilder(@NonNull Fragment fragment) {
        Bundle arguments = fragment.getArguments();
        if (arguments == null) {
            arguments = new Bundle();
            fragment.setArguments(arguments);
        }
        return BundleBuilder.buildUpon(arguments);
    }

    @Deprecated
    @Nullable
    public static <T> T findById(@NonNull FragmentManager fragmentManager, @IdRes int id) {
        //noinspection unchecked
        return (T) fragmentManager.findFragmentById(id);
    }

    @Nullable
    public static <T> T findById(@NonNull FragmentActivity activity, @IdRes int id) {
        //noinspection deprecation
        return findById(activity.getSupportFragmentManager(), id);
    }

    @Nullable
    public static <T> T findById(@NonNull Fragment parentFragment, @IdRes int id) {
        //noinspection deprecation
        return findById(parentFragment.getChildFragmentManager(), id);
    }

    @Deprecated
    @Nullable
    public static <T> T findByTag(@NonNull FragmentManager fragmentManager, @NonNull String tag) {
        //noinspection unchecked
        return (T) fragmentManager.findFragmentByTag(tag);
    }

    @Nullable
    public static <T> T findByTag(@NonNull FragmentActivity activity, @NonNull String tag) {
        //noinspection deprecation
        return findByTag(activity.getSupportFragmentManager(), tag);
    }

    @Nullable
    public static <T> T findByTag(@NonNull Fragment parentFragment, @NonNull String tag) {
        //noinspection deprecation
        return findByTag(parentFragment.getChildFragmentManager(), tag);
    }

    @Deprecated
    public static void add(@NonNull Fragment fragment, @NonNull FragmentManager fragmentManager,
                           @IdRes int containerViewId, @Nullable String tag) {
        fragmentManager.beginTransaction()
                .add(containerViewId, fragment, tag)
                .commit();
    }

    @Deprecated
    public static void add(@NonNull Fragment fragment, @NonNull FragmentManager fragmentManager,
                           @IdRes int containerViewId) {
        //noinspection deprecation
        add(fragment, fragmentManager, containerViewId, null);
    }

    public static void add(@NonNull Fragment fragment, @NonNull FragmentActivity activity,
                           @IdRes int containerViewId) {
        //noinspection deprecation
        add(fragment, activity.getSupportFragmentManager(), containerViewId);
    }

    public static void add(@NonNull Fragment fragment, @NonNull Fragment parentFragment,
                           @IdRes int containerViewId) {
        //noinspection deprecation
        add(fragment, parentFragment.getChildFragmentManager(), containerViewId);
    }

    @Deprecated
    public static void add(@NonNull Fragment fragment, @NonNull FragmentManager fragmentManager,
                           @NonNull String tag) {
        // Pass 0 as in {@link android.support.v4.app.BackStackRecord#add(Fragment, String)}.
        //noinspection deprecation
        add(fragment, fragmentManager, 0, tag);
    }

    public static void add(@NonNull Fragment fragment, @NonNull FragmentActivity activity,
                           @NonNull String tag) {
        //noinspection deprecation
        add(fragment, activity.getSupportFragmentManager(), tag);
    }

    public static void add(@NonNull Fragment fragment, @NonNull Fragment parentFragment,
                           @NonNull String tag) {
        //noinspection deprecation
        add(fragment, parentFragment.getChildFragmentManager(), tag);
    }

    /**
     * @deprecated Always use an id or tag for restoration.
     */
    public static void add(@NonNull Fragment fragment, @NonNull FragmentActivity activity) {
        //noinspection deprecation
        add(fragment, activity.getSupportFragmentManager(), 0, null);
    }

    /**
     * @deprecated Always use an id or tag for restoration.
     */
    public static void add(@NonNull Fragment fragment, @NonNull Fragment parentFragment) {
        //noinspection deprecation
        add(fragment, parentFragment.getChildFragmentManager(), 0, null);
    }

    public static void remove(@NonNull Fragment fragment) {

        if (fragment.isRemoving()) {
            return;
        }

        fragment.getFragmentManager().beginTransaction()
                .remove(fragment)
                .commit();
    }

    @Deprecated
    public static void replace(@NonNull Fragment fragment, @NonNull FragmentManager fragmentManager,
                               @IdRes int containerViewId, @Nullable String tag) {
        fragmentManager.beginTransaction()
                .replace(containerViewId, fragment, tag)
                .commit();
    }

    @Deprecated
    public static void replace(@NonNull Fragment fragment, @NonNull FragmentManager fragmentManager,
                               @IdRes int containerViewId) {
        //noinspection deprecation
        replace(fragment, fragmentManager, containerViewId, null);
    }

    public static void replace(@NonNull Fragment fragment, @NonNull FragmentActivity activity,
                               @IdRes int containerViewId) {
        //noinspection deprecation
        replace(fragment, activity.getSupportFragmentManager(), containerViewId);
    }

    public static void replace(@NonNull Fragment fragment, @NonNull Fragment parentFragment,
                               @IdRes int containerViewId) {
        //noinspection deprecation
        replace(fragment, parentFragment.getChildFragmentManager(), containerViewId);
    }

    @Deprecated
    public static void replace(@NonNull Fragment fragment, @NonNull FragmentManager fragmentManager,
                               @NonNull String tag) {
        // Pass 0 as in {@link android.support.v4.app.BackStackRecord#replace(Fragment, String)}.
        //noinspection deprecation
        replace(fragment, fragmentManager, 0, tag);
    }

    public static void replace(@NonNull Fragment fragment, @NonNull FragmentActivity activity,
                               @NonNull String tag) {
        //noinspection deprecation
        replace(fragment, activity.getSupportFragmentManager(), tag);
    }

    public static void replace(@NonNull Fragment fragment, @NonNull Fragment parentFragment,
                               @NonNull String tag) {
        //noinspection deprecation
        replace(fragment, parentFragment.getChildFragmentManager(), tag);
    }

    public static void replace(@NonNull Fragment fragment, @NonNull FragmentActivity activity) {
        //noinspection deprecation
        replace(fragment, activity.getSupportFragmentManager(), 0, null);
    }

    public static void replace(@NonNull Fragment fragment, @NonNull Fragment parentFragment) {
        //noinspection deprecation
        replace(fragment, parentFragment.getChildFragmentManager(), 0, null);
    }

    public static void executePendingTransactions(@NonNull FragmentActivity activity) {
        activity.getSupportFragmentManager().executePendingTransactions();
    }

    public static void executePendingTransactions(@NonNull Fragment fragment) {
        fragment.getChildFragmentManager().executePendingTransactions();
    }
}
