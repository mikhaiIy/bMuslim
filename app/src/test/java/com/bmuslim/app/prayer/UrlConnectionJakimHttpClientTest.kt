package com.bmuslim.app.prayer

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlConnectionJakimHttpClientTest {
    private val url = "https://www.e-solat.gov.my/index.php"

    @Test
    fun redirectsAreNotFollowedAndAreRejected() {
        val connection = FakeConnection(status = 302, body = "redirect")
        val client = client(connection)

        val result = client.get(url)

        assertEquals(JakimHttpResult.HttpFailure(302), result)
        assertFalse(connection.instanceFollowRedirects)
        assertTrue(connection.disconnected)
        assertFalse(connection.inputStreamRead)
    }

    @Test
    fun nonSuccessfulStatusIsRejectedAndDisconnected() {
        val connection = FakeConnection(status = 503, body = "unavailable")

        assertEquals(JakimHttpResult.HttpFailure(503), client(connection).get(url))
        assertTrue(connection.disconnected)
    }

    @Test
    fun declaredAndStreamedBodyLimitsAreEnforced() {
        val declared = FakeConnection(status = 200, body = "ok", declaredLength = 5)
        val streamed = FakeConnection(status = 200, body = "12345", declaredLength = -1)

        assertEquals(JakimHttpResult.ResponseTooLarge, client(declared, maximumBodyBytes = 4).get(url))
        assertFalse(declared.inputStreamRead)
        assertTrue(declared.disconnected)
        assertEquals(JakimHttpResult.ResponseTooLarge, client(streamed, maximumBodyBytes = 4).get(url))
        assertTrue(streamed.inputStreamRead)
        assertTrue(streamed.disconnected)
    }

    @Test
    fun successfulBoundedBodyIsReturnedAndDisconnected() {
        val connection = FakeConnection(status = 200, body = "{}")

        assertEquals(JakimHttpResult.Success("{}"), client(connection).get(url))
        assertTrue(connection.disconnected)
    }

    @Test
    fun ioAndTimeoutFailuresMapToNoConnectionAndDisconnect() {
        val io = FakeConnection(statusFailure = IOException("network"))
        val timeout = FakeConnection(statusFailure = SocketTimeoutException("timeout"))

        assertEquals(JakimHttpResult.NoConnection, client(io).get(url))
        assertEquals(JakimHttpResult.NoConnection, client(timeout).get(url))
        assertTrue(io.disconnected)
        assertTrue(timeout.disconnected)
    }

    @Test
    fun connectionFactoryIoFailureMapsToNoConnection() {
        val client = UrlConnectionJakimHttpClient(
            connectionFactory = HttpConnectionFactory { throw IOException("dns") },
        )

        assertEquals(JakimHttpResult.NoConnection, client.get(url))
    }

    private fun client(
        connection: FakeConnection,
        maximumBodyBytes: Int = 128 * 1024,
    ) = UrlConnectionJakimHttpClient(
        maximumBodyBytes = maximumBodyBytes,
        connectionFactory = HttpConnectionFactory { connection },
    )

    private class FakeConnection(
        private val status: Int = 200,
        private val body: String = "{}",
        private val declaredLength: Long = body.toByteArray().size.toLong(),
        private val statusFailure: IOException? = null,
    ) : HttpURLConnection(URL("https://www.e-solat.gov.my/")) {
        var disconnected = false
        var inputStreamRead = false

        override fun getResponseCode(): Int {
            statusFailure?.let { throw it }
            return status
        }

        override fun getContentLengthLong(): Long = declaredLength

        override fun getInputStream(): InputStream {
            inputStreamRead = true
            return ByteArrayInputStream(body.toByteArray())
        }

        override fun disconnect() {
            disconnected = true
        }

        override fun usingProxy(): Boolean = false
        override fun connect() = Unit
    }
}
