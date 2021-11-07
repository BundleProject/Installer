package org.bundleproject.installer

import com.google.gson.*
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.DefaultHelpFormatter
import com.xenomachina.argparser.default
import io.ktor.client.request.*
import org.bundleproject.installer.api.response.VersionResponse
import org.bundleproject.installer.gui.frames.InstallerGui
import org.bundleproject.installer.utils.*
import java.io.File
import java.io.InputStreamReader
import java.lang.IllegalArgumentException

/**
 * Parses command-line arguments and opens the gui
 *
 * @since 0.0.1
 */
suspend fun main(args: Array<String>) {
    ArgParser(args, helpFormatter = DefaultHelpFormatter()).parseInto(::InstallerParams).run {
        if (silent) {
            if (path == null) throw IllegalArgumentException("Path does not exist or it wasn't specified.")

            if (!multimc) installOfficial(path!!, mcversion, inject)
            else installMultiMC(getMultiMCInstanceFolder(path!!)!!, mcversion)

            return
        }

        InstallerGui.isVisible = true
    }
}

/**
 * Installs Bundle to a normal .minecraft (specifically the launcher dir NOT game dir)
 *
 * @since 0.0.1
 */
suspend fun installOfficial(path: File, mcversion: String, inject: Boolean) {
    println("Installing using the official launcher.")

    val latest = http.get<VersionResponse>("$API/$API_VERSION/bundle/version").data.launchWrapper

    var versionJson = File(path, "versions/$mcversion/$mcversion.json")
    val json = InputStreamReader(versionJson.inputStream()).use {
        JsonParser.parseReader(it).asJsonObject
    }

    val mainClass = json.get("mainClass").asString
    val libs = JsonArray().also {
        json.getAsJsonArray("libraries")
            .filter  { !(it.asJsonObject.get("name")?.asString?.startsWith("org.bundleproject:launchwrapper:") ?: false) }
            .forEach { lib -> it.add(lib) }
    }

    val bundleLib = if (!inject) {
        // switch target version json to bundle's own folder
        versionJson = File(path, "versions/$mcversion-bundle/$mcversion-bundle.json")
        versionJson.parentFile.mkdir()

        // copy over existing jar to prevent re-downloading
        val jar = File(path, "versions/$mcversion/$mcversion.jar")
        if (jar.exists()) jar.copyTo(File(path, "versions/$mcversion-bundle/$mcversion-bundle.jar"))

        json.also {
            it.addProperty("id", "$mcversion-bundle")
        }
    } else {
        JsonObject()
    }
    // add bundle library
    bundleLib.addProperty("name", "org.bundleproject:launchwrapper:$latest")
    bundleLib.addProperty("url", "https://jitpack.io/")
    libs.add(bundleLib)
    json.add("libraries", libs)

    // make the launcher run bundle rather than the game's main class
    json.addProperty("mainClass", "org.bundleproject.bundle.main.Main")

    // input original main class into the game arguments
    val gameArguments = json.getAsJsonObject("arguments").getAsJsonArray("game")
    gameArguments.add("--bundleMainClass")
    gameArguments.add(mainClass)

    versionJson.writeText(gson.toJson(json))
}

/**
 * Installs Bundle to MultiMC as a `jar mod`
 *
 * @since 0.0.1
 */
suspend fun installMultiMC(instanceFolder: File, instance: String) {
    println("Installing through MultiMC")

    val mmcFolder = instanceFolder.parentFile!!

    val metaFolder = File(mmcFolder, "meta")
    val bundleMetaFolder = File(metaFolder, "org.bundleproject")
    bundleMetaFolder.mkdir()

    val latest = http.get<VersionResponse>("$API/$API_VERSION/bundle/version").data.updater

    val versionFile = File(bundleMetaFolder, "$latest.json")

    val time = getCurrentTime()

    val versionJson = JsonObject()
    versionJson.addProperty("formatVersion", 1)
    versionJson.addProperty("mainClass", "org.bundleproject.bundle.main.Main")
    versionJson.addProperty("releaseTime", time)
    versionJson.addProperty("name", "Bundle")
    versionJson.addProperty("order", 10)
    versionJson.addProperty("uid", "org.bundleproject")
    versionJson.addProperty("version", latest)

    versionJson.add("requires", JsonArray().also { arr ->
        arr.add(JsonObject().also { obj ->
            obj.addProperty("uid", "net.minecraft")
        })
    })

    val libs = JsonArray()
    libs.add(JsonObject().also {
        it.addProperty("name", "org.bundleproject:launchwrapper:$latest")
        it.addProperty("url", "https://jitpack.io/")
    })
    versionJson.add("libraries", libs)

    versionFile.writeText(gson.toJson(versionJson))

    val bundleIndexFile = File(bundleMetaFolder, "index.json")
    val bundleIndexJson = if (bundleIndexFile.exists()) {
        InputStreamReader(bundleIndexFile.inputStream()).use { JsonParser.parseReader(it).asJsonObject }
    } else {
        JsonObject()
    }

    bundleIndexJson.addProperty("formatVersion", 1)
    bundleIndexJson.addProperty("name", "Bundle")
    bundleIndexJson.addProperty("uid", "org.bundleproject")
    val versions =
        if (bundleIndexJson.has("versions")) bundleIndexJson.getAsJsonArray("versions")
        else JsonArray()

    versions.add(JsonObject().also {
        it.addProperty("releaseTime", time)
        it.add("requires", JsonArray().also { arr ->
            arr.add(JsonObject().also { obj ->
                obj.addProperty("uid", "net.minecraft")
            })
        })
        it.addProperty("version", latest)
    })

    bundleIndexFile.writeText(gson.toJson(bundleIndexJson))

    val metaIndexFile = File(metaFolder, "index.json")
    val metaIndexJson = InputStreamReader(metaIndexFile.inputStream()).use { JsonParser.parseReader(it).asJsonObject }

    val packages = JsonArray().also {
        metaIndexJson.getAsJsonArray("packages")
            .filter { it.asJsonObject.get("uid").asString != "org.bundleproject" }
            .forEach { arr -> it.add(arr) }
    }
    packages.add(JsonObject().also {
        it.addProperty("name", "Bundle")
        it.addProperty("uid", "org.bundleproject")
    })
    metaIndexJson.add("packages", packages)

    metaIndexFile.writeText(gson.toJson(metaIndexJson))

    val mmcPackFile = File(instanceFolder, "$instance/mmc-pack.json")
    val mmcPackJson = InputStreamReader(mmcPackFile.inputStream()).use { JsonParser.parseReader(it).asJsonObject }

    val components = JsonArray().also { arr ->
        mmcPackJson.getAsJsonArray("components")
            .filter { it.asJsonObject.get("uid").asString != "org.bundleproject" }
            .forEach { arr.add(it) }
    }
    components.add(JsonObject().also {
        it.addProperty("cachedName", "Bundle")
        it.add("cachedRequires", JsonArray().also { arr ->
            arr.add(JsonObject().also {
                it.addProperty("uid", "org.bundleproject")
            })
        })
        it.addProperty("cachedVersion", latest)
        it.addProperty("uid", "org.bundleproject")
        it.addProperty("version", latest)
    })
    mmcPackJson.add("components", components)

    mmcPackFile.writeText(gson.toJson(mmcPackJson))
}

/**
 * Argument class
 *
 * @since 0.0.1
 */
class InstallerParams(parser: ArgParser) {
    val silent by parser.flagging(
        "-s", "--silent",
        help = "Do not display a gui and install automatically."
    )

    val multimc by parser.flagging(
        "--multimc",
        help = "Installs into MultiMC rather than a normal minecraft installation."
    )

    val path by parser.storing(
        "-p", "--path",
        help = "Install directory.",
    ) { File(this) }.default { getDefaultMinecraftDir() }

    val mcversion by parser.storing(
        "-v", "--version",
        help = "Version profile to install bundle to."
    ).default { "1.8.9" }

    val inject by parser.flagging(
        "-i", "--inject",
        help = "Inject profile rather than creating a new one. (Only affected in official launcher install)"
    ).default { true }

}