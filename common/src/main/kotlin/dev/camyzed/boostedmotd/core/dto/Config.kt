package dev.camyzed.boostedmotd.core.dto

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val debug: Boolean = false,
    val redisbungee: Boolean, // should hook to redisbungee for player count
    val servers: Map<String, ServerConfig> // ip to config
)

@Serializable
data class ServerConfig(
    val motds: List<LinesConfig>,

    val icon: String? = null,
    val hover: List<String>? = null,
    val forceLine1: String? = null,
    val forceLine2: String? = null,
    val pingPassthrough: Boolean = false
)

@Serializable
data class LinesConfig(
    val line1: String? = "",
    val line2: String? = "",
    val hover: List<String>? = null,
    val weight: Int = 1
)