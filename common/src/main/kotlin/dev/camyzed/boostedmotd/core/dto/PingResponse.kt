package dev.camyzed.boostedmotd.core.dto

import java.nio.file.Path

data class PingResponse(
    val line1: String,
    val line2: String,
    val icon: Path?,
    val hover: List<String>
)
