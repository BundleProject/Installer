package org.bundleproject.installer.api.response

import org.bundleproject.installer.api.data.VersionData

data class VersionResponse(val success: Boolean, val data: VersionData)