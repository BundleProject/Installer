package org.bundleproject.installer.updater

import io.ktor.client.request.*
import org.bundleproject.installer.api.data.VersionData
import org.bundleproject.installer.api.response.VersionResponse
import org.bundleproject.installer.utils.API
import org.bundleproject.installer.utils.API_VERSION
import org.bundleproject.installer.utils.PRERELEASE
import org.bundleproject.installer.utils.http

suspend fun getLatestUpdate(): String =
    http.get<VersionResponse>("$API/$API_VERSION/bundle/version").data.let { if (PRERELEASE) it.prerelease else it.release }.installer


