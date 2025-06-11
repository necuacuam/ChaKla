package com.necuacuam.chaklabe.network

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL

object Esp32Locator {

    fun getLocalIpAddress(context: Context): String? {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipInt = wifiManager.connectionInfo.ipAddress
        return InetAddress.getByAddress(
            byteArrayOf(
                (ipInt and 0xff).toByte(),
                (ipInt shr 8 and 0xff).toByte(),
                (ipInt shr 16 and 0xff).toByte(),
                (ipInt shr 24 and 0xff).toByte()
            )
        ).hostAddress
    }

    fun scan(context: Context, onFound: (String) -> Unit, onFail: () -> Unit = {}) {
        val baseIp = getLocalIpAddress(context)?.substringBeforeLast(".") ?: return onFail()

        CoroutineScope(Dispatchers.IO).launch {
            val found = CompletableDeferred<String?>()

            val jobs = (2..254).map { i ->
                async {
                    val targetIp = "$baseIp.$i"
                    try {
                        val url = URL("http://$targetIp/status")
                        val connection = url.openConnection() as HttpURLConnection
                        connection.connectTimeout = 500
                        connection.readTimeout = 500
                        connection.requestMethod = "GET"
                        val response = connection.inputStream.bufferedReader().readText()
                        if (response.trim() == "ok") {
                            found.complete(targetIp)
                        }
                    } catch (_: Exception) {
                        // ignore
                    }
                }
            }

            val result = try {
                withTimeout(5000) { found.await() }
            } catch (_: TimeoutCancellationException) {
                null
            }

            jobs.forEach { it.cancel() }

            withContext(Dispatchers.Main) {
                if (result != null) {
                    onFound(result)
                } else {
                    onFail()
                }
            }
        }
    }

}
