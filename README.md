# SmartShulkers

Turn shulker boxes into storage that sorts itself. Give a shulker a filter and it picks up, deletes or sells matching items the moment you walk over them. SmartShulkers is a Paper plugin, players join with a vanilla client. Releases are on [Modrinth](https://modrinth.com/plugin/smartshulker).

Runs on Paper and Purpur from 1.21 up to 26.3.

## The three shulkers

Combine a shulker box (any color) with one extra item in a crafting grid. The contents of the box are kept.

| Shulker | Add | What it does |
| --- | --- | --- |
| Smart Shulker | Book | Picks up matching items straight into itself |
| Garbage Shulker | Lava Bucket | Deletes matching items you walk over |
| Sell Shulker | Emerald | Sells matching items for money, needs Vault and an economy plugin |

Dyeing still works the normal way. A dyed Smart, Garbage or Sell Shulker keeps its type, filter and contents, and so does one that a piston, dispenser or water breaks.

To turn one back into a plain shulker box, put it alone in a crafting grid. The contents stay.

## Setting the filter

Pick up an item and click the shulker in your inventory with it:

- **Left click** adds the item to the whitelist, or removes it if it's already there.
- **Right click** adds the item to the blacklist, or removes it if it's already there.

You can also do it without opening the inventory: hold the shulker in your off hand and the filter item in your main hand, then sneak and click the air. Left click edits the whitelist, right click edits the blacklist. Inventory sorting mods can't trigger that by accident.

Both ways can be switched off under `settings.general.filter-methods` in `config.yml` (`inventory-click` and `hand-shortcut`). If a sorting mod keeps changing filters, turn off `inventory-click`.

A shulker has either a whitelist or a blacklist, never both. Once one list has an item, the other is locked until you empty it again. The lore on the shulker shows whichever list is in use.

With a whitelist, the shulker only takes the listed items. With only a blacklist, it takes everything except the listed items (items in `blacklist.yml` are still skipped). A blacklisted item is never taken. Sell Shulkers only sell items that have a price in `prices.yml`.

## Selling

Sell Shulkers need [Vault](https://www.spigotmc.org/resources/vault.34315/) (or VaultUnlocked) and an economy plugin. Without one the Sell Shulker stays off and its recipe isn't registered.

`settings.sellshulker.sell_when` decides when the contents are sold:

| Value | Sells |
| --- | --- |
| `INSTA` | every item as it is picked up |
| `STACK` | when any stack in the shulker reaches its max size |
| `LAST_SLOT` | when only one free slot is left |
| `FULL` | when every slot is taken |

Prices go in `prices.yml`, either per item or as a stack price:

```yaml
STONE: 0.5        # 1 stone = 0.5
DIAMONDx3: 300    # 3 diamonds = 300, so 100 each
```

## Commands and permissions

| Command | Permission | What it does |
| --- | --- | --- |
| `/smartshulkers reload` (`/ss rl`) | `smartshulkers.reload` | Reloads the config and recipes |
| `/smartshulkers sellall` | `smartshulkers.sell.command` | Sells everything sellable in every Sell Shulker you carry |

The crafting and filter permissions are configurable under `permissions` in `config.yml`:

| Default node | Allows |
| --- | --- |
| `smartshulkers.craft.smartshulker` | crafting a Smart Shulker |
| `smartshulkers.craft.garbageshulker` | crafting a Garbage Shulker |
| `smartshulkers.craft.sellshulker` | crafting a Sell Shulker |
| `smartshulkers.modify.smartshulker` | changing a Smart Shulker's filter |
| `smartshulkers.modify.garbageshulker` | changing a Garbage Shulker's filter |
| `smartshulkers.modify.sellshulker` | changing a Sell Shulker's filter |

## Config

- `config.yml`: names, lore, pickup messages (chat or action bar), sounds, the permission nodes, and which of the three shulkers are enabled.
- `blacklist.yml`: materials that can't go into a filter.
- `prices.yml`: what the Sell Shulker pays.

`settings.general.ignore-items` makes shulkers skip items with a custom name, lore or custom model data. `named` takes plain names or `/regex/i` entries, for example `/Very Cool .*/i`.

## Building

```
./gradlew build
```

The jar ends up in `build/libs`. It needs JDK 21 or newer.

Publishing to Modrinth is `./gradlew modrinth`. It reads the token from the `modrinthToken` Gradle property (or `MODRINTH_TOKEN`), uploads the shaded jar with the newest section of `CHANGELOG.md` as the changelog. Add `-PmodrinthDryRun` to see what would be sent without uploading.

## Metrics

SmartShulkers reports anonymous usage to [bStats](https://bstats.org/plugin/bukkit/SmartShulkers/25707). You can turn that off in `plugins/bStats/config.yml`.

## License

MIT, see [LICENSE](LICENSE).
