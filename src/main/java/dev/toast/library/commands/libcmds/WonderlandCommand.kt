package dev.toast.library.commands.libcmds

import dev.toast.library.WonderlandLibrary
import dev.toast.library.commands.WLPlayerCommand
import dev.toast.library.configs.ConfigManager
import dev.toast.library.configs.WLBytesConfig
import dev.toast.library.configs.WLConfig
import dev.toast.library.configs.WLYamlConfig
import dev.toast.library.extensions.toMiniMessageComponent
import dev.toast.library.utils.ChatStructure
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class WonderlandCommand : WLPlayerCommand(
    "wonderland",
    "",
    3
) {
    override fun executeCommand(sender: Player, args: Array<String>): Boolean {
        return true
    }

    fun constructHelpMenu(): ChatStructure.PaginatedMessage {
        val messageMenu = ChatStructure.createPaginatedMessage(
            listOf(
                ChatStructure.RAINBOW + "| --------- Help -------- |",
                constructHelpMessage("/wonderland help", "Shows this menu", "/wonderland help 1"),
                constructHelpMessage("/wonderland config", "Control your configs right from the chat.", "/wonderland config read Index.yml")
            ), 10
        )

        return messageMenu
    }

    init {
        this.addSubcommand("help") { player, args ->
            val messageMenu = constructHelpMenu()

            if (args.size == 1) {
                player.sendMessage(messageMenu.getPage(args[0].toInt()))
            }
            true
        }

        this.addSubcommand("config") { player, args ->
            if (args.size >= 3 && args[2].lowercase() == "read" ) {
                val fileName = args[3].substringBefore('.')
                val config = WonderlandLibrary.getConfigManager().getConfig(fileName)

                when (config.getExtension()) {
                    ".bit" -> {
                        val eleConfig = config as WLBytesConfig
                        val configName = eleConfig.getName()
                        val configLocation = eleConfig.fullPath
                        val quickAccess = eleConfig.quickAccess
                        val properties: Map<String, Any> = eleConfig.getProperties()
                        val propComponents: MutableList<Component> = mutableListOf()

                        val lineBreak = "<bold>--------------------------------------------".toMiniMessageComponent()
                        val space = " ".toMiniMessageComponent()
                        val nameComp: Component = "<aqua>Config Name: </aqua><gold>$configName</gold>".toMiniMessageComponent()
                        val localComp: Component = "<aqua>Location: </aqua>$configLocation".toMiniMessageComponent()
                        val quickAccComp: Component = "<aqua> Quick Access: </aqua><gold>$quickAccess</gold>".toMiniMessageComponent()

                        propComponents.add(lineBreak)
                        propComponents.add(nameComp)
                        propComponents.add(localComp)
                        propComponents.add(quickAccComp)
                        propComponents.add(space)

                        for (prop in properties) {

                            fun getMessage(): Component {
                                val propName = prop.key
                                val propValueString = prop.value.toString()

                                if (propValueString.length > 20) {
                                    val shortValue = propValueString.substring(0, 20) + "..."
                                    val hoverComponent = ChatStructure.createHoverableComponent("...", propValueString)
                                    val comp = Component.text().append(Component.text("$propName: ").color(TextColor.color(0xFFD700)))
                                        .append(Component.text(shortValue).color(TextColor.color(0xFFFF00)).hoverEvent(hoverComponent))

                                    return comp.build()
                                } else {
                                    val comp = Component.text().append(Component.text("$propName: ").color(TextColor.color(0xFFD700)))
                                        .append(Component.text(propValueString).color(TextColor.color(0xFFFF00)))
                                    return comp.build()
                                }
                            }


                            propComponents.add(getMessage())
                        }


                        propComponents.add(lineBreak)
                        val menu = ChatStructure.createPaginatedMessage(propComponents, 75)
                        player.sendMessage(menu.getPage(1))


                    }
                    ".yml" -> {
                        val eleConfig = config as WLYamlConfig
                        val configName = eleConfig.getName()
                        val configLocation = eleConfig.fullPath
                        val quickAccess = eleConfig.quickAccess
                        val properties: Map<String, Any> = eleConfig.getProperties()
                        val propComponents: MutableList<Component> = mutableListOf()

                        val lineBreak = "<bold>--------------------------------------------".toMiniMessageComponent()
                        val space = " ".toMiniMessageComponent()
                        val nameComp: Component = "<aqua>Config Name: </aqua><gold>$configName</gold>".toMiniMessageComponent()
                        val localComp: Component = "<aqua>Location: </aqua>$configLocation".toMiniMessageComponent()
                        val quickAccComp: Component = "<aqua> Quick Access: </aqua><gold>$quickAccess</gold>".toMiniMessageComponent()

                        propComponents.add(lineBreak)
                        propComponents.add(nameComp)
                        propComponents.add(localComp)
                        propComponents.add(quickAccComp)
                        propComponents.add(space)

                        for (prop in properties) {
                            val propValueString = prop.value.toString()
                            val message = "<gold>${prop.key}:</gold> <yellow>$propValueString</yellow>".toMiniMessageComponent()
                            propComponents.add(message)

                        }

                        propComponents.add(lineBreak)
                        val menu = ChatStructure.createPaginatedMessage(propComponents, 75)
                        player.sendMessage(menu.getPage(1))
                    }
                }

            }
            else if (args.size >= 3 && args[2].lowercase() == "reload" ) {
                val fileName = args[3].substringBefore('.')
                WonderlandLibrary.getConfigManager().reloadConfig(fileName)
                player.sendMessage(ChatStructure.GREEN + "$fileName - Reloaded!")
            }
            true
        }
    }

    private fun constructHelpMessage(command: String, description: String, usageExample: String): Component {
        // Set up the MiniMessage with a TagResolver that includes standard tags
        val miniMessage = MiniMessage.builder()
            .tags(
                TagResolver.builder()
                    .resolver(StandardTags.color())
                    .resolver(StandardTags.decorations())
                    .resolver(StandardTags.hoverEvent())
                    .resolver(StandardTags.clickEvent())
                    .build()
            )
            .build()


        // Create the command text component
        var commandComponent = miniMessage.deserialize("<aqua>$command</aqua> - <gray>$description</gray>")
        println("Command Component: $commandComponent")
        if (usageExample.isNotEmpty()) {
            val comp = commandComponent.append(ChatStructure.clickableCommand(usageExample, usageExample))
            commandComponent = comp
        }

        val textMessage = Component.empty()
            .append(commandComponent)

        return textMessage
    }

    fun getPropertyKeys(superConfig: WLConfig): List<String> {
        val propsList: MutableList<String> = mutableListOf()

        when (superConfig.getExtension()) {
            ".bit" -> {
                val config = superConfig as WLBytesConfig
                val props = config.getProperties()
                for (prop in props) {
                    propsList.add(prop.key)
                }
            }
            ".yml" -> {
                val config = superConfig as WLYamlConfig
                val props = config.getProperties()
                for (prop in props) {
                    propsList.add(prop.key)
                }
            }
            // Add more cases if needed for other config file types
        }

        return propsList
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): List<String> {
        when (args.size) {
            1 -> {
                return listOf("help", "config")
            }

            2 -> {
                if (args[0].lowercase() == "config") {
                    return listOf("new", "modify", "delete", "read", "reload")
                }
            }

            3 -> {
                if (args[0].lowercase() == "config") {
                    when (args[1].lowercase()) {
                        "new" -> return listOf("dummyconfig")
                        "modify", "delete", "read", "reload" -> return ConfigManager.getAllConfigNames()
                    }
                }
            }

            4 -> {
                if (args[0].lowercase() == "config" && args[2].lowercase() == "modify") {
                    val fileName = args[3].substringBefore('.')
                    val config = WonderlandLibrary.getConfigManager().getConfig(fileName)

                    return getPropertyKeys(config)

                }

                if (args[0].lowercase() == "config" && args[2].lowercase() == "read") {
                    val fileName = args[3].substringBefore('.')
                    val config = WonderlandLibrary.getConfigManager().getConfig(fileName)

                    val propsList: MutableList<String> = mutableListOf()
                    when (config.getExtension()) {
                        ".bit" -> {
                            val eleConfig = config as WLBytesConfig
                            val properties: Map<String, Any> = eleConfig.getProperties()

                            for (prop in properties) {
                                propsList.add(prop.key)
                            }

                        }
                        ".yml" -> {
                            val eleConfig = config as WLYamlConfig
                            val properties: Map<String, Any> = eleConfig.getProperties()

                            for (prop in properties) {
                                propsList.add(prop.key)
                            }
                        }
                    }

                    return propsList

                }

            }

            5 -> {
                if (args[0].lowercase() == "config" && args[2].lowercase() == "modify") {
                    if (args[3].lowercase() == "setproperty") {
                        val configName = args[4].lowercase()
                        val superConfig = WonderlandLibrary.getConfigManager().getConfig(configName)

                        return getPropertyKeys(superConfig)
                    }
                }
            }
        }
        return emptyList()
    }
}
