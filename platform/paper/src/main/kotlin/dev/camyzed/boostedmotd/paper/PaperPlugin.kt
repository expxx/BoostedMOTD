package dev.camyzed.boostedmotd.paper

import com.destroystokyo.paper.event.server.PaperServerListPingEvent
import dev.camyzed.boostedmotd.core.Core
import dev.faststats.bukkit.BukkitMetrics
import dev.faststats.core.ErrorTracker
import dev.faststats.core.Metrics
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerListPingEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.CachedServerIcon
import java.util.UUID
import java.util.logging.Level
import java.util.logging.LogRecord
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

class PaperPlugin : JavaPlugin(), Listener {
    private val miniMessage = MiniMessage.miniMessage()
    private var core: Core? = null
    private var metrics: Metrics? = null

    override fun onEnable() {
        val javaLogger = object : java.util.logging.Logger("BoostedMOTD", null) {
            override fun log(record: LogRecord?) {
                if (record != null) {
                    when (record.level) {
                        Level.SEVERE -> logger.severe(record.message)
                        Level.WARNING -> logger.warning(record.message)
                        Level.INFO -> logger.info(record.message)
                        else -> logger.fine(record.message)
                    }
                }
            }
        }
        javaLogger.level = Level.FINEST
        if (!dataPath.exists()) dataPath.createDirectories()
        this.core = Core(dataPath, javaLogger)

        this.metrics = BukkitMetrics.factory()
            .errorTracker(ErrorTracker.contextAware())
            .token("eadc767f6c4e5c345551e238283179f1")
            .create(this)

        this.server.pluginManager.registerEvents(this, this)
    }

    override fun onDisable() {
        metrics?.shutdown()
        core?.deconstruct()
    }

    @EventHandler
    fun onPing(event: PaperServerListPingEvent) {
        val pingManager = core?.getPingManager() ?: return
        val serverAddress = event.hostname ?: return

        val ping = pingManager.onPingReceive(serverAddress)
        val l1 = miniMessage.deserialize(ping.line1)
        val l2 = miniMessage.deserialize(ping.line2)

        val hover = ping.hover
            .map { miniMessage.deserialize(it) }
            .map { LegacyComponentSerializer.legacySection().serialize(it) }
            .map { PaperServerListPingEvent.ListedPlayerInfo(it, UUID.randomUUID()) }

        event.listedPlayers.clear()
        event.listedPlayers.addAll(hover)
        event.motd(l1.appendNewline().append(l2))
        ping.icon?.let { event.serverIcon = server.loadServerIcon(it.toFile()) }
    }
}
