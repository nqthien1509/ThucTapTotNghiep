package com.example.thuctaptotnghiep.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id") val id: String,
    @SerializedName("email") val email: String?,
    @SerializedName("displayName") val displayName: String?,
    @SerializedName("avatarUrl") val avatarUrl: String?,
    @SerializedName("school") val school: String?,
    @SerializedName("bio") val bio: String?
)