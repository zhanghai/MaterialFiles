/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.sftp.client

import android.os.Build
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

// @see https://android-developers.googleblog.com/2018/03/cryptography-changes-in-android-p.html
// @see net.schmizz.sshj.common.SecurityUtils
// @see net.schmizz.sshj.DefaultConfig.DefaultConfig
// SSHJ requires BouncyCastle to be registered before enabling most functionality by default, so we
// better keep BouncyCastle registered.
object SecurityProviderHelper {
    fun init() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            // On older Android versions, JarURLConnectionImpl.getInputStream() throws a
            // "SecurityException: Incorrect signature" when it's called by Apache FTPServer if we
            // replace Bouncy Castle. We are only required to replace Bouncy Castle on P and above
            // anyway, so don't do that before Lollipop MR1.
            return
        }
        val bouncyCastleProvider = BouncyCastleProvider()
        Security.removeProvider(bouncyCastleProvider.name)
        Security.addProvider(bouncyCastleProvider)
    }
}
