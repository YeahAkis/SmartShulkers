package org.notionsmp.smartshulkers.listeners

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.ShulkerBox
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.inventory.ItemStack
import org.notionsmp.smartshulkers.SmartShulkers

class BlockPistonListener(private val plugin: SmartShulkers) : Listener {
    private val breakHandler = ShulkerBreakHandler(plugin)

    @EventHandler
    fun onPistonExtend(event: BlockPistonExtendEvent) {
        val target = event.block.getRelative(event.direction)
        val replacement = replacementFor(target) ?: return
        event.isCancelled = true
        dropReplacement(target, replacement)
    }

    @EventHandler
    fun onPistonRetract(event: BlockPistonRetractEvent) {
        if (!event.isSticky) return
        val target = event.block.getRelative(event.direction)
        val replacement = replacementFor(target) ?: return
        event.isCancelled = true
        dropReplacement(target, replacement)
    }

    private fun replacementFor(block: Block) =
        if (block.state !is ShulkerBox) null else breakHandler.buildReplacementItem(block)

    private fun dropReplacement(block: Block, replacement: ItemStack) {
        block.type = Material.AIR
        block.world.dropItemNaturally(block.location.add(0.5, 0.5, 0.5), replacement)
    }
}