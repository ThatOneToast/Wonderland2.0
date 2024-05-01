package dev.toast.library.utils

import java.util.*

object CooldownManager {

    private val cooldowns: MutableMap<UUID, Long> = mutableMapOf()

    /**
     * @param uuid of the thing you wish to check against cooldown
     * @param time is the time you wish the cooldown to end.
     */
    fun applyCooldown(uuid: UUID, time: Int) {
        cooldowns[uuid] = time.toLong()
    }

    /**
     * Checks if the object is still on cooldown, returns true if its on cooldown.
     * @param uuid of the thing to check
     * @return true or false
     */
    fun isOnCooldown(uuid: UUID): Boolean {
        val currentTime: Long = System.currentTimeMillis()

        if (!cooldowns.containsKey(uuid)) return false

        if (cooldowns.get(uuid)!! <= currentTime) {
            cooldowns.remove(uuid)
            return false
        }

        return true
    }

    /**
     * @param uuid
     * @param time
     */
    fun updateCooldown(uuid: UUID, time: Long) {
        cooldowns[uuid] = time
    }

    /**
     * @param uuid
     * @param time
     * @throws IllegalStateException if the uuid isnt on cooldown.
     */
    fun subtractTime(uuid: UUID, time: Long) {
        if (!cooldowns.containsKey(uuid)) throw IllegalStateException("$uuid isn't on cooldown.")
        cooldowns[uuid] = cooldowns.get(uuid)!! - time
    }

    /**
     * @param uuid
     * @param time
     * @throw IllegalStateException if the uuid isnt on cooldown.
     */
    fun addTime(uuid: UUID, time: Long) {
        if (!cooldowns.containsKey(uuid)) throw IllegalStateException("$uuid isn't on cooldown.")
        cooldowns[uuid] = cooldowns.get(uuid)!! + time
    }

    /**
     * @param uuid
     */
    fun getCooldownTimeInSec(uuid: UUID): Int {
        return ((cooldowns[uuid]!! - System.currentTimeMillis()) / 1000).toInt()
    }

    /**
     * Returns the minute and second left on the cooldown.
     * @param uuid
     * @return a pair. First is minute, Second is seconds
     */
    fun getCooldownTimeInMin(uuid: UUID): Pair<Int, Int> {
        val cooldownTimeInSec = getCooldownTimeInSec(uuid)
        val minutes = cooldownTimeInSec / 60
        val seconds = cooldownTimeInSec % 60
        return Pair(minutes, seconds)
    }

    /**
     * Returns the hour, minute, and seconds.
     * @param uuid
     * @returns Pair of Hour, Minute, Second
     */
    fun getCooldownTimeinHour(uuid: UUID): Triple<Int, Int, Int> {
        val cooldownTimeInSec: Int = getCooldownTimeInSec(uuid)
        val hours: Int = cooldownTimeInSec / 3600
        val minutes: Int = (cooldownTimeInSec % 3600) / 60
        val seconds: Int = (cooldownTimeInSec % 3600) % 60
        return Triple(hours, minutes, seconds)
    }


}