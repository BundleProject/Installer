package org.bundleproject.installer.utils.launcher.profiles

import com.google.gson.JsonParser
import org.bundleproject.installer.utils.gson
import java.io.File

/**
 * Utility to handle adding launcher profiles
 *
 * @since 0.2.0
 */
class LauncherProfilesJson(private val file: File) {
    private val json = JsonParser.parseString(file.readText()).asJsonObject

    fun addProfile(id: String, profile: LauncherProfile) {
        val profiles = json.getAsJsonObject("profiles")

        profiles.add(id, gson.toJsonTree(profile))
    }

    fun save() {
        file.writeText(gson.toJson(json))
    }
}