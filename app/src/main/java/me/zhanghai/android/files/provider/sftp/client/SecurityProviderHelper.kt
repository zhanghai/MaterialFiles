/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.sftp.client

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

// @see https://android-developers.googleblog.com/2018/03/cryptography-changes-in-android-p.html
// @see net.schmizz.sshj.common.SecurityUtils
// @see net.schmizz.sshj.DefaultConfig.DefaultConfig
// SSHJ requires BouncyCastle to be registered before enabling most functionality by default, so we
// better keep BouncyCastle registered.
object SecurityProviderHelper {
    fun init() {
        val bouncyCastleProvider = BouncyCastleProvider()
        Security.removeProvider(bouncyCastleProvider.name)
        Security.addProvider(bouncyCastleProvider)
    }
}
