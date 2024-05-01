package dev.toast.library

import dev.toast.library.configs.ConfigManager
import dev.toast.library.extensions.HandleCustomEvents
import dev.toast.library.utils.CooldownManager
import org.bukkit.plugin.Plugin
import java.io.File

/**
 * Main library class for the WonderlandLibrary, handling the initialization and management
 * of configurations and utilities needed by the plugin.
 */
class WonderlandLibrary {
    // Manager for handling configurations within the plugin.
    private lateinit var configManager: ConfigManager

    // Manager for handling cooldown operations across the plugin.
    val cooldownManager = CooldownManager

    /**
     * Initializes the library with necessary directories and managers.
     *
     * @param plugin The plugin to which this library belongs.
     */
    fun start(plugin: Plugin) {
        instance = plugin
        // Ensure the plugin's data folder exists, creating if not.
        plugin.dataFolder.mkdirs()
        // Create a directory specifically for configurations.
        File(plugin.dataFolder, "Configs").mkdirs()
        // Initialize the configuration manager.
        configManager = ConfigManager()

        plugin.server.pluginManager.registerEvents(HandleCustomEvents(), plugin)

    }

    /**
     * Performs any necessary cleanup operations upon termination of the plugin.
     */
    fun terminate() {
        // Implementation can include shutting down managers or saving data.
    }

    /**
     * Retrieves the configuration manager for accessing and managing plugin configurations.
     *
     * @return The active ConfigManager instance.
     */
    fun getConfigManager(): ConfigManager {
        return configManager
    }

    companion object {
        // Static instance of the plugin, used for global access within the package.
        @JvmStatic
        private lateinit var instance: Plugin

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
    }
}
