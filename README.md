# Reactor

[![License](https://img.shields.io/github/license/Reactor-Hytale/Reactor?style=for-the-badge&color=F3CB09)](LICENSE)
![Status](https://img.shields.io/badge/status-educational-292929?style=for-the-badge)
![JDK](https://img.shields.io/badge/JDK-25-1C89CC?style=for-the-badge)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-7F52FF?style=for-the-badge)

---

**Reactor is not a server you should use. It's a server you should learn from.**

After 4 years of building Minecraft servers from scratch (and failing), I'm documenting everything I've learned about game server architecture. This is the resource I wish I had when I started.

**If you're a developer who wants to understand:**
- How a plugin system really works
- What a microkernel architecture looks like in practice
- Why classloaders matter for isolation
- How to design an SDK that doesn't suck

...then Reactor might help you.

---

## What Reactor is (and isn't)

| **It IS** | **It IS NOT** |
| :--- | :--- |
| A documented architecture study | A production-ready Hytale server |
| A showcase of plugin-first design | A replacement for the official server |
| An educational resource | A finished product |
| My best attempt after 4 years of failure | A competition to anyone |

---

## The core idea

**Plugin-first microkernel architecture:**

- Small, stable kernel
- Clean public APIs
- Implementation details hidden
- Features as plugins
- Explicit lifecycle control
- SDK for plugin developers

[Learn more about the architectural decisions →](https://docs.reactor.codes)

---

## For whom?

1. **Students of software architecture** – See a real microkernel in action
2. **Plugin developers** – Understand what a clean SDK looks like
3. **Curious programmers** – Learn how game servers work internally
4. **Future server creators** – Don't make my mistakes

---

## Quick facts

- **Code:** ~16k lines of Kotlin/Java
- **Architecture:** Microkernel
- **Plugin isolation:** Classloaders
- **Status:** Educational preview (not playable)
- **History:** 4 years of learning from failure

---

## Requirements (to compile and explore the code)

- Java 25
- 256MB RAM (for the kernel)
- Curiosity about server architecture

## Compile (for learning, not for playing)

```bash
./gradlew :launcher:runtime:build
