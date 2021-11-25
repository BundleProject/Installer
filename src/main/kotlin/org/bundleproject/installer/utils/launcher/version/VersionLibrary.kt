package org.bundleproject.installer.utils.launcher.version

/**
 * A library entry for version jsons
 *
 * @since 0.2.0
 */
data class VersionLibrary(
    val name: String,
    val url: String,
    val path: String,
    val sha1: String? = null,
    val size: Int,
)