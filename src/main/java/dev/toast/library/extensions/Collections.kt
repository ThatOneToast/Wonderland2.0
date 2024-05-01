package dev.toast.library.extensions

import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.*


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


fun combineUUIDs(uuid1: UUID, uuid2: UUID): UUID {
    val byteBuffer = ByteBuffer.allocate(32)
    byteBuffer.putLong(uuid1.mostSignificantBits)
    byteBuffer.putLong(uuid1.leastSignificantBits)
    byteBuffer.putLong(uuid2.mostSignificantBits)
    byteBuffer.putLong(uuid2.leastSignificantBits)
    val combinedBytes = byteBuffer.array()

    val md = MessageDigest.getInstance("SHA-1")
    val sha1Bytes = md.digest(combinedBytes)

    val msb = ByteBuffer.wrap(sha1Bytes, 0, 8).long
    val lsb = ByteBuffer.wrap(sha1Bytes, 8, 8).long

    return UUID(msb, lsb)
}