package dev.simonsickle.flux.feature.settings.sync

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import dev.simonsickle.flux.core.common.LocalIsTv

@Composable
fun SyncSendRoute(
    onNavigateUp: () -> Unit,
    viewModel: SyncViewModel = hiltViewModel()
) {
    val senderState by viewModel.senderState.collectAsState()
    val isTv = LocalIsTv.current

    if (isTv) {
        TvSyncSendScreen(
            state = senderState,
            onStartSending = viewModel::startSending,
            onStopSending = viewModel::stopSending,
            onReset = viewModel::resetSender,
            onNavigateUp = onNavigateUp
        )
    } else {
        MobileSyncSendScreen(
            state = senderState,
            onStartSending = viewModel::startSending,
            onStopSending = viewModel::stopSending,
            onReset = viewModel::resetSender,
            onNavigateUp = onNavigateUp
        )
    }
}

@Composable
fun SyncReceiveRoute(
    onNavigateUp: () -> Unit,
    viewModel: SyncViewModel = hiltViewModel()
) {
    val receiverState by viewModel.receiverState.collectAsState()
    val isTv = LocalIsTv.current

    if (isTv) {
        TvSyncReceiveScreen(
            state = receiverState,
            onStartDiscovery = viewModel::startDiscovery,
            onSelectDevice = viewModel::connectToDevice,
            onConnectManually = viewModel::connectManually,
            onSubmitPin = viewModel::submitPin,
            onImport = viewModel::importPayload,
            onReset = viewModel::resetReceiver,
            onNavigateUp = onNavigateUp
        )
    } else {
        MobileSyncReceiveScreen(
            state = receiverState,
            onStartDiscovery = viewModel::startDiscovery,
            onSelectDevice = viewModel::connectToDevice,
            onConnectManually = viewModel::connectManually,
            onSubmitPin = viewModel::submitPin,
            onImport = viewModel::importPayload,
            onReset = viewModel::resetReceiver,
            onNavigateUp = onNavigateUp
        )
    }
}
