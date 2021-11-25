package org.bundleproject.installer

import com.google.gson.*
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.DefaultHelpFormatter
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.bundleproject.installer.api.response.VersionResponse
import org.bundleproject.installer.gui.frames.InstallerGui
import org.bundleproject.installer.updater.getLatestUpdate
import org.bundleproject.installer.utils.*
import org.bundleproject.installer.utils.launcher.profiles.LauncherProfile
import org.bundleproject.installer.utils.launcher.profiles.LauncherProfileTypes
import org.bundleproject.installer.utils.launcher.profiles.LauncherProfilesJson
import org.bundleproject.installer.utils.launcher.version.VersionJson
import org.bundleproject.installer.utils.launcher.version.VersionLibrary
import java.awt.Desktop
import java.io.File
import java.io.InputStreamReader
import java.net.URI
import javax.swing.JOptionPane
import kotlin.IllegalArgumentException

/**
 * Parses command-line arguments and opens the gui
 *
 * @since 0.0.1
 */
fun main(args: Array<String>) = mainBody {
    ArgParser(args, helpFormatter = DefaultHelpFormatter()).parseInto(::InstallerParams).run {
        val update = if (!noUpdate) runBlocking { getLatestUpdate() }.takeIf { it != INSTALLER_VERSION } else null

        if (silent) {
            if (update != null) {
                println("There is an installer update available!")
                println("Get it here at https://github.com/BundleProject/Installer/releases/tag/$update")
                println()
            }

            if (path == null) throw IllegalArgumentException("Path does not exist or it wasn't specified.")

            runBlocking {
                if (!multimc) installOfficial(path!!, mcversion)
                else installMultiMC(getMultiMCInstanceFolder(path!!)!!, mcversion)
            }

            return@mainBody
        }

        InstallerGui.isVisible = true

        if (update != null) {
            JOptionPane.showOptionDialog(InstallerGui, "There is an installer update available!", "Installer Update", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, arrayOf("Update", "Maybe Later"), "Update").also {
                if (it == JOptionPane.YES_OPTION) {
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(URI.create("https://github.com/BundleProject/Installer/releases/tag/$update"))
                    }
                }
            }
        }
    }
}

/**
 * Installs Bundle to a normal .minecraft (specifically the launcher dir NOT game dir)
 *
 * @since 0.0.1
 */
suspend fun installOfficial(path: File, mcversion: String) {
    println("Installing using the official launcher.")

    val latest = http.get<VersionResponse>("$API/$API_VERSION/bundle/version").data.launchWrapper

    val versionJson = try {
        VersionJson.of(File(path, "versions/$mcversion/$mcversion.json"))
    } catch (e: IllegalArgumentException) {
        InstallerGui.err(e.message ?: "No error message provided.")
        return
    }

    if (versionJson.id.endsWith("-bundle")) {
        InstallerGui.err("This is already a bundle installation. Please select the original version!")
        return
    }

    versionJson.id = versionJson.id + "-bundle"
    versionJson.addGameArgument("--bundleMainClass", versionJson.mainClass)
    versionJson.mainClass = "org.bundleproject.launchwrapper.MainKt"
    versionJson.addLibrary(VersionLibrary(name = "org.bundleproject:launchwrapper:$latest", url = "https://jitpack.io/org/bundleproject/launchwrapper/$latest/launchwrapper-$latest.jar", path = "org/bundleproject/launchwrapper/$latest/launchwrapper-$latest.jar", sha1 = http.get<String>("https://jitpack.io/org/bundleproject/launchwrapper/$latest/launchwrapper-$latest.jar.sha1"), size = http.get<HttpResponse>("https://jitpack.io/org/bundleproject/launchwrapper/$latest/launchwrapper-$latest.jar").contentLength()!!.toInt()))

    val versionJsonFile = File(path, "versions/$mcversion-bundle/$mcversion-bundle.json")
    versionJsonFile.parentFile.mkdir()
    versionJson.save(versionJsonFile)

    val jar = File(path, "versions/$mcversion/$mcversion.jar")
    if (jar.exists()) jar.copyTo(File(path, "versions/$mcversion-bundle/$mcversion-bundle.jar"))

    val availableLaunchers = LauncherProfileTypes.findAvailableTypes(path)
    val selectedLauncher = if (availableLaunchers.size > 1) {
        val option = JOptionPane.showOptionDialog(InstallerGui, "There are multiple Minecraft launchers available! Which one would you like to use?", "Bundle Installer", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, availableLaunchers.map { it.friendlyName }.toTypedArray(), availableLaunchers.first().friendlyName)
        availableLaunchers[option]
    } else {
        availableLaunchers.first()
    }

    val launcherProfilesJson = LauncherProfilesJson(File(path, selectedLauncher.profileName))
    launcherProfilesJson.addProfile(
        "bundle-$mcversion",
        LauncherProfile(
            getCurrentTime(),
            "data:image/png;base64,${getBase64(getResource("/bundle.png").readBytes())}",
            getCurrentTime(),
            versionJson.id,
            "Bundle Launchwrapper $latest for $mcversion"
        )
    )
    launcherProfilesJson.save()

    InstallerGui.success("Successfully installed Bundle")
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

    val noUpdate by parser.flagging(
        "--no-update",
        help = "Prevent installer from checking for updates."
    )
}