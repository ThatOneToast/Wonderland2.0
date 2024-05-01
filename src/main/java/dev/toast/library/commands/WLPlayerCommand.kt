package dev.toast.library.commands

import dev.toast.library.WonderlandLibrary
import dev.toast.library.extensions.combineUUIDs
import dev.toast.library.utils.ChatStructure
import dev.toast.library.utils.CooldownManager
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

abstract class WLPlayerCommand(
    val name: String,
    val permission: String,
    val cooldown: Int = 0
) {
    private val commandUUID = UUID.randomUUID()
    private val subcommands: MutableMap<String, (Player, Array<String>) -> Boolean> = hashMapOf()

    fun addSubcommand(subcommand: String, handler: (Player, Array<String>) -> Boolean) {
        subcommands[subcommand.lowercase(Locale.getDefault())] = handler
    }

    abstract fun executeCommand(sender: Player, args: Array<String>): Boolean
    abstract fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String>

    init {
        val commandMap = Bukkit.getCommandMap()

        val officialCommand: Command = object: Command(name) {
            override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
                if(canExecute(sender, args)) {
                    sender as Player
                    val uuid = combineUUIDs(commandUUID, sender.uniqueId)

                    if (args.isNotEmpty() && subcommands.containsKey(args[0].toLowerCase())) {

                        CooldownManager.applyCooldown(uuid, cooldown)
                        return subcommands[args[0].lowercase(Locale.getDefault())]!!.invoke(sender, args.copyOfRange(1, args.size))
                    }

                    CooldownManager.applyCooldown(uuid, cooldown)
                    return executeCommand(sender, args)
                }
                return false
            }

            override fun tabComplete(sender: CommandSender, alias: String, args: Array<String>): List<String> {
                return onTabComplete(sender, this, alias, args)
            }
        }
        commandMap.register(WonderlandLibrary.getPlugin().name, officialCommand)
    }

    private fun canExecute(sender: CommandSender, args: Array<String>): Boolean {
        if(sender !is Player) {
            sender.sendMessage("Only players can use this command.")
            return false
        }
        if (!sender.hasPermission(permission)) {
            sender.sendMessage("You do not have permission to use this command.")
            return false
        }
        val uuid = combineUUIDs(commandUUID, sender.uniqueId)

        if(CooldownManager.isOnCooldown(uuid)) {
            sender.sendMessage(ChatStructure.RED + "You are on cooldown ${CooldownManager.getCooldownTimeInSec(uuid)}")
            return false
        }

        return true
    }


}