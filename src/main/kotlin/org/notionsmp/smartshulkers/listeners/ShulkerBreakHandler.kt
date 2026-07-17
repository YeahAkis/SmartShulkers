package org.notionsmp.smartshulkers.listeners

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.ShulkerBox
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.persistence.PersistentDataType
import org.notionsmp.smartshulkers.SmartShulkers
import org.notionsmp.smartshulkers.utils.ShulkerManager

class ShulkerBreakHandler(private val plugin: SmartShulkers) {

    fun buildReplacementItem(block: Block): ItemStack? {
        val shulkerBox = block.state as? ShulkerBox ?: return null
        val contents = shulkerBox.inventory.contents
        val meta = Bukkit.getItemFactory().getItemMeta(block.type) as? BlockStateMeta ?: return null
        meta.blockState = shulkerBox

        val creator = when {
            meta.persistentDataContainer.has(plugin.smartShulkerKey) && plugin.configManager.isSmartShulkerEnabled ->
                ShulkerManager::createSmartShulker
            meta.persistentDataContainer.has(plugin.garbageShulkerKey) && plugin.configManager.isGarbageShulkerEnabled ->
                ShulkerManager::createGarbageShulker
            meta.persistentDataContainer.has(plugin.sellShulkerKey) && plugin.configManager.isSellShulkerEnabled ->
                ShulkerManager::createSellShulker
            else -> return null
        }

        return buildReplacementItem(block, meta, contents, creator)
    }

    private fun buildReplacementItem(
        block: Block,
        meta: BlockStateMeta,
        contents: Array<ItemStack?>,
        creator: (ItemStack, List<Material>) -> ItemStack
    ): ItemStack {
        val items = meta.persistentDataContainer.get(plugin.itemsKey, PersistentDataType.STRING)
            ?.split(",")
            ?.mapNotNull { runCatching { Material.valueOf(it) }.getOrNull() }
            ?: emptyList()

        val newShulker = creator(ItemStack(block.type), items)
        val newMeta = newShulker.itemMeta as BlockStateMeta
        val newShulkerBox = newMeta.blockState as ShulkerBox

        contents.forEachIndexed { index, item ->
            if (item != null) {
                newShulkerBox.inventory.setItem(index, item)
            }
        }

        newMeta.blockState = newShulkerBox
        newShulker.itemMeta = newMeta
        return newShulker
    }
}