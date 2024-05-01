package dev.toast.library.configs

import dev.toast.library.WonderlandLibrary
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream

/**
 * Handles the serialization of configuration data into bytes, supporting binary storage of configuration states.
 * This class allows configurations to be saved in a binary format and provides functionality to manipulate
 * and retrieve properties dynamically.
 *
 * @property name The name of the configuration, used as the filename.
 * @property folder The directory under which the binary configuration file will be stored.
 * @property quickAccess Boolean indicating whether this configuration should be kept readily accessible in memory.
 * @property defaultProperties Default set of properties to initialize the configuration with.
 */
open class WLBytesConfig(
    val name: String,
    open val folder: String,
    open val quickAccess: Boolean,
    private val defaultProperties: Map<String, Any> = mapOf()
) : WLConfig {
    private var properties: MutableMap<String, Any> = defaultProperties.toMutableMap()

    // Path where the binary configuration file is stored.
    val fullPath = "${WonderlandLibrary.getPlugin().dataFolder.absolutePath}/Configs/$name.bit"
    // Filename of the binary configuration file.
    val fullFileName = "$name.bit"

    init {
        // Initialize properties from the default map.
        properties.putAll(defaultProperties)
    }

    /**
     * Retrieves a property from the configuration by traversing nested keys.
     *
     * @param propertyName Dot-separated path to the property in the nested map structure.
     * @return The value associated with the specified property name or null if it does not exist.
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
     * @param name The key corresponding to the property to be set.
     * @param value The new value to be assigned to the key.
     */
    fun setProperty(name: String, value: Any) {
        properties[name] = value
    }

    /**
     * Replaces all current properties with a new set of properties.
     *
     * @param props A map containing the new properties.
     */
    fun replaceProperties(props: Map<String, Any>) {
        properties = props.toMutableMap()
    }

    /**
     * Resets the properties to their default values.
     */
    fun resetToDefault() {
        properties.clear()
        properties.putAll(defaultProperties)
    }

    /**
     * Serializes the current configuration state to a byte array.
     *
     * @return A byte array representing the serialized state of the configuration.
     */
    fun toBytes(): ByteArray {
        ByteArrayOutputStream().use { bos ->
            ObjectOutputStream(bos).use { oos ->
                oos.writeObject(this)
                oos.flush()
                return bos.toByteArray()
            }
        }
    }

    companion object {
        /**
         * Deserializes a byte array back into a WLBytesConfig object.
         *
         * @param byteArray The byte array to deserialize.
         * @return An instance of WLBytesConfig initialized from the byte array.
         */
        fun fromBytes(byteArray: ByteArray): WLBytesConfig {
            return ConfigManager.deserializeConfigs(byteArray)
        }
    }
}
