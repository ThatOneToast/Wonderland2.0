package dev.toast.library.configs

import dev.toast.library.WonderlandLibrary
import org.yaml.snakeyaml.Yaml
import java.io.StringReader
import java.io.StringWriter

/**
 * Class that manages configuration files in YAML format for a Minecraft plugin.
 *
 * @property name The name of the configuration file.
 * @property folder The directory where the configuration file is stored.
 * @property quickAccess Determines whether this configuration should be readily accessible in memory.
 * @property defaultProperties Default key-value pairs that initialize the configuration properties.
 */
open class WLYamlConfig(
    private val cname: String,
    open val folder: String,
    open val quickAccess: Boolean,
    private val defaultProperties: Map<String, Any> = mapOf()
) : WLConfig {
    private var properties: MutableMap<String, Any> = defaultProperties.toMutableMap()
    private var name = cname

    // Full path to the configuration file, combining the folder and file name.
    val fullPath = "${WonderlandLibrary.getPlugin().dataFolder.absolutePath}/Configs/$name.yml"
    // Full name of the YAML file.
    private val fullFileName = "$name.yml"

    init {
        // Initialize properties with default values.
        properties.putAll(defaultProperties)
    }

    /**
     * Retrieves a property from the configuration by traversing nested keys.
     *
     * @param propertyName The dot-separated keys leading to the desired value.
     * @return The value associated with the specified keys or null if not found or not reachable.
     */
    fun getProperty(propertyName: String): Any? {
        val keys = propertyName.split(".")
        var currentMap: Any? = properties

        for (key in keys) {
            currentMap = if (currentMap is Map<*, *>) currentMap[key] else return null
        }
        return currentMap
    }

    /**
     * Sets or updates a property in the configuration.
     *
     * @param name The key associated with the value to be set.
     * @param value The value to be stored in the configuration.
     */
    fun setProperty(name: String, value: Any) {
        properties[name] = value
    }

    /**
     * Replaces all properties with a new set of properties.
     *
     * @param props A map containing the new properties.
     */
    fun replaceProperties(props: Map<String, Any>) {
        properties = props.toMutableMap()
    }

    /**
     * Returns all properties
     */
    fun getProperties(): MutableMap<String, Any> {
        return properties
    }

    /**
     * Resets the properties to their default values as specified at initialization.
     */
    fun resetToDefault() {
        properties.clear()
        properties.putAll(defaultProperties)
    }

    /**
     * Serializes the configuration into a YAML formatted string.
     *
     * @return A string containing the YAML representation of the configuration.
     */
    fun toYaml(): String {
        val yaml = Yaml()
        val writer = StringWriter()
        val yamlMap = mapOf(
            "name" to name,
            "folder" to folder,
            "quickAccess" to quickAccess,
            "properties" to formatYamlValue(properties)
        )
        yaml.dump(yamlMap, writer)
        return writer.toString()
    }

    /**
     * Formats a given value for YAML output, ensuring correct serialization of nested maps and strings.
     *
     * @param value The value to format.
     * @return The formatted value.
     */
    private fun formatYamlValue(value: Any): Any {
        return when (value) {
            is Map<*, *> -> value.mapValues { (_, v) -> formatYamlValue(v!!) }
            is Set<*> -> value.map { formatYamlValue(it!!) }.toSet()
            is List<*> -> value.map { formatYamlValue(it!!) }
            is String -> "\"$value\""
            else -> value
        }
    }

    companion object {
        /**
         * Creates a WLYamlConfig object from a YAML string.
         *
         * @param yamlString The YAML string to parse.
         * @return A new WLYamlConfig object initialized with data from the YAML string.
         */
        fun fromYaml(yamlString: String): WLYamlConfig {
            val yaml = Yaml()
            val data: Map<String, Any> = yaml.load(StringReader(yamlString))
            return WLYamlConfig(
                data["name"] as? String ?: throw IllegalArgumentException("Folder is missing name"),
                data["folder"] as? String ?: throw IllegalArgumentException("Folder is missing"),
                data["quickAccess"] as? Boolean ?: throw IllegalArgumentException("QuickAccess is missing"),
                data["properties"] as? Map<String, Any> ?: throw IllegalArgumentException("Properties are missing")
            )
        }
    }

    override fun getExtension(): String {
        return ".yml"
    }

    final override fun getName(): String {
        return cname
    }

    override fun getFullFileName(): String {
        return fullFileName
    }

    override fun setName(string: String) {
        name = string
    }

    override fun getPathIncludingExtension(): String {
        return fullPath
    }
}
