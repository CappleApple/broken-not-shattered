# Broken Not Shattered for Fabric 1.20.1

Fabric port of Broken Not Shattered 1.2.0. Equipment remains in its slot at zero durability, keeps its data, and becomes usable again after repair. The original NeoForge project remains at the repository root.

## Installation

- Minecraft 1.20.1, Fabric Loader 0.19.5 or newer, and Java 17.
- Fabric API is required; this project builds against `0.92.12+1.20.1`.
- Install the mod and Fabric API on both the client and dedicated server.

Use `build/libs/broken_not_shattered-1.2.0+mc1.20.1-fabric.jar` after building. The `-sources.jar` is for development.

## Behavior

Broken tools mine at hand speed and cannot qualify for tool drops. Broken equipment cannot use its normal item actions. Attack damage, armor, armor toughness, knockback resistance, and combat enchantment effects are suppressed while attack speed and stored enchantments are retained. Repairing one durability point restores the item.

Broken equipment uses a saved per-stack seed for worn textures and fractures. Copying, saving, networking, and repairing preserve that seed. Old broken items receive a seed during server inventory, equipment, dropped-item, item-frame, and open-container processing.

The client renders wear on standard item models, equipped vanilla armor, armor trims, and elytra. Models with custom renderers retain their own rendering. A GeckoLib armor adapter is included but has not been validated with third-party equipment. The rendered smoke test covers vanilla inventory icons; equipped armor, trims, elytra, and third-party renderer combinations require separate visual testing.

## Configuration and tags

Client settings are stored in `config/broken_not_shattered-client.json`. Restart the client or reload resource packs with F3+T after editing it.

```json
{
  "tooltip": { "enabled": true, "text": "[BROKEN]", "color": "RED" },
  "appearance": {
    "enabled": true,
    "minBreakLines": 2,
    "maxBreakLines": 4,
    "breakLineOverrides": ["minecraft:golden_pickaxe=2-5"],
    "fading": 0.3,
    "darkening": 0.18,
    "scuffing": 0.18
  }
}
```

Crack ranges are clamped to 0–8 and reversed ends are sorted. Fading, darkening, and scuffing range from 0 to 1. Zero cracks disables fractures. The default tooltip text uses the translation key `tooltip.broken_not_shattered.broken`; invalid colors fall back to red.

Datapack item tags use these full paths, in priority order:

| Path | Behavior |
| --- | --- |
| `data/broken_not_shattered/tags/items/ignore.json` | Excludes items from this mod. |
| `data/broken_not_shattered/tags/items/shatters.json` | Allows normal destruction on exhaustion. |
| `data/broken_not_shattered/tags/items/protected.json` | Includes items with positive maximum durability even when vanilla does not consider them damageable. |

The tags are empty by default. For example, allow golden swords to break normally:

```json
{ "replace": false, "values": ["minecraft:golden_sword"] }
```

Save that example as the `shatters.json` path above and reload the datapack.

## Development

Run Gradle with JDK 21. The build selects a Java 17 toolchain and produces Java 17 bytecode.

```powershell
.\gradlew.bat build
.\gradlew.bat runGameTest
.\gradlew.bat runClientSmoke
```

`build` runs deterministic wear regression tests. `runGameTest` runs a dedicated Fabric test server. `runClientSmoke` renders test icons in a hidden, muted client, releases the mouse, saves `build/client-smoke/fabric-1.20.1-items.png`, and exits. Test mods and fixtures are in `src/gametest` and are excluded from the distribution JAR.

Licensed under [CC BY-NC-SA 4.0 with Modpack/Server Exception](LICENSE).
