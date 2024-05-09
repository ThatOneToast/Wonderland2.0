package dev.toast.library.rpx

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import kotlin.random.Random


enum class Logs(val material: Material) {
    OAK_LOG(Material.OAK_LOG),
    SPRUCE_LOG(Material.SPRUCE_LOG),
    BIRCH_LOG(Material.BIRCH_LOG),
    JUNGLE_LOG(Material.JUNGLE_LOG),
    ACACIA_LOG(Material.ACACIA_LOG),
    DARK_OAK_LOG(Material.DARK_OAK_LOG),
    CRIMSON_STEM(Material.CRIMSON_STEM),
    WARPED_STEM(Material.WARPED_STEM);

    companion object {
        fun isLog(material: Material): Boolean {
            return values().any { it.material == material }
        }
    }
}

enum class Leaves(val material: Material) {
    OAK_LEAVES(Material.OAK_LEAVES),
    SPRUCE_LEAVES(Material.SPRUCE_LEAVES),
    BIRCH_LEAVES(Material.BIRCH_LEAVES),
    JUNGLE_LEAVES(Material.JUNGLE_LEAVES),
    ACACIA_LEAVES(Material.ACACIA_LEAVES),
    DARK_OAK_LEAVES(Material.DARK_OAK_LEAVES),
    MANGROVE_LEAVES(Material.MANGROVE_LEAVES),
    AZALEA_LEAVES(Material.AZALEA_LEAVES),
    FLOWERING_AZALEA_LEAVES(Material.FLOWERING_AZALEA_LEAVES);

    companion object {
        fun isLeaf(material: Material): Boolean {
            return values().any { it.material == material }
        }
    }
}

enum class Ores(val material: Material) {
    COAL_ORE(Material.COAL_ORE),
    IRON_ORE(Material.IRON_ORE),
    GOLD_ORE(Material.GOLD_ORE),
    DIAMOND_ORE(Material.DIAMOND_ORE),
    LAPIS_ORE(Material.LAPIS_ORE),
    REDSTONE_ORE(Material.REDSTONE_ORE),
    EMERALD_ORE(Material.EMERALD_ORE),
    NETHER_QUARTZ_ORE(Material.NETHER_QUARTZ_ORE),
    NETHER_GOLD_ORE(Material.NETHER_GOLD_ORE),
    ANCIENT_DEBRIS(Material.ANCIENT_DEBRIS),
    COPPER_ORE(Material.COPPER_ORE),
    DEEPSLATE_COAL_ORE(Material.DEEPSLATE_COAL_ORE),
    DEEPSLATE_IRON_ORE(Material.DEEPSLATE_IRON_ORE),
    DEEPSLATE_GOLD_ORE(Material.DEEPSLATE_GOLD_ORE),
    DEEPSLATE_DIAMOND_ORE(Material.DEEPSLATE_DIAMOND_ORE),
    DEEPSLATE_LAPIS_ORE(Material.DEEPSLATE_LAPIS_ORE),
    DEEPSLATE_REDSTONE_ORE(Material.DEEPSLATE_REDSTONE_ORE),
    DEEPSLATE_EMERALD_ORE(Material.DEEPSLATE_EMERALD_ORE),
    DEEPSLATE_COPPER_ORE(Material.DEEPSLATE_COPPER_ORE);

    companion object {
        fun isOre(material: Material): Boolean {
            return values().any { it.material == material }
        }
    }
}


class VeinMiner : Listener {
    private val enabledPlayers = mutableSetOf<Player>()

    fun enableVeinMiner(player: Player) {
        enabledPlayers.add(player)
    }

    fun disableVeinMiner(player: Player) {
        enabledPlayers.remove(player)
    }

    fun isVeinMinerEnabled(player: Player): Boolean {
        return enabledPlayers.contains(player)
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val block = event.block
        val player = event.player
        val itemInHand = player.inventory.itemInMainHand

        if (isVeinMinerEnabled(player) && !player.isSneaking && isToolAppropriate(itemInHand, block)) {
            val blocksToBreak = mutableSetOf<Block>()
            collectConnectedBlocks(block, blocksToBreak, block.type)
            blocksToBreak.forEach { b ->
                handleBlockBreak(player, b, itemInHand)
            }
        }
    }

    private fun handleBlockBreak(player: Player, block: Block, tool: ItemStack) {
        val smeltedItem = getSmeltedItem(block.type)
        val itemsToDrop = if (smeltedItem != null) listOf(smeltedItem) else block.getDrops(tool)

        itemsToDrop.forEach { item ->
            val amount = if (Random.nextDouble() < 0.03) item.amount * 2 else item.amount
            block.world.dropItemNaturally(block.location, ItemStack(item.type, amount))
        }
        block.type = Material.AIR
    }

    private fun getSmeltedItem(type: Material): ItemStack? {
        return when (type) {
            Material.IRON_ORE -> ItemStack(Material.IRON_INGOT)
            Material.DEEPSLATE_IRON_ORE -> ItemStack(Material.IRON_INGOT)
            Material.GOLD_ORE -> ItemStack(Material.GOLD_INGOT)
            Material.DEEPSLATE_GOLD_ORE -> ItemStack(Material.GOLD_INGOT)
            Material.COPPER_ORE -> ItemStack(Material.COPPER_INGOT)
            Material.DEEPSLATE_COPPER_ORE -> ItemStack(Material.COPPER_INGOT)
            else -> null
        }
    }

    private fun isToolAppropriate(item: ItemStack, block: Block): Boolean {
        return (Logs.isLog(block.type) && item.type.toString().contains("AXE")) ||
                (Ores.isOre(block.type) && item.type.toString().contains("PICKAXE"))
    }

    private fun collectConnectedBlocks(block: Block, blocksToBreak: MutableSet<Block>, type: Material, depth: Int = 0) {
        if (depth > 15) return // Adjust depth limit to balance performance

        val relatives = listOf(
            block.getRelative(1, 0, 0), block.getRelative(-1, 0, 0),
            block.getRelative(0, 1, 0), block.getRelative(0, -1, 0),
            block.getRelative(0, 0, 1), block.getRelative(0, 0, -1)
        )

        for (relative in relatives) {
            if ((Leaves.isLeaf(relative.type) || Logs.isLog(relative.type) || Ores.isOre(relative.type)) &&
                blocksToBreak.add(relative)
            ) {
                collectConnectedBlocks(relative, blocksToBreak, type, depth + 1)
            }
        }
    }
}
