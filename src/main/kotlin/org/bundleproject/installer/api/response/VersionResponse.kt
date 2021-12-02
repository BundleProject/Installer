package org.bundleproject.installer.api.response

import org.bundleproject.installer.api.data.VersionsData

data class VersionResponse(val success: Boolean, val data: VersionsData)