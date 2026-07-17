package org.notionsmp.smartshulkers.listeners

import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockExplodeEvent
import org.notionsmp.smartshulkers.SmartShulkers

class BlockExplodeListener(private val plugin: SmartShulkers) : Listener {
    private val breakHandler = ShulkerBreakHandler(plugin)

    @EventHandler
    fun onBlockExplode(event: BlockExplodeEvent) {
        for (block in event.blockList().toList()) {
            if (block.state !is ShulkerBox) continue
            val replacement = breakHandler.buildReplacementItem(block) ?: continue

            event.blockList().remove(block)
            block.world.dropItemNaturally(block.location.add(0.5, 0.5, 0.5), replacement)
            block.type = Material.AIR
        }
    }
}