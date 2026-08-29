# Broken Not Shattered

Broken Not Shattered is a focused NeoForge 1.21.1 utility mod. Damageable items reach genuine zero remaining durability and become broken instead of disappearing.

The original `ItemStack` stays in place. Its count, components, enchantments, custom name, model data, attachments/capabilities, and mod-specific state are not copied into a replacement and are not rewritten. Brokenness is always derived from the current durability:

```text
damage >= max damage
```

There is no separate broken item, persistent broken flag, inventory scan, custom packet, or fake one-durability state.

## Repair

Any mechanism that reduces the stack's damage below its maximum immediately makes it functional again. Vanilla anvils and Mending, modded repair machines, commands, scripts, and direct component mutation all work without calling a Broken Not Shattered API.

## Broken behavior

Handled broken items:

- mine at hand speed and do not qualify as the correct tool for drops;
- expose no NeoForge `ItemAbility`, including tool actions, sweeping, shielding, casting, throwing, brushing, and fire-starting;
- cannot start or finish their normal item-use actions;
- contribute no standard attack damage, attack speed, armor, armor toughness, or knockback-resistance modifiers;
- do not run weapon hit callbacks or weapon damage/knockback enchantment modifiers;
- cannot start or continue NeoForge elytra flight;
- append `[BROKEN]` to the normal tooltip by default.

The player can still left-click blocks and entities. A broken tool mines approximately like a hand, and a broken weapon attacks approximately like an empty hand. Equipped armor remains equipped. Stored item components and modifiers are never deleted; their effects are filtered only while the durability-derived broken condition is true.

The standard combat-attribute filter is deliberately conservative. It suppresses vanilla's normal weapon and armor combat attributes while leaving unrelated modded utility attributes alone.

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
```

The default text uses `tooltip.broken_not_shattered.broken` for localization. A changed `text` value is displayed literally. `color` accepts Minecraft named text colors; an invalid or non-color name safely falls back to red.

## Compatibility notes

- Durability preservation intercepts the mapped 1.21.1 `ItemStack.hurtAndBreak` path immediately before vanilla shrinks the stack.
- The normal break callback still runs exactly once on the usable-to-broken transition, retaining the usual equipment break animation/sound and statistic where vanilla supplies that callback.
- Further durability attempts on an already-broken handled stack are ignored, preventing repeat effects and overflow.
- Creative players do not consume durability through vanilla paths, as usual. A stack explicitly set to zero durability is still broken in creative; there is no client-side bypass.
- Functional restrictions are evaluated by shared/server game logic. The client-only code is limited to the tooltip.
- Custom items that bypass `ItemStack.hurtAndBreak` and implement their own destruction or functionality may require their owning pack/mod to use `ignore`, or a targeted integration outside this mod's generic scope.

## Building

Use Java 21:

```text
./gradlew build
./gradlew test
./gradlew runGameTestServer
./gradlew runServer
```
