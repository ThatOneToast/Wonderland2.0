package dev.toast.library.commands

import dev.toast.library.WonderlandLibrary
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

abstract class WLConsoleCommand(
    val name: String,
    val subCommand1Name: String?,
    val subCommand2Name: String?
) {

    abstract fun executeCommand(sender: CommandSender, args: Array<String>): Boolean
    abstract fun subCommand1(sender: CommandSender, args: Array<String>): Boolean
    abstract fun subCommand2(sender: CommandSender, args: Array<String>): Boolean

    init {
        val commandMap = Bukkit.getCommandMap()

        val officialCommand: Command = object: Command(name) {
            override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
                if (!canExecute(sender)) {
                    sender.sendMessage("Only the Console can use this command.")
                    return true  // Return true to indicate the command syntax was correct but not allowed
                }

                return when {
                    args.isNotEmpty() && args[0] == subCommand1Name -> subCommand1(sender, args.copyOfRange(1, args.size))
                    args.size > 1 && args[1] == subCommand2Name -> subCommand2(sender, args.copyOfRange(2, args.size))
                    else -> executeCommand(sender, args)
                }
            }
        }
        commandMap.register(WonderlandLibrary.getPlugin().name, officialCommand)
    }

    private fun canExecute(sender: CommandSender): Boolean {
        if(sender is Player) {
            sender.sendMessage("Only the Console can use this command.")
            return false
        }
        return true
    }
}