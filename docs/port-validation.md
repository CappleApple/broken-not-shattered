# Port validation

Validation performed September 19, 2026. The repository-root NeoForge 1.21.1 implementation was version 1.1.3; this NeoForge 26.3 port was version 1.2.0.

| Target | Build and unit tests | Dedicated server | Client check |
| --- | --- | --- | --- |
| Original NeoForge 1.21.1 | Passed, 33 tests | 7 GameTests passed | Original client implementation unchanged |
| NeoForge 26.3 | Passed, 23 tests | 84 durable items checked | 10 client target classes transformed; accessor verified |

The server checks exercise exhaustion without stack replacement or loss, single break callbacks, repeated damage, mining and use suppression, combat attributes, saved seeds, copying, repairs, and enchantment effects. Unit tests cover deterministic wear and fracture geometry. NeoForge geometry tests also check interpolated vertex colors and fracture-edge normals.

The client check runs through the actual loader, applies client mixins, verifies the transformed accessor, and exits before opening a window. It does not visually validate rendering. Server results are in `run-smoke/smoke-result.txt`; client results are in `run-client-mixin/client-mixin-result.txt`, relative to the port directory.

This port uses the standard item and equipment render paths without the original class-specific GeckoLib and Mowzie's Mobs adapters. NeoForge 26.3 uses a beta loader.

## Reproducing the checks

See the [port guide](ports.md) and [target README](../ports/26.3-neoforge/README.md) for JDK requirements, dependencies, and exact commands. The [Windows build helper](../scripts/build-ports.ps1) collects the original and this target's installable JARs and SHA-256 hashes in `build/ports/`; `-Validate` also runs the server gates.

JUnit reports are under `ports/26.3-neoforge/build/test-results/test/`. Generated reports, logs, game directories, and screenshots are excluded from source control.

## Icon update: 1.1.4 / 1.2.1

Rebuilt the original 1.1.4 and port 1.2.1 distribution JARs and verified that their metadata references the supplied PNG and that the packaged image matches it byte for byte. Gameplay source is unchanged; the runtime checks above were not repeated for this icon update.
