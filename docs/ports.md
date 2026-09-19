# Minecraft 26.3 / NeoForge port

This branch contains Broken Not Shattered 1.2.0 for Minecraft 26.3 / NeoForge under [ports/26.3-neoforge](../ports/26.3-neoforge/README.md). The repository root retains the original NeoForge 1.21.1 source and version 1.1.3. The original branch is [main](https://github.com/CappleApple/broken-not-shattered/tree/main).

## Installation

Install the matching port JAR on both client and server with Java 25. See the [target README](../ports/26.3-neoforge/README.md) for loader and dependency versions. Do not install multiple variants together. The `-sources.jar` and Forge `-slim.jar` are development outputs.

## Building

Use JDK 25 to build this port:

```powershell
cd ports/26.3-neoforge
.\gradlew.bat build runSmokeServer --max-workers=2
```

The installable JAR is written to `ports/26.3-neoforge/build/libs/`. From the repository root, the [Windows helper](../scripts/build-ports.ps1) collects the original and this port into `build/ports/`, with SHA-256 checksums:

```powershell
.\scripts\build-ports.ps1
.\scripts\build-ports.ps1 -Target 26.3-neoforge -Validate
```

Set `JAVA17_HOME`, `JAVA21_HOME`, and `JAVA25_HOME` as needed; the helper also searches Gradle's downloaded toolchains. `-Validate` runs the dedicated-server regression gate. Client checks are separate commands in the target README.

## Configuration and validation

Client configuration is `config/broken_not_shattered-client.toml`. Item tags use `data/broken_not_shattered/tags/item/`, with priority `ignore`, then `shatters`, then normal/protected handling. `broken_not_shattered:break_seed` is a saved and synchronized item data component. Copying, saving, and repairing retain the seed within this target. Cross-version world downgrades and loader migration are not supported workflows.

See the [validation results](port-validation.md) for unit, server, and client checks and their limits. The [repository license](../LICENSE) includes the existing modpack/server permission.

## Other version branches

Each branch contains its matching port and preserves the original NeoForge 1.21.1 project.

- [1.20.1-forge](https://github.com/CappleApple/broken-not-shattered/tree/cappleapple/1.20.1-forge)
- [1.20.1-fabric](https://github.com/CappleApple/broken-not-shattered/tree/cappleapple/1.20.1-fabric)
- [1.21.1-fabric](https://github.com/CappleApple/broken-not-shattered/tree/cappleapple/1.21.1-fabric)
- [26.2-neoforge](https://github.com/CappleApple/broken-not-shattered/tree/cappleapple/26.2-neoforge)
- [26.2-fabric](https://github.com/CappleApple/broken-not-shattered/tree/cappleapple/26.2-fabric)
- [26.3-neoforge](https://github.com/CappleApple/broken-not-shattered/tree/cappleapple/26.3-neoforge)
- [26.3-fabric](https://github.com/CappleApple/broken-not-shattered/tree/cappleapple/26.3-fabric)
