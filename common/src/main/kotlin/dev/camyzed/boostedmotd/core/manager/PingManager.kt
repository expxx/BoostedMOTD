package dev.camyzed.boostedmotd.core.manager

import dev.camyzed.boostedmotd.core.Core
import dev.camyzed.boostedmotd.core.dto.Config
import dev.camyzed.boostedmotd.core.dto.LinesConfig
import dev.camyzed.boostedmotd.core.dto.PingResponse
import dev.camyzed.boostedmotd.core.dto.ServerConfig
import java.nio.file.Path
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.io.path.Path

class PingManager(
    private val core: Core,
    private val dataDir: Path,
    private val logger: Logger
) {

    val NO_MOTD_FOUND = PingResponse("&4No MOTD Found", "", null, listOf())

    fun onPingReceive(hostAddress: String): PingResponse {
        val config = core.getConfig()
        val server: ServerConfig = config.servers.filter { it.key == hostAddress }.firstNotNullOfOrNull { it.value }
            ?: config.servers["default"] ?: return NO_MOTD_FOUND

        val randomMotdSelection = mutableListOf<LinesConfig>()
        server.motds.forEach { motd ->
            repeat(motd.weight) {
                randomMotdSelection.add(motd)
            }
        }

        val selection = randomMotdSelection.randomOrNull() ?: return NO_MOTD_FOUND
        if (config.debug) logger.info("Received ping from $hostAddress. Found ${randomMotdSelection.size} MOTD(s) to select from.")
        if (config.debug) logger.info("Selected MOTD: $selection")
        val resp = PingResponse(
            line1 = server.forceLine1?.replace("{selection}", selection.line1 ?: "")
                ?: selection.line1
                ?: "",
            line2 = server.forceLine2?.replace("{selection}", selection.line2 ?: "")
                ?: selection.line2
                ?: "",
            icon = server.icon?.let { dataDir.resolve(it) },
            hover = selection.hover ?: server.hover ?: listOf()
        )
        if (config.debug) logger.info("Constructed PingResponse: $resp")
        return resp
    }

}