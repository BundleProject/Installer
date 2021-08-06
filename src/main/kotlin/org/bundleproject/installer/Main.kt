package org.bundleproject.installer

import com.google.gson.*
import com.xenomachina.argparser.ArgParser
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.bundleproject.installer.utils.*
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Files

fun main(args: Array<String>) {
    ArgParser(args).parseInto(::InstallerParams).run {
        if (silent) {
            install(this)
            return@run
        }
    }
}

fun install(params: InstallerParams) {
    // default launcher
    if (!params.multimc) {
        val latest = JsonParser.parseString(runBlocking {
            http.get<String>("$API/$API_VERSION/bundle/version")
        }).asJsonObject.get("updater").asString

        val versionJson = File(params.path, "versions/${params.mcversion}/${params.mcversion}.json")
        val json = InputStreamReader(versionJson.inputStream()).use {
            JsonParser.parseReader(it).asJsonObject
        }

        val libs = json.getAsJsonArray("libraries")

        val bundleLib = JsonObject()
        bundleLib.add("name", JsonPrimitive("com.github.BundleProject:Bundle:$latest"))
        bundleLib.add("url", JsonPrimitive("https://jitpack.io/"))
        libs.add(bundleLib)
        json.add("libraries", libs)

        Files.write(versionJson.toPath(), GsonBuilder().setPrettyPrinting().create().toJson(json).toByteArray())
    }
}

class InstallerParams(parser: ArgParser) {
    val silent by parser.flagging(
        "-s", "--silent",
        help = "Do not display a gui and install automatically."
    )

    var multimc = parser.flagging(
        "-mmc", "--multimc",
        help = "Installs into MultiMC rather than a normal minecraft installation."
    ).value

    var path = File(parser.storing(
        "-p", "--path",
        help = "Install directory."
    ).value)

    var mcversion = parser.storing(
        "-v", "--version",
        help = "Version profile to install bundle to."
    ).value

}