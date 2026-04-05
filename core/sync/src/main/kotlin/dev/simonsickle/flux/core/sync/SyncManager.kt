package dev.simonsickle.flux.core.sync

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.simonsickle.flux.core.common.SettingsRepository
import dev.simonsickle.flux.core.database.dao.AddonDao
import dev.simonsickle.flux.core.database.dao.BookmarkDao
import dev.simonsickle.flux.core.database.dao.WatchHistoryDao
import dev.simonsickle.flux.core.database.entity.BookmarkEntity
import dev.simonsickle.flux.core.database.entity.InstalledAddonEntity
import dev.simonsickle.flux.core.database.entity.WatchHistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val addonDao: AddonDao,
    private val bookmarkDao: BookmarkDao,
    private val watchHistoryDao: WatchHistoryDao
) {
    companion object {
        private const val TAG = "SyncManager"
        private const val SERVER_TIMEOUT_MS = 5 * 60 * 1000L // 5 minutes
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val discovery = SyncDiscovery(context)

    private val _senderState = MutableStateFlow<SenderState>(SenderState.Idle)
    val senderState: StateFlow<SenderState> = _senderState.asStateFlow()

    private val _receiverState = MutableStateFlow<ReceiverState>(ReceiverState.Idle)
    val receiverState: StateFlow<ReceiverState> = _receiverState.asStateFlow()

    private var server: SyncServer? = null
    private var nsdRegistration: SyncDiscovery.NsdRegistration? = null
    private var timeoutJob: Job? = null

    // ── Sender (share settings) ──

    fun startSending() {
        scope.launch {
            try {
                val pin = SyncCrypto.generatePin()
                val salt = SyncCrypto.generateSalt()

                val syncServer = SyncServer(
                    pin = pin,
                    salt = salt,
                    payloadProvider = { buildPayloadBlocking() },
                    onPaired = { _senderState.value = SenderState.Paired },
                    onSyncComplete = {
                        _senderState.value = SenderState.Complete
                        stopSending()
                    },
                    onError = { error ->
                        _senderState.value = SenderState.Error(error)
                        stopSending()
                    }
                )

                syncServer.start()
                server = syncServer
                val port = syncServer.listeningPort

                nsdRegistration = discovery.registerService(port)

                val ipAddress = getLocalIpAddress()
                _senderState.value = SenderState.WaitingForReceiver(
                    pin = pin,
                    ipAddress = ipAddress,
                    port = port
                )

                // Auto-shutdown after timeout
                timeoutJob = scope.launch {
                    delay(SERVER_TIMEOUT_MS)
                    if (_senderState.value is SenderState.WaitingForReceiver ||
                        _senderState.value is SenderState.Paired
                    ) {
                        _senderState.value = SenderState.Error("Timed out waiting for connection")
                        stopSending()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start sender", e)
                _senderState.value = SenderState.Error(e.message ?: "Failed to start server")
            }
        }
    }

    fun stopSending() {
        timeoutJob?.cancel()
        timeoutJob = null
        nsdRegistration?.unregister()
        nsdRegistration = null
        server?.stop()
        server = null
        if (_senderState.value !is SenderState.Complete && _senderState.value !is SenderState.Error) {
            _senderState.value = SenderState.Idle
        }
    }

    fun resetSender() {
        stopSending()
        _senderState.value = SenderState.Idle
    }

    // ── Receiver (receive settings) ──

    fun startDiscovery() {
        _receiverState.value = ReceiverState.Discovering(emptyList())

        scope.launch {
            discovery.discoverDevices().collect { device ->
                val current = _receiverState.value
                if (current is ReceiverState.Discovering) {
                    val updated = current.devices.toMutableList()
                    if (updated.none { it.host == device.host && it.port == device.port }) {
                        updated.add(device)
                        _receiverState.value = ReceiverState.Discovering(updated)
                    }
                }
            }
        }
    }

    fun connectToDevice(device: SyncDiscovery.DiscoveredDevice) {
        _receiverState.value = ReceiverState.EnterPin(device)
    }

    fun connectManually(host: String, port: Int) {
        val device = SyncDiscovery.DiscoveredDevice(name = host, host = host, port = port)
        _receiverState.value = ReceiverState.EnterPin(device)
    }

    fun submitPin(device: SyncDiscovery.DiscoveredDevice, pin: String) {
        _receiverState.value = ReceiverState.Connecting

        scope.launch(Dispatchers.IO) {
            val client = SyncClient(device.host, device.port)
            try {
                when (val result = client.pair(pin)) {
                    is SyncClient.PairResult.Success -> {
                        val payload = client.fetchPayload(pin, result.salt)
                        withContext(Dispatchers.Main) {
                            _receiverState.value = ReceiverState.ReviewPayload(payload)
                        }
                    }
                    is SyncClient.PairResult.InvalidPin -> {
                        withContext(Dispatchers.Main) {
                            _receiverState.value = ReceiverState.PinError(device, "Wrong PIN. Try again.")
                        }
                    }
                    is SyncClient.PairResult.AlreadyPaired -> {
                        withContext(Dispatchers.Main) {
                            _receiverState.value = ReceiverState.Error("Another device already paired")
                        }
                    }
                    is SyncClient.PairResult.Error -> {
                        withContext(Dispatchers.Main) {
                            _receiverState.value = ReceiverState.Error(result.message)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sync failed", e)
                withContext(Dispatchers.Main) {
                    _receiverState.value = ReceiverState.Error(e.message ?: "Connection failed")
                }
            } finally {
                client.close()
            }
        }
    }

    fun importPayload(payload: SyncPayload, options: SyncImportOptions) {
        scope.launch {
            try {
                _receiverState.value = ReceiverState.Importing

                withContext(Dispatchers.IO) {
                    if (options.settings) {
                        settingsRepository.setDefaultContentType(payload.settings.defaultContentType)
                        settingsRepository.setPreferredPlayer(payload.settings.preferredPlayer)
                        settingsRepository.setSubtitleLanguage(payload.settings.subtitleLanguage)
                        settingsRepository.setHardwareAcceleration(payload.settings.hardwareAcceleration)
                    }

                    if (options.realDebridToken) {
                        settingsRepository.setRealDebridToken(payload.settings.realDebridToken)
                    }

                    if (options.addons) {
                        for (addon in payload.addons) {
                            addonDao.insertAddon(
                                InstalledAddonEntity(
                                    id = addon.id,
                                    transportUrl = addon.transportUrl,
                                    manifestJson = addon.manifestJson,
                                    enabled = addon.enabled,
                                    orderIndex = addon.orderIndex
                                )
                            )
                        }
                    }

                    if (options.bookmarks) {
                        for (bookmark in payload.bookmarks) {
                            bookmarkDao.insert(
                                BookmarkEntity(
                                    contentId = bookmark.contentId,
                                    contentType = bookmark.contentType,
                                    title = bookmark.title,
                                    poster = bookmark.poster,
                                    addedAt = bookmark.addedAt
                                )
                            )
                        }
                    }

                    if (options.watchHistory) {
                        for (entry in payload.watchHistory) {
                            watchHistoryDao.upsert(
                                WatchHistoryEntity(
                                    contentId = entry.contentId,
                                    contentType = entry.contentType,
                                    title = entry.title,
                                    poster = entry.poster,
                                    lastPosition = entry.lastPosition,
                                    duration = entry.duration,
                                    lastWatchedAt = entry.lastWatchedAt,
                                    videoId = entry.videoId
                                )
                            )
                        }
                    }
                }

                _receiverState.value = ReceiverState.Complete
            } catch (e: Exception) {
                Log.e(TAG, "Import failed", e)
                _receiverState.value = ReceiverState.Error(e.message ?: "Import failed")
            }
        }
    }

    fun resetReceiver() {
        _receiverState.value = ReceiverState.Idle
    }

    // ── Helpers ──

    private fun buildPayloadBlocking(): SyncPayload {
        // This runs on NanoHTTPD's server thread, use runBlocking-safe first() calls
        return kotlinx.coroutines.runBlocking {
            val token = settingsRepository.realDebridToken.first()
            val contentType = settingsRepository.defaultContentType.first()
            val player = settingsRepository.preferredPlayer.first()
            val subtitleLang = settingsRepository.subtitleLanguage.first()
            val hwAccel = settingsRepository.hardwareAcceleration.first()
            val addons = addonDao.getAllAddons().first()
            val bookmarks = bookmarkDao.getAll().first()
            val history = watchHistoryDao.getAll().first()

            SyncPayload(
                settings = SyncSettings(
                    realDebridToken = token,
                    defaultContentType = contentType,
                    preferredPlayer = player,
                    subtitleLanguage = subtitleLang,
                    hardwareAcceleration = hwAccel
                ),
                addons = addons.map { addon ->
                    SyncAddon(
                        id = addon.id,
                        transportUrl = addon.transportUrl,
                        manifestJson = addon.manifestJson,
                        enabled = addon.enabled,
                        orderIndex = addon.orderIndex
                    )
                },
                bookmarks = bookmarks.map { bm ->
                    SyncBookmark(
                        contentId = bm.contentId,
                        contentType = bm.contentType,
                        title = bm.title,
                        poster = bm.poster,
                        addedAt = bm.addedAt
                    )
                },
                watchHistory = history.map { entry ->
                    SyncWatchHistoryEntry(
                        contentId = entry.contentId,
                        contentType = entry.contentType,
                        title = entry.title,
                        poster = entry.poster,
                        lastPosition = entry.lastPosition,
                        duration = entry.duration,
                        lastWatchedAt = entry.lastWatchedAt,
                        videoId = entry.videoId
                    )
                }
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun getLocalIpAddress(): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ip = wifiManager.connectionInfo.ipAddress
        return "${ip and 0xFF}.${ip shr 8 and 0xFF}.${ip shr 16 and 0xFF}.${ip shr 24 and 0xFF}"
    }

    // ── State models ──

    sealed interface SenderState {
        data object Idle : SenderState
        data class WaitingForReceiver(
            val pin: String,
            val ipAddress: String,
            val port: Int
        ) : SenderState
        data object Paired : SenderState
        data object Complete : SenderState
        data class Error(val message: String) : SenderState
    }

    sealed interface ReceiverState {
        data object Idle : ReceiverState
        data class Discovering(val devices: List<SyncDiscovery.DiscoveredDevice>) : ReceiverState
        data class EnterPin(val device: SyncDiscovery.DiscoveredDevice) : ReceiverState
        data class PinError(val device: SyncDiscovery.DiscoveredDevice, val message: String) : ReceiverState
        data object Connecting : ReceiverState
        data class ReviewPayload(val payload: SyncPayload) : ReceiverState
        data object Importing : ReceiverState
        data object Complete : ReceiverState
        data class Error(val message: String) : ReceiverState
    }
}
