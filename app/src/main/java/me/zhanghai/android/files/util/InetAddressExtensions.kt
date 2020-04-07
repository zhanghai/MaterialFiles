/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import me.zhanghai.android.files.app.wifiManager
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.net.UnknownHostException
import kotlin.reflect.KClass

fun KClass<InetAddress>.getLocalAddress(): InetAddress? {
    val wifiInfo = wifiManager.connectionInfo
    if (wifiInfo != null) {
        // WifiStateMachine doesn't support IPv6 as of P, so no need to get the original
        // InetAddress object with reflection.
        val address = wifiInfo.ipAddress
        if (address != 0) {
            return intToInetAddress(address)
        }
    }
    try {
        for (networkInterface in NetworkInterface.getNetworkInterfaces()) {
            if (!networkInterface.isUp || networkInterface.isLoopback) {
                continue
            }
            for (inetAddress in networkInterface.inetAddresses) {
                // Works for consumer IPv4 addresses.
                if (!inetAddress.isSiteLocalAddress) {
                    continue
                }
                return inetAddress
            }
        }
    } catch (e: SocketException) {
        e.printStackTrace()
    }
    return null
}

/*
 * @see android.net.NetworkUtils#intToInetAddress(int)
 */
private fun intToInetAddress(hostAddress: Int): InetAddress {
    val addressBytes = byteArrayOf(
        (0xff and hostAddress).toByte(),
        (0xff and (hostAddress shr 8)).toByte(),
        (0xff and (hostAddress shr 16)).toByte(),
        (0xff and (hostAddress shr 24)).toByte()
    )
    return try {
        InetAddress.getByAddress(addressBytes)
    } catch (e: UnknownHostException) {
        throw AssertionError()
    }
}
