package dev.simonsickle.flux.data.debrid.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RealDebridUserDto(
    val id: Int = 0,
    val username: String = "",
    val email: String = "",
    val premium: Int = 0,
    val expiration: String = "",
    val points: Int = 0
)

@Serializable
data class UnrestrictLinkResponseDto(
    val id: String = "",
    val filename: String = "",
    val mimeType: String = "",
    val filesize: Long = 0,
    val link: String = "",
    val download: String = ""
)

@Serializable
data class AddMagnetResponseDto(
    val id: String = "",
    val uri: String = ""
)

@Serializable
data class TorrentInfoDto(
    val id: String = "",
    val filename: String = "",
    val status: String = "",
    val links: List<String> = emptyList(),
    val files: List<TorrentFileDto> = emptyList(),
    val progress: Int = 0
)

@Serializable
data class TorrentFileDto(
    val id: Int = 0,
    val path: String = "",
    val bytes: Long = 0,
    val selected: Int = 0
)
