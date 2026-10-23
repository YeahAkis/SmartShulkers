package org.notionsmp.smartshulkers.listeners

import io.papermc.paper.event.block.BlockBreakBlockEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.notionsmp.smartshulkers.utils.ShulkerManager

class BlockDestroyListener : Listener {
    @EventHandler
    fun onBlockBreakBlock(event: BlockBreakBlockEvent) {
        if (!ShulkerManager.isShulkerBox(event.block.type)) return
        val rebuilt = ShulkerManager.itemFromBlock(event.block) ?: return

        val index = event.drops.indexOfFirst { it.type == event.block.type }
        if (index >= 0) event.drops[index] = rebuilt else event.drops.add(rebuilt)
    }
}
