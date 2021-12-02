package org.bundleproject.installer.utils

import org.bundleproject.libversion.Version

const val API = "https://api.bundleproject.org"
const val API_VERSION = "v1"

val INSTALLER_VERSION = Version.of("__GRADLE_VERSION__")
const val PRERELEASE = true