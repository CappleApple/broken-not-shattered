# Broken Not Shattered

Broken Not Shattered is a focused NeoForge 1.21.1 utility mod. Damageable items reach genuine zero remaining durability and become broken instead of disappearing.

The original `ItemStack` stays in place, preserving its count, enchantments, custom name, model data, attachments/capabilities, and existing mod-specific state. The server adds a saved `broken_not_shattered:break_seed` component to identify its visual pattern. Brokenness is always derived from the current durability:

```text
damage >= max damage
```

The saved seed controls appearance only. Functional behavior continues to follow the real durability boundary, and the component uses normal item save data and inventory/equipment synchronization.

## Repair

Any mechanism that reduces the stack's damage below its maximum immediately makes it functional again. Vanilla anvils and Mending, modded repair machines, commands, scripts, and direct component mutation all work without calling a Broken Not Shattered API.

## Broken behavior

Handled broken items:

- mine at hand speed and do not qualify as the correct tool for drops;
- expose no NeoForge `ItemAbility`, including tool actions, sweeping, shielding, casting, throwing, brushing, and fire-starting;
- cannot start or finish their normal item-use actions;
- retain their attack-speed modifiers while contributing no standard attack damage, armor, armor toughness, or knockback-resistance modifiers;
- do not run weapon hit callbacks or weapon damage/knockback enchantment modifiers;
- cannot start or continue NeoForge elytra flight;
- generate individual worn textures with jagged, branching cracks, faded colors, darkening, and scuffs;
- let cracks turn, fork repeatedly, and stop inside the texture, with occasional complete fractures separating GUI icons and compatible flat held/dropped models along matching jagged edges;
- show worn textures on equipped humanoid armor, its material layers and trims, and elytra in real time;
- append `[BROKEN]` to the normal tooltip by default.

The player can still left-click blocks and entities. A broken tool mines approximately like a hand, and a broken weapon attacks approximately like an empty hand. Equipped armor remains equipped. Stored item components and modifiers are never deleted; their effects are filtered only while the durability-derived broken condition is true.

The standard combat-attribute filter is deliberately conservative. It suppresses vanilla's normal weapon damage and armor combat attributes while retaining attack speed and leaving unrelated modded utility attributes alone.

The broken appearance is generated from the resolved item sprites and armor textures, including resource-pack replacements. The server assigns a random seed the first time an item breaks: two independently broken gold pickaxes have different patterns. The seed survives copying, repair, subsequent breaks, network synchronization, saving, and world joins. A deliberate duplicate of an already seeded item retains its pattern. Temporary held-item render copies also reuse the equipped stack's geometry cache.

Existing broken items without a seed receive one on the server through inventory ticks, equipped-item updates, dropped-item/item-frame loading, or opening their container. Renderers never assign seeds or display a provisional random pattern. Rebuilding the texture cache or reloading resources reproduces the saved pattern; changing appearance settings can intentionally change the generated result.

The client preserves item tinting, glint, model overrides, original render-pass state, and sprite animation metadata. Only the worn base texture uses remapped texture coordinates; enchantment glint keeps the model's original atlas coordinates and normal scale. GUI icons and flat held/dropped models separate along the generated seams, with textured interior faces through the model depth. True 3D held models receive texture wear without geometric separation. Other ordinary baked-item contexts, including item frames, receive texture wear too.

Equipped armor uses the same stack pattern for its material textures and trims. The normal model, equipment slot, dye colors, glint, and animations remain in use. Material wear runs after NeoForge's shared armor texture selection, including replacement player armor layers that use that hook. GeckoLib armor that selects its own texture internally has a separate integration. Trims are supported in the standard humanoid armor layer and Mowzie's Mobs' replacement player layer. Breaking or repairing an item changes its appearance on the next render, including on entities already wearing it. Elytra use their resolved texture when it is available as a resource.

Generated textures exist only in memory. The cache is bounded to 256 textures and 32 MiB of generated pixel storage, expires unused textures after 15 seconds, and clears on world changes and resource/config reloads. Individual source images larger than 1,048,576 pixels, unavailable textures (such as downloaded cape textures), and temporary cache exhaustion fall back to their original textures. Items with a completely custom renderer, and armor renderers that bypass NeoForge's texture hook or replace its result internally, need an integration with that renderer.

## Datapack item tags

All tags are under the `broken_not_shattered` namespace and ship empty so packs can populate them.

- `broken_not_shattered:ignore` makes the mod do absolutely nothing to the item. The owning mod retains complete control.
- `broken_not_shattered:shatters` retains normal Minecraft break/destruction behavior but does not opt out of unrelated owning-mod behavior.
- `broken_not_shattered:protected` is a force-include escape hatch for unusual stacks that expose positive `DAMAGE` and `MAX_DAMAGE` components but are not reported as normally damageable.

Priority is `ignore`, then `shatters`, then normal/protected handling. Therefore `ignore` wins if an item appears in multiple tags.

Example tag file at `data/example/tags/item/ignore.json`:

```json
{
  "replace": false,
  "values": [
    "example:unusual_durability_item"
  ]
}
```

## Client config

NeoForge writes the client config as `broken_not_shattered-client.toml`:

```toml
[tooltip]
enabled = true
text = "[BROKEN]"
color = "RED"

[appearance]
enabled = true
minBreakLines = 2
maxBreakLines = 4
breakLineOverrides = []
fading = 0.3
darkening = 0.18
scuffing = 0.18
```

The default text uses `tooltip.broken_not_shattered.broken` for localization. A changed `text` value is displayed literally. `color` accepts Minecraft named text colors; an invalid or non-color name safely falls back to red.

The break-line range counts main cracks and is inclusive, with each end between `0` and `8`; reversed ends are sorted. Each main crack can develop smaller branches, including branches that split again. Most main cracks stop inside the texture; only complete fractures separate model pieces. To customize a particular item, set, for example, `breakLineOverrides = ["minecraft:golden_pickaxe=2-5"]`. The last entry for an item wins. A `0-0` range disables its break lines and geometric separation while retaining surface wear. Fading, darkening, and scuffing each accept `0.0` through `1.0`; zero disables that modifier. Setting appearance `enabled = false` restores ordinary visuals without changing broken-item mechanics. These settings take effect when NeoForge reloads the client config.

## Compatibility notes

- Durability preservation intercepts the mapped 1.21.1 `ItemStack.hurtAndBreak` path immediately before vanilla shrinks the stack.
- The normal break callback still runs exactly once on the usable-to-broken transition, retaining the usual equipment break animation/sound and statistic where vanilla supplies that callback.
- Further durability attempts on an already-broken handled stack are ignored, preventing repeat effects and overflow.
- Creative players do not consume durability through vanilla paths, as usual. A stack explicitly set to zero durability is still broken in creative; there is no client-side bypass.
- Functional restrictions are evaluated by shared/server game logic. Appearance and tooltip code run only on the client.
- Custom items that bypass `ItemStack.hurtAndBreak` and implement their own destruction or functionality may require their owning pack/mod to use `ignore`, or a targeted integration outside this mod's generic scope.

## Building

Use Java 21:

```text
./gradlew build
./gradlew test
./gradlew runGameTestServer
./gradlew runServer
./gradlew runClientSmoke
```

`runClientSmoke` launches an isolated development client, creates a temporary flat world under `run-client-smoke`, and captures real renders in `build/client-smoke`. It checks distinct item patterns, equipped player armor and elytra, texture-object reuse across 30 frames with fresh stack copies, repair restoration, and unchanged tool pixels after item/resource reloads. `-PsmokeEnchanted=true` uses enchanted diamond axes and equipment with glint animation frozen for pixel comparisons. `-PsmokeAnimationCompat=true` requires EMF, ETF, Better Combat, its EMF compatibility bridge, and an active `FA+Player-v1.1.zip`; it also renders a Better Combat attack and captures enlarged first-person items.

With Mowzie's Mobs installed in the test client's mods directory, `-PsmokeArmorLayer=mowzie` exercises its replacement armor layer. `-PsmokeArmorSet=irons_spellbooks:netherite_mage` checks Iron's Spells' GeckoLib armor. The test uses separate players with a fixed skin and removes party-hat cosmetic layers from its test renderer for repeatable screenshots. Dedicated GameTests cover seed generation, old-item migration, copying, save/load, network serialization, repair, and re-breaking. The smoke-test class and GameTests are excluded from the distributable JAR.
