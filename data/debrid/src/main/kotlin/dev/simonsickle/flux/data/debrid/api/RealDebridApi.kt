package dev.simonsickle.flux.data.debrid.api

import dev.simonsickle.flux.data.debrid.dto.*
import retrofit2.http.*

interface RealDebridApi {
    @GET("user")
    suspend fun getUser(): RealDebridUserDto

    @FormUrlEncoded
    @POST("unrestrict/link")
    suspend fun unrestrictLink(@Field("link") link: String): UnrestrictLinkResponseDto

    @FormUrlEncoded
    @POST("torrents/addMagnet")
    suspend fun addMagnet(@Field("magnet") magnet: String): AddMagnetResponseDto

    @FormUrlEncoded
    @POST("torrents/selectFiles/{id}")
    suspend fun selectFiles(
        @Path("id") id: String,
        @Field("files") files: String = "all"
    )

    @GET("torrents/info/{id}")
    suspend fun getTorrentInfo(@Path("id") id: String): TorrentInfoDto

    @GET("torrents/instantAvailability/{hash}")
    suspend fun getInstantAvailability(@Path("hash") hash: String): Map<String, @JvmSuppressWildcards Any>
}
