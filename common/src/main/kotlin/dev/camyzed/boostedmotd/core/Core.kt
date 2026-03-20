package dev.camyzed.boostedmotd.core

import com.charleskorn.kaml.Yaml
import dev.camyzed.boostedmotd.core.dto.Config
import dev.camyzed.boostedmotd.core.manager.PingManager
import engineering.swat.watch.ActiveWatch
import engineering.swat.watch.Approximation
import engineering.swat.watch.Watch
import engineering.swat.watch.WatchScope
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.logging.Logger

class Core(
    private val dataDir: Path,
    private val logger: Logger
) {
    private val root = Paths.get("/home/container")

    private var config: Config
    private val configWatch: ActiveWatch

    private val pingManager: PingManager

    private val configPath: Path = dataDir.resolve("config.yml")

    init {
        logger.info("Initializing Core")

        logger.info("Initial configuration load")
        this.config = this.reloadConfig()

        val configRootPath = root.resolve(configPath)
        logger.info("Setting up config file watcher to $configRootPath (ia: ${configRootPath.isAbsolute})")
        val watcherSetup = Watch.build(configRootPath, WatchScope.PATH_ONLY)
            .onOverflow(Approximation.DIFF)
            .on {
                logger.info("Config change detected! Reloadin'")
                this.config = this.reloadConfig()
            }
        configWatch = watcherSetup.start()

        logger.info("Creating ping manager")
        pingManager = PingManager(this, dataDir, logger)
    }

    fun deconstruct() {
        logger.info("Deconstructing Core")
        configWatch.close()
    }

    fun reloadConfig(): Config {
        logger.info("Reloading config...")
        if (!configPath.toFile().exists()) {
            dataDir.toFile().mkdirs()
            logger.warning("Config file does not exist. Creating default config.")
            val defaultConfig = this::class.java.getResourceAsStream("/config.yml")
            Files.write(configPath, defaultConfig.readAllBytes())
        }
        val cfg = Yaml.default.decodeFromString(
            Config.serializer(),
            configPath.toFile().readText()
        )
        logger.info("Loaded config file.")
        if (cfg.debug) logger.info(cfg.toString())
        return cfg
    }

    fun getConfig(): Config {
        return config
    }

    fun getPingManager(): PingManager {
        return pingManager
    }

}