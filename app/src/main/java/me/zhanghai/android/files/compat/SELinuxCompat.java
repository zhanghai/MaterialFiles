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
import me.zhanghai.java.reflected.ReflectedClass;
import me.zhanghai.java.reflected.ReflectedMethod;

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
    private static final ReflectedMethod<?> sIsSELinuxEnabledMethod = new ReflectedMethod<>(
            sSELinuxClass, "isSELinuxEnabled");

    @NonNull
    private static final ReflectedMethod<?> sIsSELinuxEnforcedMethod = new ReflectedMethod<>(
            sSELinuxClass, "isSELinuxEnforced");

    @NonNull
    private static final ReflectedMethod<?> sSetFSCreateContextMethod = new ReflectedMethod<>(
            sSELinuxClass, "setFSCreateContext", String.class);

    @NonNull
    private static final ReflectedMethod<?> sSetFileContextMethod = new ReflectedMethod<>(
            sSELinuxClass, "setFileContext", String.class, String.class);

    @NonNull
    private static final ReflectedMethod<?> sGetFileContextStringMethod = new ReflectedMethod<>(
            sSELinuxClass, "getFileContext", String.class);

    @NonNull
    private static final ReflectedMethod<?> sGetPeerContextMethod = new ReflectedMethod<>(
            sSELinuxClass, "getPeerContext", FileDescriptor.class);

    @NonNull
    private static final ReflectedMethod<?> sGetFileContextFileDescriptorMethod =
            new ReflectedMethod<>(sSELinuxClass, "getFileContext", FileDescriptor.class);

    @NonNull
    private static final ReflectedMethod<?> sGetContextMethod = new ReflectedMethod<>(sSELinuxClass,
            "getContext");

    @NonNull
    private static final ReflectedMethod<?> sGetPidContextMethod = new ReflectedMethod<>(
            sSELinuxClass, "getPidContext", int.class);

    @NonNull
    private static final ReflectedMethod<?> sCheckSELinuxAccessMethod = new ReflectedMethod<>(
            sSELinuxClass, "checkSELinuxAccess", String.class, String.class, String.class,
            String.class);

    @NonNull
    private static final ReflectedMethod<?> sNativeRestoreconMethod = new ReflectedMethod<>(
            sSELinuxClass, "native_restorecon", String.class, int.class);

    @NonNull
    private static final ReflectedMethod<?> sRestoreconStringMethod = new ReflectedMethod<>(
            sSELinuxClass, "restorecon", String.class);

    @NonNull
    private static final ReflectedMethod<?> sRestoreconFileMethod = new ReflectedMethod<>(
            sSELinuxClass, "restorecon", File.class);

    @NonNull
    private static final ReflectedMethod<?> sRestoreconRecursiveMethod = new ReflectedMethod<>(
            sSELinuxClass, "restoreconRecursive", File.class);

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
