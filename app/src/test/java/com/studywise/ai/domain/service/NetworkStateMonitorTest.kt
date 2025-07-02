package com.studywise.ai.domain.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import com.studywise.ai.data.service.NetworkStateMonitorImpl
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for NetworkStateMonitor
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class NetworkStateMonitorTest {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var connectivityManager: ConnectivityManager

    @MockK
    private lateinit var network: Network

    @MockK
    private lateinit var networkCapabilities: NetworkCapabilities

    private lateinit var networkStateMonitor: NetworkStateMonitor

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        
        networkStateMonitor = NetworkStateMonitorImpl(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `test initial network state when connected to WiFi`() = runTest {
        // Given
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns false
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) } returns true

        // When
        val state = networkStateMonitor.networkState.value

        // Then
        assertTrue(state.isConnected)
        assertTrue(state.isWifi)
        assertFalse(state.isCellular)
        assertFalse(state.isMetered)
        assertTrue(state.isValidated)
        assertEquals(NetworkType.WIFI, state.networkType)
    }

    @Test
    fun `test initial network state when connected to cellular`() = runTest {
        // Given
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns false
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns true
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) } returns false

        // When
        val state = networkStateMonitor.networkState.value

        // Then
        assertTrue(state.isConnected)
        assertFalse(state.isWifi)
        assertTrue(state.isCellular)
        assertTrue(state.isMetered)
        assertTrue(state.isValidated)
        assertEquals(NetworkType.CELLULAR, state.networkType)
    }

    @Test
    fun `test network state when disconnected`() = runTest {
        // Given
        every { connectivityManager.activeNetwork } returns null
        every { connectivityManager.getNetworkCapabilities(any()) } returns null

        // When
        val state = networkStateMonitor.networkState.value

        // Then
        assertFalse(state.isConnected)
        assertFalse(state.isWifi)
        assertFalse(state.isCellular)
        assertFalse(state.isMetered)
        assertFalse(state.isValidated)
        assertEquals(NetworkType.NONE, state.networkType)
    }

    @Test
    fun `test isNetworkAvailable returns correct value`() {
        // Given
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasTransport(any()) } returns true
        every { networkCapabilities.hasCapability(any()) } returns true

        // When
        val isAvailable = networkStateMonitor.isNetworkAvailable()

        // Then
        assertTrue(isAvailable)
    }

    @Test
    fun `test isWifiConnected returns correct value`() {
        // Given
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns false
        every { networkCapabilities.hasCapability(any()) } returns true

        // When
        val isWifi = networkStateMonitor.isWifiConnected()

        // Then
        assertTrue(isWifi)
    }

    @Test
    fun `test network state changes are emitted`() = runTest {
        // Given
        val callback = slot<ConnectivityManager.NetworkCallback>()
        every { connectivityManager.registerNetworkCallback(any(), capture(callback)) } just Runs
        every { connectivityManager.unregisterNetworkCallback(any()) } just Runs
        every { connectivityManager.activeNetwork } returns null
        every { connectivityManager.getNetworkCapabilities(any()) } returns null

        // When - collect network state changes
        val stateFlow = networkStateMonitor.networkStateChanges
        val job = stateFlow.launchIn(this)

        // Simulate network becoming available
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns false
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) } returns true

        callback.captured.onAvailable(network)

        // Then
        val newState = networkStateMonitor.networkState.value
        assertTrue(newState.isConnected)
        assertTrue(newState.isWifi)

        job.cancel()
    }
}