package org.bundleproject.installer

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.xenomachina.argparser.ArgParser
import io.ktor.client.request.*
import org.bundleproject.installer.utils.*
import java.io.File
import java.io.InputStreamReader

suspend fun main(args: Array<String>) {
    ArgParser(args).parseInto(::InstallerParams).run {
        if (silent) {
            install(this)
            return@run
        }
    }
}

suspend fun install(params: InstallerParams) {
    // default launcher
    if (!params.multimc) {
        val latest = JsonParser.parseString(
            http.get<String>("$API/$API_VERSION/bundle/version")
        ).asJsonObject.get("updater").asString

        InputStreamReader(File(params.path, "versions/${params.mcversion}/${params.mcversion}.json")
            .inputStream()).use {
            val json = JsonParser.parseReader(it).asJsonObject
            val libs = json.getAsJsonArray("libraries")

            val bundleLib = JsonObject()
            bundleLib.add("name", JsonPrimitive("com.github.BundleProject:Bundle:$latest"))
            libs.add(JsonObject())
        }
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