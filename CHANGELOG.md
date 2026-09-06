# EvecualMC Updates & Changelog

## [1.6.3] - 2026-09-06
### Fixed & Improved
- **Car Collision & Wall Phasing Fix**:
  - Re-ordered bounding box calculation (`calculateBoundingBox()`) to execute **BEFORE** `move(MovementType.SELF, velocity)` in `CarEntity.java`.
  - Added rotation collision validation (`canRotateTo`) so turning near walls checks for block space availability, preventing AABB embedding inside solid walls.
  - Zeroes horizontal speed on wall collision (`horizontalCollision`), preventing cars from phasing or sliding through walls while rotating or driving.
- **EV Helicopter Autopilot Overhaul**:
  - Re-engineered the 4-stage Helipad Autopilot system:
    - *Stage 0 (Climb)*: Smooth vertical climb to safe cruising altitude with course alignment.
    - *Stage 1 (Proportional Navigation)*: Calculates course heading, smoothly turns towards the target helipad, Banks realistically (-20° to +20° roll), and throttles speed proportionally with alignment and distance.
    - *Stage 2 (Precision Hover & Centering)*: Arrives above pad, levels pitch/roll, damps speed, and aligns heading squarely with North / pad facing.
    - *Stage 3 (Vertical Touchdown)*: Performs controlled vertical descent, seats squarely on pad center, cuts engines, and begins rapid charging.
- **Remade Helicopter Weapon Arm (Rotary Minigun & Rocket Pod Pods)**:
  - Redesigned 3D weapon hardpoint models in `HeliEntityModel.java`: feature heavy carbon wing pylon stubs, 6-barrel Rotary Vulcan Minigun Pods with vented heat shrouds and muzzle flash rings, Quad-Cell Guided Plasma Rocket Pods with glowing red warhead tips, and FLIR Gimbal Targeting pods.
  - Upgraded weapon firing mechanics (`fireWeaponPlasma`): dual kinetic plasma stream with fireworks muzzle flash particles, sonic boom trails, rapid machine-gun audio, and 14.0 damage + explosive impact VFX.
- **16-Block Wireless Helipad Charging**:
  - `HeliChargerBlockEntity` now scans a 16-block radius for any active `ChargerBlockEntity` (Vehicle Charger Base).
  - Helipads wirelessly extract power from any vehicle charger base within 16 blocks to rapid-charge landed EV Helicopters, producing animated electric arc particle beams between the base charger and the helipad pad.

## [1.6.2] - 2026-09-06
### Fixed & Improved
- **Wire Cable Shader Artifact Fix**:
  - Disabled ambient occlusion (`ambientocclusion: false` in models and returning `1.0F` in block class) and configured cutout render layer (`RenderLayer.getCutout()`), eliminating solid black boxes on thin wire cables when shader packs are enabled.
- **Wire Energy Throughput Infobox**:
  - Looking at an electrical wire (`WireBlock`) now displays a live HUD overlay displaying active energy throughput (e.g. `⚡ Transferring 50 EU/t`).
- **Wind Turbine Infobox & 3D Rotor Animation**:
  - Looking at a Wind Turbine displays dedicated HUD telemetry (`⚡ Power Output: 50 EU/t`).
  - Added dynamic 3D 4-blade aerodynamic rotor fan propeller renderer at the top nacelle (Segment 3) with continuous rotation animation.

## [1.6.1] - 2026-09-06
### Added & Improved
- **4-Block High Wind Turbine Multi-Block Structure**:
  - Overhauled **Wind Turbine** into a 4-block high vertical multi-block structure featuring a foundation base, lower & upper lattice shaft columns, and a top turbine head with 4-blade rotor fan.
- **Universal Wire Cable Connection**:
  - Electrical Wires (`WireBlock`) now seamlessly connect to the Wind Turbine on all sides.
  - Power generated (50 EU/t) automatically distributes into all connected wire networks and battery banks attached to the turbine tower.

## [1.6.0] - 2026-09-06
### Added & Improved
- **Wind Turbine ("Vindkraftverk")**:
  - Added directional **Wind Turbine** block (`WindTurbineBlock` / `WindTurbineBlockEntity`).
  - **High Power Generation**: Generates **50 EU/tick** continuously.
  - **Back Output Port**: Power outputs strictly out of the **BACK** face of the block (`facing.getOpposite()`), feeding directly into attached wires, batteries, or chargers.
  - Added crafting recipe (`Steel Ingot`, `Wire`, `Electric Engine`) and Field Guide / Tip Menu documentation.
- **Seamless Multi-Block Battery Cluster Merging**:
  - **Visual Seamless Connection**: Adjacent Battery blocks now connect seamlessly in 6-cardinal directions (`NORTH`, `SOUTH`, `EAST`, `WEST`, `UP`, `DOWN`) with CTM connected texture support.
  - **Unified Energy Matrix**: Contiguous Battery blocks automatically merge into a single unified battery cluster, aggregating total capacity (`N * 600 EU`) and equalizing charge levels across all blocks in the cluster.

## [1.4.0] - 2026-09-05
### Added & Improved
- **EV Helicopter (EV Heli)**:
  - Added full-sized drivable high-speed electric helicopter (`EvHeliEntity` / `HeliItem`).
  - **Flight Speed & Controls**: 12 blocks per second (0.60 bps) normal cruise speed, and **20 blocks per second** (1.00 bps) on Sprint/Boost!
  - **Dynamic Aerodynamics & VTOL Flight**: Space to ascend vertically, Shift/Down to descend smoothly, and active altitude hold hover computer when stationary.
  - **Dual Spinning Rotors**: Detailed main 4-blade top rotor and rear anti-torque tail rotor with dynamic rotation and motion blur.
  - **Aerodynamic Tilting**: Realistic nose-down forward pitch tilt and side banking roll during turns.
  - **Full Customization**: Custom color liveries using Dyes (6 variants), tinted cockpit bubble glass using Stained Glass (12 tints), and high-power turbine upgrades.
  - **Internal Cargo**: 27-slot onboard cargo bay accessible by pressing 'Z'.
  - **Active Chunk Loading**: Automatically keeps its current chunk force-loaded while alive.
- **Heli Charger Helipad**:
  - Added the **Heli Charger** block (`HeliChargerBlock` / `HeliChargerBlockEntity`).
  - Place directly on top of or adjacent to a **Vehicle Charger Base** to power it.
  - Automatically rapid-charges any EV Heli landed on the pad (300 EU/s) with electric charging sparks and sound effects.
- **Electronic Combiner Recipe & In-Game Guide**:
  - Added the **EV Helicopter** CAD blueprint crafting recipe to the Electronic Combiner (Vehicles category).
  - Added Field Guide & Tip Menu topics for EV Heli and Heli Charger.

## [1.3.14] - 2026-09-05
### Added & Improved
- **RC Vehicle Active Chunk Loading**:
  - RC Cars, RC Drones, and RC Robots now maintain active chunk tickets with `ServerWorld.setChunkForced(...)` on their current chunk positions while alive.
  - Vehicles can drive, fly, excavate, and return to base autonomously across vast distances without freezing or unloading mid-flight. Tickets are cleanly released upon entity removal/despawn.
- **3D Parking Spot Elevation & True Alignment**:
  - Fixed an issue where RC vehicles treated the entire vertical Y column above or below a parking spot as a valid docking position.
  - Entities now strictly verify both horizontal centering (`<= 0.35m`) and true vertical elevation (`dy <= 0.45m`) directly on top of the physical parking pad block.
- **Untaken / Unoccupied Parking Spot Selection**:
  - Autonomous return algorithms now check whether a candidate parking spot or helipad is already occupied by another RC vehicle (`isSpotOccupied(...)`).
  - If a pad is occupied, the vehicle automatically skips it and navigates to the nearest available, unreserved parking spot.
- **Buttery-Smooth Autopilot & Docking Animations**:
  - Overhauled vehicle auto-return physics with smooth throttle ramping, proportional steering, dynamic bank/pitch aerodynamic tilt for drones, and smooth tank tread rotation for robots, completely eliminating jerky stutter.

## [1.3.13] - 2026-09-05
### Fixed & Improved
- **Electronic Combiner UI Polish**:
  - Replaced text/emoji labels on blueprint tab buttons with crisp native item icon rendering and active cyan highlight glows, eliminating `VS 16` character glyph artifacts.
  - Resolved slot-on-text overlap by shifting CAD header watermark and re-indexing slot mounting coordinates across all 8 recipes so socket boxes never collide with title banners.
  - Adjusted the bottom live HUD status bar inside the canvas and padded the Player Inventory header (`Y=76`) to eliminate border collision and text overlap.
  - Polished technical CAD side-view vector illustrations for vehicles, drones, utility droids, transceivers, and controller terminals.
- **RC Car Model Floating Spaces & Gap Fix**:
  - Closed the open void behind the cabin by adding a solid rear engine deck & trunk cover.
  - Extended and seated the rear spoiler wing struts directly onto the engine deck without floating gaps.
  - Extended front nose cone, front splitter lip, cabin greenhouse, and side skirts flush against the chassis plate.
  - Placed wheels tightly against chassis sides with seamless axle clearance.
- **RC Drone Model Floating Spaces & Gap Fix**:
  - Replaced isolated disconnected arm squares with continuous structural carbon fiber motor booms integrated directly into the central fuselage.
  - Redesigned landing struts and skids: anchored legs solidly from fuselage to skids, added upturned nose/tail skid tips, and sealed top battery canopy and bottom cargo bay flush with the airframe.

## [1.3.12] - 2026-09-05
### Fixed & Improved
- **RC Robot Trackband (Tread) Rotation Direction Fix**:
  - Inverted the road wheel rotation pitch and tread cleat translation vector in `RcRobotEntityModel`.
  - The top track cleats now roll forward from rear to front and bottom cleats roll backward in true physical contact with the terrain when moving forward, and reverse direction cleanly when driving backward or spinning on the spot.

## [1.3.11] - 2026-09-05
### Added & Improved
- **RC Drone Helipad Autopilot Overhaul**:
  - **Home Helipad Memory**: The RC Drone now records and persists its home helipad (`homeHelipadPos`) across chunk unloads and world restarts via NBT serialization.
  - **Expanded 128m Search Radius & Full World Height**: Expanded autonomous search from 32m to 128m horizontal radius. Rebuilt the scan algorithm using palette chunk section acceleration (`section.hasAny(...)`) across the full vertical build height (-64 to +320), eliminating the bug where high-altitude flight missed ground-level helipads.
  - **Client-Server Autopilot Synchronization**: Registered `AUTO_RETURNING` tracked data with Minecraft's DataTracker so the client knows when autopilot is active. Camera mouse look and remote client heading packets no longer fight or hijack drone heading while returning home.
  - **Precision Approach & Docking**: Smooth cruise altitude tracking (`Math.max(getY() + 3.0, helipadY + 7.5)`), yaw alignment, horizontal glide deceleration, and vertical descent landing cleanly center and align the drone on its helipad.
- **Electronic Combiner Wide Interface & CAD Blueprints**:
  - **Wider 240px GUI**: Expanded the Electronic Combiner interface from 176px to 240px width with custom slate-900 CAD blueprint area (`22, 19` to `200, 71`), centered player inventory slots, and real-time status display.
  - **Side-View Blueprint Visuals**: Added high-detail technical side-view vector illustrations for all 8 craftable machines:
    - *Electric Car*: Sleek coupe silhouette with alloy wheels, chassis frame, raked windshield, tinted canopy, LED headlights, and taillights.
    - *RC Car*: Off-road buggy silhouette with knobby tires, front bullbar, roll cage, high-downforce rear spoiler wing, and whip antenna.
    - *RC Drone*: Quadcopter airframe with central avionics pod, top antenna dome, underslung battery pack, dual rotor booms, and spinning propeller blur disks.
    - *RC Robot*: Tracked utility droid with caterpillar tank treads, armored torso, glowing cyan sensor visor, excavator boom arm, and rear cargo hopper.
    - *RC Controller*: Handheld transmitter contour with ergonomic grip wings, central color telemetry LCD, and top broadcast antenna mast.
    - *Stationary RC Controller*: Standing terminal console with heavy floor pedestal, support pillar, angled keyboard desk, display monitor, and high-gain dish.
    - *RC Sender & RC Receiver*: Technical green & cyber-navy PCB substrates with logic IC chips, quartz crystal oscillators, induction coils, and pin headers.
  - **Physical Machine Socket Positioning**: Crafting slots dynamically move to their exact physical mounting positions on each machine silhouette (e.g. engine in front hood, wheels on rear axle, props on rotors, sensor on robot head).
  - **Accepted Item Names & Component Diagnostics**: Slot info and hover tooltips now clearly display the exact item name accepted by each slot (e.g. `Electric Engine or Upgraded Engine`, `Glass Block or Stained Glass`, `RC Sender`), required quantity, socket location on the machine, and current mount status.

## [1.3.10] - 2026-09-05
### Fixed & Improved
- **RC Robot Auto-Docking Pathfinding & Camera Fix**:
  - Fixed client-server heading conflict where looking through the RC camera in first-person (FPV) or third-person view sent camera mouse packets that fought with and overrode the robot's navigation yaw.
  - Upgraded RC Robot obstacle climbing with 1.25m step height and automatic jump-climbing over ledges, dirt steps, and uneven slopes.
  - Added smart unstick routine: if obstructed for more than 14 ticks, the robot backs up and turns away to navigate cleanly around walls and obstacles.
  - Reduced energy consumption during auto-return to ensure robots low on battery make it safely back to the parking pad.
  - Synchronized auto-return state on client and server to guarantee butter-smooth driving and docking into Robot Parking Spots.

## [1.3.9] - 2026-09-04
### Added & Improved
- **RC Parking & Unparking Forward Advance**:
  - When an RC vehicle (RC Car, RC Drone, or RC Robot) enters its parking spot or charging pad, it securely parks, aligns with the pad's facing direction, powers down, and disconnects/unpairs.
  - When paired or linked again using an RC Controller or Stationary Ground Terminal, the vehicle automatically advances one block forward in its facing direction, clearing the parking spot so it is ready to drive or fly immediately without getting stuck or pinned to the pad.

## [1.3.8] - 2026-09-04
### Added & Improved
- **EvecualTechShader Next-Gen Visual Overhaul**:
  - **Volumetric Sun God Rays (Crepuscular Beams)**: Added real-time screen-space ray marched light shafts streaming down through trees, mountain ridges, and vehicle chassis during sunrise, midday, and sunset.
  - **Screen-Space Ambient Occlusion (SSAO)**: Integrated multi-scale spiral depth ambient occlusion for deep contact shadows under car chassis, tires, block bevels, and structural crevices.
  - **Atmospheric Rayleigh Scattering**: Added dynamic time-of-day sky transitions with warm golden hour horizons, deep azure midday skies, coral sunsets, and star-filled midnight atmospheres with luminous sun/moon coronas.
  - **Dynamic Water Waves & Caustics**: Created custom `gbuffers_water` shaders with animated Gerstner wave displacement, Fresnel specular reflections, and crystal-clear aquatic depth tint.
  - **Waving Foliage & Vegetation**: Added wind wave vertex displacement in `gbuffers_terrain.vsh` for leaves, grass, flowers, and crops.
  - **Automotive Specular Clearcoat & Fresnel**: Upgraded entity and vehicle renderers with multi-lobe specular gloss and rim lighting on the Electric Car, RC Car, RC Drone, and RC Robot.
  - **Dual-Ring Cinematic Bloom & Lens Effects**: High-precision multi-scale Gaussian bloom on neon wires, monitors, battery gauges, ore crystals, and headlights, complete with subtle edge chromatic aberration.

## [1.3.7] - 2026-09-04
### Fixed & Improved
- **RC Parking Spot Forward Alignment & Centering**:
  - RC vehicles (`RcCarEntity`, `RcDroneEntity`, `RcRobotEntity`) now automatically orient facing directly forward with their parking spot block's horizontal facing direction when docking or parking.
  - Vehicles squarely center their position on the pad and lock velocity to prevent sliding or misaligned docking.
- **Passive Headlight Energy Consumption**:
  - Headlights on RC Cars, RC Drones, and RC Robots now consume passive energy (1 EU per second) while active.
  - If the battery drains completely, the lights automatically extinguish.
- **Stationary RC Controller Sneak Fix**:
  - Removed sneak/shift key disconnection from the Stationary RC Controller so pressing Shift (e.g. descending drone or crouching) does not exit the terminal.
  - Disconnection is now cleanly mapped to pressing `F` (camera key) or moving away from the console.
- **Eliminated RC Vehicle Third-Person Vibration**:
  - Fixed client-server position tracking conflict where network packets were overriding rotation and snapping position back and forth while the player piloted in third-person view.
  - Camera rotation lerp now resolves floating-point epsilon jitter cleanly without micro-stutter.
- **Ridable Electric Car Hitbox Adjustment**:
  - Adjusted the standard ridable Electric Car hitbox to exactly 2.0 blocks wide (2.0m width × 1.88m height).

## [1.3.6] - 2026-09-04
### Added & Improved
- **Stationary RC Controller (Ground Command Terminal)**:
  - Added the **Stationary RC Controller** (`stationary_rc_controller`), a heavy-duty ground-based block terminal variant of the handheld RC controller.
  - **Pre-Placement Radio Pairing**:
    - The terminal must be paired before placement: right-clicking the item onto any RC vehicle (RC Car, RC Drone, or RC Robot) synchronizes frequency and locks telemetry directly to that vehicle.
    - Custom tooltip dynamically displays paired vehicle name and state.
  - **Stationary Ground Terminal Operation**:
    - Once placed on the ground, right-clicking the terminal activates the remote control link.
    - Unlike handheld controllers, the operator remains stationary and physically immobilized at the console terminal while controlling the vehicle.
  - **Locked Vehicle Perspective**:
    - When connected to the terminal, the player camera is strictly locked to the remote vehicle's perspective.
    - Player perspective is prevented from switching back to first/third-person player view while operating the terminal.
    - Disconnecting (via Sneak/Shift or pressing F, or walking out of console range) immediately returns camera control to the player.
  - **Fabrication & Field Guide**:
    - Added crafting recipes in the Electronic Combiner (under the RC category) and Crafting Table.
    - Integrated HUD diagnostic overlay and Field Guide entry for the Stationary RC Controller.

## [1.3.5] - 2026-09-04
### Fixed & Improved
- **Wireless RC Charger Energy Intake & Synchronization**:
  - Fixed `BatteryBlockEntity` to recognize `RcChargerBlockEntity` as an active consumer during direct neighbor checks and 64-hop wire network BFS traversal.
  - Increased wire network transfer rate to 10 EU/tick to efficiently charge the Wireless RC Charger (up to 2000 EU capacity).
  - Added direct intake from adjacent batteries in `RcChargerBlockEntity.tick()`.
  - Added client network synchronization (`sync()`) on energy insertion, extraction, and updates so real-time charge levels accurately reflect on HUD overlays and diagnostic screens.
- **Drone Auto-Return & Landing Overhaul**:
  - Fixed search algorithm bug where `x += 2, y += 2, z += 2` skipped 87.5% of coordinates. Implemented an exhaustive, lag-free chunk section search covering the full 64-block radius without skipping any blocks.
  - Added fixed cruise altitude stabilization to prevent altitude drift during return.
  - Refined the 3-stage autopilot: climb to safe cruising altitude, traverse directly over the helipad center, and execute a controlled vertical descent straight onto the pad.
  - Docks smoothly with recharge audio and particle effects upon touching down.
- **Vehicle Platform Centering & Anti-Premature Docking**:
  - Overhauled `isInParkingSpot()` across all vehicles (`RcCarEntity`, `RcDroneEntity`, `RcRobotEntity`): vehicles now require their horizontal center to be within ±0.32–0.35m of the pad's center.
  - Eliminated premature unpairing and engine cutoff when only a fraction of a vehicle's hitbox entered a pad's block coordinate.
  - Auto-return navigation now guides vehicles all the way to the center of their parking pads before shutting down and unpairing.

## [1.3.4] - 2026-09-04
### Added & Improved
- **Wireless RC Charger (Full Cube Power Station & Wire Terminal)**:
  - Upgraded the RC Charger from a flat pad into a full metallic cube terminal: the **Wireless RC Charger**.
  - Enabled electrical wires (`WireBlock`) to directly connect to the Wireless RC Charger on all six faces.
  - Wirelessly broadcasts charging power across a 16-block radius to all designated parking spots.
- **Dedicated Robot Parking Spot (`robot_parking_spot`)**:
  - Added the **Robot Parking Spot** block and item specifically engineered for RC Excavator Robots.
  - Features custom industrial metallic pad textures with amber hazard stripes and robotic iconography.
  - Registered block, block item, models, blockstates, and creative tab entries.
- **Vehicle Docking Separation & Glitch Fixes**:
  - Removed the glitch where the charger block itself acted as a parking spot or auto-return target.
  - Strict 1-to-1 parking spot alignment:
    - **RC Car**: Only docks and auto-returns to **RC Parking Spot** (`rc_parking_spot`).
    - **RC Drone**: Only docks and auto-returns to **Drone Helipad** (`drone_parking_spot`).
    - **RC Robot**: Only docks and auto-returns to **Robot Parking Spot** (`robot_parking_spot`).
  - Vehicles shut down motors and unpair cleanly when entering their designated parking spots.
- **Active "Charge Ready" Particle Transmission Effects**:
  - When the Wireless RC Charger has power (`energy > 0`), it emits a pulsing electric spark idle effect on all in-range parking spots (Car, Drone, and Robot), visually indicating that the spot is connected and ready to charge.
  - Active charging between the wireless station and parked vehicles produces animated electric arc beams and audio hums.
- **Diagnostics & Field Guide Updates**:
  - Added dedicated Field Guide and HUD tips for the Robot Parking Spot and updated Wireless RC Charger information.

## [1.3.3] - 2026-09-04
### Added & Improved
- **Electronic Combiner Multi-Stage Blueprint System & Strict Slot Validation**:
  - **Category-Based Interface**:
    - Opening the Electronic Combiner initially presents two high-tech category options: **🚗 Vehicles** and **📡 Radio Control (RC)**.
    - Selecting **Vehicles** presents ridable road vehicle blueprints (**Electric Car**).
    - Selecting **RC** presents all radio-controlled systems and components: **🏎️ RC Car**, **🚁 RC Drone**, **🤖 RC Robot**, **🎮 RC Controller**, **📡 RC Sender**, and **📟 RC Receiver**.
  - **Strict Slot Insertion Validation**:
    - Every slot strictly enforces its designated ingredient: players cannot insert mismatched items or clutter slots with unauthorized items.
    - Quick-move / Shift-click intelligently routes matching materials exclusively into their designated recipe slots.
  - **Visual Blueprint Guidance & Requirement Checklist**:
    - Ghost holographic items render in empty slots indicating where each required component belongs.
    - Numeric count badges show exact quantities required (e.g. `4x` Steel Ingot, `1x` RC Sender).
    - Real-time slot status highlights met requirements in green and unfulfilled counts in red/amber.
    - Detailed hover tooltips list component names, exact counts (`In Slot: X / Y`), and whether they are required or optional.
  - **Tech Tree Integration & New Electronics**:
    - Registered **RC Sender** (`rc_sender`) and **RC Receiver** (`rc_receiver`) items with custom pixel art textures.
    - Enforced the tech tree hierarchy: RC Car, RC Drone, and RC Robot strictly require an **RC Sender** in Slot 0.
    - The RC Controller strictly requires an **RC Receiver** in Slot 0.
    - Both RC Sender and RC Receiver can be crafted directly in the Electronic Combiner under the RC category.
  - **Seamless Navigation & Refund Protection**:
    - Added `⬅ Menu` and `⬅ Back` buttons enabling smooth navigation between categories, blueprints, and the crafting view.
    - Returning to the menu or changing blueprints automatically refunds non-matching items in crafting slots to the player's inventory to prevent item loss.
    - Added Field Guide entries for RC Sender and RC Receiver in the in-game Tip Menu.

## [1.3.2] - 2026-09-04
### Added & Improved
- **Two-Tier Layered RC HUD (Persistent Telemetry & Prominent Temporary Info Notifications)**:
  - Built `RcHudManager` to handle RC Controller HUD rendering and notification lifecycles.
  - Positioned temporary info notifications (e.g. "No parking or charger within 64 blocks", "RC Car parked", "RC Drone landed", "RC Robot docked & charging", "RC Controller paired") directly **OVER** the persistent telemetry text.
  - Gave temporary notifications a set duration of 5.0 seconds (100 ticks) with a smooth alpha fade-out during the final second.
  - Styled temporary notifications in a golden-amber bordered translucent dark badge matching the mod's visual design.
  - The persistent telemetry line (energy meter, controls, tool, distance, altitude) is rendered cleanly underneath without flickering or getting erased by notifications.
  - Implemented `InGameHudMixin` to seamlessly capture and route all actionbar messages while holding the RC Controller into the temporary notification layer.
  - Added direct and wireless charging support for the RC Robot on the RC Charger pad.

## [1.3.1] - 2026-09-04
### Fixed & Improved
- **Resolved Actionbar HUD Text Overflow & Clipping**:
  - Completely redesigned the actionbar HUD overlay messages for the RC Robot, RC Car, and RC Drone.
  - Replaced overly verbose hotkey strings with compact, color-coded badges (`[F:Cam Z:Cargo L:Light C:Dock]`).
  - Added responsive screen-width detection: automatically drops secondary hints on lower resolutions or high GUI scales to prevent clipping.
  - Implemented `sendSafeActionBar`: dynamically calculates pixel text width against screen bounds and gracefully truncates with an ellipsis if space is constrained, guaranteeing actionbar text never spills off the left or right edges of the screen.
  - Shortened equipped tool names exceeding maximum display width to maintain clean telemetry.

## [1.3.0] - 2026-09-04
### Added & Improved
- **Fixed Text Overlap in In-Game Tip Menu & Field Guide**:
  - Expanded dialog dimensions (420x260) to provide ample room for multi-line explanations and long controls.
  - Added scissor clipping and dynamic multi-line word wrapping for step-by-step setup guides, overview summaries, and control badge labels to eliminate text spilling over borders.
  - Implemented smooth scrollbar and mouse-wheel scrolling for the Topics sidebar, preventing sidebar entries from overlapping the bottom border or help text.
  - Truncated long sidebar topic names cleanly with ellipses.
- **Universal Golden-Bordered HUD Tooltips for All Mod Blocks & Entities (Matching Design)**:
  - Upgraded the in-game crosshair HUD overlay to match the golden/amber glowing border and deep slate backdrop from the reference design across all mod components.
  - Automatically sizes the HUD box to match text content, preventing text truncation or overlap between titles, live energy indicators, and status messages.
  - Added rich tooltips for all blocks and entities: Solar Panel, Battery, Combiner, Car Charger, Charger Extension, Power Wire, Parking Bay, RC Charger, RC Parking Spot, Drone Helipad, Electric Car, RC Car, RC Drone, and RC Robot.
- **All Mod Items Added to the Field Guide**:
  - Added dedicated topics and entries for every remaining mod item: RC Controller, Steel Ingot, Electric Engine, Turbo Engine, Trunk Expansion, Charger Cable, and Artisan Ice Creams (Vanilla, Chocolate, Sweet Berry, Electric).

## [1.2.9] - 2026-09-04
### Added & Improved
- **Interactive Field Guide & Tip Menu for All Mod Blocks & Entities**:
  - Right-clicking any mod block (`SolarPanelBlock`, `BatteryBlock`, `WireBlock`, `ChargerBlock`, `ChargerExtensionBlock`, `ParkingLinesBlock`, `RcChargerBlock`, `RcParkingSpotBlock`, `DroneParkingSpotBlock`) now opens an interactive, modern, dark-glassmorphic Field Guide and diagnostics screen.
  - Right-clicking items in hand while sneaking (`CarItem`, `RcCarItem`, `RcDroneItem`, `RcRobotItem`) opens the tip menu directly from inventory without placing the vehicle.
  - Added quick-access `💡 Tips` buttons inside `CarTrunkScreen` and `ElectronicCombinerScreen`.
  - Added global `H` keybind shortcut: pressing `H` instantly opens the tip and diagnostics menu for whatever vehicle you are driving/controlling, whatever entity or block is in your crosshair, or the item in your hand.
  - Real-time diagnostics bar displaying live EU energy storage, generation rate, charging status, and docking states.
  - Clean sidebar with icons to effortlessly switch between all 14 mod blocks, items, vehicles, and electronics with full control reference tables, setup instructions, and pro tips.

## [1.2.8] - 2026-09-04
### Added & Improved
- **RC Drone Left/Right Strafing in First-Person (FPV) Mode (`A` / `D`)**:
  - In First-Person view, pressing `A` and `D` now performs lateral strafing left and right without altering yaw, creating authentic FPV drone flight mechanics.
  - Added aerodynamic lateral banking roll (-18° left, +18° right) when strafing sideways.
  - In Third-Person orbit view, `A` and `D` remain as in-place yaw rotations, giving the best of both perspectives.
- **Universal Mouse Steering & Aiming in First-Person Mode across All RC Vehicles**:
  - **RC Robot**: Moving mouse in FP mode rotates the robot body directly on the spot (tank tread differential drive) and tilts the ocular sensor dome and right tool arm up/down for precise tool aiming.
  - **RC Car**: Moving mouse in FP mode steers and rotates the vehicle heading through the windshield cockpit.
  - Synchronized real-time yaw over the network for all vehicles (`RC_ROBOT_INPUT_PACKET_ID`, `RC_CAR_INPUT_PACKET_ID`, `RC_DRONE_INPUT_PACKET_ID`) with anti-rubberbanding client packet guards.
  - Unified arrow keys, mouse wheel zoom, RMB perspective toggle, and dynamic action HUD across all RC vehicles.

## [1.2.7] - 2026-09-04
### Added & Improved
- **Direct Mouse Steering for RC Drone in First-Person (FPV) Mode**:
  - In First-Person camera view, horizontal mouse movement now turns the drone heading and yaw directly with zero input latency, providing authentic FPV drone flight feel.
  - Synchronizes real-time drone heading over network (`RC_DRONE_INPUT_PACKET_ID`) to ensure accurate server physics and multiplayer alignment while preserving 144Hz+ local mouse responsiveness without packet rubber-banding.
  - Reset camera yaw offset when switching into FP mode, locking the view along the drone's forward line of sight.
  - Arrow keys also support direct in-place drone steering when in FP mode.
- **Fixed RC Robot Caterpillar Track Bands Rolling During Linear Motion**:
  - Fixed a client-side calculation bug where tank treads were only rolling during in-place turns.
  - Cleats and road wheels now roll and cycle continuously when moving forward, reversing, accelerating, and steering differentially on both client and server.

## [1.2.6] - 2026-09-04
### Added & Improved
- **Real World Block Light on All RC Vehicles (Torch-Level Illumination)**:
  - Upgraded vehicle headlights and spotlights to emit real Minecraft block light at level 15 (equivalent to torches and lanterns) into the surrounding environment.
  - As the vehicle drives or flies, the dynamic light source follows smoothly from block to block, illuminating terrain, dark caves, and structures for all players and shaders.
  - Supports underwater and waterlogged travel; automatically cleans up when turned off or when vehicle is collected.
- **In-Place Drone Rotation (Turns On The Spot)**:
  - Re-engineered drone steering physics: pressing `A` or `D` now rotates the drone cleanly in place on the spot rather than swinging in wide lateral curves.
  - Immediately damps horizontal momentum when rotating in hover mode, keeping the drone centered right where it is.
- **Continuous Drone Propeller Animation & High-Speed Blur**:
  - Fixed propeller animation: quadcopter propellers now spin continuously and smoothly at authentic high RPM whenever the motors are active or the drone is airborne.
  - Preserved rotational angles cleanly in the model to avoid blade jitter, with realistic motion blur disc sweeps and ghost blade trails.
- **Animated Caterpillar Tracks & Band Movement on RC Robot**:
  - Fully animated the RC Robot's tank tread bands ("bands") and internal drive units.
  - Added 8 spinning road wheels (4 per track) that rotate in real-time according to speed and steering direction.
  - Added 24 cycling tread cleats along the top and bottom of the rubber track belts that physically roll across the ground, moving forward on the ground and backward on the return run, with differential rotation during pivot turns.

## [1.2.5] - 2026-09-04
### Added & Improved
- **RC Camera Mouse Scroll Wheel Zoom**:
  - Implemented intuitive scroll wheel zooming while in RC camera mode (`F`):
    - **Third-Person Orbit Mode**: Scroll wheel smoothly adjusts camera distance between 1.0m (tight close-up) and 12.0m (wide panoramic field).
    - **First-Person (FPV) Mode**: Scroll wheel adjusts optical zoom factor from 1.0x to 5.0x magnification, seamlessly adjusting camera FOV.
  - Automatically suppresses hotbar slot switching while zooming through the camera.
- **RMB Perspective Toggle (First Person / Third Person)**:
  - Pressing Right Mouse Button (RMB) in RC camera mode instantly toggles between:
    - **First Person (FPV) Mode**: Authentic cockpit/sensor perspective from the vehicle's eye height (`0.35m` on RC Car, `0.25m` on RC Drone, `0.65m` on RC Robot).
    - **Third Person Orbit Mode**: Exterior chase camera orbiting the vehicle at customizable distance with full mouse aim.
  - Intercepted before Minecraft item use events, eliminating accidental block placement or item consumption while controlling vehicles.
- **Togglable Vehicle Lights ('L')**:
  - Added dedicated toggleable light system for all RC vehicles (`RcCarEntity`, `RcDroneEntity`, `RcRobotEntity`).
  - Pressing `L` (or rebindable key in Controls) toggles headlights/spotlights on the active or aimed RC vehicle with click audio feedback.
  - **Dynamic Night Vision / Illumination**: When vehicle lights are active in camera mode, camera vision is fully brightened, providing crystal-clear visibility in deep caves and nighttime.
  - **3D Light Cone & Beam Rendering**:
    - **RC Car**: Dual forward-projecting headlight beams and glowing lamp lenses.
    - **RC Drone**: High-intensity forward/downward aerial spotlight cone for nighttime reconnaissance.
    - **RC Robot**: Aimable robotic cybernetic visor lamp and work light cone that tracks with head aim.
  - Light state is synchronized across the network and persisted in NBT (`LightOn`).
- **Fixed RC Robot Tool Holding**:
  - Re-engineered 3D tool positioning and matrix hierarchy in `RcRobotEntityRenderer`.
  - Tools (pickaxes, axes, shovels, swords) now grip naturally inside the hydraulic clamp jaws, pointing upright and forward into action orientation.
  - Tool movement dynamically syncs with the articulated arm swing animations during mining.

## [1.2.4] - 2026-09-04
### Added & Improved
- **RC Robot Double Chest Inventory (54 Slots)**:
  - Upgraded RC Robot storage to a full double chest capacity (54 slots, 6 rows x 9 columns) with the standard double chest container interface.
  - **Automated Drop Collection**: All items mined by the RC Robot are automatically placed directly into its 54-slot inventory (overflow drops cleanly at the block).
  - **Vacuum Collector**: Robot automatically vacuums up any nearby dropped items within 1.5 blocks and deposits them into its cargo hold.
  - **Full Inventory Persistence**: The 54-slot cargo bay is preserved in item NBT when the robot is collected (via Sneak + Right Click or broken) and restored when placed in the world.
  - **Inventory Inspection Tooltip**: The RC Robot item tooltip now displays the exact number of used cargo slots.
- **Convenient 'Z' Key Cargo Access**:
  - Pressing `Z` opens the RC Robot's cargo inventory:
    - While controlling the robot remotely (link active or in camera view up to 256m).
    - When standing near or aiming at the RC Robot in the world.
    - Right-clicking with an empty hand while disarmed also opens the 54-slot inventory.
  - Updated controller action bar HUD prompt to include `Z: Cargo`.
- **Authentic Minecraft Tool Mining Physics**:
  - Implemented authentic block hardness and tool suitability mechanics:
    - **One-Shot Mining**: An Axe one-shots leaves, logs, planks, and wooden blocks; a Pickaxe one-shots stone, cobblestone, ores, and concrete; a Shovel one-shots dirt, sand, and gravel.
    - **Progressive Mining with Wrong Tools**: Unsuitable tools (e.g. an Axe hitting Concrete or Stone, or a Pickaxe hitting Logs) do NOT one-shot blocks! They deal progressive mining damage with visual cracking stages (0-9) and hit sounds.
    - **Continuous Mining Support**: Holding LMB continuously strikes target blocks every 4 ticks (5x/sec) with fluid mechanical arm swings.

## [1.2.3] - 2026-09-04
### Added & Improved
- **Ultra-Smooth Mouse Camera Turning**:
  - Replaced stepped arrow key camera rotation with continuous, fluid mouse-driven look controls across all RC vehicles (RC Car, RC Drone, and RC Robot).
  - Intercepts mouse deltas cleanly in `MouseMixin` when RC camera is active, redirecting rotation directly into vehicle orbit tracking without spinning the player's physical avatar.
  - Added exponential smoothing (`lerp(0.25F, ...)`) to camera yaw and pitch for smooth, cinematic panning.
- **Visible Player in RC Camera View**:
  - Configured client `WorldRenderEvents.AFTER_ENTITIES` hook to render the local player character in the world while viewing through the RC camera.
  - Players can now clearly see themselves standing in the environment, holding their RC controller or watching the vehicles operate.
- **RC Robot Companion (`RcRobotEntity` & `RcRobotItem`)**:
  - Added rugged all-terrain RC Robot featuring tank-track locomotion, auto-stepping over full 1-block obstacles, and an optical cybernetic sensor head.
  - **Universal RC Controller Integration**: Aim and right-click with the RC Controller to pair with the robot; drive it remotely using `W`/`A`/`S`/`D` and view its forward camera with `F`.
  - **Tool Equipping & Dual-Hand Actions**:
    - Right-click the robot with any tool (pickaxes, axes, shovels, swords, hoes, shears) to equip it into the robotic arm clamp.
    - Right-click with an empty hand while disarmed to retrieve the equipped tool.
    - Shift + right-click with an empty hand to pick up the robot into your inventory (perserving battery charge and equipped tool).
  - **LMB Remote Tool Use**:
    - While remote-controlling the robot in camera view, pressing Left Mouse Button (LMB / attack key) commands the robot to use its equipped tool where the camera is aimed.
    - Blocks: Mines and breaks blocks in reach (up to 4.5 blocks), checking tool suitability, dropping items, playing break sounds, producing particles, and applying durability damage to the tool.
    - Entities: Attacks hostile or target entities in range, dealing weapon attack damage and knockback.
    - Features animated mechanical arm swings and tool strikes in sync with actions.
  - **Auto-Docking & Charging**:
    - Press `C` to engage auto-docking to nearby RC Chargers or RC Parking Spots.
    - Low battery (<5%) triggers automated homing and safety docking.

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
