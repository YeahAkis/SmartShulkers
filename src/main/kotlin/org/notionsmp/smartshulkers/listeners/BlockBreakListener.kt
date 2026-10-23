package org.notionsmp.smartshulkers.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.notionsmp.smartshulkers.SmartShulkers
import org.notionsmp.smartshulkers.utils.ShulkerManager

class BlockBreakListener(private val plugin: SmartShulkers) : Listener {
    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (!ShulkerManager.isShulkerBox(event.block.type)) return
        val drop = ShulkerManager.itemFromBlock(event.block) ?: return

        event.isDropItems = false
        event.block.world.dropItemNaturally(event.block.location.add(0.5, 0.5, 0.5), drop)
    }
}
