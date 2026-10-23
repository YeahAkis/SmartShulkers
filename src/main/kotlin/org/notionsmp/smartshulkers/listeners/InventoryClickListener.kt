package org.notionsmp.smartshulkers.listeners

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.notionsmp.smartshulkers.SmartShulkers
import org.notionsmp.smartshulkers.utils.FilterEditor

class InventoryClickListener(private val plugin: SmartShulkers) : Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (!plugin.config.getBoolean("settings.general.filter-methods.inventory-click", true)) return
        if (!FilterEditor.isAnyTypeEnabled()) return

        if (event.click != ClickType.LEFT && event.click != ClickType.RIGHT) return
        val player = event.whoClicked as? Player ?: return
        val cursor = event.cursor
        if (cursor.type == Material.AIR) return
        val clicked = event.currentItem ?: return
        val typeKey = FilterEditor.typeKeyOf(clicked) ?: return

        event.isCancelled = true
        FilterEditor.edit(player, clicked, cursor, typeKey, blacklist = event.click == ClickType.RIGHT)
            ?.let { event.currentItem = it }
    }
}
