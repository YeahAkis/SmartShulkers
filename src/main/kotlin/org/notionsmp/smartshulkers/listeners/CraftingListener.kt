package org.notionsmp.smartshulkers.listeners

import org.bukkit.Keyed
import org.bukkit.block.ShulkerBox
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.notionsmp.smartshulkers.SmartShulkers
import org.notionsmp.smartshulkers.utils.ShulkerManager

class CraftingListener(private val plugin: SmartShulkers) : Listener {
    @EventHandler
    fun onPrepareCraft(event: PrepareItemCraftEvent) {
        val key = (event.recipe as? Keyed)?.key ?: return
        if (key.namespace != plugin.smartShulkerKey.namespace) return

        val shulkerItem = event.inventory.matrix.firstOrNull { it != null && ShulkerManager.isShulkerBox(it.type) } ?: return
        val special = ShulkerManager.isSmartShulker(shulkerItem) ||
                ShulkerManager.isGarbageShulker(shulkerItem) ||
                ShulkerManager.isSellShulker(shulkerItem)

        if (key.key.startsWith("uncraft_")) {
            event.inventory.result = if (special) cleanCopy(shulkerItem) else null
            return
        }

        if (special) {
            event.inventory.result = null
            return
        }

        val result = event.inventory.result ?: return
        val permissionPath = when {
            ShulkerManager.isSmartShulker(result) -> "permissions.craft_smartshulker"
            ShulkerManager.isGarbageShulker(result) -> "permissions.craft_garbageshulker"
            ShulkerManager.isSellShulker(result) -> "permissions.craft_sellshulker"
            else -> return
        }

        if (!event.view.player.hasPermission(plugin.configManager.getString(permissionPath)!!)) {
            event.inventory.result = null
            return
        }
        copyContents(shulkerItem, result)
    }

    private fun cleanCopy(source: ItemStack): ItemStack? {
        val result = ItemStack(source.type)
        copyContents(source, result)
        return result
    }

    private fun copyContents(source: ItemStack, destination: ItemStack) {
        val sourceMeta = source.itemMeta as? BlockStateMeta ?: return
        val sourceState = sourceMeta.blockState as? ShulkerBox ?: return
        val contents = sourceState.inventory.contents ?: return

        val destMeta = destination.itemMeta as? BlockStateMeta ?: return
        val destState = destMeta.blockState as? ShulkerBox ?: return

        destState.inventory.contents = contents
        destState.update()
        destMeta.blockState = destState
        destination.itemMeta = destMeta
    }
}