package org.notionsmp.smartshulkers.listeners

import org.bukkit.block.ShulkerBox
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.notionsmp.smartshulkers.SmartShulkers

class BlockBreakListener(private val plugin: SmartShulkers) : Listener {
    private val breakHandler = ShulkerBreakHandler(plugin)

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (event.block.state !is ShulkerBox) return
        val replacement = breakHandler.buildReplacementItem(event.block) ?: return

        event.isDropItems = false
        event.block.world.dropItemNaturally(event.block.location.add(0.5, 0.5, 0.5), replacement)
    }
}