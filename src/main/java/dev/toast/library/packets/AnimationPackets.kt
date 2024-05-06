package dev.toast.library.packets

import dev.toast.library.extensions.sendPackets
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer
import org.bukkit.entity.Player

class AnimationPacketArgumentException(override val message: String) : Exception(message)

enum class AnimationPackets(val send: (Player, Float?) -> Unit) {
    HURT_ANIMATION({ player, yaw ->
        if (yaw == null) throw AnimationPacketArgumentException("The Hurt animation packet requires a yaw from where the player was damaged.")
        val entityPlayer = (player as CraftPlayer).handle
        val hurtPacket = net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket(entityPlayer.id, yaw)
        val packets = listOf(hurtPacket)

        player.sendPackets(packets)
    }),





    ;


}
