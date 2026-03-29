package dev.simonsickle.flux.core.model

enum class ContentType(val value: String) {
    MOVIE("movie"),
    SERIES("series"),
    CHANNEL("channel"),
    TV("tv");

    companion object {
        fun fromValue(value: String): ContentType =
            entries.firstOrNull { it.value == value } ?: MOVIE
    }
}
