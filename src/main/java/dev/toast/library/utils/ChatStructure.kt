package dev.toast.library.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage

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

        /**
         * Creates a clickable chat component that suggests a command when clicked.
         *
         * @param text The visible text of the component.
         * @param command The command to suggest.
         * @return The clickable component.
         */
        fun clickableCommand(text: String, command: String): Component {
            return Component.text(text)
                .color(NamedTextColor.GREEN)
                .clickEvent(ClickEvent.suggestCommand(command))
                .hoverEvent(HoverEvent.showText(Component.text("Click to suggest command")))

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
            val gradiant = colors.joinToString(separator = ":", transform = { it.colorTag })
            return MiniMessage.miniMessage().deserialize("<$gradiant$message<reset>")
        }

        fun format(component: Component): Component {
            val miniMessage = MiniMessage.miniMessage()
            val componentString = miniMessage.serialize(component)
            return miniMessage.deserialize("$componentString<reset>")
        }

    }

    // create an operator fun +
    operator fun plus(other: String): Component {
        return MiniMessage.miniMessage().deserialize("$colorTag$other")
    }





    class PaginatedMessage(private val items: List<Component>, private val itemsPerPage: Int = 7) {
        private val totalPages: Int = (items.size + itemsPerPage - 1) / itemsPerPage

        fun getPage(page: Int): Component {
            val pageIdx = page.coerceIn(1, totalPages) - 1
            val startIdx = pageIdx * itemsPerPage
            val endIdx = minOf(startIdx + itemsPerPage, items.size)

            val pageItems = Component.text("Page $page of $totalPages\n", NamedTextColor.YELLOW)
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
