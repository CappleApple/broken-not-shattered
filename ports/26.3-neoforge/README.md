# Broken Not Shattered for NeoForge 26.3

Version 1.2.0 preserves durable stacks at zero durability and restores their functionality when repaired. The original NeoForge 1.21.1 project remains at the repository root.

## Installation

Use Minecraft 26.3, NeoForge 26.3.0.6-beta, and Java 25. Install the matching JAR on both client and server. This target uses a beta NeoForge release.

The distributable is `build/libs/broken_not_shattered-neoforge-26.3-1.2.0.jar`. No separate library mod is required.

## Behavior and configuration

The original stack, saved wear seed, name, enchantments, components, and attachments remain intact. Broken items lose mining, use, gliding, and normal weapon/armor combat benefits while retaining attack speed. Repairing one durability point restores their behavior. Old broken stacks receive a seed during authoritative server lifecycle processing.

Client settings remain in `config/broken_not_shattered-client.toml`, with the same tooltip and appearance keys as the [original configuration reference](../../README.md#client-configuration). Item models use resolved render layers; equipment materials and trims use the current equipment render pipeline. Generated textures and fracture geometry have bounded caches. Resource/config reloads and world changes clear them.

Tag priority remains `ignore`, `shatters`, then `protected` or normal damageability. Full datapack paths are:

- `data/broken_not_shattered/tags/item/ignore.json`
- `data/broken_not_shattered/tags/item/shatters.json`
- `data/broken_not_shattered/tags/item/protected.json`

All ship empty. Example contents to allow golden swords to shatter normally in `shatters.json`:

```json
{ "replace": false, "values": ["minecraft:golden_sword"] }
```

Custom renderers that bypass Minecraft's item or equipment render paths need separate integrations. The original 1.21.1 GeckoLib and Mowzie's Mobs adapters are not claimed compatible with this version.

## Development and validation

Run with JDK 25 from this directory:

```powershell
.\gradlew.bat build --max-workers=2
.\gradlew.bat runSmokeServer --max-workers=2
.\gradlew.bat runClientMixinSmoke --max-workers=2
```

`build` runs the unit tests and creates the JAR. `runSmokeServer` launches an isolated, local dedicated server, tests all 84 vanilla damageable items, and exits. Its report is `run-smoke/smoke-result.txt`; it checks preservation, single break callbacks, mining/use suppression, combat attributes, seed copying and persistence, repairs, Sharpness, Protection, and gliding. Fixture classes are excluded from the distributable JAR.

`runClientMixinSmoke` loads the actual client mixins during mod construction, verifies the transformed accessor, writes `run-client-mixin/client-mixin-result.txt`, and exits before a window opens. This and pure geometry/pixel checks are separate from visual inspection in a running client. No manual equipment-render or third-party-mod visual compatibility claim is made for this port.

Licensed under [CC BY-NC-SA 4.0 with Modpack/Server Exception](LICENSE).
