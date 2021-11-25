package org.bundleproject.installer.utils.launcher.profiles

import java.io.File

/**
 * Utility to manage the multiple editions of Minecraft launchers
 *
 * @since 0.2.0
 */
enum class LauncherProfileTypes(val profileName: String, val friendlyName: String) {
    WIN32("launcher_profiles.json", "Direct Download"),
    MICROSOFT_STORE("launcher_profiles_microsoft_store.json", "Microsoft Store");

    companion object {
        fun findAvailableTypes(folder: File): List<LauncherProfileTypes> = buildList {
            for (type in values()) {
                if (File(folder, type.profileName).exists())
                    add(type)
            }
        }
    }
}