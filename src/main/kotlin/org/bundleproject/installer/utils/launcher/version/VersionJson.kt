package org.bundleproject.installer.utils.launcher.version

import com.google.gson.JsonParser
import org.bundleproject.installer.utils.launcher.version.impl.*
import java.io.File
import kotlin.reflect.KFunction

/**
 * A compatibility layer between all schemas of minecraft version jsons
 *
 * @since 0.2.0
 */
interface VersionJson {
    var mainClass: String
    var id: String

    fun addGameArgument(vararg argument: String)
    fun addLibrary(library: VersionLibrary)
    fun save(file: File)

    companion object {
        private val schemas = mapOf<Int, KFunction<VersionJson>>(
            21 to ::Schema2021,
            14 to ::Schema2014,
            15 to ::Schema2014,
            16 to ::Schema2014,
            17 to ::Schema2014,
            18 to ::Schema2014,
        )

        fun of(
            file: File,
            minimumLauncherVersion: Int =
                JsonParser.parseString(file.readText()).asJsonObject.get("minimumLauncherVersion").asInt
        ): VersionJson =
            schemas[minimumLauncherVersion]?.call(file)
                ?: throw IllegalArgumentException("Unknown minimum launcher version: $minimumLauncherVersion")
    }
}