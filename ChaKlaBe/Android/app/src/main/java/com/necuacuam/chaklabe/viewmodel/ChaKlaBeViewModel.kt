package com.necuacuam.chaklabe.viewmodel

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import com.necuacuam.chaklabe.storage.SettingsDataStore
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ChaKlaBeViewModel : ViewModel() {

    var rotationAngle by mutableStateOf(0)
        private set

    fun rotateClockwise() {
        rotationAngle = (rotationAngle + 90) % 360
    }

    fun rotateCounterClockwise() {
        rotationAngle = (rotationAngle + 270) % 360
    }


    private lateinit var settingsStore: SettingsDataStore

    fun init(context: Context) {
        settingsStore = SettingsDataStore(context)

        // Load IP on startup
        CoroutineScope(Dispatchers.IO).launch {
            settingsStore.cameraIpAddress.collect {
                cameraIpAddress = it
            }
        }
    }

    fun saveCameraIpAddress(ip: String) {
        cameraIpAddress = ip
        CoroutineScope(Dispatchers.IO).launch {
            settingsStore.saveCameraIpAddress(ip)
        }
    }

    var cameraIpAddress by mutableStateOf("")

    var streamedFrame by mutableStateOf<ImageBitmap?>(null)
        private set
    var isStreaming by mutableStateOf(false)
        private set

    var esp32Ip by mutableStateOf<String?>(null)
        private set

    var isLocating by mutableStateOf(false)
        private set

    var statusMessage by mutableStateOf<String>("")
        private set

    sealed class StatusMessage(val text: String) {
        object Idle : StatusMessage("Idle")
        object Locating : StatusMessage("Locating camera device...")
        object NotFound : StatusMessage("Camera device not found")
        class StreamingStarted(ip: String) : StatusMessage("Streaming from $ip")
        object StreamingStopped : StatusMessage("")
        class StreamingFailed(reason: String) :
            StatusMessage("Streaming failed: ${reason.take(50)}")
    }

    fun takePicture(context: Context) {
        isStreaming = false
        Log.i("takePicture", "stopStream")
        stopStream() // no longer clears esp32Ip

        val ip = esp32Ip ?: return

        CoroutineScope(Dispatchers.IO).launch {
            delay(300) // ensure stream loop exits

            try {
                val url = URL("http://$ip/picture")
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connectTimeout = 3000

                val bytes = connection.inputStream.readBytes()
                Log.i("takePicture", "Picture size: ${bytes.size} bytes")

                // TODO: save to gallery + play camera shutter sound
                saveImageToGallery(context , bytes)

                withContext(Dispatchers.Main) {
                    isStreaming = true
                    startVideoStream()
                }
            } catch (e: Exception) {
                Log.e("takePicture", "Failed: ${e.message}", e)
            }
        }
    }


    fun toggleStream(context: Context) {
        if (isStreaming) {
            stopStream()
        } else {
            startStreaming(context)
        }
    }

    private fun startStreaming(context: Context) {
        val ip = cameraIpAddress
        if (ip.isBlank()) {
            statusMessage = StatusMessage.StreamingFailed("Camera IP not set").text
            return
        }

        esp32Ip = ip
        isStreaming = true
        statusMessage = StatusMessage.StreamingStarted(ip).text
        startVideoStream()
    }


    fun startVideoStream() {
        Log.i("startVideoStream","ip $esp32Ip")
        val ip = esp32Ip ?: return
        val url = URL("http://$ip/stream")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("Accept", "multipart/x-mixed-replace")
                connection.doInput = true
                connection.connectTimeout = 5000
                connection.readTimeout = 0

                val input = connection.inputStream.bufferedReader()

                val boundary = "--frame"
                var contentLength = -1

                while (isStreaming) {
                    var line = input.readLine()
                    // Search for boundary
                    while (line != null && !line.startsWith(boundary)) {
                        line = input.readLine()
                    }
                    if (line == null) break

                    // Read headers
                    while (true) {
                        line = input.readLine()
                        if (line.isNullOrBlank()) break
                        if (line.startsWith("Content-Length", ignoreCase = true)) {
                            contentLength = line.split(":")[1].trim().toIntOrNull() ?: -1
                        }
                    }

                    if (contentLength <= 0) continue

                    // Read image bytes
                    val imageBytes = ByteArray(contentLength)
                    var bytesRead = 0
                    while (bytesRead < contentLength) {
                        val read = connection.inputStream.read(
                            imageBytes, bytesRead, contentLength - bytesRead
                        )
                        if (read == -1) break
                        bytesRead += read
                    }

                    // Decode
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    bitmap?.let {
                        streamedFrame = it.asImageBitmap()
                    }
                }

                connection.disconnect()
                statusMessage = StatusMessage.StreamingStopped.text
            } catch (e: Exception) {
                Log.e("VideoStream", "Streaming error: ${e.message}", e)
                statusMessage = StatusMessage.StreamingFailed(e.message?.take(50) ?: "unknown").text
            }
        }
    }


    private fun stopStream() {
        isStreaming = false
    }


    fun updateStreamedFrame(jpegBytes: ByteArray) {
        val bitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
        streamedFrame = bitmap?.asImageBitmap()
    }


    suspend fun saveImageToGallery(context: Context, imageBytes: ByteArray) {
        val filename = "ChaKla_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
        val mimeType = "image/jpeg"
        val relativeLocation = "Pictures/ChaKla"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val uri = resolver.insert(contentUri, contentValues)

        if (uri != null) {
            resolver.openOutputStream(uri).use { outputStream ->
                outputStream?.write(imageBytes)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Picture saved to gallery", Toast.LENGTH_SHORT).show()
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to save picture", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
