package org.bundleproject.installer.utils.launcher.version.impl

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import org.bundleproject.installer.utils.gson
import org.bundleproject.installer.utils.launcher.version.VersionJson
import org.bundleproject.installer.utils.launcher.version.VersionLibrary
import java.io.File

class Schema2021(private val file: File) : VersionJson {
    private val json: JsonObject = JsonParser.parseString(file.readText()).asJsonObject

    override var mainClass: String
        get() = json.get("mainClass").asString
        set(value) { json.add("mainClass", JsonPrimitive(value)) }

    override var id: String
        get() = json.get("id").asString
        set(value) { json.add("id", JsonPrimitive(value)) }

    override fun addGameArgument(vararg argument: String) {
        val gameArguments = json.getAsJsonObject("arguments").getAsJsonArray("game")

        for (arg in argument) {
            gameArguments.add(arg)
        }
    }

    override fun addLibrary(library: VersionLibrary) {
        val libJson = JsonObject()
        libJson.addProperty("name", library.name)

        val downloads = JsonObject()
        val artifact = JsonObject()
        artifact.addProperty("url", library.url)
        artifact.addProperty("path", library.path)
        library.sha1?.let { artifact.addProperty("sha1", it) }
        library.size?.let { artifact.addProperty("size", it) }
        downloads.add("artifact", artifact)
        libJson.add("downloads", downloads)

        json.getAsJsonArray("libraries").add(libJson)
    }

    override fun save(file: File) {
        file.writeText(gson.toJson(json))
    }
}