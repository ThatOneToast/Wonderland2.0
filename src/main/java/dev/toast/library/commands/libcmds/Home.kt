package dev.toast.library.commands.libcmds

import dev.toast.library.WonderlandLibrary
import dev.toast.library.commands.WLPlayerCommand
import dev.toast.library.utils.ChatStructure
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Home : WLPlayerCommand(
    "home",
    "wonderland.home",
    15
) {
    override fun executeCommand(sender: Player, args: Array<String>): Boolean {
        if (!WonderlandLibrary.getHomeManager().hasHome(sender)) {
            sender.sendMessage(ChatStructure.RAINBOW + "You haven't set a home please do so with /home set")
        } else {
            val home = WonderlandLibrary.getHomeManager().getHome(sender)
            sender.sendMessage(ChatStructure.RAINBOW + "Teleporting...")
            sender.teleport(home)
        }
        return true
    }

    init {
        this.addSubcommand("set") { player, args ->
            val location = player.location
            WonderlandLibrary.getHomeManager().setHome(player, location)
            player.sendMessage(ChatStructure.RAINBOW + "Set home at ${location.x} / ${location.y} / ${location.z}")
            true
        }

    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): List<String> {
        return listOf("set")
    }
}