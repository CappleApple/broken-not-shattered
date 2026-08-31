# Changelog

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
