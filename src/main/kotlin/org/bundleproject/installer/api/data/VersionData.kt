package org.bundleproject.installer.api.data

import com.google.gson.annotations.SerializedName

data class VersionData(
    val installer: String,
    val updater: String,
    @SerializedName("launchwrapper") val launchWrapper: String
)