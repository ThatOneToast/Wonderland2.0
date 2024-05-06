package dev.toast.library.rpx.combat

import dev.toast.library.rpx.combat
import dev.toast.library.rpx.save
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

class ManaNotInitialized(override val message: String) : Exception(message)

object ManaSystem : Listener {

    private val playerMana: MutableMap<UUID, Triple<Double, Double, Double>> = mutableMapOf()     // PlayerUUID (MaxMana, ManaRegen, CurrentMana)

    @EventHandler(priority = EventPriority.MONITOR)
    private fun playerJoins(event: PlayerJoinEvent) {
        playerMana[event.player.uniqueId] = Triple(event.player.combat.stats.maxMana, event.player.combat.stats.manaRegen, 0.0)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun playerLeave(event: PlayerQuitEvent) {
        val maxMana = playerMana[event.player.uniqueId]!!.first
        val manaRegen = playerMana[event.player.uniqueId]!!.second

        event.player.combat.stats.maxMana = maxMana
        event.player.combat.stats.manaRegen = manaRegen
        event.player.save()
        playerMana.remove(event.player.uniqueId)
    }

    fun setMaxMana(player: Player, mana: Double) {
        val playerCombat = player.combat.combatConfig
        playerCombat.setProperty("maxMana", mana)
        val currentManaRegen = playerMana[player.uniqueId]?.second ?: 0.5
        val currentMana = playerMana[player.uniqueId]?.third ?: 0.0
        playerMana[player.uniqueId] = Triple(mana, currentManaRegen, currentMana)
    }

    fun getMaxMana(player: Player): Double {
        return playerMana[player.uniqueId]?.first ?: 100.0
    }

    fun setManaRegen(player: Player, manaRegen: Double) {
        val playerCombat = player.combat.combatConfig
        playerCombat.setProperty("manaRegen", manaRegen)
        val currentMaxMana = playerMana[player.uniqueId]?.first ?: 100.0
        val currentMana = playerMana[player.uniqueId]?.third ?: 0.0
        playerMana[player.uniqueId] = Triple(currentMaxMana, manaRegen, currentMana)
    }

    fun getManaRegen(player: Player): Double {
        return playerMana[player.uniqueId]?.second ?: 0.5
    }

    fun getMana(player: Player): Double {
        return playerMana[player.uniqueId]?.third ?: 100.0
    }

    fun setMana(player: Player, mana: Double) {
        val currentMaxMana: Double = playerMana[player.uniqueId]?.first ?: 100.0
        val currentManaRegen: Double = playerMana[player.uniqueId]?.second ?: 0.5
        playerMana[player.uniqueId] = Triple(currentMaxMana, currentManaRegen, mana)
    }

    fun canISpend(player: Player, cost: Double): Boolean = getMana(player) >= cost


}