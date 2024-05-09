package dev.toast.library.rpx.homes

import dev.toast.library.WonderlandLibrary
import dev.toast.library.commands.libcmds.Home
import dev.toast.library.configs.WLYamlConfig
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.io.File
import java.util.*

class HomeManager : Listener {
    private val playerHomes: MutableMap<UUID, Location> = mutableMapOf()

    init {
        Home()
    }


    @EventHandler
    private fun getHomeConfig(event: PlayerJoinEvent) {
        try {
            val config =
                WonderlandLibrary.getConfigManager().getConfig("Homes-${event.player.uniqueId}") as WLYamlConfig
            val locationX = config.getProperty("home1.x") as Double
            val locationY: Double = config.getProperty("home1.y") as Double
            val locationZ: Double = config.getProperty("home1.z") as Double
            val locationYaw: Float = config.getProperty("home1.yaw") as Float
            val locationPitch: Float = config.getProperty("home1.pitch") as Float
            val locationWorld: String = config.getProperty("home1.world") as String
            val location = Location(Bukkit.getWorld(locationWorld), locationX, locationY, locationZ, locationYaw, locationPitch)
            playerHomes[event.player.uniqueId] = location
        } catch (_: Exception) {

        }
    }

    @EventHandler
    private fun saveConfig(event: PlayerQuitEvent) {
        val dataFolderPath = WonderlandLibrary.getPlugin().dataFolder.absolutePath
        val playerFolder = File(dataFolderPath, "/Configs/Player/${event.player.uniqueId}")
        File(playerFolder.absolutePath).mkdirs()
        val location = playerHomes[event.player.uniqueId] as Location

        val proprs: MutableMap<String, Any> = mutableMapOf()
        proprs["home1.X"] = location.x
        proprs["home1.Y"] = location.y
        proprs["home1.Z"] = location.z
        proprs["home1.Yaw"] = location.yaw
        proprs["home1.Pitch"] = location.pitch
        proprs["home.world"] = location.world

        val config = WLYamlConfig(
            "Homes-${event.player.uniqueId}",
            playerFolder.absolutePath,
            false,
            proprs
        )
        try {
            WonderlandLibrary.getConfigManager().createConfig(config)
        } catch (_: Exception) {
            config.save()
        }


    }


    fun setHome(player: Player, location: Location) {
        playerHomes[player.uniqueId] = location
    }

    fun getHome(player: Player): Location {
        return playerHomes[player.uniqueId] ?: throw IllegalStateException("No home")
    }

    fun hasHome(player: Player): Boolean {
        return playerHomes.containsKey(player.uniqueId)
    }

    fun saveHomes() {

        val dataFolderPath = WonderlandLibrary.getPlugin().dataFolder.absolutePath
        playerHomes.forEach { uuid, location ->
            val playerFolder = File(dataFolderPath, "/Configs/Player/${uuid}").absolutePath
            File(playerFolder).mkdirs()
            val proprs: MutableMap<String, Any> = mutableMapOf()
            proprs["home1"] = location
            val config = WLYamlConfig(
                "Homes-${uuid}",
                playerFolder,
                false,
                proprs
            )

            try {
                WonderlandLibrary.getConfigManager().createConfig(config)
            } catch (_: Exception) {
                config.save()
            }
        }

    }
}