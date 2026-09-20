# Broken Not Shattered for Minecraft 26.2 / Fabric

Durable items remain at zero durability instead of disappearing. Repair restores their normal behavior. This is a separate build; the original Minecraft 1.21.1 NeoForge project remains at the repository root.

Requires Java 25, Fabric Loader 0.19.5 or later, and Fabric API for Minecraft 26.2. Install the same mod build on the client and server so the server-owned break seed can synchronize.

Set `JAVA_HOME` to a Java 25 installation, then build from this directory:

```powershell
.\gradlew.bat build
```

The installable JAR is written to `build/libs/broken_not_shattered-1.2.1+mc26.2-fabric.jar`. The `-sources.jar` is for development.

Client settings are written to `config/broken_not_shattered-client.json`. Press F3+T after editing to reload appearance settings. The settings match the original behavior:

| Section | Keys |
| --- | --- |
| `tooltip` | `enabled`, `text`, `color` |
| `appearance` | `enabled`, `minBreakLines`, `maxBreakLines`, `breakLineOverrides`, `fading`, `darkening`, `scuffing` |

A break-line override is a string such as `minecraft:golden_pickaxe=2-5`. Both bounds must be 0 through 8. The default is 2 through 4 main cracks; some cracks split the rendered model while shorter cracks stop within the texture. Wear uses the original resource-pack texture and preserves transparency.

Datapacks can control item handling with `data/broken_not_shattered/tags/item/ignore.json`, `shatters.json`, and `protected.json`. `ignore` takes precedence, followed by `shatters`; `protected` covers stacks with durability components that are not normally damageable. No extra entries are provided by default.

Validation commands:

```powershell
.\gradlew.bat test
.\gradlew.bat runSmokeServer
.\gradlew.bat runClientMixinSmoke
```

`runSmokeServer` starts an isolated server, verifies preservation and suppression against loaded game registries, writes `run-smoke/smoke-result.txt`, and stops. `runClientMixinSmoke` checks client mixin transformation before opening a window and writes `run-client-mixin/client-mixin-result.txt`. This gate does not verify rendered appearance in a running client.

Licensed under [CC BY-NC-SA 4.0 with Modpack/Server Exception](LICENSE).

The runtime fixtures are in the development-only `smoke` source set. `build` audits both JARs to exclude fixtures and checks the production metadata, mixins, access widener, and license.
