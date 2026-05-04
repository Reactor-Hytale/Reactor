# Reactor - Experimental Preview

[![License](https://img.shields.io/github/license/Reactor-Hytale/Reactor?style=for-the-badge&color=F3CB09)](LICENSE)
![Status](https://img.shields.io/badge/status-development-292929?style=for-the-badge)
![JDK](https://img.shields.io/badge/JDK-25-1C89CC?style=for-the-badge)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-7F52FF?style=for-the-badge)

---

This is a showcase of software architecture and design based on all my years of experience building Minecraft servers from scratch. 
It's far from perfect, but it's exactly what I would have wanted when I started learning the Spigot and Hytale APIs :)

## What is?

Reactor is built around a **plugin-first microkernel architecture**.

The core idea is simple:

- keep the kernel small;
- expose stable public APIs;
- isolate implementation details;
- load features as modules or plugins;
- provide explicit lifecycle control;
- avoid accidental dependencies on internals;
- make plugin development easier through an SDK instead of forcing every plugin author to reinvent infrastructure.

In other words, is an attempt to design the kind of extensible server foundation that a large modding ecosystem needs before the ecosystem grows around unstable internals.

---
## Documentation

> [!WARNING]
> Isn't a production-ready Hytale server.
> The code is experimental, incomplete, and intended to validate architectural ideas rather than provide a finished runtime.
> [Click here to see architectural decisions](https://docs.reactor.codes/docs)

---
## Requirements
- [Java 25](https://adoptium.net/es/temurin/releases?version=25&os=any&arch=any)
- 256MB RAM minimum (512MB+ recommended)
- Any modern OS (Linux, Windows, macOS)

## Compile
Use the command: `./gradlew :launcher:runtime:build` and execute the generated jar in `launcher/runtime/build/runtime/`
