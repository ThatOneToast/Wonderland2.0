package dev.toast.library.extensions

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class PlayerRightClickEvent(
    val player: Player,
    val mainHand: ItemStack,
    val offHand: ItemStack,
    val interactEvent: PlayerInteractEvent
): Event() {
    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }
}