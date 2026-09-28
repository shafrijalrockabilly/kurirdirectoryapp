package com.latihan.kurirdirectoryapp

import com.google.gson.annotations.SerializedName

/**
 * Model data untuk menyimpan informasi perusahaan pengguna dari JSONPlaceholder.
 */
data class CompanyInfo(
    @SerializedName("name")
    val companyName: String = ""
)
