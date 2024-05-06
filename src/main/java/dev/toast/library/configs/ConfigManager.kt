package dev.toast.library.configs

import dev.toast.library.WonderlandLibrary
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
    private val configsIndexFile = File("$dataPath/Index.bit")

    private var memoryConfigs: MutableSet<WLConfig> = mutableSetOf()
    private var indexedConfigs: MutableMap<String, String> = mutableMapOf()

    /**
     * Reloads a specific configuration by its name, updating the in-memory cache.
     *
     * @param name The name of the configuration to reload.
     */
    fun reloadConfig(name: String) {
        val newConfig = getConfig(name, skipMemCheck = true) as? WLYamlConfig ?: return
        memoryConfigs.removeIf { it is WLYamlConfig && it.getName() == name }
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
     * Retrieves a configuration by name, either from memory or disk.
     *
     * @param name The name of the configuration to retrieve.
     * @param skipMemCheck If true, skips memory check and loads directly from disk.
     * @return The requested configuration object.
     * @throws IllegalStateException if the configuration cannot be found or loaded.
     * @throws NullPointerException if there is no config that exists.
     */
    fun getConfig(
        name: String,
        skipMemCheck: Boolean = false,
    ): WLConfig {
        if (!skipMemCheck) {
            memoryConfigs.firstOrNull { config ->
                (config is WLYamlConfig || config is WLBytesConfig && config.getName() == name)
            }?.let { return it }
        }

        // Try to retrieve the configuration from indexed paths
        indexedConfigs[name]?.let { path ->
            val configFile = File(path)
            return loadConfigFromFile(configFile)
        }

        // Try to find and load the configuration file in the plugin's data folder
        val configFile: File =
            WonderlandLibrary.getPlugin().dataFolder.walk()
                .filter { it.isFile }
                .find { it.nameWithoutExtension == name }
                ?: throw ConfigurationNotFoundException("Configuration file '$name' does not exist.")

        return loadConfigFromFile(configFile)
    }

    private fun loadConfigFromFile(configFile: File): WLConfig {
        return when (configFile.extension) {
            "bit" -> {
                WLBytesConfig.fromBytes(configFile.readBytes()).also {
                    indexedConfigs[it.getName()] = configFile.absolutePath
                }
            }
            "yml" -> {
                WLYamlConfig.fromYaml(configFile.readText()).also {
                    indexedConfigs[it.getName()] = configFile.absolutePath
                }
            }
            else -> {
                throw ConfigurationNotFoundException("Unsupported configuration file format for '${configFile.name}'.")
            }
        }
    }

    class ConfigurationNotFoundException(message: String) : Exception(message)

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
     * Checks if a specific configuration is in the in-memory cache.
     *
     * @param name The name of the configuration to check.
     * @return True if the configuration is in memory, false otherwise.
     */
    fun isConfigInMemory(name: String): Boolean {
        return memoryConfigs.any { config ->
            (config is WLYamlConfig || config is WLBytesConfig) && config.getName() == name
        }
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
                    indexedConfigs[config.getName()] = config.fullPath
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
                    indexedConfigs[config.getName()] = config.fullPath
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

    init {
        initializeConfigFiles()
    }

    /**
     * Initializes configuration files and directories on disk if they do not already exist.
     */
    private fun initializeConfigFiles() {
        if (!memoryConfigsFile.exists()) {
            memoryConfigsFile.createNewFile()
            saveInternals()
        }
        if (!configsIndexFile.exists()) {
            configsIndexFile.createNewFile()
            saveInternals()
        }
    }

    /**
     * Saves all configuration data to disk.
     */
    private fun saveInternals() {
        val memConfig =
            WLBytesConfig(
                "MemoryConfigs",
                WonderlandLibrary.getPlugin().dataFolder.absolutePath,
                false,
                mapOf(Pair("Configs", memoryConfigs)),
            )
        val indexConfig =
            WLBytesConfig(
                "Index",
                WonderlandLibrary.getPlugin().dataFolder.absolutePath,
                true,
                mapOf(Pair("Configs", indexedConfigs)),
            )
        memoryConfigsFile.writeBytes(memConfig.toBytes())
        configsIndexFile.writeBytes(indexConfig.toBytes())
    }

    /**
     * Initializes the configuration manager by loading configurations from disk into memory.
     */
    fun start() {
        try {
            val configSetBytes = memoryConfigsFile.readBytes()
            val configSet: WLBytesConfig = WLBytesConfig.fromBytes(configSetBytes)
            val configsMapBytes = configsIndexFile.readBytes()
            val configsMap: WLBytesConfig = WLBytesConfig.fromBytes(configsMapBytes)

            val memoryConfigsRaw = configSet.getProperty("configs")
            memoryConfigs = memoryConfigsRaw as? MutableSet<WLConfig> ?: mutableSetOf()

            val indexedConfigsPre: MutableList<String> = configsMap.getProperty("configs") as? MutableList<String> ?: mutableListOf()
            val indexMap: MutableMap<String, String> = mutableMapOf()
            for (str: String in indexedConfigsPre) {
                val split = str.split(":")
                indexMap[split[0]] = split[1]
            }

            indexedConfigs = indexMap
        } catch (e: Exception) {
            // Log the error, handle the exception, or rethrow it as necessary
            println("Error initializing configurations: ${e.message}")
        }
    }

    companion object {
        private val allDataFolderFileNames: List<String> =
            WonderlandLibrary.getPlugin().dataFolder.walk()
                .filter { it.isFile }
                .map { it.name }
                .toList()

        /**
         * Returns a list of file names
         */
        fun getAllConfigNames(): List<String> = allDataFolderFileNames

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
