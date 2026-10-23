# Changelog

## 1.1.1

- Smart, Garbage and Sell Shulkers keep their type, filter and contents when a piston, dispenser or flowing water breaks them, before they dropped as plain shulker boxes

## 1.1.0

- Added a blacklist to every shulker. Left click with an item still edits the whitelist, right click now edits the blacklist. A shulker with only a blacklist takes everything except the listed items
- Added a way to edit filters without the inventory screen, which sorting mods can trigger by accident (#11): hold the shulker in your off hand and the item in your main hand, sneak and left click the air for the whitelist or right click the air for the blacklist
- Both filter methods have a toggle in `settings.general.filter-methods`, so servers with sorting mods can turn off inventory clicking
- Fixed shulker boxes not being craftable with anything but SmartShulkers' own recipes, vanilla dyeing works again (#11). Dyeing a Smart, Garbage or Sell Shulker keeps its type, filter and contents
- Garbage and Sell Shulkers now keep their contents when they are crafted from a filled shulker box, before only Smart Shulkers did
- A shulker that is already a Smart, Garbage or Sell Shulker can no longer be run through a conversion recipe again
- Fixed picking up part of a stack into a Smart Shulker still giving you the whole stack, which duplicated items
- `ignore-items.named` now understands the `/regex/i` form shown in the config, plain entries are matched case insensitively against the plain item name
- No longer rewrites `config.yml` (and strips its comments) when Vault is missing, sell shulkers are just inactive
- The three identical revert recipes per shulker color are now a single one
- Verified on Paper up to Minecraft 26.3
