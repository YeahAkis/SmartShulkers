package org.notionsmp.smartshulkers.utils

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
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

    fun getShulkerItems(shulker: ItemStack?): List<Material> = readMaterials(shulker, SmartShulkers.instance.itemsKey)

    fun getShulkerBlacklist(shulker: ItemStack?): List<Material> = readMaterials(shulker, SmartShulkers.instance.blacklistKey)

    private fun readMaterials(shulker: ItemStack?, key: NamespacedKey): List<Material> {
        return shulker?.itemMeta?.persistentDataContainer?.get(key, PersistentDataType.STRING)
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
            add(plugin.mm.deserialize(config.getString("$settingsPath.lore.accepts")!!))
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

    fun getItemName(material: Material): String {
        return material.translationKey()
            .substringAfterLast('.')
            .split('_')
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
    }
}