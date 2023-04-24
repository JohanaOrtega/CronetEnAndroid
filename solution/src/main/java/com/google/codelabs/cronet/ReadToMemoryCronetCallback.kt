package com.google.codelabs.cronet

import android.util.Log
import org.chromium.net.UrlRequest
import org.chromium.net.UrlResponseInfo
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.channels.Channels

internal abstract class ReadToMemoryCronetCallback : UrlRequest.Callback() {
    private val bytesReceived = ByteArrayOutputStream()
    private val receiveChannel = Channels.newChannel(bytesReceived)

    final override fun onRedirectReceived(
        request: UrlRequest, info: UrlResponseInfo?, newLocationUrl: String?
    ) {
        request.followRedirect()
    }

    final override fun onResponseStarted(request: UrlRequest, info: UrlResponseInfo) {
        Log.i(TAG, "****** Response Started ******")
        Log.i(TAG, "*** Headers Are *** ${info.allHeaders}")

        request.read(ByteBuffer.allocateDirect(BYTE_BUFFER_CAPACITY_BYTES))
    }

    final override fun onReadCompleted(
        request: UrlRequest, info: UrlResponseInfo, byteBuffer: ByteBuffer
    ) {
        byteBuffer.flip()
        receiveChannel.write(byteBuffer)
        byteBuffer.clear()
        request.read(byteBuffer)
    }

    final override fun onSucceeded(request: UrlRequest, info: UrlResponseInfo) {
        val bodyBytes = bytesReceived.toByteArray()

        onSucceeded(request, info, bodyBytes)
    }

    abstract fun onSucceeded(
        request: UrlRequest, info: UrlResponseInfo, bodyBytes: ByteArray)

    companion object {
        private const val TAG = "ReadToMemoryCronetCallback"
        private const val BYTE_BUFFER_CAPACITY_BYTES = 64 * 1024
    }
}