package org.bundleproject.installer.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import org.bundleproject.installer.gui.InstallerGui
import java.awt.image.BufferedImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO

val http = HttpClient(Apache)
val gson: Gson = GsonBuilder().setPrettyPrinting().create()

/**
 * Gets a buffered image from the resources of
 * the jar
 *
 * @since 0.0.1
 */
fun getResourceImage(path: String): BufferedImage =
    ImageIO.read(InstallerGui::class.java.getResource(path))

/**
 * Gets the launcher minecraft directory
 * or null if it doesn't exist
 *
 * @since 0.0.1
 */
fun getDefaultMinecraftDir(): File? {
    val dir = when (OSChecker.detectedOS) {
        OSType.Windows, OSType.Unknown -> File(System.getenv("APPDATA"), ".minecraft")
        OSType.Mac -> File(System.getProperty("user.home"), "Library/Application Support/Minecraft")
        OSType.Linux -> File(System.getProperty("user.home"), ".minecraft")
    }
    if (!dir.exists()) return null
    return dir
}

/**
 * Gets the instance folder in MultiMC
 * from the user specified directory
 *
 * @since 0.0.1
 */
fun getMultiMCInstanceFolder(dir: File): File? {
    if (!dir.exists()) return null

    var parent: File? = dir
    while (parent != null) {
        if (parent.name.lowercase() == "multimc") return File(parent, "instances")
        parent = parent.parentFile
    }
    return null
}

/**
 * Get a list of all MultiMC instance names
 *
 * @since 0.0.1
 */
fun getMultiMCInstanceNames(dir: File): List<String>? {
    val instanceFolder = getMultiMCInstanceFolder(dir) ?: return null

    return instanceFolder.listFiles()!!
        .filter { it.isDirectory }
        .filter { File(it, ".minecraft").exists() }
        .filter { File(it, "instance.cfg").exists() }
        .map { file -> file.name }
}

/**
 * Get all versions in either a MultiMC or
 * official context
 *
 * @since 0.0.1
 */
fun getVersionsForFolder(dir: File): List<String>? {
    val multimc = getMultiMCInstanceNames(dir)
    if (multimc != null) return multimc

    return (File(dir, "versions").listFiles() ?: return null)
        .map { it.name }
}

/**
 * Gets the current time, formatted for Minecraft
 *
 * @since 0.0.1
 */
fun getCurrentTime(): String =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(Calendar.getInstance().time)

