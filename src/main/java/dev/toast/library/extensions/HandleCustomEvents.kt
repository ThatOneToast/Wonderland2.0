package dev.toast.library.extensions

import dev.toast.library.WonderlandLibrary
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class HandleCustomEvents : Listener{

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun callLeftRightClicks(event: PlayerInteractEvent) {

        if (event.action == Action.RIGHT_CLICK_AIR) {
            WonderlandLibrary.getPlugin().server.pluginManager.callEvent(PlayerRightClickEvent(
                event.player,
                event.player.inventory.itemInMainHand,
                event.player.inventory.itemInOffHand,
                event
            ))
        }

        else if (event.action == Action.LEFT_CLICK_AIR) {
            WonderlandLibrary.getPlugin().server.pluginManager.callEvent(PlayerLeftClickEvent(
                event.player,
                event.player.inventory.itemInMainHand,
                event.player.inventory.itemInOffHand,
                event
            ))
        }
    }
}