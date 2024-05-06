package dev.toast.library.rpx.combat

import dev.toast.library.WonderlandLibrary
import dev.toast.library.configs.WLYamlConfig
import dev.toast.library.packets.AnimationPackets
import dev.toast.library.rpx.combat
import dev.toast.library.utils.ChatStructure
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import java.io.File
import java.util.*
import kotlin.math.max
import kotlin.math.min

data class PlayerCombatAttr(
    val uuid: UUID,
    var maxHealth: Double,
    var healthRegen: Double,
    var maxMana: Double,
    var manaRegen: Double,
    var armor: Double,
    var race: String,
) {
    private val player =
        Bukkit.getPlayer(uuid)
            ?: throw IllegalArgumentException("$uuid Isn't a valid player UUID")

    fun toProperties(): Map<String, Any> {
        val data =
            mapOf(
                "uuid" to player.uniqueId.toString(),
                "maxHealth" to maxHealth,
                "healthRegen" to healthRegen,
                "maxMana" to maxMana,
                "manaRegen" to manaRegen,
                "armor" to armor,
                "race" to race,
            )

        return data
    }
}

class DamageSystem : Listener {
    private val playerHealth: MutableMap<UUID, Double> = mutableMapOf()
    private val lastDamageTime: MutableMap<UUID, Long> = mutableMapOf()

    private val globalMaxHealth: Double = 20000.0

    private val armorReductionPercentage = 0.00375 // 0.375% per armor point
    private val armorReductionFromStrength = 0.0025 // 0.25% per strength point.

    private val damageIncreaseFromStrength = 0.25 // +0.25 Damage

    private fun startupActions() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(WonderlandLibrary.getPlugin(), {
            Bukkit.getOnlinePlayers().forEach { player ->
                val currentHealth = playerHealth[player.uniqueId] ?: 0.0
                val playerConfig = WonderlandLibrary.getConfigManager().getConfig("Combat-${player.uniqueId}") as WLYamlConfig
                val playerMaxHealth: Double = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue ?: 0.0
                val regenAmount = playerConfig.getProperty("healthRegen") as? Double ?: 2.0

                val newHealth: Double =
                    when {
                        // Add the regeneration amount if it doesn't exceed GLOBAL_MAX_HEALTH
                        (currentHealth + regenAmount) <= globalMaxHealth ||
                            (currentHealth + regenAmount) <= playerMaxHealth
                        -> currentHealth + regenAmount

                        // Cap at GLOBAL_MAX_HEALTH if current health is already over it
                        currentHealth > globalMaxHealth -> globalMaxHealth

                        currentHealth == playerMaxHealth || currentHealth == globalMaxHealth -> -1.0

                        // Otherwise, add 7% of playerMaxHealth
                        else -> currentHealth + (playerMaxHealth * 0.07)
                    }

                if (newHealth == -1.0) return@forEach

                if (currentHealth < playerMaxHealth && currentHealth > 0.0) {
                    if (newHealth >= playerMaxHealth) {
                        playerHealth[player.uniqueId] = playerMaxHealth
                    } else {
                        playerHealth[player.uniqueId] = newHealth
                    }
                }

                if (WonderlandLibrary.config().getProperty("DebugMode") == true) {
                    player.sendMessage(
                        ChatStructure.gradiantColor(
                            "Your new health is: ${newHealth.toInt()}",
                            arrayOf(ChatStructure.AQUA, ChatStructure.GREEN),
                        ),
                    )
                }
            }
        }, 0L, 20L)
    }

    fun getPlayerHealth(player: Player): Double {
        return playerHealth[player.uniqueId] ?: 40.0
    }

    fun setPlayerHealth(
        player: Player,
        amount: Double,
    ) {
        playerHealth[player.uniqueId] = amount
    }

    init {
        startupActions()
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun registerPlayerInCombat(event: PlayerJoinEvent) {
        val attributes = PlayerCombatAttr(event.player.uniqueId, 40.0, 2.0, 100.0, 5.0, 40.0, "Human")
        registerPlayer(attributes)
        playerHealth[event.player.uniqueId] = event.player.health
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun deRegisterPlayerInCombat(event: PlayerQuitEvent) {
        playerHealth.remove(event.player.uniqueId)
    }
    private fun onHealthRegen(event: EntityRegainHealthEvent) {
        if (event.entity !is Player) return
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun handleRevive(event: PlayerRespawnEvent) {
        val playerCombatConfig = WonderlandLibrary.getConfigManager().getConfig("Combat-${event.player.uniqueId}") as WLYamlConfig
        val maxHealth =
            playerCombatConfig.getProperty("maxHealth") as? Double
                ?: 20.0

        playerHealth[event.player.uniqueId] = maxHealth
        event.player.health = maxHealth * 0.7
    }

    private fun checkDelay(player: Player) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastDamageTime.getOrDefault(player.uniqueId, 0L) < 250L) return
        else lastDamageTime[player.uniqueId] = currentTime
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    private fun handleEntityDamage(event: EntityDamageByEntityEvent) {
        event.isCancelled = true

        fun getStrengthDamage(): Double? {
            if (event.damager !is Player) return null
            val damager = event.damager as Player

            return damager.combat.stats.strength * damageIncreaseFromStrength
        }

        fun getDexDamage(): Double? {

            val damager = event.damager
            if (damager !is Arrow) return null
            val shooter = damager.shooter
            if (shooter !is Player) return null

            val inventory = shooter.inventory
            val mainHandItem = inventory.itemInMainHand

            val isUsingBow = mainHandItem.type == Material.BOW
            val isUsingCrossbow = mainHandItem.type == Material.CROSSBOW

            val dexPoints = shooter.combat.stats.dexterity

            if (isUsingBow) {
                val damageIncreaseFromDex: Double = 0.25 * dexPoints
                return damageIncreaseFromDex
            }

            if (isUsingCrossbow) {
                val damageIncreaseFromDex: Double = 0.25 * dexPoints
                return damageIncreaseFromDex
            }

            /*
                Weapon: ( : Complete
                 - + Bow Damage
                 - +1 CrossBow Damage
                )
                Armor: ( : WI
                - Step a little higher +1%
                - 3% Less fall damage
                - +1 Fall Distance
            )
                */
            return null
        }

        fun updateHealth() {
            if (event.entity !is Player) return
            val player = event.entity as Player
            val currentHealth = playerHealth[player.uniqueId] ?: 0.0

            var finalDamage = 0.0
            if (getStrengthDamage() != null) finalDamage += getStrengthDamage()!!
            if (getDexDamage() != null) finalDamage += getDexDamage()!!

            checkDelay(player)
            val newHealth = max(0.0, min(currentHealth - finalDamage, globalMaxHealth))
            if (newHealth <= 0.0) {
                player.health = 0.0
            } else {
                playerHealth[player.uniqueId] = newHealth
            }

            if (WonderlandLibrary.config().getProperty("DebugMode") == true) {
                player.sendMessage(
                    ChatStructure.gradiantColor(
                        "Your health is ${newHealth.toInt()}",
                        arrayOf(ChatStructure.LIGHT_PURPLE, ChatStructure.AQUA),
                    ),
                )
            }

            AnimationPackets.HURT_ANIMATION.send(player, event.damager.location.yaw)
        }


        updateHealth()
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun handleGenericDamage(event: EntityDamageEvent) {
        if (event.entity !is Player) return
        val player = event.entity as Player
        event.isCancelled = true

        checkDelay(player)

        val currentHealth = player.combat.stats.health
        val maxHealth = player.combat.stats.maxHealth

        val finalDamage = getDamageValue(player, event.damage)

        val newHealth = max(0.0, min(currentHealth - finalDamage, maxHealth))
        if (newHealth <= 0.0) {
            player.health = 0.0
        } else {
            playerHealth[player.uniqueId] = newHealth
        }
        if (WonderlandLibrary.config().getProperty("DebugMode") == true) {
            player.sendMessage(
                ChatStructure.gradiantColor(
                    "Your health is ${newHealth.toInt()}",
                    arrayOf(ChatStructure.LIGHT_PURPLE, ChatStructure.AQUA),
                ),
            )
        }

        AnimationPackets.HURT_ANIMATION.send(player, player.location.yaw)
    }

    private fun registerPlayer(combatAttr: PlayerCombatAttr) {
        val dataFolderPath = WonderlandLibrary.getPlugin().dataFolder.absolutePath
        val playerFolder = File(dataFolderPath, "/Configs/Player/${combatAttr.uuid}")
        if (!playerFolder.exists()) playerFolder.mkdirs()
        playerHealth[combatAttr.uuid] = combatAttr.maxHealth

        val combatConfig =
            WLYamlConfig(
                "Combat-${combatAttr.uuid}",
                playerFolder.absolutePath,
                true,
                combatAttr.toProperties(),
            )

        try {
            WonderlandLibrary.getConfigManager().createConfig(combatConfig)
        } catch (_: IllegalArgumentException) {
            val config = WonderlandLibrary.getConfigManager().getConfig("Combat-${combatAttr.uuid}") as WLYamlConfig
            val player =
                Bukkit.getPlayer(UUID.fromString((config.getProperty("uuid") as? String)?.replace("\"", "")))
                    ?: throw IllegalArgumentException("Player doesn't exist ${config.getProperty("uuid") as? String}")
            val health = player.combat.stats.maxHealth

            player.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue = health
            player.healthScale = 40.0
        }
    }

    /**
     * This function takes in a player and a damage amount from the vanilla damage system.
     */
    private fun getDamageValue(
        player: Player,
        damage: Double,
    ): Double {
        val armor = player.combat.stats.armor
        val strength = player.combat.stats.strength

        val armorReduction = armor * armorReductionPercentage
        val strengthReduction = strength * armorReductionFromStrength

        val damageReduction = min(armorReduction + strengthReduction, 1.0)
        val reducedDamage = damage * (1 - damageReduction)

        return reducedDamage
    }
}
