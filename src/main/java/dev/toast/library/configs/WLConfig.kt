package dev.toast.library.configs

import java.io.Serializable

enum class ConfigType {
    BYTES,
    YAML,

    ;
}

interface WLConfig : Serializable
