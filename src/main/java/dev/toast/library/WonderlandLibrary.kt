package dev.toast.library

import dev.toast.library.commands.libcmds.WonderlandCommand
import dev.toast.library.configs.ConfigManager
import dev.toast.library.configs.WLYamlConfig
import dev.toast.library.extensions.HandleCustomEvents
import dev.toast.library.rpx.combat.DamageSystem
import dev.toast.library.rpx.combat.ManaSystem
import dev.toast.library.utils.CooldownManager
import org.bukkit.GameRule
import org.bukkit.plugin.Plugin
import java.io.File
import java.util.logging.Level

/**
 * Main library class for the WonderlandLibrary, handling the initialization and management
 * of configurations and utilities needed by the plugin.
 */
class WonderlandLibrary {
    // Manager for handling cooldown operations across the plugin.
    val cooldownManager = CooldownManager

    /**
     * Initializes the library with necessary directories and managers.
     *
     * @param plugin The plugin to which this library belongs.
     */
    fun start(plugin: Plugin) {
        instance = plugin
        plugin.dataFolder.mkdirs()
        File(plugin.dataFolder, "Configs").mkdirs()

        configManager = ConfigManager()

        val props: MutableMap<String, Any> = mutableMapOf()
        props["DebugMode"] = false
        props["RPGMode"] = false

        val wlconfig =
            WLYamlConfig(
                "WonderlandOptions",
                plugin.dataFolder.absolutePath,
                true,
                props,
            )
        try {
            configManager.createConfig(wlconfig)
        } catch (e: IllegalArgumentException) {
            getPlugin().logger.log(Level.INFO, "Loading WonderlandOptions...")
        }
        plugin.server.pluginManager.registerEvents(HandleCustomEvents(), plugin)

        val wonderlandOptions = configManager.getConfig("WonderlandOptions") as WLYamlConfig
        val rpgSystem = wonderlandOptions.getProperty("RPGMode") as Boolean

        if (rpgSystem) {
            damageSystem = DamageSystem()
            getPlugin().server.worlds.forEach { world ->
                world.setGameRule(GameRule.NATURAL_REGENERATION, false)
            }
            plugin.server.pluginManager.registerEvents(DamageSystem(), getPlugin())
            plugin.server.pluginManager.registerEvents(ManaSystem, getPlugin())
        }

        WonderlandCommand()
    }

    /**
     * Performs any necessary cleanup operations upon termination of the plugin.
     */
    fun terminate() {
        configManager.terminate()
    }

    fun getConfigManager(): ConfigManager {
        return configManager
    }

    companion object {
        // Static instance of the plugin, used for global access within the package.
        @JvmStatic
        private lateinit var instance: Plugin

        private lateinit var configManager: ConfigManager

        @JvmStatic
        private lateinit var damageSystem: DamageSystem

        /**
         * Gets the plugin instance associated with this library.
         *
         * @return The currently initialized Plugin instance.
         * @throws IllegalStateException if the library is accessed before being properly initialized.
         */
        @JvmStatic
        fun getPlugin(): Plugin {
            if (::instance.isInitialized) {
                return instance
            } else {
                throw IllegalStateException("WonderlandLibrary is not initialized!")
            }
        }

        /**
         * Retrieves the configuration manager for accessing and managing plugin configurations.
         *
         * @return The active ConfigManager instance.
         */
        fun getConfigManager(): ConfigManager = configManager

        @JvmStatic
        fun getDamageSystem(): DamageSystem = damageSystem

        @JvmStatic
        fun config(): WLYamlConfig {
            return configManager.getConfig("WonderlandOptions") as WLYamlConfig
        }
    }
}
