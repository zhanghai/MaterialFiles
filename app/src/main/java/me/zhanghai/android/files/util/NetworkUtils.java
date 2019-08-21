/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class NetworkUtils {

    private NetworkUtils() {}

    @Nullable
    public static InetAddress getLocalInetAddress(@NonNull Context context) {
        // WifiManagerPotentialLeak
        WifiManager wifiManager = ContextCompat.getSystemService(context.getApplicationContext(),
                WifiManager.class);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            // WifiStateMachine doesn't support IPv6 as of P, so no need to get the original
            // InetAddress object with reflection.
            int addressInt = wifiInfo.getIpAddress();
            if (addressInt != 0) {
                return intToInetAddress(addressInt);
            }
        }
        try {
            for (Enumeration<NetworkInterface> networkInterfaces =
                 NetworkInterface.getNetworkInterfaces(); networkInterfaces.hasMoreElements(); ) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }
                for (Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                     inetAddresses.hasMoreElements(); ) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    // Works for consumer IPv4 addresses.
                    if (!inetAddress.isSiteLocalAddress()) {
                        continue;
                    }
                    return inetAddress;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * @see android.net.NetworkUtils#intToInetAddress(int)
     */
    @NonNull
    private static InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = {
                (byte)(0xff & hostAddress),
                (byte)(0xff & (hostAddress >> 8)),
                (byte)(0xff & (hostAddress >> 16)),
                (byte)(0xff & (hostAddress >> 24))
        };
        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }
}
