# EvecualMC ⚡ Helicopters, Electric Cars, Autonomous RC Drones & Power Grids

[![Minecraft 1.20.1](https://img.shields.io/badge/Minecraft-1.20.1-brightgreen.svg)](https://modrinth.com/mods)
[![Fabric Loader](https://img.shields.io/badge/Modloader-Fabric-blue.svg)](https://fabricmc.net)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://adoptium.net)
[![License: Unlicense](https://img.shields.io/badge/License-Unlicense-lightgrey.svg)](https://unlicense.org)

**EvecualMC** is a feature-rich, high-tech engineering and vehicle mod for **Minecraft 1.20.1 (Fabric)**. Built with precision physics, modern rendering, and deep automation systems, EvecualMC brings drivable electric helicopters, ridable road cars, autonomous radio-controlled (RC) drones, tracked mining droids, wireless power grids, CAD blueprint crafting, and next-gen visual shaders directly into your world!

---

## 🌟 Key Highlights

* **🚁 Drivable EV Helicopters & VTOL Aerodynamics**: Cruise the skies at **12–20 blocks/second** with dynamic main and tail rotor blur, pitch/roll banking tilt, dye liveries, stained glass tints, 27-slot onboard cargo bay, and active chunk loading.
* **🚗 Full-Sized Electric Cars**: High-speed 2.0m wide ridable road vehicles with trunk storage, custom dye jobs, auto-park navigation, and charger stations.
* **📡 Autonomous RC System (Cars, Drones & Mining Robots)**:
  * **RC Buggy**: High-speed off-road vehicle with 256m remote range and 9-slot trunk.
  * **RC Quadcopter Drone**: 512m control range, authentic FPV optics (1x–5x zoom), lateral strafing, 9-slot cargo, altitude hold, and autonomous helipad docking.
  * **RC Excavator Robot**: All-terrain tracked utility droid featuring a **54-slot chest**, auto-vacuuming drops, authentic tool mining (pickaxes, axes, shovels, swords), jump climbing, and active chunk loading.
* **🎮 Handheld Controllers & Base Terminals**: Operable via handheld telemetry controllers with active HUD overlays or stationary ground command terminals with camera locks.
* **⚡ Solar & Wireless Power Infrastructure**: Construct solar panels, energy storage batteries, power wires, car chargers, and **16-block Wireless RC Chargers** with electric spark transmission fields.
* **📐 Electronic Combiner (CAD Blueprint Crafting)**: A 240px slate-900 GUI with technical side-view vector blueprints, strict socket validation, component holograms, and diagnostic tooltips.
* **✨ Custom EvecualTechShader & Connected Textures**: Built-in shader pack delivering volumetric sun god rays, SSAO, dynamic waving water/foliage, motion blur, connected glass/ores/bookshelves, and emissive glowing ore veins.
* **💡 Interactive Field Guide & Diagnostics**: Press `H` anytime to open a dark-glassmorphic guide screen with live energy telemetries, topic guides, and full control references.

---

## 🚁 EV Helicopter (EV Heli)

The flagship aerial vehicle in EvecualMC!
- **Flight Mechanics**: Vertical Take-Off and Landing (VTOL). Hold `Space` to ascend, `Shift` to descend, and use `W/A/S/D` for directional flight. Tap `Ctrl` for High-Speed Sprint Boost (20 blocks/sec).
- **Aerodynamics & Visuals**: Dual spinning 4-blade top rotor and rear anti-torque tail rotor, realistic nose pitch tilt during forward flight, and side banking roll on turns.
- **Customization**: Apply any Minecraft Dye to change fuselage liveries (6 variants) or Stained Glass for cockpit bubble tints (12 tints).
- **Logistics**: Includes an internal **27-slot cargo bay** (Press `Z` to access) and keeps its active chunk force-loaded while airborne.
- **Helipad Rapid Charging**: Land on a **Heli Charger** pad connected to your power grid for automated rapid recharging (300 EU/s).

---

## 📡 Radio-Controlled (RC) Vehicle Suite

Piloting RC vehicles provides long-range exploration, automated logistics, and remote mining without risking your player:

| Vehicle | Range | Cargo | Special Features |
| :--- | :---: | :---: | :--- |
| **🏎️ RC Car** | 256m | 9 Slots | Fast off-road handling, directional camera orbit, wireless fast charging. |
| **🚁 RC Drone** | 512m | 9 Slots | FPV camera mode with 1x–5x scroll zoom, lateral `A/D` strafing, togglable spotlights, 128m auto-return to home helipad (`C`). |
| **🤖 RC Robot** | 256m | 54 Slots | Caterpillar tank tread animation, auto-vacuuming drop collection, tool equipping (Pickaxe/Axe/Shovel/Sword), LMB remote mining, auto-climbing (1.25m step height). |

---

## 🎮 Controls Quick Reference

| Action | Control | Context |
| :--- | :---: | :--- |
| **Open Field Guide / Diagnostics** | `H` | Global / Crosshair on block or vehicle |
| **Camera View Toggle (FPV / Chase)** | `F` or `RMB` | Active RC Controller link |
| **Vehicle Spotlight / Headlights** | `L` | Active RC Controller link |
| **Access Vehicle Cargo Bay** | `Z` | Near vehicle or active RC link |
| **Auto-Return & Docking** | `C` | RC Drone / RC Robot / RC Car |
| **Camera Zoom (FPV & Orbit)** | `Scroll Wheel` | Active Camera View (`F`) |
| **Orbit Camera Angle** | `Arrow Keys` / `Mouse` | Active Camera View (`F`) |
| **Helicopter Boost (20 bps)** | `Ctrl` | Piloting EV Helicopter |

---

## ⚡ Electric Power Grid & Infrastructure

Build complete industrial energy networks to automate vehicle charging:
1. **Solar Panels**: Clean daytime energy generation.
2. **Battery Storage Units**: High-capacity EU buffer storage for your grid.
3. **Power Wires**: Connect blocks across multi-node electrical networks.
4. **Car Charger & Extension Pads**: High-rate charging stations for ridable Electric Cars.
5. **Wireless RC Charger & Parking Pads**: Broadcasts inductive power across a 16-block radius to designated **Car Parking Spots**, **Drone Helipads**, and **Robot Parking Spots**.

---

## 📐 Electronic Combiner (CAD Crafting)

Crafting advanced vehicles requires the **Electronic Combiner**:
- Select between **🚗 Vehicles** and **📡 Radio Control (RC)** categories.
- View real-time 2D technical CAD vector blueprints for every machine.
- Strict socket insertion ensures ingredients (Engines, Glass, RC Senders, RC Receivers) are mounted in their exact physical locations.
- Ghost hologram outlines guide component placement with real-time status diagnostics.

---

## ✨ Next-Gen Visuals & Shader Integration

EvecualMC includes **EvecualTechShader**, a custom Iris-compatible shader pack optimized for high performance and visual beauty:
- **Atmospheric Lighting**: Screen-space volumetric sun rays (god rays), Rayleigh sky scattering, and multi-scale Gaussian bloom on emissive wires and headlights.
- **Surface Detail**: Screen-Space Ambient Occlusion (SSAO), clearcoat automotive specular reflections, and animated Gerstner water waves.
- **Connected Textures & Emissive Ores**: Seamless connected glass, connected bookshelves, connected mineral blocks, and glowing emissive ore veins.

---

## 📦 Requirements & Installation

### Requirements
- **Minecraft**: `1.20.1`
- **Fabric Loader**: `>= 0.15.11`
- **Fabric API**: `>= 0.92.2+1.20.1`
- **Java**: `Java 17` or higher

### Installation
1. Install **Fabric Loader** for Minecraft 1.20.1 on your launcher (Modrinth App, Prism Launcher, CurseForge, or Official Launcher).
2. Download **Fabric API** and **EvecualMC** `.jar` files.
3. Place both `.jar` files into your `.minecraft/mods` directory.
4. Launch the game and enjoy!

---

## 📄 License

This mod is released under the **Unlicense** — public domain software. Feel free to use EvecualMC in any modpack, server, or project!
