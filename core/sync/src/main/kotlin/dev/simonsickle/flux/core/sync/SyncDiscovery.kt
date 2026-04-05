package dev.simonsickle.flux.core.sync

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/**
 * Wraps Android NSD (Network Service Discovery) for finding Flux sync servers on the LAN.
 * Uses mDNS service type "_fluxsync._tcp." for zero-conf discovery.
 */
class SyncDiscovery @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "SyncDiscovery"
        private const val SERVICE_TYPE = "_fluxsync._tcp."
        private const val SERVICE_NAME_PREFIX = "FluxSync-"
    }

    private val nsdManager: NsdManager
        get() = context.getSystemService(Context.NSD_SERVICE) as NsdManager

    data class DiscoveredDevice(
        val name: String,
        val host: String,
        val port: Int
    )

    /** Register this device as a sync server so receivers can find it. */
    fun registerService(port: Int): NsdRegistration {
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = "$SERVICE_NAME_PREFIX${android.os.Build.MODEL}"
            serviceType = SERVICE_TYPE
            setPort(port)
        }

        var registration: NsdManager.RegistrationListener? = null

        val listener = object : NsdManager.RegistrationListener {
            override fun onRegistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "NSD registration failed: $errorCode")
            }

            override fun onUnregistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "NSD unregistration failed: $errorCode")
            }

            override fun onServiceRegistered(info: NsdServiceInfo) {
                Log.d(TAG, "NSD service registered: ${info.serviceName}")
            }

            override fun onServiceUnregistered(info: NsdServiceInfo) {
                Log.d(TAG, "NSD service unregistered")
            }
        }

        registration = listener
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, listener)

        return NsdRegistration { nsdManager.unregisterService(listener) }
    }

    /** Discover Flux sync servers on the local network. Emits devices as they're found. */
    fun discoverDevices(): Flow<DiscoveredDevice> = callbackFlow {
        val discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(serviceType: String) {
                Log.d(TAG, "NSD discovery started")
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.d(TAG, "NSD discovery stopped")
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "NSD found: ${serviceInfo.serviceName}")
                if (serviceInfo.serviceName.startsWith(SERVICE_NAME_PREFIX)) {
                    resolveService(serviceInfo) { device ->
                        device?.let { trySend(it) }
                    }
                }
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "NSD lost: ${serviceInfo.serviceName}")
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "NSD start discovery failed: $errorCode")
                close()
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "NSD stop discovery failed: $errorCode")
            }
        }

        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)

        awaitClose {
            try {
                nsdManager.stopServiceDiscovery(discoveryListener)
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping NSD discovery", e)
            }
        }
    }

    private fun resolveService(
        serviceInfo: NsdServiceInfo,
        onResolved: (DiscoveredDevice?) -> Unit
    ) {
        nsdManager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
            override fun onResolveFailed(info: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "NSD resolve failed: $errorCode")
                onResolved(null)
            }

            override fun onServiceResolved(info: NsdServiceInfo) {
                val host = info.host?.hostAddress ?: return onResolved(null)
                onResolved(
                    DiscoveredDevice(
                        name = info.serviceName.removePrefix(SERVICE_NAME_PREFIX),
                        host = host,
                        port = info.port
                    )
                )
            }
        })
    }

    fun interface NsdRegistration {
        fun unregister()
    }
}
