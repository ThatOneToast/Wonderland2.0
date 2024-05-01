package dev.toast.library.configs

import dev.toast.library.WonderlandLibrary
import dev.toast.library.extensions.toBytes
import java.io.ByteArrayInputStream
import java.io.File
import java.io.ObjectInputStream
import java.util.logging.Level

/**
 * Manages the loading, saving, and accessing of configuration files for the plugin.
 * Supports different configuration formats including YAML and binary.
 */
class ConfigManager {
    private val dataPath = WonderlandLibrary.getPlugin().dataFolder.absolutePath
    private val memoryConfigsFile = File("$dataPath/MemoryConfigs.bit")
    private val configsIndexFile = File("$dataPath/index.bit")

    private var memoryConfigs: MutableSet<WLConfig> = mutableSetOf()
    private var indexedConfigs: MutableMap<String, String> = mutableMapOf()

    init {
        initializeConfigFiles()
    }

    /**
     * Initializes configuration files and directories on disk if they do not already exist.
     */
    private fun initializeConfigFiles() {
        if (!memoryConfigsFile.exists()) {
            memoryConfigsFile.createNewFile()
            memoryConfigsFile.writeBytes(memoryConfigs.toBytes())
        }
        if (!configsIndexFile.exists()) {
            configsIndexFile.createNewFile()
            configsIndexFile.writeBytes(indexedConfigs.toBytes())
        }
    }

    /**
     * Reloads a specific configuration by its name, updating the in-memory cache.
     *
     * @param name The name of the configuration to reload.
     */
    fun reloadConfig(name: String) {
        val newConfig = getConfig(name, skipMemCheck = true) as? WLYamlConfig ?: return
        memoryConfigs.removeIf { it is WLYamlConfig && it.name == name }
        memoryConfigs.add(newConfig)
    }

    /**
     * Refreshes the entire cache of configurations from disk.
     */
    fun refreshCaches() {
        memoryConfigs.clear()
        val memConfigs: MutableSet<WLConfig> = deserializeConfigs(memoryConfigsFile.readBytes())
        memoryConfigs.addAll(memConfigs)
    }

    /**
     * Saves all configuration data to disk.
     */
    private fun saveInternals() {
        val memConfig = WLBytesConfig(
            "Cache",
            WonderlandLibrary.getPlugin().dataFolder.absolutePath,
            false
        )
        val indexConfig = WLBytesConfig(
            "Index",
            WonderlandLibrary.getPlugin().dataFolder.absolutePath,
            true
        )
        memoryConfigsFile.writeBytes(memConfig.toBytes())
        configsIndexFile.writeBytes(indexConfig.toBytes())
    }

    /**
     * Retrieves a configuration by name, either from memory or disk.
     *
     * @param name The name of the configuration to retrieve.
     * @param skipMemCheck If true, skips memory check and loads directly from disk.
     * @return The requested configuration object.
     * @throws IllegalStateException if the configuration cannot be found or loaded.
     * @throws NullPointerException if there is no config that exists.
     */
    fun getConfig(name: String, skipMemCheck: Boolean = false): WLConfig {
        if (!skipMemCheck) {
            memoryConfigs.firstOrNull { config ->
                (config is WLYamlConfig || config is WLBytesConfig && config.name == name)
            }?.let { return it }
        }

        for (indexedConfig in indexedConfigs) {
            if (indexedConfig.key == name) {
                val configFile = File(indexedConfig.value)
                if (configFile.endsWith(".bit")) {
                    return WLBytesConfig.fromBytes(configFile.readBytes())
                }
                else if (configFile.endsWith("yml")) {
                    return WLYamlConfig.fromYaml(configFile.readText())
                }
            }
        }

        val configFile: File = WonderlandLibrary.getPlugin().dataFolder.walk()
            .filter { it.isFile }
            .find { it.name == name} ?: throw NullPointerException("$name doesn't exist.")

        if (configFile.endsWith(".bit")) {
            val config = WLBytesConfig.fromBytes(configFile.readBytes())
            indexedConfigs[config.name] = config.fullPath
            return config
        }
        else if (configFile.endsWith(".yml")) {
            val config = WLYamlConfig.fromYaml(configFile.readText())
            indexedConfigs[config.name] = config.fullPath
            return config
        }

        throw NullPointerException("$name doesn't exist.")
    }

    /**
     * Checks if a configuration file exists on disk.
     *
     * @param config The configuration to check.
     * @return True if the file exists, false otherwise.
     */
    fun doIExists(config: WLConfig): Boolean {
        when (config) {
            is WLYamlConfig -> return File(config.fullPath).exists()
            is WLBytesConfig -> return File(config.fullPath).exists()
        }
        return false
    }

    /**
     * Creates and registers a new configuration, saving it to disk.
     *
     * @param config The configuration to create.
     * @throws IllegalArgumentException if the configuration already exists.
     */
    fun createConfig(config: WLConfig) {
        when (config) {

            is WLYamlConfig -> {
                if (!doIExists(config)) {
                    File(config.fullPath).apply {
                        createNewFile()
                        writeText(config.toYaml())
                    }
                    if (config.quickAccess) {
                        memoryConfigs.add(config)
                    }
                    indexedConfigs[config.name] = config.fullPath
                } else {
                    throw IllegalArgumentException("This config already exists")
                }
            }

            is WLBytesConfig -> {
                if (!doIExists(config)) {
                    File(config.fullPath).apply {
                        createNewFile()
                        writeBytes(config.toBytes())
                    }
                    if (config.quickAccess) {
                        memoryConfigs.add(config)
                    }
                    indexedConfigs[config.name] = config.fullPath
                } else {
                    throw IllegalArgumentException("This config already exists")
                }
            }

            else -> {
                WonderlandLibrary.getPlugin().logger.log(Level.SEVERE, "Unknown config type Config: $config")
            }

        }
    }

    /**
     * Performs necessary cleanup and saves all configurations when the plugin is terminated.
     */
    fun terminate() {
        saveInternals()
    }

    /**
     * Initializes the configuration manager by loading configurations from disk into memory.
     */
    fun start() {
        val configSet: MutableSet<WLConfig> = deserializeConfigs(memoryConfigsFile.readBytes())
        val configsMap: MutableMap<String, String> = deserializeConfigs(configsIndexFile.readBytes())

        memoryConfigs = configSet
        indexedConfigs = configsMap

    }

    companion object {
        /**
         * Generic method to deserialize configuration data from bytes into the specified type.
         *
         * @param bytes The byte array to deserialize.
         * @return An instance of the specified type, containing the configuration data.
         */
        inline fun <reified T> deserializeConfigs(bytes: ByteArray): T {
            ByteArrayInputStream(bytes).use { bais ->
                ObjectInputStream(bais).use { ois ->
                    return ois.readObject() as T
                }
            }
        }
    }
}
