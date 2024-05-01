package dev.toast.library.extensions

import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.io.Serializable


fun <T : Serializable> Set<T>.toBytes(): ByteArray {
    val baos = ByteArrayOutputStream()
    ObjectOutputStream(baos).use { oos ->
        oos.writeObject(this as Serializable)
    }
    return baos.toByteArray()
}

fun <K : Serializable, V : Serializable> Map<K, V>.toBytes(): ByteArray {
    val baos = ByteArrayOutputStream()
    ObjectOutputStream(baos).use { oos ->
        oos.writeObject(this as Serializable) // Cast to Serializable, assuming all keys and values are Serializable
    }
    return baos.toByteArray()
}


