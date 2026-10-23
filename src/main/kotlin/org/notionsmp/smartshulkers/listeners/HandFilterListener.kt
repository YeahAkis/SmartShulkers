package org.notionsmp.smartshulkers.listeners

import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.notionsmp.smartshulkers.SmartShulkers
import org.notionsmp.smartshulkers.utils.FilterEditor

class HandFilterListener(private val plugin: SmartShulkers) : Listener {

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        val blacklist = when (event.action) {
            Action.LEFT_CLICK_AIR -> false
            Action.RIGHT_CLICK_AIR -> true
            else -> return
        }
        val player = event.player
        if (!plugin.config.getBoolean("settings.general.filter-methods.hand-shortcut", true)) return
        if (!player.isSneaking || !FilterEditor.isAnyTypeEnabled()) return

        val shulker = player.inventory.itemInOffHand
        val item = player.inventory.itemInMainHand
        if (item.type == Material.AIR) return
        val typeKey = FilterEditor.typeKeyOf(shulker) ?: return

        event.isCancelled = true
        event.setUseItemInHand(Event.Result.DENY)
        FilterEditor.edit(player, shulker, item, typeKey, blacklist)
            ?.let { player.inventory.setItemInOffHand(it) }
    }
}
