package dev.toast.library.rpx

import dev.toast.library.WonderlandLibrary
import dev.toast.library.configs.WLYamlConfig
import dev.toast.library.rpx.combat.ManaSystem
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

val wonderlandRegen = NamespacedKey(WonderlandLibrary.getPlugin(), "healthregen")
val wonderlandStrength = NamespacedKey(WonderlandLibrary.getPlugin(), "strength")
val wonderlandDexterity = NamespacedKey(WonderlandLibrary.getPlugin(), "dexterity")
val wonderlandIntelligence = NamespacedKey(WonderlandLibrary.getPlugin(), "intelligence")
val wonderlandWisdom = NamespacedKey(WonderlandLibrary.getPlugin(), "wisdom")

class NotEnoughManaException(message: String) : Exception(message)

class ManaOutOfBounds(message: String) : Exception(message)

class PlayerCombat(private val player: Player) {
    inner class Stats {
        var health: Double
            get() = WonderlandLibrary.getDamageSystem().getPlayerHealth(player)
            set(value) = WonderlandLibrary.getDamageSystem().setPlayerHealth(player, value)

        var maxHealth: Double
            get() = config.getProperty("maxHealth") as? Double ?: 40.0
            set(value) = config.setProperty("maxHealth", value)

        var mana: Double
            get() = ManaSystem.getMana(player)
            set(value) = ManaSystem.setMana(player, value)

        var maxMana: Double
            get() = config.getProperty("maxMana") as? Double ?: 100.0
            set(value) = config.setProperty("maxMana", value)

        var manaRegen: Double
            get() = config.getProperty("manaRegen") as? Double ?: 1.5
            set(value) = config.setProperty("manaRegen", value)

        var shield: Double
            get() = WonderlandLibrary.getDamageSystem().getPlayerShield(player)
            set(value) = WonderlandLibrary.getDamageSystem().setPlayerShield(player, value)

        var maxShield: Double
            get() = config.getProperty("maxShield") as? Double ?: 0.0
            set(value) = config.setProperty("maxShield", value)

        var shieldRegen: Double
            get() = config.getProperty("shieldRegen") as? Double ?: 0.0
            set(value) = config.setProperty("shieldRegen", value)


        var armor: Double
            get() = config.getProperty("armor") as? Double ?: 0.0
            set(value) = config.setProperty("armor", value)

        var healthRegen: Double
            get() = player.persistentDataContainer.getOrDefault(wonderlandRegen, PersistentDataType.DOUBLE, 2.0)
            set(value) = player.persistentDataContainer.set(wonderlandRegen, PersistentDataType.DOUBLE, value)

        var strength: Int
            get() = player.persistentDataContainer.getOrDefault(wonderlandStrength, PersistentDataType.INTEGER, 0)
            set(value) = player.persistentDataContainer.set(wonderlandStrength, PersistentDataType.INTEGER, value)

        var dexterity: Int
            get() = player.persistentDataContainer.getOrDefault(wonderlandDexterity, PersistentDataType.INTEGER, 0)
            set(value) = player.persistentDataContainer.set(wonderlandDexterity, PersistentDataType.INTEGER, value)

        var intelligence: Int
            get() = player.persistentDataContainer.getOrDefault(wonderlandIntelligence, PersistentDataType.INTEGER, 0)
            set(value) = player.persistentDataContainer.set(wonderlandIntelligence, PersistentDataType.INTEGER, value)

        var wisdom: Int
            get() = player.persistentDataContainer.getOrDefault(wonderlandWisdom, PersistentDataType.INTEGER, 0)
            set(value) = player.persistentDataContainer.set(wonderlandWisdom, PersistentDataType.INTEGER, value)

    }

    val stats = Stats()
    val race = config.getProperty("race") as? String ?: "Human-Defaulted"

    /**
     * Spends the allotted amount of mana.
     * @param amount Double
     * @throws NotEnoughManaException if the player doesn't have enough mana.
     */
    fun spendMana(amount: Double) {
        if (ManaSystem.canISpend(player, amount)) {
            ManaSystem.setMana(player, (ManaSystem.getMana(player) - amount))
        } else {
            throw NotEnoughManaException("${player.name} doesn't have enough mana.")
        }
    }

    fun getManaRegen(): Double {
        return ManaSystem.getManaRegen(player)
    }

    fun setManaRegen(amount: Double) {
        ManaSystem.setManaRegen(player, amount)
    }

    /**
     * Sets the mana of the player
     * @param amount
     * @throws ManaOutOfBounds if the mana is above max mana.
     */
    fun setMana(amount: Double) {
        if (amount <= ManaSystem.getMaxMana(player)) {
            ManaSystem.setMana(player, amount)
        } else {
            throw ManaOutOfBounds(
                "Cannot set the mana of ${player.name} to $amount because the players max mana is ${ManaSystem.getMaxMana(player)}",
            )
        }
    }

    fun getMana(): Double {
        return ManaSystem.getMana(player)
    }

    fun setMaxMana(amount: Double) {
        ManaSystem.setMaxMana(player, amount)
    }

    val config: WLYamlConfig
        get() = WonderlandLibrary.getConfigManager().getConfig("Combat-${player.uniqueId}") as WLYamlConfig
}

val Player.combat: PlayerCombat
    get() = PlayerCombat(this)

/**
 * Internal save. This saves all relative data to players for Wonderland.
 */
fun Player.save() {
    combat.config.save()
}

fun getPlayerStrength(player: Player): Int {
    return player.combat.stats.strength
}

fun setPlayerStrength(
    player: Player,
    strength: Int,
) {
    player.combat.stats.strength = strength
}
