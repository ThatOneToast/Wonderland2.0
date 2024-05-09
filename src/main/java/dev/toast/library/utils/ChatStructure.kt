package dev.toast.library.utils

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import org.bukkit.entity.Player

@Suppress("UNUSED")
enum class ChatStructure(val colorTag: String) {
    BLACK("<black>"),
    DARK_BLUE("<dark_blue>"),
    DARK_GREEN("<dark_green>"),
    DARK_AQUA("<dark_aqua>"),
    DARK_RED("<dark_red>"),
    DARK_PURPLE("<dark_purple>"),
    GOLD("<gold>"),
    GRAY("<gray>"),
    DARK_GRAY("<dark_gray>"),
    BLUE("<blue>"),
    GREEN("<green>"),
    AQUA("<aqua>"),
    RED("<red>"),
    LIGHT_PURPLE("<light_purple>"),
    YELLOW("<yellow>"),
    WHITE("<white>"),


    GRADIENT_BLUE_TO_PURPLE("<gradient:blue:purple>"),
    GRADIENT_GREEN_TO_YELLOW("<gradient:green:yellow>"),


    RAINBOW("<rainbow>")


    ;

    companion object {
        /**
         * Creates a clickable chat component that opens a URL when clicked.
         *
         * @param text The visible text of the component.
         * @param url The URL to open when the component is clicked.
         * @return The clickable component.
         */
        fun clickableLink(text: String, url: String): Component {
            return Component.text(text)
                .color(NamedTextColor.BLUE)
                .decorate(TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.openUrl(url))
                .hoverEvent(HoverEvent.showText(Component.text("Click to open $url")))
        }

        fun createHoverableComponent(text: String, hoverText: String): Component {
            val hoverEvent = HoverEvent.showText(Component.text(hoverText))
            return Component.text(text).hoverEvent(hoverEvent)
        }

        /**
         * Creates a clickable chat component that suggests a command when clicked.
         *
         * @param text The visible text of the component.
         * @param command The command to suggest.
         * @return The clickable component.
         */
        fun clickableCommand(text: String, command: String): Component {
            val miniMessage = MiniMessage.builder()
                .tags(
                    TagResolver.builder()
                    .resolver(StandardTags.color())
                    .resolver(StandardTags.decorations())
                    .resolver(StandardTags.hoverEvent())
                    .resolver(StandardTags.clickEvent())
                    .build())
                .build()


            return miniMessage.deserialize("<hover:show_text:'$text'><click:suggest_command:'$command'> <gold>[USAGE]</gold>")
        }


        fun createPaginatedMessage(items: List<Component>, itemsPerPage: Int): PaginatedMessage {
            return PaginatedMessage(items, itemsPerPage)
        }

        /**
         * Creates a clickable chat component that runs a command when clicked.
         *
         * @param text The visible text of the component.
         * @param command The command to run.
         * @return The clickable component.
         */
        fun clickableRunCommand(text: String, command: String): Component {
            return Component.text(text)
                .color(NamedTextColor.RED)
                .clickEvent(ClickEvent.runCommand(command))
                .hoverEvent(HoverEvent.showText(Component.text("Click to run command")))
        }

        fun gradiantColor(message: String, colors: Array<ChatStructure>): Component {
            var gradiant = ""
            colors.forEachIndexed { index, color ->
                val colorTag = color.colorTag.removeSurrounding("<", ">")
                gradiant += colorTag
                if (index < colors.size - 1) {
                    gradiant += ":"
                }
            }
            return MiniMessage.miniMessage().deserialize("<gradient:$gradiant>$message<reset>")
        }



        fun format(component: Component): Component {
            val miniMessage = MiniMessage.miniMessage()
            val componentString = miniMessage.serialize(component)
            return miniMessage.deserialize("$componentString<reset>")
        }

        fun formatString(text: String): Component {
            return MiniMessage.miniMessage().deserialize(text)
        }

        fun sendMessage(player: Player, message: Component) {
            val audience = Audience.audience(player)
            audience.sendMessage(message)
        }

        fun sendMessage(player: Player, message: String) {
            val audience = Audience.audience(player)
            audience.sendMessage(MiniMessage.miniMessage().deserialize(message))
        }



    }

    operator fun plus(other: String): Component {
        return MiniMessage.miniMessage().deserialize("$colorTag$other")
    }


    fun format(text: String): Component {
        return MiniMessage.miniMessage().deserialize(
            "${colorTag}$text<reset>",
            StandardTags.color(), StandardTags.clickEvent(), StandardTags.hoverEvent(), StandardTags.decorations(),
        )
    }

    class PaginatedMessage(private val items: List<Component>, private val itemsPerPage: Int = 7) {
        private val totalPages: Int = (items.size + itemsPerPage - 1) / itemsPerPage

        fun getPage(page: Int): Component {
            val pageIdx = page.coerceIn(1, totalPages) - 1
            val startIdx = pageIdx * itemsPerPage
            val endIdx = minOf(startIdx + itemsPerPage, items.size)

            val pageItems = GOLD + "Page: $page /$totalPages\n\n      - This houses only commands associated with the wonderland library. \n\n"
            val components = items.subList(startIdx, endIdx).fold(pageItems) { comp, item ->
                comp.append(item).append(Component.newline())
            }

            return components.append(getNavigationButtons(page))
        }

        private fun getNavigationButtons(currentPage: Int): Component {
            val prev = if (currentPage > 1)
                Component.text("[Prev]")
                    .color(NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.runCommand("/command page ${currentPage - 1}"))
                    .append(Component.text(" "))
            else
                Component.empty()

            val next = if (currentPage < totalPages)
                Component.text("[Next]")
                    .color(NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.runCommand("/command page ${currentPage + 1}"))

            else
                Component.empty()

            return prev.append(next)
        }
    }

}

fun finalizeComponent(component: Component): Component {
    val miniMessage = MiniMessage.miniMessage()
    val serialized = miniMessage.serialize(component)
    return miniMessage.deserialize("$serialized<reset>")
}
