package com.latihan.kurirdirectoryapp

import com.google.gson.annotations.SerializedName

/**
 * Model data utama yang merepresentasikan entitas pengguna/kurir dari API GET /users.
 */
data class UserResponse(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("name")
    val name: String = "",

    @SerializedName("username")
    val username: String = "",

    @SerializedName("email")
    val email: String = "",

    @SerializedName("phone")
    val phone: String = "",

    @SerializedName("company")
    val company: CompanyInfo = CompanyInfo()
)
