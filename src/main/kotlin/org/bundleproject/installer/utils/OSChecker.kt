package org.bundleproject.installer.utils

object OSChecker {

    val detectedOS: OSType = getOperatingSystemType()

    private fun getOperatingSystemType(): OSType {
        return with (System.getProperty("os.name", "generic").lowercase()) {
            when {
                contains("win") -> OSType.Windows
                contains("mac") || contains("darwin") -> OSType.Mac
                contains("nux") -> OSType.Linux
                else -> OSType.Unknown
            }
        }
    }

}

enum class OSType {
    Windows, Mac, Linux, Unknown
}