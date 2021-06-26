/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import android.os.AsyncTask
import jcifs.context.SingletonContext
import jcifs.smb.SmbException
import jcifs.smb.SmbFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.zhanghai.android.files.util.CloseableLiveData
import me.zhanghai.android.files.util.Failure
import me.zhanghai.android.files.util.Loading
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.Success
import me.zhanghai.android.files.util.getLocalAddress
import me.zhanghai.android.files.util.toLinkedSet
import me.zhanghai.android.files.util.valueCompat
import java.net.Inet4Address
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class LanSmbServerListLiveData : CloseableLiveData<Stateful<List<LanSmbServer>>>() {
    private var loadFuture: Future<*>? = null

    init {
        loadValue()
    }

    fun loadValue() {
        cancelLoadingValue()
        value = Loading(value?.value)
        loadFuture = (AsyncTask.THREAD_POOL_EXECUTOR as ExecutorService).submit {
            try {
                val newServerSet = mutableSetOf<LanSmbServer>()
                Executors.newFixedThreadPool(60).asCoroutineDispatcher().use { dispatcher ->
                    runBlocking(dispatcher) {
                        val serverChannel = produce {
                            launch {
                                getServersByComputerBrowserService().consumeEach { send(it) }
                            }
                            launch {
                                getServersByScanningSubnet().consumeEach { send(it) }
                            }
                        }
                        serverChannel.consumeEach {
                            // Use linked set to preserve UI stability.
                            val serverSet = valueCompat.value?.toLinkedSet() ?: linkedSetOf()
                            serverSet += it
                            val servers = serverSet.toList()
                            postValue(Loading(servers))
                            newServerSet += it
                        }
                    }
                }
                // Remove old servers that aren't found any more.
                val newServers = (valueCompat.value ?: emptyList()).toMutableList()
                newServers.retainAll(newServerSet)
                postValue(Success(newServers))
            } catch (e: Exception) {
                postValue(Failure(valueCompat.value, e))
            }
        }
    }

    // If a computer running recent Windows 10 is elected the master browser, it won't actually
    // provide the service to others (jCIFS-NG NetServerEnumIterator gets
    // ERROR_SERVICE_NOT_INSTALLED), as SMBv1 has been disabled. Windows now uses WS-Discovery, but
    // it doesn't have a good standalone Java implementation and Samba doesn't support it.
    // https://social.technet.microsoft.com/Forums/en-US/bd0af6aa-51ec-477a-8c81-888a4e60bd94/master-browser-service-broken-after-creator-update#2c6b9e65-da8a-4e41-a2cb-db086443ef87
    // https://docs.microsoft.com/en-nz/windows-server/storage/file-server/troubleshoot/smbv1-not-installed-by-default-in-windows
    private fun CoroutineScope.getServersByComputerBrowserService(
    ): ReceiveChannel<LanSmbServer> =
        produce {
            launch {
                @Suppress("DEPRECATION")
                val lan = SmbFile("smb://")
                val domains = try {
                    lan.listFiles()
                } catch (e: SmbException) {
                    e.printStackTrace()
                    return@launch
                }
                val nameServiceClient = SingletonContext.getInstance().nameServiceClient
                for (domain in domains) {
                    launch {
                        val servers = try {
                            domain.listFiles()
                        } catch (e: SmbException) {
                            e.printStackTrace()
                            return@launch
                        }
                        for (server in servers) {
                            launch {
                                // Drop the trailing slash
                                val host = server.name.dropLast(1)
                                val address = try {
                                    nameServiceClient.getByName(host).toInetAddress()
                                } catch (e: UnknownHostException) {
                                    e.printStackTrace()
                                    return@launch
                                }
                                send(LanSmbServer(host, address))
                            }
                        }
                    }
                }
            }
        }

    private fun CoroutineScope.getServersByScanningSubnet(): ReceiveChannel<LanSmbServer> =
        produce {
            launch {
                val localAddress = InetAddress::class.getLocalAddress()
                if (localAddress !is Inet4Address || !localAddress.isSiteLocalAddress) {
                    return@launch
                }
                val nameServiceClient = SingletonContext.getInstance().nameServiceClient
                for (address in localAddress.getSubnetAddresses()) {
                    launch {
                        val nbtAddresses = try {
                            nameServiceClient.getNbtAllByAddress(address.hostAddress)
                        } catch (e: UnknownHostException) {
                            e.printStackTrace()
                            return@launch
                        }
                        val host = nbtAddresses.firstOrNull()?.hostName ?: return@launch
                        send(LanSmbServer(host, address))
                    }
                }
            }
        }

    private fun Inet4Address.getSubnetAddresses(): Sequence<Inet4Address> =
        sequence {
            val addressBytes = address
            for (i in 0..99) {
                for (j in 0..2) {
                    val lastBit = 100 * j + i
                    if (lastBit > 255) {
                        continue
                    }
                    addressBytes[3] = lastBit.toByte()
                    yield(InetAddress.getByAddress(addressBytes) as Inet4Address)
                }
            }
        }

    override fun close() {
        cancelLoadingValue()
    }

    private fun cancelLoadingValue() {
        loadFuture?.cancel(true)
        loadFuture = null
    }
}
