package dev.toast.library.configs

import java.io.Serializable

enum class ConfigType {
    BYTES,
    YAML,

    ;
}

interface WLConfig : Serializable {

    fun getExtension(): String
    fun getName(): String
    fun getFullFileName(): String
    fun setName(string: String)
    fun getPathIncludingExtension(): String

}