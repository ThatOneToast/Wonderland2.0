package dev.toast.library.commands.libcmds

import dev.toast.library.WonderlandLibrary
import dev.toast.library.commands.WLPlayerCommand
import dev.toast.library.utils.ChatStructure
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class VeinMineToggle : WLPlayerCommand(
    name = "VMtoggle",
    permission = "",
    cooldown = 3

) {
    override fun executeCommand(sender: Player, args: Array<String>): Boolean {
        val veinMiner = WonderlandLibrary.getVeinMiner()
        if (veinMiner.isVeinMinerEnabled(sender)) {
            veinMiner.disableVeinMiner(sender)
            sender.sendMessage(ChatStructure.gradiantColor("VeinMiner Disabled", arrayOf(ChatStructure.GREEN, ChatStructure.RED, ChatStructure.RED )))

        } else {
            veinMiner.enableVeinMiner(sender)
            sender.sendMessage(ChatStructure.gradiantColor("VeinMiner Enabled!", arrayOf(ChatStructure.GREEN, ChatStructure.GREEN )))
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): List<String> {
        return listOf()
    }
}