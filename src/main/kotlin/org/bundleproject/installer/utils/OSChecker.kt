package org.bundleproject.installer.utils

val currentOs: OSType by lazy {
    val osName = System.getProperty("os.name", "generic").lowercase()
    when {
        osName.contains("win") -> OSType.Windows
        osName.contains("mac") || osName.contains("darwin") -> OSType.Mac
        osName.contains("nux") -> OSType.Linux
        else -> OSType.Unknown
    }
}

enum class OSType {
    Windows, Mac, Linux, Unknown
}