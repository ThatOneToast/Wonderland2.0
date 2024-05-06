package dev.toast.library.rpx

import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class ItemStats(itemStack: ItemStack) {
    private val itemMeta = itemStack.itemMeta ?: throw IllegalArgumentException("Item Meta is null : ${itemStack.displayName()}")

    var strength: Int
        get() = itemMeta.persistentDataContainer.getOrDefault(wonderlandStrength, PersistentDataType.INTEGER, 0)
        set(value) = itemMeta.persistentDataContainer.set(wonderlandStrength, PersistentDataType.INTEGER, value)

    var dexterity: Int
        get() = itemMeta.persistentDataContainer.getOrDefault(wonderlandDexterity, PersistentDataType.INTEGER, 0)
        set(value) = itemMeta.persistentDataContainer.set(wonderlandDexterity, PersistentDataType.INTEGER, value)

    var intelligence: Int
        get() = itemMeta.persistentDataContainer.getOrDefault(wonderlandIntelligence, PersistentDataType.INTEGER, 0)
        set(value) = itemMeta.persistentDataContainer.set(wonderlandIntelligence, PersistentDataType.INTEGER, value)

    var wisdom: Int
        get() = itemMeta.persistentDataContainer.getOrDefault(wonderlandWisdom, PersistentDataType.INTEGER, 0)
        set(value) = itemMeta.persistentDataContainer.set(wonderlandWisdom, PersistentDataType.INTEGER, value)

}


val ItemStack.stats: ItemStats
    get() = ItemStats(this)