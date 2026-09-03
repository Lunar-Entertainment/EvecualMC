# EvecualMC - Minecraft Fabric 1.20.1 Base Mod

A complete, production-ready base mod for **Minecraft 1.20.1** using the **Fabric Loader** and **Fabric API**.

---

## 🎮 Quick Start: Launching Minecraft

To launch Minecraft 1.20.1 directly with the mod installed, simply run:

### Windows Batch (Double-click or CLI)
```cmd
launch.bat
```
*(You can also simply double-click `launch.bat` in Windows Explorer)*

### PowerShell
```powershell
.\launch.ps1
```

### Gradle Command
```cmd
.\gradlew runClient
```

> **Note:** The first time you run the client, Gradle and Fabric Loom will download Minecraft 1.20.1 client assets and libraries into the dev environment. All game saves, configurations, and logs for the dev client will be isolated in the `run/` directory.

---

## 📦 Installing to Your Standard Minecraft Launcher

If you prefer playing through the official Minecraft Launcher, CurseForge, Modrinth, or Prism Launcher:

1. Run:
   ```cmd
   launch.bat --install
   ```
   *or*
   ```powershell
   .\launch.ps1 -Install
   ```
2. This will compile the latest release JAR and automatically copy it to `%APPDATA%\.minecraft\mods\`.
3. Launch Minecraft 1.20.1 with Fabric Loader installed from your launcher.

---

## 🔨 Building the Mod JAR

To build the mod JAR without launching:

```cmd
.\gradlew build
```
*or*
```cmd
launch.bat --build
```

The output JAR files will be created in `build/libs/`:
- `evecualmc-1.0.0.jar` (the remapped mod JAR ready for distribution)
- `evecualmc-1.0.0-sources.jar` (mod source code JAR)

---

## 📁 Project Structure

```
evecualMC/
├── build.gradle                               # Gradle build script with Fabric Loom 1.7.4
├── gradle.properties                          # Minecraft, Fabric API, and mod versions
├── settings.gradle                            # Repository and project configuration
├── launch.bat                                 # Windows 1-click launcher script
├── launch.ps1                                 # PowerShell launcher script
├── src/
│   └── main/
│       ├── java/
│       │   └── com/evecual/evecualmc/
│       │       ├── EvecualMC.java             # Main mod entrypoint & item registry
│       │       ├── client/
│       │       │   └── EvecualMCClient.java   # Client-specific mod initializer
│       │       └── mixin/
│       │           └── ExampleMixin.java      # SpongePowered Mixin hook example
│       └── resources/
│           ├── fabric.mod.json                # Fabric mod metadata & entrypoints
│           ├── evecualmc.mixins.json          # Mixin configuration
│           └── assets/
│               └── evecualmc/
│                   └── icon.png               # Mod icon (64x64 / 128x128 PNG)
└── run/                                       # Generated when running client
```

---

## 🧩 Key Configurations

- **Minecraft Version:** `1.20.1`
- **Fabric Loader:** `0.15.11`
- **Fabric API:** `0.92.2+1.20.1`
- **Yarn Mappings:** `1.20.1+build.10`
- **Target Java Version:** `Java 17+` (compatible with Java 17 and Java 21)
- **Mod ID:** `evecualmc`
