# Changelog

## 2026-09-19 - 1.2.1

### Changed

- Replace the mod icon with the supplied compact PNG.

### Fixed

- Display the mod icon in Fabric 26.3 mod metadata.
- Collect the current release JAR when older versions remain in the build output directory.

## 2026-09-19 - 1.2.0

### Added

- Add a Fabric build for Minecraft 26.3.
- Add an independent port build and a Windows artifact helper while retaining the original NeoForge 1.21.1 build.

## 2026-09-09 - 1.1.3

### Changed

- Generate jagged cracks with bends, interior endpoints, multiple forks, and secondary branches from each item's saved seed.
- Let most cracks stop within the texture, with occasional complete fractures that separate the item model along matching jagged edges.
- Keep the configured break-line range as the number of main cracks, with smaller branches generated along them.

## 2026-09-09 - 1.1.2

### Changed

- Save each item's break-pattern seed in a server-synchronized item component, preserving its appearance across copies, repairs, item loads, and world joins.
- Assign saved seeds to existing broken items when they are loaded, ticked, equipped, or accessed in a container.

### Fixed

- Keep enchantment glint at its original scale instead of stretching it across the generated texture.
- Remap only worn base-texture coordinates, preserving the normal enchantment overlay on split item geometry.

## 2026-09-09 - 1.1.1

### Fixed

- Keep held-item break patterns and generated textures stable when a renderer submits temporary item copies.
- Apply equipped armor wear through NeoForge's shared texture hook, including replacement player armor layers.
- Apply worn armor trims in Mowzie's Mobs' replacement player armor layer.
- Apply wear to GeckoLib armor that selects its texture internally, including Iron's Spells armor.

## 2026-09-09 - 1.1.0

### Added

- Generate individual broken-item textures with fading, darkening, scuffs, and irregular crack edges.
- Configure an inclusive break-line count range and optional ranges for individual item IDs.
- Show generated wear on equipped humanoid armor, dyed material layers, armor trims, and elytra.
- Apply texture wear to ordinary baked 3D models and item-frame models.

### Changed

- Replace the fixed diagonal split with individually seeded break lines and matching interior cut faces.
- Keep generated appearances stable for each loaded stack and regenerate them after world or resource/config changes.
- Restore original textures immediately when an item is repaired.

## 2026-08-31 - 1.0.2

### Added

- Render broken GUI icons as two procedurally split, slightly separated halves of the resolved item model.
- Apply the split to compatible flat 2D held and dropped models without changing true 3D models in those contexts.
- Fill the exposed held/dropped break with texture-matched interior faces instead of leaving the model open and transparent.

### Changed

- Keep attack-speed modifiers active while an item is broken.

## 2026-08-28 - 1.0.1

### Changed

- Added the supplied Broken Not Shattered artwork as the in-game mod-list icon.

## 2026-08-28 - 1.0.0

### Added

- Preserve handled damageable stacks at genuine zero remaining durability.
- Disable broken-item mining, tool abilities, item use, weapon bonuses, standard combat attributes, and elytra flight.
- Restore all behavior automatically when any system repairs at least one durability.
- Add `ignore`, `shatters`, and `protected` item tags.
- Add a configurable, localized `[BROKEN]` tooltip.
