# Broken Not Shattered: Forge 1.20.1

Forge port of Broken Not Shattered 1.2.0. The original NeoForge 1.21.1 project remains at the repository root.

Durable items stop at zero durability instead of disappearing. They keep their name, enchantments, NBT, and repair history. Broken tools use hand mining speed and cannot harvest tool-required drops; broken weapons and armor lose combat benefits while retaining attack speed. Item use, tool actions, shields, and elytra flight stop until the item is repaired.

Broken items receive a server-owned random wear seed. The seed survives copying, saving, networking, repair, and subsequent breaks. This version stores it in item NBT as `broken_not_shattered:break_seed`. Inventories, opened containers, equipped items, dropped items, and item frames migrate older broken stacks automatically.

Client rendering retains the original textures, tint, animation, and enchantment glint while adding seeded cracks, fading, darkening, and scuffs. Flat item models can separate along complete fractures. Armor, armor trims, and elytra textures also receive wear.

## Installation

- Minecraft 1.20.1
- Forge 47.4.23 or later in the 47.x series
- Java 17
- Install the mod on both the server and clients.

The release JAR includes MixinExtras; no separate library download is required.

## Configuration

Client settings are in `config/broken_not_shattered-client.toml`.

| Key | Default | Effect |
| --- | --- | --- |
| `tooltip.enabled` | `true` | Show the broken-state tooltip. |
| `tooltip.text` | `[BROKEN]` | Use the localized default or custom text. |
| `tooltip.color` | `RED` | Minecraft named color; invalid names fall back to red. |
| `appearance.enabled` | `true` | Enable worn textures and fracture geometry. |
| `appearance.minBreakLines` | `2` | Minimum main cracks, from 0 to 8. |
| `appearance.maxBreakLines` | `4` | Maximum main cracks, from 0 to 8. |
| `appearance.breakLineOverrides` | `[]` | Item-specific ranges, such as `minecraft:golden_pickaxe=2-5`. |
| `appearance.fading` | `0.3` | Desaturation, from 0 to 1. |
| `appearance.darkening` | `0.18` | Darkening, from 0 to 1. |
| `appearance.scuffing` | `0.18` | Scratch density, from 0 to 1. |

Item-tag overrides use these datapack paths:

- `data/broken_not_shattered/tags/items/ignore.json`: exclude items from this mod entirely.
- `data/broken_not_shattered/tags/items/shatters.json`: allow normal destruction at zero durability.
- `data/broken_not_shattered/tags/items/protected.json`: include items with positive maximum durability even when their normal damageability check is false.

Priority is `ignore`, then `shatters`, then normal/protected durability handling. Tag entries do not add durability to items that lack it.

## Compatibility

Forge tool actions, attribute events, armor texture hooks, and custom elytra hooks are covered. Unrelated utility attributes remain active. Custom renderers that bypass baked item models keep their own item appearance.

Optional armor adapters target GeckoLib 4.8.4 and Mowzie's Mobs 1.8.2 for Minecraft 1.20.1. Their texture and trim signatures were checked against the published JARs. These third-party combinations have not been runtime-tested on this port.

## Building and validation

Run from this directory with Java 17:

```powershell
.\gradlew.bat build
.\gradlew.bat test
.\gradlew.bat runGameTestServer
.\gradlew.bat runClient -PclientSmoke
```

The distribution JAR is `build/libs/broken_not_shattered-1.20.1-forge-1.2.0.jar`. The `-slim.jar` is an intermediate artifact without the bundled MixinExtras library.

The opt-in client probe mutes sound, hides its window, releases the mouse, renders healthy and broken icons with multiple seeds and glint, captures `build/client-smoke/forge-1.20.1-items.png`, and exits. Development GameTests and the client probe are excluded from distribution JARs.

Licensed under [CC BY-NC-SA 4.0 with Modpack/Server Exception](LICENSE).
