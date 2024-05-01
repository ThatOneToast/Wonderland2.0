package dev.toast.library.items

import dev.toast.library.WonderlandLibrary
import dev.toast.library.extensions.PlayerLeftClickEvent
import dev.toast.library.extensions.PlayerRightClickEvent
import dev.toast.library.utils.ChatStructure
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

interface WLItemEnchance

@Suppress("unused")
open class WLItem(
    val name: String,
    val material: Material,
    val customModelData: Int
) {

    open fun leftClick(event: PlayerLeftClickEvent) {

    }

    open fun rightClick(event: PlayerRightClickEvent) {
        val pdcMainHand = event.mainHand.itemMeta.persistentDataContainer
        val pdcMainHandValue = pdcMainHand.get(NamespacedKey(WonderlandLibrary.getPlugin(), "demo"), PersistentDataType.BOOLEAN)
        event.player.sendMessage(ChatStructure.RAINBOW + pdcMainHandValue.toString().uppercase())
    }

    open fun applyEnchants(): Map<Enchantment, Int> {
        return mapOf(Pair(Enchantment.DAMAGE_ALL, 10))
    }

    open fun applyAttributes(): Set<AttributeModifier> {
        val damageAttr = AttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE.name, 12.5, AttributeModifier.Operation.ADD_NUMBER)
        return setOf(damageAttr)
    }

    open fun applyFlags(): Set<ItemFlag> {
        val flags: MutableSet<ItemFlag> = mutableSetOf()
        flags.add(ItemFlag.HIDE_ENCHANTS)

        return flags
    }

    open fun applyLore(): List<Component> {
        val lore: MutableList<Component> = mutableListOf()

        val redBlueGreenGradiant = arrayOf(ChatStructure.RED, ChatStructure.BLUE, ChatStructure.GREEN)

        lore.add(ChatStructure.GRADIENT_GREEN_TO_YELLOW + "Super Dank Weapon!")
        lore.add(ChatStructure.gradiantColor("Now this is super dank!", redBlueGreenGradiant))

        return lore
    }

    open fun applyPDC(meta: ItemMeta): PersistentDataContainer {
        val pdc = meta.persistentDataContainer
        pdc.set(NamespacedKey(WonderlandLibrary.getPlugin(), "demo"), PersistentDataType.BOOLEAN, true)
        return pdc
    }


    fun build(): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta

        meta.displayName()
        meta.setCustomModelData(customModelData)

        val enchants: Map<Enchantment, Int> = applyEnchants()
        for (enchant in enchants) {
            meta.addEnchant(enchant.key, enchant.value, true)
        }

        val attributes: Set<AttributeModifier> = applyAttributes()
        for (attr in attributes) {
            meta.addAttributeModifier(Attribute.valueOf(attr.name), attr)
        }
        applyFlags().forEach { meta.addItemFlags(it) }
        meta.lore(applyLore())
        applyPDC(meta)
        item.itemMeta = meta
        return item
    }

}