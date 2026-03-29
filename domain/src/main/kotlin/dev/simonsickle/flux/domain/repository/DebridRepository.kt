package dev.simonsickle.flux.domain.repository

import dev.simonsickle.flux.core.model.ResolvedStream
import dev.simonsickle.flux.core.model.StreamInfo

interface DebridRepository {
    suspend fun resolveStream(stream: StreamInfo): ResolvedStream
    suspend fun checkInstantAvailability(infoHashes: List<String>): Map<String, Boolean>
    suspend fun getUserInfo(): DebridUserInfo?
    suspend fun isConfigured(): Boolean
}

data class DebridUserInfo(
    val username: String,
    val email: String,
    val premium: Int,
    val expiration: String,
    val points: Int
)
