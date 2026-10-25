package org.notionsmp.smartshulkers.utils

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.notionsmp.smartshulkers.SmartShulkers

object FilterEditor {
    fun typeKeyOf(shulker: ItemStack?): NamespacedKey? {
        val plugin = SmartShulkers.instance
        return when {
            ShulkerManager.isSmartShulker(shulker) -> plugin.smartShulkerKey
            ShulkerManager.isGarbageShulker(shulker) -> plugin.garbageShulkerKey
            ShulkerManager.isSellShulker(shulker) -> plugin.sellShulkerKey
            else -> null
        }
    }

    fun isAnyTypeEnabled(): Boolean {
        val config = SmartShulkers.instance.configManager
        return config.isSmartShulkerEnabled || config.isGarbageShulkerEnabled || config.isSellShulkerEnabled
    }

    fun edit(player: Player, shulker: ItemStack, item: ItemStack, typeKey: NamespacedKey, blacklist: Boolean): ItemStack? {
        val plugin = SmartShulkers.instance
        val config = plugin.configManager
        val settingsPath = ShulkerManager.settingsPath(typeKey)
        val typeName = settingsPath.substringAfterLast('.')

        if (!player.hasPermission(config.getString("permissions.modify_$typeName")!!)) return null

        var whitelist = ShulkerManager.getShulkerItems(shulker)
        var blacklistItems = ShulkerManager.getShulkerBlacklist(shulker)
        val removing = item.type in (if (blacklist) blacklistItems else whitelist)

        if (!removing) {
            if (blacklist && whitelist.isNotEmpty() || !blacklist && blacklistItems.isNotEmpty()) {
                SoundManager.playSound(player, "sounds.error")
                sendMessage(player, settingsPath, if (blacklist) "has_whitelist" else "has_blacklist")
                return null
            }
            if (!blacklist) {
                val allowed = if (typeKey == plugin.sellShulkerKey) config.canSellItem(item.type) else !config.isBlacklisted(item.type)
                if (!allowed) {
                    SoundManager.playSound(player, "sounds.error")
                    sendMessage(player, settingsPath, "error")
                    return null
                }
            }
        }

        if (blacklist) {
            blacklistItems = if (removing) blacklistItems - item.type else blacklistItems + item.type
        } else {
            whitelist = if (removing) whitelist - item.type else whitelist + item.type
        }

        return shulker.clone().apply {
            editMeta { meta ->
                val pdc = meta.persistentDataContainer
                pdc.set(plugin.itemsKey, PersistentDataType.STRING, whitelist.joinToString(",") { it.name })
                if (blacklistItems.isEmpty()) {
                    pdc.remove(plugin.blacklistKey)
                } else {
                    pdc.set(plugin.blacklistKey, PersistentDataType.STRING, blacklistItems.joinToString(",") { it.name })
                }
                meta.lore(ShulkerManager.buildLore(settingsPath, whitelist, blacklistItems))
            }
        }
    }

    fun clear(player: Player, shulker: ItemStack, typeKey: NamespacedKey): ItemStack? {
        val plugin = SmartShulkers.instance
        val settingsPath = ShulkerManager.settingsPath(typeKey)
        val typeName = settingsPath.substringAfterLast('.')

        if (!player.hasPermission(plugin.configManager.getString("permissions.modify_$typeName")!!)) return null

        return shulker.clone().apply {
            editMeta { meta ->
                val pdc = meta.persistentDataContainer
                pdc.set(plugin.itemsKey, PersistentDataType.STRING, "")
                pdc.remove(plugin.blacklistKey)
                meta.lore(ShulkerManager.buildLore(settingsPath, emptyList(), emptyList()))
            }
        }
    }

    private fun sendMessage(player: Player, settingsPath: String, key: String) {
        val messagePath = "$settingsPath.message"
        val config = plugin().config
        if (!config.getBoolean("$messagePath.enabled", true)) return
        val message = config.getString("$messagePath.$key") ?: return

        when (config.getString("$messagePath.type")?.uppercase() ?: "ACTIONBAR") {
            "CHAT" -> player.sendMessage(plugin().mm.deserialize(message))
            "ACTIONBAR" -> player.sendActionBar(plugin().mm.deserialize(message))
        }
    }

    private fun plugin() = SmartShulkers.instance
}
