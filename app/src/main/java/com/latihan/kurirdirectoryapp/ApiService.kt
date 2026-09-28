package com.latihan.kurirdirectoryapp

import retrofit2.Response
import retrofit2.http.GET

/**
 * Interface Retrofit yang mendefinisikan endpoint REST API untuk pengambilan data pengguna.
 */
interface ApiService {

    /**
     * Mengambil daftar seluruh pengguna/kurir dari endpoint /users.
     */
    @GET("users")
    suspend fun getAllUsers(): Response<List<UserResponse>>
}
