# EvecualMC Updates & Changelog

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
