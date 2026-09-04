# EvecualMC Updates & Changelog

## [1.2.2] - 2026-09-04
### Added & Improved
- **Connected Blocks in Shader & Texture Pipeline**:
  - **Connected Glass**: Seamless borderless glass and glass panes with crystal-clear interiors (clears out distracting interior vanilla scratches/streaks) and crisp beveled outer frames for standard glass, tinted glass, and all 16 stained glass colors.
  - **Connected Bookshelves**: Continuous horizontal shelf connection removing vertical inner borders, creating seamless long wooden library bookshelves.
  - **Connected Mineral / Ore Blocks**: Unified metallic and gemstone paneling for Iron, Gold, Diamond, Emerald, Lapis, Redstone, Netherite, and Copper blocks that connect into massive sleek panels with smooth luster and perimeter bevel highlights.
  - **Connected Emissive Ores & Veins**: Dynamic mineral vein emission for Coal, Iron, Copper, Gold, Redstone, Emerald, Lapis, Diamond, and Nether Quartz ores (and all Deepslate variants). Veins glow with vibrant elemental colors that radiate and bloom across adjacent ore blocks.
  - **Evecual Tech Connected Blocks**: Connected parking bay stripes for RC & Drone parking spots, and seamless multi-block solar panel arrays.
  - **Iris Shader Enhancements (`EvecualTechShader`)**:
    - Added `block.properties` block ID definitions (10001: Glass, 10002: Bookshelf, 10003: Ore Blocks, 10004: Ores, 10005: Evecual Tech).
    - Added normalized face UV and block-type aware shader passes in `gbuffers_terrain` and `gbuffers_textured`.
    - Added in-shader toggle options in `shaders.properties`: `CONNECTED_BLOCKS`, `CONNECTED_GLASS`, `EMISSIVE_ORES`, and `ORE_GLOW_INTENSITY` (Subtle, Vibrant, Radiant).
    - Repacked updated `EvecualTechShader.zip` to both mod `shader/` and client `run/shaderpacks/`.
  - **Continuity & Indium Runtime Support**:
    - Bundled complete CTM connected texture sets (47-tile and horizontal) in `assets/minecraft/optifine/ctm/` and `assets/evecualmc/optifine/ctm/`.
    - Added `indium-1.0.34+mc1.20.1.jar` into `run/mods/` to enable FRAPI for Continuity on Sodium.
    - Configured `options.txt` with active Continuity resource packs.

## [1.2.1] - 2026-09-04
### Added & Improved
- **RC Parking Spot & Drone Parking Spot**:
  - Added dedicated low-profile `RcParkingSpotBlock` for RC Cars and `DroneParkingSpotBlock` for RC Drones.
  - Automatic Vehicle Power-Down: Entering their respective parking spot turns off the vehicle (RC Car cuts throttle and velocity; RC Drone shuts down propellers and lands).
  - Automatic Unpairing: Parked vehicles unpair from their controller, disconnecting the link and requiring explicit re-pairing by aiming and right-clicking with the RC Controller to operate again.
  - Auto-Dock Navigation: Pressing `C` on the controller now intelligently targets either nearby RC Chargers or their respective parking spots.
- **Powered RC Charger & 16-Block Wireless Charging**:
  - Renamed "RC Car Fast Charger" to "RC Charger".
  - RC Chargers now require electrical energy supplied via Wires, Batteries, or Solar Panels (no longer cheat-generates infinite power).
  - 16-Block Wireless Inductive Field: If an RC Charger has stored power, it wirelessly recharges any RC Car parked in an RC Parking Spot or RC Drone docked in a Drone Parking Spot within a 16-block radius.
  - Added animated wireless electric spark transmission beams connecting the charger to parked vehicles.

## [1.2.0] - 2026-09-04
### Added & Improved
- **High-Speed RC Drone (512-Block Range)**:
  - Added new `RcDroneEntity` quadcopter and `RcDroneItem`.
  - Pairable using the same universal `RcControllerItem` (aiming at a drone pairs with the drone; aiming at a car pairs with the car).
  - True 3D quadcopter flight physics with gyroscopic hovering, altitude hold, pitch/roll banking tilt, agile yaw turning, and turbo boost sprint mode (`Ctrl`).
  - Flight controls: `W`/`S` (pitch forward/back), `A`/`D` (roll strafe & bank), `Space` (ascend), `Shift` (descend), `Ctrl` (sprint boost).
  - High-range transmitter antenna delivering **512 blocks** of control range ($512^2 = 262,144$ dist sq) with chunk tracking configured up to 34 chunks (544 blocks).
  - Integrated 9-slot cargo hold (`RC Drone Cargo (9 Slots)`) accessible by right-clicking with an empty hand or pressing `Z`. Drone item retains cargo inventory in NBT upon pickup.
  - Full RC Camera view (`F`), 360° orbit and tilt via arrow keys, and real-time flight telemetry HUD showing battery, altitude, and range.
  - Return to Charger (`C` key): Drones automatically track and land directly onto nearby `RcChargerBlockEntity` pads to fast recharge.
- **Extended RC Car Range**:
  - Increased RC Car remote control range from 64 blocks to **256 blocks** ($256^2 = 65,536$ dist sq) with chunk tracking expanded to 18 chunks (288 blocks).
- **Motion Blur for Propellers, Wheels & Shaders**:
  - **Drone Propellers**: High-speed counter-rotating rotor blur discs and 3 multi-pass ghost blades with exponential alpha falloff.
  - **RC Car Wheels**: Rotational motion blur rim trails that intensify with vehicle velocity.
  - **Full-Size Electric Car Wheels**: High-speed rotational motion blur trails on all four alloy wheels.
  - **Iris Shader Motion Blur**: Integrated velocity-vector motion blur into `EvecualTechShader` composite pass with configurable sample density (Low, Medium, High).

## [1.1.4] - 2026-09-04
### Added & Improved
- **Arrow Keys RC Camera Orbit & Tilt**:
  - While looking through the RC car camera (`F`), press Left / Right Arrow keys to rotate the camera 360° around the RC car.
  - Press Up / Down Arrow keys to tilt the camera up and down.
  - Integrated with `CameraMixin` using bytecode args modification on `Camera.update()` so the camera remains oriented around the vehicle.
  - Camera resets smoothly to default third-person chase angle on camera toggle.
- **Openable RC Car Trunk**:
  - Right-clicking the RC car with an empty hand (without sneaking) opens its compact 9-slot trunk (`RC Car Trunk (9 Slots)`).
  - Pressing `Z` (Open Trunk key) while looking at the RC car also opens its trunk.
  - Sneak + Right-Click retains its function to pick up the RC car into inventory.
  - Items in the trunk are preserved inside the RC car item's NBT data when picked up or dropped upon damage, functioning like a portable mini-trunk.
  - Item tooltip displays the number of items stored in the RC car's trunk.

## [1.1.3] - 2026-09-04
### Fixed & Improved
- **RC Controller Player Immobility**: Injected `KeyboardInputMixin` to zero all player input (`movementForward`, `movementSideways`, `jumping`, `sneaking`) and horizontal velocity when RC remote driving link is active. The player remains completely stationary while controlling the RC car.
- **Dedicated F-Key RC Camera Toggle**:
  - Pressing `F` while piloting the RC car toggles into third-person chase camera mode attached directly to the RC car.
  - Automatically consumes vanilla `swapHandsKey` during the client start tick to prevent accidental offhand item swapping.
  - Automatically restores player perspective if the RC link is deactivated or the car moves beyond 64 blocks.
- **Precision RC Charger Docking & Pathfinding**:
  - Tightened `RcChargerBlockEntity` docking detection box to the physical top pad so cars on adjacent slabs/blocks are never prematurely docked or stopped.
  - Upgraded `RcCarEntity` auto-pilot navigation with proportional throttle approach control, direct yaw alignment, and auto-centering onto the charger pad (docking within 0.25m).
  - Added smart obstacle unstick routine (brief reverse and steering turn) to navigate around walls and corners.
  - Increased inductive fast charging rate from 5 to 10 E/tick with electric spark particles and docking chime.
- **Entity Tracking Range**: Configured `trackRangeChunks(10)` (160 blocks) for both the Electric Car and RC Car to maintain smooth multiplayer & client sync over extended distances.

## [1.1.2] - 2026-09-04
- Added RC Car Fast Charger block with solar inductive trickle charging and auto-docking beacon.
- Increased full-size car auto-park search and pathing radius to 50 blocks.
- Enhanced Iris shader pack skies, dynamic clouds, sun rays, metallic reflections, and emissive block lighting.

## [1.1.1] - 2026-09-03
- Fixed car glass translucency and reflections in Iris shader pack.
- Added shader quality presets (Low, Medium, High).
- Fixed RC Controller pairing mechanism with nearby RC cars.

## [1.1.0] - 2026-09-03
- Added Remote Control (RC) Car and RC Controller item.
- Added Car Fabricator GUI slot tooltips with ingredient hints.
- Added custom Evecual Tech Iris shader pack.
- Added vanilla, chocolate, sweet berry, and electric volt ice cream.
