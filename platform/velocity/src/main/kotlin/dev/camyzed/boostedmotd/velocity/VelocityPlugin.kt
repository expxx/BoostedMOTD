package dev.camyzed.boostedmotd.velocity

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyPingEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.ServerPing
import com.velocitypowered.api.util.Favicon
import dev.camyzed.boostedmotd.core.Core
import dev.faststats.core.ErrorTracker
import dev.faststats.core.Metrics
import dev.faststats.velocity.VelocityMetrics
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.slf4j.Logger
import java.nio.file.Path
import java.util.UUID
import java.util.logging.Level
import java.util.logging.LogRecord
import kotlin.jvm.optionals.getOrNull

@Plugin(
    id = "boostedmotd",
    name = "BoostedMOTD",
    version = "1.0.0",
    description = "A plugin to boost your MOTD with dynamic content and custom icons.",
    authors = ["camyzed"]
)
class VelocityPlugin @Inject constructor(
    private val logger: Logger,
    private val server: ProxyServer,
    @DataDirectory dataDirectory: Path,
    private val metricsFactory: VelocityMetrics.Factory
) {
    private val core: Core
    private val miniMessage = MiniMessage.miniMessage()
    private var metrics: Metrics? = null

    init {

        val javaLogger = object : java.util.logging.Logger("BoostedMOTD", null) {
            override fun log(record: LogRecord?) {
                if (record != null) {
                    when (record.level) {
                        Level.SEVERE -> logger.error(record.message)
                        Level.WARNING -> logger.warn(record.message)
                        Level.INFO -> logger.info(record.message)
                        else -> logger.debug(record.message)
                    }
                }
            }
        }
        javaLogger.level = Level.FINEST
        this.core = Core(dataDirectory, javaLogger)
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        this.metrics = metricsFactory
            .errorTracker(ErrorTracker.contextAware())
            .token("eadc767f6c4e5c345551e238283179f1")
            .create(this)
    }

    @Subscribe
    fun onProxyStop(event: ProxyShutdownEvent) {
        metrics?.shutdown()
        core.deconstruct()
    }

    @Subscribe
    fun onPing(event: ProxyPingEvent) {
        val pingManager = core.getPingManager()
        val serverAddress = event.connection.rawVirtualHost.getOrNull() ?: "default"

        val ping = pingManager.onPingReceive(serverAddress)
        val l1 = miniMessage.deserialize(ping.line1)
        val l2 = miniMessage.deserialize(ping.line2)

        val hover = ping.hover
            .map { miniMessage.deserialize(it) }
            .map { LegacyComponentSerializer.legacySection().serialize(it) }
            .map { ServerPing.SamplePlayer(it, UUID.randomUUID()) }

        val pong = event.ping.asBuilder()
            .description(l1.appendNewline().append(l2))
            .samplePlayers(hover)
        ping.icon?.let { pong.favicon(Favicon.create(it)) }

        event.ping = pong.build()
    }

}