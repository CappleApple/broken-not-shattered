# Port validation

Validation performed September 19, 2026. The repository-root NeoForge 1.21.1 implementation was version 1.1.3; this Fabric 1.20.1 port was version 1.2.0.

| Target | Build and unit tests | Dedicated server | Client check |
| --- | --- | --- | --- |
| Original NeoForge 1.21.1 | Passed, 33 tests | 7 GameTests passed | Original client implementation unchanged |
| Fabric 1.20.1 | Passed, 13 tests | 5 GameTests passed | Hidden, muted client rendered item icons; screenshot inspected |

The server checks exercise exhaustion without stack replacement or loss, single break callbacks, repeated damage, mining and use suppression, combat attributes, saved seeds, copying, repairs, and enchantment effects. Unit tests cover deterministic wear and fracture geometry.

The hidden, muted client rendered healthy and broken inventory icons with distinct seeds and enchantment glint. The screenshot was inspected at `build/client-smoke/fabric-1.20.1-items.png`, relative to the port directory. Equipped armor, trims, elytra, and third-party rendering combinations remain visually unverified.

Optional renderer adapters are described in the target README; their third-party combinations have not been runtime-verified on this port.

## Reproducing the checks

See the [port guide](ports.md) and [target README](../ports/1.20.1-fabric/README.md) for JDK requirements, dependencies, and exact commands. The [Windows build helper](../scripts/build-ports.ps1) collects the original and this target's installable JARs and SHA-256 hashes in `build/ports/`; `-Validate` also runs the server gates.

JUnit reports are under `ports/1.20.1-fabric/build/test-results/test/`. Generated reports, logs, game directories, and screenshots are excluded from source control.

## Icon update: 1.1.4 / 1.2.1

Rebuilt the original 1.1.4 and port 1.2.1 distribution JARs and verified that their metadata references the supplied PNG and that the packaged image matches it byte for byte. Gameplay source is unchanged; the runtime checks above were not repeated for this icon update.
