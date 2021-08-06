package org.bundleproject.installer.utils

import io.ktor.client.*
import io.ktor.client.engine.cio.*

val http = HttpClient(CIO)