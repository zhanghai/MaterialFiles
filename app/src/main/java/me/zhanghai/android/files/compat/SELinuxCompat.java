/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat;

import android.os.Build;

import java.io.File;
import java.io.FileDescriptor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import me.zhanghai.android.files.reflected.ReflectedClass;
import me.zhanghai.android.files.reflected.ReflectedClassMethod;

/*
 * @see android.os.SELinux
 * @see <a href="https://android.googlesource.com/platform/frameworks/base/+/jb-mr1-release/core/java/android/os/SELinux.java">
 *      jb-mr1-release/SELinux.java</a>
 * @see <a href="https://android.googlesource.com/platform/prebuilts/runtime/+/master/appcompat/hiddenapi-light-greylist.txt">
 *      hiddenapi-light-greylist.txt</a>
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class SELinuxCompat {

    static {
        RestrictedHiddenApiAccess.allow();
    }

    @NonNull
    private static final ReflectedClass<?> sSELinuxClass = new ReflectedClass<>(
            "android.os.SELinux");

    @NonNull
    private static final ReflectedClassMethod<?> sIsSELinuxEnabledMethod =
            new ReflectedClassMethod<>(sSELinuxClass, "isSELinuxEnabled");

    @NonNull
    private static final ReflectedClassMethod<?> sIsSELinuxEnforcedMethod =
            new ReflectedClassMethod<>(sSELinuxClass, "isSELinuxEnforced");

    @NonNull
    private static final ReflectedClassMethod<?> sSetFSCreateContextMethod =
            new ReflectedClassMethod<>(sSELinuxClass, "setFSCreateContext", String.class);

    @NonNull
    private static final ReflectedClassMethod<?> sSetFileContextMethod = new ReflectedClassMethod<>(
            sSELinuxClass, "setFileContext", String.class, String.class);

    @NonNull
    private static final ReflectedClassMethod<?> sGetFileContextStringMethod =
            new ReflectedClassMethod<>(sSELinuxClass, "getFileContext", String.class);

    @NonNull
    private static final ReflectedClassMethod<?> sGetPeerContextMethod = new ReflectedClassMethod<>(
            sSELinuxClass, "getPeerContext", FileDescriptor.class);

    @NonNull
    private static final ReflectedClassMethod<?> sGetFileContextFileDescriptorMethod =
            new ReflectedClassMethod<>(sSELinuxClass, "getFileContext", FileDescriptor.class);

    @NonNull
    private static final ReflectedClassMethod<?> sGetContextMethod = new ReflectedClassMethod<>(
            sSELinuxClass, "getContext");

    @NonNull
    private static final ReflectedClassMethod<?> sGetPidContextMethod = new ReflectedClassMethod<>(
            sSELinuxClass, "getPidContext", int.class);

    @NonNull
    private static final ReflectedClassMethod<?> sCheckSELinuxAccessMethod =
            new ReflectedClassMethod<>(sSELinuxClass, "checkSELinuxAccess", String.class,
                    String.class, String.class, String.class);

    @NonNull
    private static final ReflectedClassMethod<?> sNativeRestoreconMethod =
            new ReflectedClassMethod<>(sSELinuxClass, "native_restorecon", String.class, int.class);

    @NonNull
    private static final ReflectedClassMethod<?> sRestoreconStringMethod =
            new ReflectedClassMethod<>(sSELinuxClass, "restorecon", String.class);

    @NonNull
    private static final ReflectedClassMethod<?> sRestoreconFileMethod = new ReflectedClassMethod<>(
            sSELinuxClass, "restorecon", File.class);

    @NonNull
    private static final ReflectedClassMethod<?> sRestoreconRecursiveMethod =
            new ReflectedClassMethod<>(sSELinuxClass, "restoreconRecursive", File.class);

    /*
     * @see android.os.SELinux#isSELinuxEnabled()
     */
    public static boolean isSELinuxEnabled() {
        return sIsSELinuxEnabledMethod.invoke(null);
    }

    /*
     * @see android.os.SELinux#isSELinuxEnforced()
     */
    public static boolean isSELinuxEnforced() {
        return sIsSELinuxEnforcedMethod.invoke(null);
    }

    /*
     * @see android.os.SELinux#setFSCreateContext(String)
     */
    @RestrictedHiddenApi
    public static boolean setFSCreateContext(@Nullable String context) {
        return sSetFSCreateContextMethod.invoke(null, context);
    }

    /*
     * @see android.os.SELinux#setFileContext(String, String)
     */
    @RestrictedHiddenApi
    public static boolean setFileContext(@NonNull String path, @NonNull String context) {
        return sSetFileContextMethod.invoke(null, path, context);
    }

    /*
     * @see android.os.SELinux#getFileContext(String)
     */
    @Nullable
    public static String getFileContext(@NonNull String path) {
        return sGetFileContextStringMethod.invoke(null, path);
    }

    /*
     * @see android.os.SELinux#getPeerContext(FileDescriptor)
     */
    @Nullable
    @RestrictedHiddenApi
    public static String getPeerContext(@NonNull FileDescriptor fd) {
        return sGetPeerContextMethod.invoke(null, fd);
    }

    /*
     * @see android.os.SELinux#getFileContext(FileDescriptor)
     */
    @Nullable
    @RequiresApi(/*Build.VERSION_CODES.Q*/29)
    @RestrictedHiddenApi
    public static String getFileContext(@NonNull FileDescriptor fd) {
        return sGetFileContextFileDescriptorMethod.invoke(null, fd);
    }

    /*
     * @see android.os.SELinux#getContext()
     */
    @Nullable
    public static String getContext() {
        return sGetContextMethod.invoke(null);
    }

    /*
     * @see android.os.SELinux#getPidContext(int)
     */
    @Nullable
    public static String getPidContext(int pid) {
        return sGetPidContextMethod.invoke(null, pid);
    }

    /*
     * @see android.os.SELinux#checkSELinuxAccess(String, String, String, String)
     */
    public static boolean checkSELinuxAccess(@NonNull String scon, @NonNull String tcon,
                                             @NonNull String tclass, @NonNull String perm) {
        return sCheckSELinuxAccessMethod.invoke(null, scon, tcon, tclass, perm);
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @RestrictedHiddenApi
    public static boolean native_restorecon(String pathname, int flags) {
        return sNativeRestoreconMethod.invoke(null, pathname, flags);
    }

    /*
     * @see android.os.SELinux#restorecon(String)
     */
    @RestrictedHiddenApi
    public static boolean restorecon(@NonNull String pathname)/* throws NullPointerException*/ {
        return sRestoreconStringMethod.invoke(null, pathname);
    }

    /*
     * @see android.os.SELinux#restorecon(File)
     */
    @RestrictedHiddenApi
    public static boolean restorecon(@NonNull File file)/* throws NullPointerException*/ {
        return sRestoreconFileMethod.invoke(null, file);
    }

    /*
     * @see android.os.SELinux#restoreconRecursive(File)
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean restoreconRecursive(@NonNull File file) {
        return sRestoreconRecursiveMethod.invoke(null, file);
    }
}
