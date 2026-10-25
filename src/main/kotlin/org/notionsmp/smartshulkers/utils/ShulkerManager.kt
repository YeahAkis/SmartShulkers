package org.notionsmp.smartshulkers.utils

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.ShulkerBox
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.notionsmp.smartshulkers.SmartShulkers

object ShulkerManager {
    fun isShulkerBox(material: Material?): Boolean {
        return material in SmartShulkers.SHULKER_BOX_TYPES
    }

    fun isSmartShulker(item: ItemStack?): Boolean {
        return item != null && isShulkerBox(item.type) &&
                item.itemMeta?.persistentDataContainer?.has(SmartShulkers.instance.smartShulkerKey) == true
    }

    fun isGarbageShulker(item: ItemStack?): Boolean {
        return item != null && isShulkerBox(item.type) &&
                item.itemMeta?.persistentDataContainer?.has(SmartShulkers.instance.garbageShulkerKey) == true
    }

    fun isSellShulker(item: ItemStack?): Boolean {
        return item != null && isShulkerBox(item.type) &&
                item.itemMeta?.persistentDataContainer?.has(SmartShulkers.instance.sellShulkerKey) == true
    }

    fun getShulkerItems(shulker: ItemStack?): List<Material> =
        readMaterials(shulker?.itemMeta?.persistentDataContainer, SmartShulkers.instance.itemsKey)

    fun getShulkerBlacklist(shulker: ItemStack?): List<Material> =
        readMaterials(shulker?.itemMeta?.persistentDataContainer, SmartShulkers.instance.blacklistKey)

    private fun readMaterials(pdc: PersistentDataContainer?, key: NamespacedKey): List<Material> {
        return pdc?.get(key, PersistentDataType.STRING)
            ?.split(",")
            ?.mapNotNull { runCatching { Material.valueOf(it) }.getOrNull() }
            ?: emptyList()
    }

    fun accepts(shulker: ItemStack?, material: Material): Boolean {
        val whitelist = getShulkerItems(shulker)
        val blacklist = getShulkerBlacklist(shulker)
        if (material in blacklist) return false
        if (whitelist.isNotEmpty()) return material in whitelist
        return blacklist.isNotEmpty() && !SmartShulkers.instance.configManager.isBlacklisted(material)
    }

    fun settingsPath(typeKey: NamespacedKey): String = when (typeKey) {
        SmartShulkers.instance.smartShulkerKey -> "settings.smartshulker"
        SmartShulkers.instance.garbageShulkerKey -> "settings.garbageshulker"
        else -> "settings.sellshulker"
    }

    fun buildLore(settingsPath: String, items: List<Material>, blacklist: List<Material>): List<Component> {
        val plugin = SmartShulkers.instance
        val config = plugin.configManager
        val itemLine = config.getString("$settingsPath.lore.itemnames") ?: "<dark_gray>- <item>"
        val blacklistLine = config.getString("$settingsPath.lore.blacklist_itemnames") ?: "<dark_red>- <item>"

        return buildList {
            if (blacklist.isEmpty()) {
                add(plugin.mm.deserialize(config.getString("$settingsPath.lore.accepts")!!))
            }
            if (items.isEmpty() && blacklist.isEmpty()) {
                add(plugin.mm.deserialize(config.getString("$settingsPath.lore.empty")!!))
            }
            items.forEach { add(plugin.mm.deserialize(itemLine.replace("<item>", getItemName(it)))) }
            if (blacklist.isNotEmpty()) {
                add(plugin.mm.deserialize(config.getString("$settingsPath.lore.blacklist") ?: "<gray>Excludes:"))
                blacklist.forEach { add(plugin.mm.deserialize(blacklistLine.replace("<item>", getItemName(it)))) }
            }
        }
    }

    fun createShulker(baseItem: ItemStack, typeKey: NamespacedKey, items: List<Material>, blacklist: List<Material> = emptyList()): ItemStack {
        val config = SmartShulkers.instance.configManager
        val settingsPath = settingsPath(typeKey)

        return ItemStack(baseItem.type).apply {
            itemMeta = (itemMeta as? BlockStateMeta)?.apply {
                persistentDataContainer.set(typeKey, PersistentDataType.BYTE, 1)
                persistentDataContainer.set(
                    SmartShulkers.instance.itemsKey,
                    PersistentDataType.STRING,
                    items.joinToString(",") { it.name }
                )
                if (blacklist.isNotEmpty()) {
                    persistentDataContainer.set(
                        SmartShulkers.instance.blacklistKey,
                        PersistentDataType.STRING,
                        blacklist.joinToString(",") { it.name }
                    )
                }

                displayName(SmartShulkers.instance.mm.deserialize(config.getString("$settingsPath.name")!!))
                lore(buildLore(settingsPath, items, blacklist))

                blockState = (baseItem.itemMeta as? BlockStateMeta)?.blockState ?: blockState
            }
        }
    }

    fun createSmartShulker(baseItem: ItemStack, items: List<Material>, blacklist: List<Material> = emptyList()) =
        createShulker(baseItem, SmartShulkers.instance.smartShulkerKey, items, blacklist)

    fun createGarbageShulker(baseItem: ItemStack, items: List<Material>, blacklist: List<Material> = emptyList()) =
        createShulker(baseItem, SmartShulkers.instance.garbageShulkerKey, items, blacklist)

    fun createSellShulker(baseItem: ItemStack, items: List<Material>, blacklist: List<Material> = emptyList()) =
        createShulker(baseItem, SmartShulkers.instance.sellShulkerKey, items, blacklist)

    fun itemFromBlock(block: Block): ItemStack? {
        val plugin = SmartShulkers.instance
        val box = block.state as? ShulkerBox ?: return null
        val meta = Bukkit.getItemFactory().getItemMeta(block.type) as? BlockStateMeta ?: return null
        meta.blockState = box
        val pdc = meta.persistentDataContainer

        val creator: (ItemStack, List<Material>, List<Material>) -> ItemStack = when {
            pdc.has(plugin.smartShulkerKey) && plugin.configManager.isSmartShulkerEnabled -> ::createSmartShulker
            pdc.has(plugin.garbageShulkerKey) && plugin.configManager.isGarbageShulkerEnabled -> ::createGarbageShulker
            pdc.has(plugin.sellShulkerKey) && plugin.configManager.isSellShulkerEnabled -> ::createSellShulker
            else -> return null
        }

        val item = creator(ItemStack(block.type), readMaterials(pdc, plugin.itemsKey), readMaterials(pdc, plugin.blacklistKey))
        val newMeta = item.itemMeta as BlockStateMeta
        val newBox = newMeta.blockState as ShulkerBox
        box.inventory.contents.forEachIndexed { index, stack ->
            if (stack != null) newBox.inventory.setItem(index, stack)
        }
        newMeta.blockState = newBox
        item.itemMeta = newMeta
        return item
    }

    fun getItemName(material: Material): String {
        return material.translationKey()
            .substringAfterLast('.')
            .split('_')
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
    }
}