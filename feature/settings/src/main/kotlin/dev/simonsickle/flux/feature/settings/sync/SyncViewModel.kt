package dev.simonsickle.flux.feature.settings.sync

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.simonsickle.flux.core.sync.SyncDiscovery
import dev.simonsickle.flux.core.sync.SyncImportOptions
import dev.simonsickle.flux.core.sync.SyncManager
import dev.simonsickle.flux.core.sync.SyncPayload
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncManager: SyncManager
) : ViewModel() {

    val senderState = syncManager.senderState
    val receiverState = syncManager.receiverState

    fun startSending() = syncManager.startSending()
    fun stopSending() = syncManager.stopSending()
    fun resetSender() = syncManager.resetSender()

    fun startDiscovery() = syncManager.startDiscovery()
    fun connectToDevice(device: SyncDiscovery.DiscoveredDevice) = syncManager.connectToDevice(device)
    fun connectManually(host: String, port: Int) = syncManager.connectManually(host, port)
    fun submitPin(device: SyncDiscovery.DiscoveredDevice, pin: String) = syncManager.submitPin(device, pin)
    fun importPayload(payload: SyncPayload, options: SyncImportOptions) = syncManager.importPayload(payload, options)
    fun resetReceiver() = syncManager.resetReceiver()

    override fun onCleared() {
        syncManager.stopSending()
        syncManager.resetReceiver()
    }
}
