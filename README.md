# Broken Not Shattered

This branch contains the **Minecraft 1.20.1 / Forge** port, version 1.2.0, in [ports/1.20.1-forge](ports/1.20.1-forge/README.md). The original NeoForge 1.21.1 project remains at the repository root. See the [port guide](docs/ports.md) for installation, builds, and the other version branches.

Broken Not Shattered lets durability items actually reach **broken** instead of disappearing.

When a handled item hits zero durability, the stack stays where it is with its name, enchantments, components, and other stored data intact. Repair it later and it works again.

Built for Minecraft 1.21.1 / NeoForge.

## What happens when an item breaks

Brokenness is based on the item's real durability:

```text
damage >= max damage
```

There is no separate hidden health bar to keep in sync.

While broken, normal tools and equipment stop doing the jobs durability is supposed to gate:

- tools mine roughly like an empty hand and no longer count as the correct tool for drops;
- NeoForge item abilities such as tool actions, shielding, brushing, casting, throwing, and fire-starting are unavailable;
- normal item-use actions cannot be started or completed;
- weapons keep their attack-speed timing but lose their standard weapon damage behavior;
- armor stops contributing its normal armor/toughness/knockback-resistance attributes;
- weapon hit/enchantment callbacks that depend on a functioning weapon are skipped; and
- broken elytra cannot start or continue flight.

The item itself is not deleted or replaced. Its stored components remain there so repairing the same stack restores the original item.

## Repairing

Anything that lowers the item's damage below its maximum repairs it automatically.

That includes vanilla anvils and Mending as well as modded repair blocks, scripts, commands, and direct component changes. Other mods do not need a Broken Not Shattered API just to repair an item.

## Broken appearance

Broken items can receive a worn version of their current texture with fading, darkening, scuffs, and seeded crack patterns. Some flat item models can also separate slightly along a complete fracture.

The pattern is tied to the stack through:

```text
broken_not_shattered:break_seed
```

so two independently broken copies can look different while the same item keeps its pattern through saves, repairs, and later breaks.

The appearance is generated from the item's resolved textures, which means normal resource-pack replacements continue to matter. Standard armor layers, trims, and elytra are supported as well; custom renderers that completely bypass the usual item/armor rendering paths may need dedicated compatibility.

A `[BROKEN]` tooltip is enabled by default.

Appearance is client-side only. The broken gameplay rules are based on real durability and are enforced by common/server logic.

## Datapack tags

Packs can control unusual items with three item tags in the `broken_not_shattered` namespace:

- `broken_not_shattered:ignore` — Broken Not Shattered leaves the item completely alone.
- `broken_not_shattered:shatters` — keep normal Minecraft destruction behavior.
- `broken_not_shattered:protected` — force handling for unusual stacks that expose durability components but are not reported as normally damageable.

Priority is `ignore`, then `shatters`, then normal/protected handling.

Example:

```json
{
  "replace": false,
  "values": [
    "example:unusual_durability_item"
  ]
}
```

placed at:

```text
data/example/tags/item/ignore.json
```

## Client configuration

NeoForge creates:

```text
config/broken_not_shattered-client.toml
```

The main options control the broken tooltip and the generated wear effect:

```toml
[tooltip]
enabled = true
text = "[BROKEN]"
color = "RED"

[appearance]
enabled = true
minBreakLines = 2
maxBreakLines = 4
fading = 0.3
darkening = 0.18
scuffing = 0.18
```

Per-item crack-count overrides are also supported, for example:

```toml
breakLineOverrides = ["minecraft:golden_pickaxe=2-5"]
```

A `0-0` range disables cracks/separation for that item while leaving the other wear effects available. Turning appearance off restores normal visuals without changing the broken-item mechanics.

## Compatibility notes

Broken Not Shattered handles the normal Minecraft/NeoForge durability path. Items from mods that destroy themselves manually or implement all of their functionality through custom code may need to be placed in `ignore` or given a targeted integration.

The normal break callback still occurs when the item crosses from usable to broken, so the usual break animation/sound and statistics can still happen. Further damage attempts on an already broken handled item are ignored until it is repaired.

The generated texture cache is bounded and cleared on resource/world/config changes. If a source texture cannot be processed safely, the original texture is used instead of breaking rendering.

## Building

Requires Java 21.

```text
./gradlew test build
./gradlew runGameTestServer
./gradlew runServer
```

There is also a development client smoke task for checking item, armor, elytra, glint, repair, and optional renderer compatibility. Those test fixtures are excluded from the release jar.
