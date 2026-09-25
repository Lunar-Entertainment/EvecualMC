# EvecualMC Updates & Changelog

## [1.8.38] - 2026-09-25
### Added & Improved
- **Elactorite Block**:
  - Added new solid crystal block: `Elactorite Block` (`evecualmc:elactorite_block`).
  - Decorative and structural crystal block crafted from 9 Elactorite crystals (and unpackable back into 9 crystals).
  - Pure decorative and portal conduit: does not consume electricity, does not require energy connections, does not display HUD overlay tips, and omits field guide help pages.
  - Mineable with pickaxes (iron or higher tier).
- **Railgun Ignite Mode & Dimensional Rift**:
  - Added "Ignite Mode" to the Lorentz Railgun.
  - Pressing `Shift + 5` while holding the railgun toggles between Standard kinetic weapon mode and Ignite Mode.
  - Intercepted seamlessly via `KeyboardMixin` so hotbar slots do not accidentally switch while activating Ignite Mode.
  - When in Ignite Mode, right-clicking on an Elactorite Block activates a dimensional rift transporting the player to the Evecual Dimension.
  - Right-clicking an Elactorite Block in the Evecual Dimension safely teleports the player back to the Overworld.
- **The Evecual Dimension**:
  - Added custom superflat dimension: `evecualmc:evecual`.
  - Pristine superflat generation with 1 layer of bedrock, 2 layers of dirt, and 1 surface layer of grass block, anchored at `y=3` with an arrival Elactorite return block.
  - Totally devoid of structures (`structure_overrides: []`), trees, and features (`features: false`, `lakes: false`).
  - Zero natural mob spawns (`spawners` emptied, monster light level clamped to 0, backed by `SpawnHelperMixin` and `MobEntitySpawnMixin` preventing all natural and ambient entity spawns).

## [1.8.37] - 2026-09-22
### Added & Improved
- **Turret Whitelist & Blacklist Player Filtering**:
  - Added full gametag filtering to Stationary Turrets with Whitelist and Blacklist modes.
  - Interactive dual-tab screen: `[⚙ Targets]` for mob target types and `[👥 Filter]` for player gametag management.
  - In-game text field with `+ Add` button, paginated player entries with `✕` removal buttons, and `Clear All`.
  - Dedicated client-to-server and server-to-client network packets ensuring seamless filter sync in multiplayer.
- **Fixed Turret Aiming & Ballistics**:
  - Corrected yaw azimuth calculation, resolving horizontal barrel aiming inversion.
  - Implemented predictive target leading: turrets calculate projectile flight time and target velocity to lead fast-moving targets.
  - Clamped pitch rotation (-60° to +45°) to prevent erratic upside-down twisting.
  - Smooth client-side barrel interpolation using previous tick angles.
  - Offset bullet muzzle spawn point and added initial tick grace to prevent projectiles from colliding with the turret base.
- **Crown Slot Mod Compatibility Overhaul**:
  - Permanently resolved item-overriding bug with Trinkets, Traveler's Backpack, and Elytra Slot.
  - Removed crown slot injection from vanilla `PlayerScreenHandler.slots`, preserving exact slot IDs across all third-party inventory mods.
  - Crown slot is rendered as an isolated client-side widget with dedicated packet handling (`CROWN_SLOT_CLICK_PACKET_ID`), preventing slot collision and random item replacement.
- **Electrical Grid Stabilization & "Short Circuit" Fix**:
  - Resolved circular energy loops where multiple generators or batteries would stall charging across the network.
  - Solar panels and wind turbines prioritize machines before batteries and ignore peer generators.
  - Battery clusters designate a deterministic master coordinator for network distribution.
  - Throttled network synchronization packets in wires, preventing network lag and packet flood.
- **Expanded Long-Distance Cable Range**:
  - Increased cable power transfer range from 32 blocks to 1024 blocks, supporting large base grids and remote power plants.
- **HUD & GUI Text Overlap Fixes**:
  - Cleaned up container title rendering across all mod screens (`StationaryTurretScreen`, `StorageUnitScreen`, `ElectricGrinderScreen`, `ElectronicDuperScreen`, `ItemChargerScreen`, `MaterializerScreen`, `TurretAmmoContainerScreen`).
  - Adjusted Energy HUD waypoint Y-offset and RC HUD bar position to eliminate overlaps with vehicle gauges and action bar messages.
- **Removed Right-Click Help Spam**:
  - Completely removed unsolicited tip screen popups when interacting with blocks or items.
- **Extensive Advancement Progression Tree**:
  - Added 45+ advancement entries covering every machine, energy component, vehicle, RC robot, weapon, ammo type, and crown in EvecualMC.

## [1.8.36] - 2026-09-19
### Added & Improved
- **Custom Music Discs: "Circuit & Stone" and "Voltage Valley"**:
  - Integrated high-fidelity custom soundtrack discs crafted from `/msc/` audio tracks:
    - **Circuit & Stone** (`evecualmc:music_disc_circuit_and_stone`):
      - Duration: 2 minutes 51 seconds (171s).
      - Redstone comparator output: 14.
      - Shaped recipe: Copper Plate, Stone, Gold Ingot, and Wire.
    - **Voltage Valley** (`evecualmc:music_disc_voltage_valley`):
      - Duration: 2 minutes 59 seconds (179s).
      - Redstone comparator output: 15.
      - Shaped recipe: Elactorite, Copper Plates, Gold Ingot, and Wire.
  - **Full Jukebox & Audio Streaming Compatibility**:
    - Converted high-bitrate stereo MP3 master recordings to clean Ogg Vorbis stream tracks located in `assets/evecualmc/sounds/records/`.
    - Registered streaming audio entries in `sounds.json` (`music_disc.circuit_and_stone` & `music_disc.voltage_valley`) with memory-efficient background audio streaming (`"stream": true`).
    - Added both discs to Minecraft's global `#minecraft:music_discs` item tag (`data/minecraft/tags/items/music_discs.json`), ensuring full native jukebox insertion, song playing, particles, and jukebox comparator signal emission.
  - **Custom Artwork & Models**:
    - Created high-contrast 16x16 pixel-art disc textures matching mod color themes (emerald-copper circuit tones for Circuit & Stone, electrifying violet-cyan neon tones for Voltage Valley).
    - Added clean standard handheld/inventory item models and creative tab registration under the EvecualMC item group.

## [1.8.35] - 2026-09-19
### Added & Improved
- **Dedicated Crown Inventory Slot (Hover-Revealed on Head)**:
  - **Dynamic Hover Reveal**:
    - Moving the mouse cursor over the head (either the helmet armor slot `(x=8, y=8)` or the player model preview head `(x=40..62, y=8..32)`) reveals a dedicated Crown Slot popping up directly on top of the head at `x=8, y=-19`.
    - Integrated a ~1.5s smooth cursor hover grace timer, allowing fluid navigation between the head and the crown slot without premature closing.
    - Slot remains visible and accessible whenever a crown is equipped.
    - Custom container tab styling with 3D beveled borders, recessed slot background, and a delicate pixel-art golden crown watermark when empty.
    - Custom tooltip: `§6👑 Crown Slot - §eLegendary Crowns Only`.
  - **Strict Slot Validation ("Nothing Else Can Be In There")**:
    - CrownSlot enforces strict filtering in `canInsert(stack)`: only allows `evecualmc:the_mechanicals_crown`, `evecualmc:the_electricians_crown`, and `evecualmc:the_castles_crown`. Any other item (helmets, blocks, tools) is completely rejected.
    - Quick-move / shift-clicking crowns automatically equips them into the Crown Slot, and shift-clicking the Crown Slot returns it to the main inventory.
  - **3D Crown Rendering in Crown Slot**:
    - Implemented `CrownFeatureRenderer` registered on `PlayerEntityRenderer` via Fabric API.
    - Equipping a crown in the Crown Slot immediately renders its 3D model on the player's head both in the world and inside the inventory character preview.
  - **Full Functional Parity**:
    - All crown passives (Kinetic Momentum, Clockwork Mending, Lightning static harvesting, wireless recharge, fortified resilience) tick every tick while equipped in the Crown Slot.
    - Keybind **[V]** triggers the active ability (Overclock Burst, Lightning Surge, Cannon Artillery) seamlessly from either the helmet slot or the dedicated crown slot.
  - **Persistence & Multiplayer Sync**:
    - Saved into player NBT (`EvecualMCCrown`), respects `keepInventory`, and syncs in real-time across multiplayer via `CROWN_SYNC_S2C_PACKET_ID`.

## [1.8.34] - 2026-09-19
### Fixed & Improved
- **Crown 3D Head Positioning Fix**:
  - Completely overhauled the crown armor rendering engine in `CrownArmorRenderer.java` using custom 3D `ModelPart` geometry.
  - Crowns now render firmly and accurately on top of the head/forehead (`Y = -6.5F` to `Y = -14.5F` relative to head pivot), completely eliminating the issue where they previously appeared around the throat/neck.
  - Custom 3D Features & Dedicated 64x64 Armor Textures:
    - **The Mechanicals**: 3D brass circlet band, 8 perimeter cog teeth/spires, prominent 8-point grand center cog with steel axle pin, twin side-meshing copper gears on temples, and rear steam boiler pressure gauge.
    - **The Electricians**: Sleek superconductor alloy band with conductive traces, front faceted Elactorite core crystal in heavy alloy bezel, twin swept lightning horn spires, twin temple Tesla coils with silver discharge spheres, and central lightning mast.
    - **The Castles**: Chiseled ashlar stone fortress battlement with royal gold bottom moulding, 4 corner watchtowers with crenellated parapets, central fortress keep crowned with a golden spire, twin forward-facing 3D heavy wrought-iron siege cannons with brass trunnions, and rear cannonball pyramid stack.
- **Removed Crafting Recipes**:
  - Permanently removed all crafting table recipe definitions for the three crowns (`the_mechanicals_crown.json`, `the_electricians_crown.json`, `the_castles_crown.json`), ensuring they cannot be crafted and remain exclusive, un-craftable artifacts.
- **Anti-Duplication & Animation Preserved**:
  - Maintained complete anti-duplication protection in `DuperRarityHelper` against the Electronic Duper.
  - Maintained multi-frame animated item textures with `.mcmeta` files.

## [1.8.33] - 2026-09-19
### Added & Improved
- **Crowns Anti-Duplication Protection & Dynamic Animation**:
  - **Duplication Blacklist**:
    - Blacklisted all three crowns (`evecualmc:the_mechanicals_crown`, `evecualmc:the_electricians_crown`, `evecualmc:the_castles_crown`) from the **Electronic Duper** replication chamber in `DuperRarityHelper.isDuplicable`.
    - Crowns cannot be duplicated or placed in the duper input slot, protecting their legendary status.
  - **Animated Multi-Frame Textures (`.png` + `.png.mcmeta`)**:
    - **The Mechanicals**: 8-frame clockwork animation with central brass cog rotating 360°, side gears meshing in reverse synchrony, and escapement spring ticks.
    - **The Electricians**: 8-frame high-voltage animation with pulsing Elactorite crystal gem, electric lightning surge traveling up the central spire, and crackling tesla coil micro-arcs.
    - **The Castles**: 8-frame fortress animation with glowing hot ember muzzles in the twin cannons, wisps of black powder smoke rising, and a sweeping royal gold sheen across the battlement parapets.
  - **Tooltip Updates**:
    - Added `✖ Non-Duplicable (Protected Artifact)` and `✨ Animated` badges across all three crown tooltips.

## [1.8.32] - 2026-09-19
### Added & Improved
- **The Three Royal Crowns**:
  - Added **The Mechanicals** (`evecualmc:the_mechanicals_crown`):
    - Steampunk brass, copper, and iron circlet with interlocking cogs and precision clockwork springs.
    - Passive: Permanent **Kinetic Momentum** granting Haste II and Speed I while equipped.
    - Passive: **Clockwork Mending** automatically restores durability to carried equipment and tools every 10 seconds.
    - Active Ability (**Key [V]**): Triggers an **Overclock Burst**, pushing all surrounding hostiles away with a kinetic shockwave and dealing damage.
    - Crafted with Copper Plates, Steel Ingots, Gold, and an Electric Engine.
  - Added **The Electricians** (`evecualmc:the_electricians_crown`):
    - High-voltage crown embedded with resonated Elactorite crystals and energized tesla coil emitters.
    - Integrated 5,000 EU internal capacitor buffer compatible with mod chargers.
    - Passive: Automatically harvests static electrical charge from active thunderstorms and rainfall to recharge its energy buffer.
    - Passive: Wirelessly supplies inductive power to any carried electric items (Electronic Zapper, Railgun, Batteries).
    - Passive: Grants total lightning strike immunity and Speed II.
    - Active Ability (**Key [V]**): Summons a targeted, focused lightning bolt on crosshairs (consuming 150 EU).
    - Crafted with Elactorite, Lightning, Electrical Wires, a Battery, and Steel Ingots.
  - Added **The Castles** (`evecualmc:the_castles_crown`):
    - Ancient fortress crown chiseled from weathered stone bricks, featuring battlement crenellations and royal gold inlay.
    - Flanked by twin functional cast-iron siege cannons on the ramparts.
    - Passive: **Fortress Bulwark** granting heavy defense (+6 Armor, +3.5 Toughness) and permanent Resistance II.
    - Passive: **Siege Ram** shoves and deals heavy impact damage to enemies collided with while sprinting.
    - Active Ability (**Key [V]**): Fires dual explosive artillery rounds from the twin cannons with muzzle smoke and blast acoustics.
    - Crafted with Steel Ammo, Steel Ingots, Iron Ingots, Gunpowder, and Stone Bricks.
- **Crown Armor Renderer & Visual Integration**:
  - Implemented Fabric `ArmorRenderer` for all three crowns so they render directly on entity and player heads.
  - Created custom 16x16 pixel art item icons and 64x32 armor textures with thematic shading and highlights.
  - Added crown registrations to the Evecual creative tab.
  - Added full English and Swedish translations.

## [1.8.31] - 2026-09-15
### Added & Improved
- **Railgun Hand Hold & Orientation Fix**:
  - Fixed 3D Lorentz Railgun model being held sideways in third-person and first-person views.
  - Replaced legacy 2D sprite rotations (`0, -90, 55`) with true 3D rifle transforms (`-80, 0, -10` in third-person and `-90, 0, -30` in first-person).
  - Aligned translation offsets (`0, 4.0, -2.5`) so the player's hand palm grips the tactical handle rather than the center of the accelerator chamber.
  - The railgun is now held upright with the holographic sight on top, the handle in the hand, and the barrel pointing forward.

## [1.8.30] - 2026-09-15
### Added & Improved
- **Quantum Materializer Fabrication & Recipe Accessibility**:
  - Added dedicated **Quantum Materializer** blueprint tab (`[ ⚛ MAT ]`) to the **Item Fabricator** terminal.
  - Fabricator recipe: 4x Steel Ingot, 4x Electrical Wire, 2x Battery, 1x Electric Engine, 2x Diamond, 2x Elactorite (consumes 1,600 EU at 10 EU/t, 8.0s).
  - Added recipe unlock advancement for standard Crafting Table assembly (`materializer.json`) so it automatically populates in the recipe book upon acquiring steel, wire, or electric engines.
- **Rich Informational Tooltips on Advanced Blocks**:
  - Implemented custom `BlockItem` tooltips across all recently added machinery:
    - **Item Fabricator** (`evecualmc:item_fabricator`): Displays energy capacity (4,000 EU), available blueprints (Zapper, Railgun, Materializer, Duper), and terminal usage guidance.
    - **Quantum Materializer** (`evecualmc:materializer`): Displays energy buffer (8,000 EU), role in quantum synthesis, and note that it is the required core for the Electronic Duper.
    - **Electronic Duper** (`evecualmc:electronic_duper`): Displays energy capacity (10,000 EU), replication rate, and explicit blacklist warning (no weapons, dupers, or vehicles).
    - **Electric Grinder** (`evecualmc:electric_grinder`): Displays energy capacity (2,000 EU), 4 EU/t consumption rate, and ore doubling yield information.
    - **Item Charger** (`evecualmc:item_charger`): Displays 5,000 EU storage buffer, inductive recharge rate, and list of supported equipment.
- **Item Fabricator UI 4-Tab Alignment**:
  - Balanced 4 blueprint tabs (`⚡ ZAP`, `💥 RAIL`, `⚛ MAT`, `💠 DUPE`) symmetrically within the glassmorphic cyber-deck HUD.
  - Added full tooltip breakdown and live ghost item previews for the Quantum Materializer synthesis process.

## [1.8.29] - 2026-09-14
### Added & Improved
- **Railgun 3D Model & Missing Texture Fix**:
  - Resolved `JsonParseException: Invalid rotation -15.0 found` by aligning tactical grip model rotation to standard Minecraft engine rotation steps (`-22.5°`).
  - Scaled model UV coordinate mappings from 32-pixel space into normalized Minecraft 16-coordinate atlas space, ensuring the 32x32 texture atlas maps properly with zero texture bleed or missing sprite fallbacks.
  - Railgun now renders in full 3D in 1st-person, 3rd-person, hotbar, and inventory with glowing holographic sight, Lorentz accelerator chamber, and alloy body.
- **Item Fabricator UI Overhaul**:
  - Redesigned the Item Fabricator GUI into an expanded 194px glassmorphic cyber-deck HUD.
  - Eliminated duplicate title text rendering and overlapping requirement labels on player inventory slots.
  - Added interactive ghost item previews and required quantity counters in empty input slots for all blueprints (`⚡ Zapper`, `💥 Railgun`, `💠 Duper`).
  - Added real-time dynamic slot validation: slot borders glow green (`✔`) when correct items and amounts are provided, amber when more items are needed, and red if an incorrect item is inserted.
  - Added ghost item preview in the output slot showing what item will be synthesized.
  - Enhanced blueprint tabs with clear layout, hover tooltips displaying complete ingredient checklists, energy cost, and craft times.
  - Repositioned player inventory and hotbar cleanly with dedicated spacing and no visual clipping.

## [1.8.28] - 2026-09-14
### Added & Improved
- **Electronic Duper Manufacturing Rebalance**:
  - Removed standard crafting table recipe for the Electronic Duper to eliminate cheap early-game duplication.
  - Electronic Dupers must now be assembled in the **Item Fabricator** using an advanced high-tier blueprint.
  - High-tier fabrication recipe:
    - 1x Quantum Materializer
    - 1x Netherite Ingot
    - 4x Elactorite
    - 1x Upgraded Electric Engine
    - 1x Diamond Block
    - 8x Wires
  - Fabrication process consumes 3,000 EU at 10 EU/t (300 ticks / 15 seconds) of intense electromagnetic assembly.
  - Added dedicated `[ 💠 DUPER ]` blueprint selection button with amber cyber-tech styling to the Item Fabricator GUI.

## [1.8.27] - 2026-09-14
### Added & Improved
- **Anti-Duplication Technology Restriction**:
  - Implemented `DuperRarityHelper.isDuplicable(ItemStack stack)` protecting rare and advanced technical items.
  - Electronic Dupers now strictly reject: Dupers themselves, Lightning Item, Zappers, RC Vehicles (RC Car, RC Drone, Pickup Drone, RC Robot, Car, Helicopter), and Railgun.
  - GUI visually displays a warning `"⛔ Non-Duplicable Technology!"` and blocks slot insertion.
- **Item Fabricator Block**:
  - Added new advanced tier workstation: **Item Fabricator** (`evecualmc:item_fabricator`) with survival recipe (Netherite Scrap, Steel, Battery, Elactorite, Combiner).
  - High-voltage energy consumer (4,000 EU storage) with full WireBlock connectivity.
  - Interactive GUI featuring blueprint toggle selection: `[ ⚡ ZAPPER ]` and `[ 💥 RAILGUN ]`.
  - Migrated Electronic Zapper crafting exclusively into the Item Fabricator.
- **Lorentz Railgun (3D Modeled & Survival Craftable)**:
  - Added heavy electromagnetic weapon: **Lorentz Railgun** (`evecualmc:railgun`).
  - High survival cost recipe in Item Fabricator: 2x Netherite Ingots, 4x Elactorite, 4x Steel Rods, 1x Upgraded Engine, 4x Copper Plates, 4x Wires (consumes 1,500 EU & 200 ticks of fabrication).
  - Stunning 3D model featuring dual parallel accelerator rails, central Lorentz chamber, tri-stage magnetic induction coils, high-density capacitor pack, ergonomic grip, shoulder stock, and holographic targeting sight.
  - Supersonic kinetic hyper-velocity beam (80-block piercing range) dealing massive entity damage with sonic boom effects, block impact explosions, and ammo compatibility (Elactorite, Steel Rods, Steel Ammo, Iron/Copper Ammo, or internal EU plasma).
- **Shader Validation Fix**:
  - Fixed shaderpack packaging script `launch.ps1` to eliminate `.NET` framework version incompatibilities (`GetRelativePath`) on PowerShell 5.1.
  - Properly synchronized Iris-compliant directory structure directly under `shaders/` and standardized `shaders.properties` configuration.

## [1.8.26] - 2026-09-14
### Added & Improved
- **EvecualTechShader Iris Compatibility & Settings Menu Fix**:
  - Fixed shaderpack ZIP structure: replaced backslash paths with standard forward slash delimiters and added explicit `shaders/` directory entries, resolving Iris's invalid shaderpack rejection.
  - Fixed shader settings menu screen configuration: restored standard `screen=<options>` root menu and `<profile>` selector token so Iris renders the interactive graphical options screen properly.
  - Standardized profile definitions syntax (`OPTION=value` and `!OPTION`) for Low, Medium, High, and Ultra presets.
  - Synchronized and validated all shaderpacks across `shader/`, `run/shaderpacks/`, and `release/`.
- **Removed Crown Accessory Items**:
  - Removed crown items, accessory slots, and custom crown mixins per design request.
  - Restored Lightning Item to its original 25-use pure lightning weapon configuration.

### Added & Improved
- **Auto Pickup Item Blacklist Filter & RMB Configuration GUI**:
  - Right-clicking (RMB) on the Auto Pickup Station now opens a dedicated **Auto Pickup Filter Configuration GUI**.
  - Provides 9 dedicated filter slots where players can place items they want the drone to ignore / blacklist (e.g. Glow Ink Sacs, Cobblestone, Seeds, Rotten Flesh).
  - Shift-click support for easily moving filter items between player inventory and configuration slots.
  - Blacklist filter items are stored in block entity NBT, synced with the linked Pickup Drone, and safely dropped on block break.
- **Subterranean & Submerged Item Prevention (Fixing Glow Ink Sacs)**:
  - Added water & fluid checks: aerial drones will never target items submerged in water or touching water (such as glow ink sacs dropped by glow squids in aquifers).
  - Added subterranean cavern filter: items buried deep beneath solid ground (> 5 blocks below terrain surface) are excluded from surface radar scans, preventing drones from trying to dive into the ground.
  - Added obstacle collision abort: if the drone collides with solid ground or a wall while its target is below it for > 35 ticks, the mission is safely aborted and blacklisted to prevent grinding against terrain.

## [1.8.24] - 2026-09-12
### Added & Improved
- **Auto Pickup Drone Camera Monitoring (LMB View)**:
  - Left-clicking (LMB) on the Auto Pickup Station now directly links into the paired Pickup Drone's camera feed, providing a remote monitoring viewport while the drone operates.
  - Intercepted block attack events (`AttackBlockCallback`) in both Survival and Creative modes so left-clicking engages the camera without damaging or breaking the station.
  - Sneak + LMB allows breaking the block normally as intended.
  - Pressing `Shift`/`Sneak` cleanly disengages from the camera feed back to first-person view.
  - While observing via the Auto Pickup station camera, autonomous drone flight and item scavenging continue uninterrupted without manual WASD overriding tasks.
- **Diagnostics & Cleanups**:
  - Resolved unused local variable `chargingAny` in `HeliChargerBlockEntity`.
  - Removed unused `Identifier` imports from `ElectronicDuperScreen` and `HeliUpgradeScreen`.

## [1.8.23] - 2026-09-12
### Fixed & Improved
- **Auto Pickup Drone Sky Flight Bug Resolved**:
  - Fixed altitude ratcheting bug where local ground raycasts defaulted to the drone's current Y position when more than 4 blocks in the air, creating an infinite upward loop (`desiredY = this.getY() + 1.0`) that forced drones to fly endlessly into the sky.
  - Implemented target-centered cruising: drones cruise smoothly at `targetY + 1.5m` during approach, and drop to `targetY + 0.35m` directly above the item once within 1.8m horizontal distance to suck it into cargo.
  - Bound return cruise altitude strictly to charger/landing pad height + clearance (`bestCharger.getY() + clearance`), preventing drones from climbing indefinitely during return-to-base maneuvers.
  - Added vertical collision damping (`if (verticalCollision && dy > 0) dy = 0`) to immediately prevent upwards thrust if contacting a ceiling or roof.
  - Expanded Auto Pickup radar scan area to 128 blocks (256m diameter).
- **Codebase & IDE Diagnostic Cleanups (70+ warnings resolved)**:
  - Added `@SuppressWarnings("deprecation")` across overridden vanilla lifecycle hooks (`onStateReplaced`, `getStateForNeighborUpdate`).
  - Switched deprecated `WorldView.isChunkLoaded(BlockPos)` to `isChunkLoaded(chunkX, chunkZ)` in `StationaryTurretBlockEntity`.
  - Added exhaustive enum coverage with `default` handling for `Direction` in `WindTurbineBlockEntityRenderer`.
  - Removed all unused imports across blocks, entities, renderers, screens, items, and recipe definitions.
  - Removed obsolete unused fields and pattern variables (`activeCharging`, `targetParkY`, `entryApproachY`, `inventory`, `root`, `TEXTURE`, `GENERIC_INVENTORY_TEXTURE`, `pct`).

## [1.8.22] - 2026-09-12
### Fixed & Improved
- **Auto Pickup Drone Parking Spot Sinking Fix**:
  - Restricted `isParkingSpotBlock` to dedicated landing pads (`PICKUP_DRONE_PARKING_SPOT_BLOCK` and `DRONE_PARKING_SPOT_BLOCK`), preventing full base blocks below (`DRONE_PICKUP_BLOCK` and `AUTO_PICKUP_BLOCK`) from misidentifying the docking position and pulling drones into the ground.
  - Implemented dynamic landing pad height offsets (`getParkingSpotPadHeight`): dynamically sets `+0.125m` (2 pixels) for `PICKUP_DRONE_PARKING_SPOT_BLOCK` and `+0.0625m` (1 pixel) for `DRONE_PARKING_SPOT_BLOCK`, ensuring drone landing gear rests flush on top of pads.
- **RC Controller Pairing & Takeoff on Parking Spot**:
  - Resolved immediate unpairing bug where right-clicking an RC Controller on a parked drone instantly unpaired the player on the subsequent tick.
  - Added `explicitlyPairedInSpot` tracking so player manual pairing remains active while docked on a charging or landing pad.
  - Permitted immediate takeoff: pressing vertical or directional movement keys (`Space`, `W`, `A`, `S`, `D`) or triggering autonomous harvest breaks the parked clamp and grants full flight control.
- **Autonomous Drone Navigation & Indoor Clearance**:
  - Replaced world top surface heightmap lookups with local ground floor raycasts (`+1.0m`), preventing drones in indoor or roofed rooms from pushing up into ceilings or stalling.
  - Added collision-aware return-to-base ascending stages: ceiling or obstruction contacts (`verticalCollision`) immediately transition from ascension to horizontal cruising.
  - Differentiated indoor vs outdoor return cruise altitudes (`2.2m` indoor vs `4.5m` outdoor).
  - Broadened `AutoPickupBlockEntity` item detection to any valid dropped item without requiring open air above, enabling indoor item scavenging.
  - Added manual RC override protection in `AutoPickupBlockEntity` so active player piloting is never hijacked by radar scans.
  - Automatic `homeHelipadPos` binding to the closest pickup parking spot on link.

## [1.8.21] - 2026-09-12
### Changed & Tuned
- **Lightning Item Durability Balancing**:
  - Adjusted the maximum durability of the `LightningItem` from 64 to **25 uses**.
  - Updated tooltips and item durability bar to reflect 25 maximum lightning strikes.

## [1.8.20] - 2026-09-12
### Added & Improved
- **Swedish (Svenska) Localization (`sv_se.json`, `sv-SE.json`)**:
  - Full comprehensive Swedish translation covering all items, blocks, machines, vehicles, drones, weapons, ammo types, keybindings, containers, and creative tabs.
  - Complete terminology translations including *Kvantmaterialiserare* (Quantum Materializer), *Elektrisk kross* (Electric Grinder), *Föremålsladdare* (Item Charger), *Elactorit* (Elactorite), *Ståltacka* (Steel Ingot), *Elbil* (Electric Car), *Elhelikopter* (EV Heli), *Upphämtningsdrönare* (Pickup Drone), *Försvarstorn* (Stationary Turret), and all ammunition tiers.

## [1.8.19] - 2026-09-12
### Added & Improved
- **Steel & Elactorite Ammunition Types (`steel_ammo`, `elactorite_ammo`)**:
  - Added **Steel Defense Ammo (`steel_ammo`)**:
    - High-velocity armor-piercing kinetic projectile dealing **22.0 base damage** (with 0.45 armor penetration).
    - Features slate-blue tracer flight glow, dense kinetic crit trails, and heavy metallic anvil impact audio.
    - Crafted with 1 Steel Ingot + 1 Gunpowder $\rightarrow$ yields 8 Steel Ammo.
  - Added **Elactorite Defense Ammo (`elactorite_ammo`)**:
    - Apex quantum plasma projectile dealing **48.0 base damage** (with 0.95 armor penetration and 2.4x velocity multiplier).
    - Features brilliant violet-cyan quantum plasma tracer lines, electric spark particle trails, and reverse portal shimmer.
    - **Impact Shockwave**: On hitting an entity, triggers a lightning flash burst (`ENTITY_LIGHTNING_BOLT_IMPACT`) and arcs high-voltage chain lightning to up to 2 nearby hostile mobs dealing 12.0 bonus AOE shock damage.
    - Crafted with 1 Elactorite + 1 Gunpowder $\rightarrow$ yields 8 Elactorite Ammo.
- **Turret System & Ammo Container Upgrades**:
  - `AmmoType` updated with full 5-tier ammunition hierarchy: **Elactorite (48 DMG)** > **Diamond (30 DMG)** > **Steel (22 DMG)** > **Iron (14 DMG)** > **Copper (6 DMG)**.
  - `TurretAmmoContainerBlockEntity` automatically prioritizes higher-tier ammunition when loading defensive sentry turrets.
  - `TurretBulletEntityRenderer` renders custom colored tracer lines for each ammo caliber.
  - `EnergyHudOverlay` displays live real-time counts for all 5 ammunition types when inspecting ammo depots.
  - Added full Field Guide topic documentation and translation keys for English (`en_us.json` & `en-US.json`).

## [1.8.18] - 2026-09-12
### Added & Improved
- **Item Charger Machine Block (`item_charger`)**:
  - Added the **Item Charger**, a high-tech energy workstation designed to slowly trickle-charge any device, tool, weapon, battery, machine, drone, or vehicle in item form.
  - **Slow Steady Charging Rate**: Charges items at a controlled high-tech rate of 1 EU/t (20 EU/second).
  - **Energy Retention on World Placement**: Items charged in the Item Charger (Battery Blocks, Storage Units, Machines, Solar Panels, Turrets, RC Cars, RC Drones, Pickup Drones, Robots, and Helicopters) retain their exact stored energy amount when placed into the world!
  - **Custom Charging GUI**: Sleek cybernetic interface featuring an animated charging cradle with glowing neon corner brackets, a vertical 4000 EU station power gauge, a horizontal item battery percentage gauge (0 - 100%), and real-time charging status telemetry.
  - **Visual & Audio FX**: Emits electric sparks and portal shimmer around the top charging cradle while active, complete with gentle periodic charging hum audio (`BLOCK_RESPAWN_ANCHOR_CHARGE`).
  - **Grid & Hopper Automation**: Fully compatible with Wire networks, Batteries, Solar Panels, and Wind Turbines. Supports top/side insertion via Hoppers/Chutes and automatic bottom extraction when items are fully charged.
  - **Crafting Recipe**: 4 Steel Ingots, 2 Steel Rods, 1 Copper Plate, 1 Redstone Dust, and 1 Glass (`recipes/item_charger.json`).
- **Electronic Zapper Battery & Energy Integration**:
  - The **Electronic Zapper** now stores up to 1000 EU and requires power to operate!
  - **Zap Mode Energy Consumption**: Disintegrating blocks consumes 5 EU per block. Warns and prevents firing if discharged.
  - **Attack Mode Energy Consumption**: Melee shock strikes consume 10 EU (deals +14 electric shock damage and chain lightning), and long-range plasma bolts consume 20 EU. Deals baseline physical damage if discharged.
  - **Dynamic In-Inventory Energy Bar**: Added custom cyan-amber-red battery level indicator rendered directly beneath the item in player hotbars and inventories.
  - Updated hover tooltip with live `§e⚡ Energy: X / 1000 EU (Y%)` readout.
- **Universal Item Energy System (`ItemEnergyHelper`)**:
  - Built a centralized system for querying, charging, discharging, and syncing energy tags across all items and their corresponding placed blocks/entities.
- **Field Guide & HUD Integration**:
  - Added dedicated Field Guide topic for `ITEM_CHARGER` in `TipTopic.java`.
  - Added real-time crosshair HUD overlay for Item Chargers in `EnergyHudOverlay.java`.
  - Added English localization entries in `en_us.json` and `en-US.json`.

## [1.8.17] - 2026-09-12
### Added & Improved
- **Electronic Zapper Handheld Tool & Weapon (`electronic_zapper`)**:
  - Crafted from 1 **Elactorite** and 2 **Steel Rods**.
  - **Mode Toggle (Shift + Right-Click)**: Instantly swap between **Zap Mode** and **Attack Mode** with distinct sound feedback (`BLOCK_RESPAWN_ANCHOR_CHARGE` / `ITEM_TRIDENT_THUNDER`) and HUD action-bar notifications.
  - **Zap Mode (Disintegration & Mining)**:
    - **Right-Click Block / Remote Beam**: Instantly breaks and harvests blocks within direct reach or up to 12 blocks away via concentrated high-voltage particle beam with sonic boom shockwave and amethyst resonance.
    - **Creature Strike**: Left-clicking mobs inflicts light baseline damage with electric spark feedback.
  - **Attack Mode (Enhanced Electric Combat)**:
    - **Melee Shock Strike**: Left-clicking enemies deals heavy physical damage plus an immediate +14.0 electric shock damage burst with flash FX and thunder impact (`ENTITY_LIGHTNING_BOLT_IMPACT`).
    - **Chain Lightning**: Arcs secondary lightning beams to up to 2 nearby hostile mobs within 6 blocks dealing +8.0 chain damage each.
    - **Plasma Shock Bolt (Right-Click)**: Discharges a concentrated long-range electric plasma arc up to 16 blocks away dealing 16.0 ranged damage to targeted creatures.
  - Built-in permanent enchanted glint and detailed hover tooltip information.
- **Steel Rod Material & Crafting (`steel_rod`)**:
  - Added **Steel Rod**, forged with 2 Steel Ingots placed vertically in a crafting table (yields 4 rods).
  - Used as high-conductive structural and electrical framing for advanced cybernetic gear.
- **Field Guide & Localization**:
  - Added full Field Guide / Tip Menu documentation for `ELECTRONIC_ZAPPER` and `STEEL_ROD`.
  - Added complete localization in English (`en_us.json` & `en-US.json`).

## [1.8.16] - 2026-09-12
### Added & Improved
- **Quantum Materializer Machine Block (`materializer`)**:
  - Added the **Quantum Materializer**, a high-power atomic synthesis workstation engineered with a dark titanium frame, magnetic vortex emitter ring, and cybernetic containment chamber.
  - **Visual & Audio FX**: Emits electric sparks and reverse portal shimmer while active, plays deep resonant beacon humming loops (`BLOCK_BEACON_AMBIENT`), and delivers a triumphant firework burst and chime on completion (`BLOCK_BEACON_ACTIVATE` + `ENTITY_PLAYER_LEVELUP`).
  - **High-Tech Cyber GUI**: Custom glassmorphic HUD screen featuring vertical EU energy gauge with tooltip, dual input slot frames (Slot 0 and Slot 1), quantum convergence progress bar, golden containment output slot (Slot 2), and live ETA countdown timer.
  - **Elactorite Synthesis**: Place 2 Iron Ingots and 1 Diamond (in either input slot) to forge **Elactorite** after 2 minutes (2400 ticks) of quantum compression at 1 EU/t.
  - **Steel Synthesis**: Place 2 Iron Ingots and 1 Coal or Charcoal to synthesize **Steel Ingots** in 20 seconds (400 ticks).
  - **Power Loss Fail-Safe**: If energy drops below 1 EU, progress pauses safely in place without resetting, protecting player progress.
  - **Hopper & Chute Automation**: Implemented `SidedInventory` with top/side input routing and bottom output extraction.
- **Electric Grinder Machine Block (`electric_grinder`)**:
  - Added the **Electric Grinder**, an industrial rotary milling workstation with tungsten-carbide grinding teeth, motor cooling radiator fins, and a copper intake funnel.
  - **Visual & Audio FX**: Emits mechanical friction sparks (`CRIT` + `ELECTRIC_SPARK`) and grindstone audio during operation, finishing with an anvil clang and copper resonance.
  - **Industrial GUI**: Custom dark slate GUI with amber EU energy gauge, animated grinding teeth progress bar, and live milling status.
  - **Copper Milling**: Grinds Copper Ingots or Raw Copper into **Copper Plates** (1 item per 5 seconds / 100 ticks) at 2 EU/t. Also supports pulverizing Copper Blocks into 9 plates simultaneously.
- **New Materials: Elactorite & Copper Plate**:
  - **Elactorite (`elactorite`)**: Rare synthetic quantum alloy ingot featuring glowing crystalline violet/cyan pixel art and lore.
  - **Copper Plate (`copper_plate`)**: Heavy cold-rolled copper plating featuring bevel edges, corner rivets, and metallic sheen.
- **Pickup Drone Recipe Overhaul**:
  - Updated shapeless crafting recipe (`pickup_drone.json`) to require `evecualmc:rc_drone`, `minecraft:hopper`, and `evecualmc:copper_plate`.
  - Added dedicated Electronic Combiner / Car Fabricator recipe for the Pickup Drone with vacuum hopper and copper hull CAD silhouette display.
- **HUD & Field Guide Diagnostics**:
  - Integrated Materializer and Electric Grinder into `EnergyHudOverlay.java` for crosshair telemetry with real-time EU capacity, progress bars, and countdown timers.
  - Added comprehensive Field Guide entries in `TipTopic.java` for `MATERIALIZER`, `ELECTRIC_GRINDER`, `ELACTORITE`, and `COPPER_PLATE`.
  - Added both machines to EvecualTechShader `block.properties` for realistic metallic specular reflections.

## [1.8.15] - 2026-09-12
### Fixed & Improved
- **Drone Autonomous Pathfinding & Flight Control**:
  - Fixed issue where the Pickup Drone flew away into the stratosphere when Auto Pickup was linked without nearby items.
  - Eliminated monotonic altitude ratcheting bug (`Math.max(this.getY())`), replacing it with terrain-surface adaptive cruising clearance and target-level descent.
  - Replaced infinite 256m blind item sweep with a focused 96m operational radar zone (192m wide) with reachability validation: verifies dropped items are not buried under solid stone, deep underground, or in lava, and ensures at least 2 blocks of open vertical air clearance for drone approach.
  - Implemented dynamic obstacle detection and avoidance: drone sweeps forward trajectory vectors and smoothly pitches up to clear walls, fences, roofs, and cliffs.
  - Added dead/despawned target verification: immediately re-targets next available item or returns home if the target item disappears instead of chasing phantom coordinates for 60 seconds.
  - Added stuck-item blacklist: skips unreachable items (e.g. trapped under slabs or behind glass) after 4 seconds to prevent stalling.
  - Expanded vacuum pickup range to 2.6 blocks.
  - Automated return-to-dock fail-safe: when radar reports no items, an undocked or floating drone is immediately commanded to return home and dock cleanly.
  - Stationary parking stability: pairing with RC Controller no longer throws the Pickup Drone off its parking pad.
  - Recognized `DRONE_PICKUP_BLOCK` and `AUTO_PICKUP_BLOCK` as valid landing and docking locations.
- **Stationary Defense Turret Head Model Fix**:
  - Elevated the swiveling turret head, sensor visor, and dual heavy barrels cleanly on top of the block model (`Y = 1.0+`), resolving the bug where the turret head was submerged inside the solid cube block model.
  - Designed heavy-duty armored mounting collar and swivel ring resting directly on top of the base pedestal.
  - Updated `VoxelShape` collision and selection outline to encompass the full pedestal and elevated turret head (`Y = 0 to 25/16`).
  - Adjusted projectile muzzle origin to `Y + 1.36` so bullets shoot straight out of the physical barrels.
- **EvecualTechShader Graphics & Shadow Overhaul (Ultra-Performant)**:
  - Implemented real-time **Screen-Space Contact Shadows (SSCS)** with early-exit 8-step raymarching: casts crisp, directional ground contact shadows under vehicles (RC cars, drones, robots), player feet, vegetation, and architecture with virtually zero performance impact.
  - Added dynamic world-space sun/moon lighting to `gbuffers_terrain.fsh` and `gbuffers_entities.fsh`: block face lighting and specular reflections dynamically track the actual position of the sun and moon across the sky.
  - Enhanced water shading in `gbuffers_water.fsh` with dynamic underwater sunlight caustics, multi-wave perturbation, and refined Fresnel sky reflection.
  - Re-packaged and verified `EvecualTechShader.zip` with clean forward-slash paths for immediate Iris and OptiFine compatibility.

## [1.8.14] - 2026-09-12
### Added & Improved
- **Auto Pickup Logistics Radar Block (`auto_pickup`)**:
  - Added new **Auto Pickup** block with custom 3D model, animated active state, particle effects, and crafting recipe.
  - Linked to a Pickup Drone via RC Controller (right-click block with paired controller).
  - Continuously scans 256m in all directions for ANY dropped items in loaded chunks.
  - Automatically dispatches the linked Pickup Drone from its docking pad to fly, vacuum up all dropped items, chain nearby items, and return safely to dock on the landing pad when finished or when cargo is full.
  - Integrated HUD inspection overlay and Field Guide diagnostics (`TipTopic.AUTO_PICKUP`).
- **Pickup Drone Infinite Control Range**:
  - Removed range barriers for Pickup Drones across server control packets and client controllers.
  - Players can now operate and control the Pickup Drone seamlessly from anywhere across the dimension.
- **Autonomous 3x3 Forced Chunk Loading**:
  - Implemented persistent 3x3 chunk loading around the Pickup Drone (`PickupDroneEntity.updatePickupChunkLoading`).
  - Keeps chunks active whether the drone is flying, harvesting, docked on its landing pad, or stationary across the map.
  - Guarantees chunk unloading cleanup when the drone is picked up or removed.
- **Tuned Flight Speeds & Power Saving**:
  - Updated max speeds:
    - **40 m/s** (2.0 blocks/tick) when boosting (Sprint / Ctrl).
    - **30 m/s** (1.5 blocks/tick) standard cruise speed.
    - **15 m/s** (0.75 blocks/tick) low-power speed when battery drops under 5% (< 30 EU).
  - Power consumption is reduced 4x (80-tick drain interval) in low-power mode (< 5%) to ensure the drone can safely return home.
- **RC Controller & Diagnostics Enhancements**:
  - RC Controller tooltips and pairing messages now display `(Range: ∞ Infinite)` when paired with a Pickup Drone.
  - Updated Field Guide entries for Pickup Drone and Auto Pickup with comprehensive operation instructions.

## [1.8.13] - 2026-09-12
### Added & Improved
- **Electric Chute 6-Directional Cornering & Multipart Models**:
  - Re-engineered Electric Chute models into modular core and arm assets (`electric_chute_core.json`, `electric_chute_arm.json`) with multipart blockstates.
  - Chutes now dynamically connect in all 6 directions (North, South, East, West, Up, Down), supporting 90° corners, L-bends, T-junctions, and multi-way networks.
  - Implemented Breadth-First Search (BFS) routing up to 64 blocks for upstream Drone Pickups and downstream Storage Units, allowing items to automatically navigate corners, turns, and elevation changes.
- **Electric Chute Power Grid Intake & Equalization**:
  - Added Electric Chute to `WireBlock.canConnectTo`, allowing power grid wires to physically and logically connect to chutes.
  - Implemented BFS wire network power extraction in `ElectricChuteBlockEntity.drawAdjacentEnergy`, pulling power from distant batteries, solar panels, and wind turbines.
  - Enabled energy equalization across chained chutes so powering any section of the chute pipeline supplies the whole network.
  - Added real-time client block entity synchronization on energy intake so HUD overlays and tip screens accurately reflect power levels.
- **Storage Unit Real-Time Item Count Synchronization**:
  - Resolved issue where Storage Unit tips were stuck displaying "1 item".
  - Overrode `markDirty()` in `StorageUnitBlockEntity` to dynamically recalculate stored items and filled slots, and immediately notify tracking clients via `markForUpdate`.
  - Serialized `TotalItems` and `FilledSlots` directly into NBT for fast client HUD querying.
  - Upgraded HUD overlays and inspection screens to query synchronized slot and item counts across connected storage clusters.
  - Added BFS wire network power extraction to Storage Units so they can charge directly from wire grids.
- **Pickup Drone 27-Slot Cargo Capacity Upgrade**:
  - Upgraded Pickup Drone cargo bay from 9 slots to a full 27-slot chest capacity (`CARGO_SIZE = 27`).
  - Updated cargo GUI to 3 full rows (`GenericContainerScreenHandler` with `GENERIC_9X3`).
  - Updated NBT serialization/deserialization for 27 slots on entities and item stacks with backward compatibility.
  - Updated HUD overlays and Field Guide documentation to reflect the 27-slot capacity.

## [1.8.12] - 2026-09-12
### Fixed & Improved
- **Electric Chute Connection & Pipeline Fix**:
  - Rewrote upstream Drone Pickup and downstream Storage Unit detection algorithms to be completely orientation-tolerant (checks facing, opposite of facing, all 6 adjacent sides, and chute chains).
  - Fixed issue where horizontal chute placement pointed at Drone Pickup skipped the station, resolving `⚠️ No Drone Pickup Station connected upstream`.
  - Added power fail-safe: Chutes can now extract power directly from connected Storage Units and docked Pickup Drones to prevent pipeline stalling.
  - Fixed drone docking detection in Drone Pickup Station: now scans the vertical column above the base block, detecting drones parked on both the station base and the landing pad.
- **HUD Crosshair Tips for All Logistics & Defense Blocks**:
  - Added real-time crosshair inspection HUD tips (`EnergyHudOverlay`) for:
    - **Drone Pickup Station**: Displays docking state (`DOCKED` / `READY`) and live connection diagnostics.
    - **Pickup Drone Landing Pad**: Displays docking readiness and validates placement directly on a Drone Pickup Station.
    - **Electric Chute**: Displays EU capacity meter, transfer status, and pipeline endpoints.
    - **Storage Unit**: Displays cluster count, multiblock slots, stored items count, and quantum field retention charge.
    - **Stationary Turret**: Displays link status, ammo container connection distance, and targeting scan radius.
    - **Turret Ammo Container**: Displays remaining ammo rounds breakdown (Copper, Iron, Diamond) and count of fed turrets.
  - Added full Field Guide entries (`TipTopic`) for Stationary Turret, Turret Ammo Container, and Turret Linker.
- **Pickup Drone Identity Fix**:
  - Fixed issue where the Pickup Drone was identified and titled as the `RC Drone` in tips and HUD overlays:
    - Prioritized `PickupDroneEntity` over `RcDroneEntity` in `EnergyHudOverlay` and `EvecualMCClient` contextual 'H' key inspect.
    - `PickupDroneEntity` now displays `🛡️ Pickup Drone` with battery meter, cargo fill count, and opens the dedicated Pickup Drone Field Guide.
    - Handheld and Stationary RC Controllers now store and display `Pickup Drone` in tooltips, link notifications, and active telemetry HUD overlays (`RcHudManager`).
- **3D Inventory Model Fix**:
  - Fixed `pickup_drone_parking_spot` block item model rendering flat/edge-on by inheriting `minecraft:block/block`.

## [1.8.11] - 2026-09-12
### Added & Improved
- **Drone Pickup Station (`drone_pickup`) & Special Parking Spot (`pickup_drone_parking_spot`)**:
  - Added dedicated **Drone Pickup Station** base and **Pickup Drone Parking Spot** helipad.
  - The special parking spot requires placement directly on top of a Drone Pickup Station.
  - Pickup Drones prioritize this special helipad when triggering Autopilot Return (C Key).
  - When landed, the station docks the drone, establishes a magnetic lock, and provides automated cargo offloading.
- **Electric Chute (`electric_chute`)**:
  - High-voltage pneumatic transfer conduit that connects between Drone Pickup Stations and Storage Units (or chains multiple conduits together).
  - Requires electricity (5 EU/transfer) and automatically draws power from wires, batteries, generators, or attached storage units.
  - Automatically draws harvested items from the docked Pickup Drone's cargo bay and deposits them into connected Storage Units with authentic pneumatic dispense audio and cyan electric spark VFX.
- **Storage Unit (`storage_unit`) & Multiblock Quantum Vault**:
  - Colossal expandable electric storage unit with **108 slots** per unit—more space than anything in vanilla Minecraft.
  - **Multiblock Expansion**: Placing multiple Storage Units adjacent to each other automatically links them into a single unified multi-block storage bank (2 units = 216 slots, 3 units = 324 slots, etc.).
  - **Paginated Vault UI**: Sleek screen displaying 54 slots per page with `[◀]` and `[▶]` page buttons, live energy meter, and connected unit telemetry.
  - **Electric Shulker Box Mechanics**: When broken while electrically charged (≥ 200 EU), the unit retains all 108 slots and energy inside its item drop without spilling. If broken uncharged, the containment field collapses and spills its contents.
  - **Power Initialization**: When placed down with stored items, consumes 200 EU of electricity to initialize and unlock the quantum storage matrix.
- **Pickup Drone Model & Texture Fixes**:
  - Sealed all geometric gaps and holes on the Pickup Drone motor booms, struts, and landing skids with solid overlapping trusses.
  - Eliminated transparent texture UV cutouts by reinforcing carbon-weave pylon and side pod textures.
  - Upgraded model rendering to `getEntityCutoutNoCull` so interior geometry and thin shrouds are never culled.
- **Shader Pack & Cable Shader Fix**:
  - Fixed `EvecualTechShader.zip` packaging: replaced Windows backslash directory separators with standard forward slash paths (`shaders/...`), resolving Iris console error *"Pack EvecualTechShader.zip is not valid! Can't load it"*.
  - Fixed wire cable face rendering: corrected `wire_side.json` UV mapping on west and east faces from transparent space (`[0,6,6,10]` and `[10,6,16,10]`) to valid wire texture pixels (`[6,0,10,6]`), restoring missing faces when shaders are enabled.
  - Upgraded `gbuffers_terrain.vsh` and `gbuffers_terrain.fsh` with world-space `geoNormal` and uniform lighting on mod blocks so wire cables never render with only one face illuminated.
- **Remade Tip Menus for Pickup Drone & Logistics**:
  - Remade the Tip Menu for the Pickup Drone (`pickup_drone`) with comprehensive defense harvester specifications, docking procedures, and flight controls.
  - Added dedicated Field Guide Tip Menus for Drone Pickup Station, Pickup Drone Parking Spot, Electric Chute, and Storage Unit.
  - Updated Shift + Right-Click on Pickup Drone Item to directly open the new Pickup Drone Tip Menu.

## [1.8.10] - 2026-09-12
### Added & Improved
- **Defense System Overhaul for Pickup Drone (`pickup_drone`)**:
  - Redesigned the 3D entity model (`PickupDroneEntityModel`) and pixel textures to give the Pickup Drone an authentic high-tech military defense system aesthetic:
    - **Stealth Composite Armor Hull**: Angular chiseled gunmetal fuselage with armored top canopy and structural spine.
    - **Tactical Sensor Radome**: Top-mounted tactical radar dome with communication mast and sensor optics.
    - **FLIR Targeting Optics Gimbal**: Front armored sensor aperture with illuminated cyan targeting lens.
    - **Magnetic Vacuum Harvester Pod**: Underslung magnetic suction cowl with illuminated cyan vortex rings and yellow-black industrial hazard stripes.
    - **Dual Defense Conduit Pods**: Armored lateral sensor pods with heatsink ventilation grilles and status LEDs.
    - **Heavy Carbon Motor Booms & Motor Cowlings**: Reinforced angular carbon fiber motor pylons with heavy-duty brushless motor housings and rotor blade safety stripes.
  - **Pixel Art Item Icon**: Remade the `pickup_drone` item icon with matching defense system gunmetal hull, carbon booms, glowing cyan optics, and magnetic intake ring.
  - **Default Tactical Coating**: Initialized default appearance to Tactical Defense Gunmetal with full custom dyeing support.

## [1.8.9] - 2026-09-12
### Added & Improved
- **Pickup Drone (`pickup_drone`)**:
  - Added the **Pickup Drone**, a direct quadcopter copy of the RC Drone engineered specifically for remote item collection and automated vacuum harvesting.
  - **Automated Cargo Vacuum**: While flying or hovering near dropped items in the world (within 1.8m), the drone automatically vacuums items into its 9-slot onboard cargo bay with authentic pickup audio feedback.
  - **Full RC Drone Flight & Controls**: Features identical 3D flight kinematics, tilt physics (pitch & roll), high-speed rotor blur discs, spotlight illumination, battery telemetry, and helipad docking/auto-return.
  - **Remote Control & Ground Station Pairing**: Fully compatible with the handheld RC Controller and Stationary Ground Terminal.
  - **Interactive Handling & Customization**: Supports color dyeing (6 variants), direct cargo access (empty hand right-click), survival item retrieval (Shift + Right-Click with empty hand), and drops itself as an item when damaged.
  - **Crafting Recipes**:
    - Shapeless crafting: 1 RC Drone + 1 Hopper -> 1 Pickup Drone.
    - Reverse conversion: 1 Pickup Drone -> 1 RC Drone.

### Removed
- **Defense Drone & Defense Controller**:
  - Completely removed the Defense Drone entity (`flying_turret`), item, model, renderer, screen, and screen handler.
  - Completely removed the Defense Controller item (`flying_turret_controller`).
  - Removed defense drone recipes, network packets, input listeners, and camera hooks.
  - Cleaned up Turret Ammo Container to link exclusively to Stationary Turrets.

## [1.8.8] - 2026-09-12
### Added & Improved
- **Freeze Player Movement During Defense Drone Control**:
  - Connected the Defense Controller (`FLYING_TURRET_CONTROLLER_ITEM`) and paired defense drone link to the client input lock system (`isRcLinkActive`).
  - While actively piloting or maneuvering the Defense Drone (via WASD, Space to ascend, Sneak to descend, or Sprint turbo), player walking, strafing, jumping, sneaking, and sprinting are completely suppressed.
  - The player's velocity is locked in place so you remain perfectly stationary on the ground while operating the Defense Controller.
  - Suppressed hand swinging and accidental block breaking when clicking Attack (LMB) to fire the drone's kinetic cannon.
  - Added Defense Drone camera pitch and yaw support to `CameraMixin` for smooth third-person camera panning and zoom.

## [1.8.7] - 2026-09-12
### Added & Improved
- **Remade Defense Turret to be the Drone (`Defense Drone`)**:
  - Rebuilt the aerial defense turret into an authentic high-fidelity combat drone (`Defense Drone`), utilizing official quadcopter geometry, aerodynamic chassis, 4 motor booms, high-RPM rotor discs, and dual underslung kinetic railcannons with energy conduits.
  - **Stationary Defense Mode (Requires Defense Controller to Move)**:
    - The Defense Drone no longer wanders or flies in circles on autonomous patrol.
    - When deployed on ground or in air, the drone maintains a rock-solid gyro-stabilized hover at its station coordinates (`stationPos`).
    - Tracks and aims directly at hostile targets across up to 1024x1024 blocks (512m radius), automatically firing kinetic rounds from linked ammo containers while remaining stationed in place.
    - If nudged or pushed by mob collisions or explosions, the drone smoothly returns to its station coordinates.
  - **Defense Controller Piloting**:
    - You need the **Defense Controller** to move, reposition, or fly the drone.
    - Full 3D directional vectoring (WASD, Space, Sneak, Sprint Turbo) with synchronized pitch and roll tilt kinematics.
    - When flight inputs cease, the drone immediately anchors its new defense station coordinates right where you left it.
    - Recall key (`C`) commands the drone to navigate directly back to the player.
  - **Interactive Handling & Survival Retrieval**:
    - Shift + Right-Click with an empty hand safely retrieves the Defense Drone into your inventory.
    - Automatically drops as a Defense Drone item on death in survival.
    - Can be placed on blocks or right-clicked in mid-air to deploy directly into hover defense mode.
    - Sneak + Right-Click on existing placed Stationary Turrets immediately converts them into a mobile Defense Drone.
    - Added shapeless crafting recipe: 1 Stationary Turret -> 1 Defense Drone.

## [1.8.6] - 2026-09-11
### Added & Improved
- **Flying Turret Remote Controller**:
  - Added dedicated handheld flight and combat remote control device (`Flying Turret Controller`) for the Flying Defense Drone with a 1024-block operating range.
  - **Pairing & Link Management**:
    - Right-click any Flying Defense Drone within 16 blocks to establish an encrypted radio telecommand link.
    - Right-click in the air to toggle remote link between `ACTIVE` and `STANDBY`.
    - Shift + Right-Click in the air to remotely access the Flying Drone's Area & Target Filter Terminal across dimensions.
  - **First-Person Remote View (FPV Camera)**:
    - Press `F` while paired to switch your camera directly to the Flying Drone's nose-mounted tactical optics.
    - Supports full mouse look steering and optical zoom.
  - **Manual 3D Flight Piloting**:
    - Full manual 3D maneuver control (WASD for horizontal directional vectoring, Space to Ascend, Sneak to Descend, Sprint / Ctrl for high-speed turbo thrusters).
  - **Manual Kinetic Cannon Trigger**:
    - Press Left-Click (Attack Key) with the controller or in FPV view to fire kinetic rounds directly at your crosshair.
  - **Instant Recall**:
    - Press `C` (Auto Park Key) with the controller to instantly command the drone to return to your current position.
  - **Crafting Recipe**: 1 Eye of Ender + 2 Steel Ingots + 1 Turret Linker + 1 Redstone.

## [1.8.5] - 2026-09-11
### Added & Improved
- **Stationary Defense Turret**:
  - Placed base defense turret with full 360-degree yaw and pitch rotation.
  - Supports configurable defense zone radius from 16x16 up to 512x512 blocks.
  - Automatically draws kinetic ammunition from linked Turret Ammo Containers via wireless encrypted quantum coordinates.
  - 2.0-second firing cooldown (40 ticks) between shots with smooth aiming kinematics.
  - Configurable target whitelist & threat filtering: Players (safe vs hostile), Hostile Monsters, Passive Animals, and Bosses.
- **Flying Defense Turret Drone**:
  - Autonomous aerial defense drone capable of high-altitude hovering and patrolling.
  - Expanded massive defense zone coverage from 16x16 up to 1024x1024 blocks (radius up to 512 blocks).
  - Configurable patrol flight altitude (6m to 48m above terrain) and dynamic return-to-base navigation.
  - 2.0-second firing cooldown with dual gimbal-stabilized kinetic rail cannons.
  - Same advanced target filtering GUI and wireless linked ammo supply.
- **Turret Ammo Container & Tiered Munitions**:
  - **Turret Ammo Container**: 27-slot dedicated munitions silo that supplies linked stationary and flying turrets across any distance.
  - **Turret Linker Device**: Handheld pairing tool that links containers to turrets with a single right-click sequence.
  - **Copper Defense Ammo**: Light projectile dealing 6 damage at 0.8x projectile speed.
  - **Iron Defense Ammo**: Standard armor-piercing kinetic projectile dealing 14 damage at 1.2x projectile speed.
  - **Diamond Defense Ammo**: High-velocity heavy tungsten-diamond projectile dealing 30 damage at 1.8x projectile speed.
  - Smart automatic ammo selection: prioritize highest tier ammo available in the linked container with particle tracers and impacts.

## [1.8.4] - 2026-09-11
### Added & Improved
- **Lightning Item Durability & Extended Cooldown**:
  - Added 64 durability points to the Lightning Item, consuming 1 durability per strike.
  - Increased cooldown from 0.5s (10 ticks) to 4.0s (80 ticks) between strikes to prevent spamming.
  - Added durability and cooldown status indicators to item tooltips.
- **Electronic Duper Tiered Item Rarity & Duration Scaling**:
  - Replaced the flat duplication cycle with a dynamic item rarity evaluation system (`DuperRarityHelper`):
    - **Common / Basic Materials** (Dirt, Stone, Cobblestone, Seeds, Wood, Gravel, Sand, etc.): 15 seconds (300 ticks), 100 EU.
    - **Uncommon / Resources** (Iron, Copper, Coal, Redstone, Lapis, Quartz, Amethyst, Gunpowder, etc.): 60 seconds (1,200 ticks), 400 EU.
    - **Precious Metals & Drops** (Gold, Ender Pearls, Blaze Rods, Slimeballs, Obsidian, etc.): 3 minutes (3,600 ticks), 800 EU.
    - **Rare / Gems** (Diamonds, Emeralds, Ancient Debris, Nautilus Shells, etc.): 15 minutes (18,000 ticks), 1,500 EU.
    - **Epic Artifacts** (Beacons, Totems of Undying, Tridents, Enchanted Apples, etc.): 25 minutes (30,000 ticks), 2,000 EU.
    - **Shulker Class** (Shulker Shells & Shulker Boxes): 35 minutes (42,000 ticks), 2,500 EU.
    - **End-Game / Legendary** (Elytra, Dragon Eggs, Nether Stars): 60 minutes (72,000 ticks), 3,000 EU.
    - **Mythic Netherite** (Netherite Ingots, Scraps, Armor, Tools, Templates): 75 minutes / 1h 15m (90,000 ticks), 3,000 EU.
  - Screen and HUD overlays now dynamically render item rarity tier, required energy, progress %, and exact remaining time in `Xh Ym Zs` format without 16-bit integer limitations.
- **Cable Transmission Distance Limitation**:
  - Power wires and cables now transmit electricity reliably up to a maximum distance of 32 blocks from the power source or battery cluster, preventing infinite distance propagation.
  - Traversed wires actively record energy throughput during electrical distribution.

### Fixed
- **Electronic Duper Missing Texture**:
  - Created dedicated 16x16 pixel-art textures (`duper_front.png`, `duper_side.png`, `duper_top.png`, `duper_bottom.png`) with cyan quantum vortex emitters, illuminated status LEDs, and reinforced alloy chassis.
  - Corrected `models/block/electronic_duper.json` texture mappings to resolve missing purple-black checkerboard rendering.
- **Battery Energy Output & Machine Compatibility**:
  - Fixed a bug where Battery Blocks failed to output electricity to connected machines (such as the Electronic Duper).
  - Generalized battery discharge to recognize any `EnergyStorage` receiver.
  - Streamlined battery cluster distribution so the whole cluster discharges uniformly and charge levels remain synchronized.

## [1.8.3] - 2026-09-10
### Fixed
- **Helicopter Fall Damage & High-Speed Flight Safety**:
  - Fixed an issue where players flying or landing the EV Helicopter at high speeds took phantom fall damage and died (`Player hit the ground too hard`).
  - Added passenger fall distance resets on flight ticks and dismount, and overrode `handleFallDamage` and `fall` in `HeliEntity` to ensure neither the helicopter nor its passengers suffer fall damage.
- **RC Robot Screen Layout & Tips Button Alignment**:
  - Re-aligned the `💡 Tips` button in `RcRobotScreen` above the right sidebar to eliminate visual overlap with the cargo title.
- **Shaderpack Auto-Sync on Launch**:
  - Updated `launch.ps1` to automatically synchronize `shader/EvecualTechShader.zip` to `run/shaderpacks/` (and `.minecraft/shaderpacks/` when installed) so the permanent daytime twilight sky palette is loaded immediately.

## [1.8.2] - 2026-09-10
### Added & Improved
- **RC Robot Dual-Arm Inventory Integration**:
  - Overhauled the RC Excavator Robot inventory interface with a dedicated custom GUI (`RcRobotScreenHandler` & `RcRobotScreen`).
  - Added dedicated **Left Arm** and **Right Arm** equipment slots directly in the robot's inventory screen:
    - **Left Arm Slot**: Exclusively accepts **blocks** (`BlockItem`) for Left Arm (RMB) placement in the world. Rejects tools, weapons, and non-block items.
    - **Right Arm Slot**: Exclusively accepts **tools / weapons / items** for Right Arm (LMB) mining and combat. Rejects block items.
  - Full drag-and-drop support: Players can freely pick any block or tool from the robot's 54 cargo slots or their player inventory and equip them directly into either hand.
  - Smart Shift-Click (`quickMove`): Shift-clicking blocks automatically equips them into an empty Left Arm; shift-clicking tools/items automatically equips them into an empty Right Arm. Shift-clicking equipped arms returns items to cargo or player inventory.
  - Seamless World Synchronization: Equipping or removing items from the screen instantly updates the robot's 3D claw models and server-side tracking.
  - Direct Empty-Hand Interaction: Right-clicking the RC Robot in person with an empty hand (without sneaking) now immediately opens the full cargo and arm management screen.

## [1.8.1] - 2026-09-10
### Fixed & Improved
- **RC Controller Right-Click & Unlinking Resolution**:
  - Fixed an issue where right-clicking with the RC Controller (such as when placing blocks with the RC Robot's left arm) unlinked the controller and forced the player camera back to normal view.
  - Normal right-click when active now preserves the active connection and never toggles to standby.
  - To intentionally switch the controller to `STANDBY` or toggle links, players use **Shift + Right-Click** (`Sneak + Use`).
  - Added `MinecraftClientMixin` to suppress client-side `doItemUse` and `doAttack` while actively looking through any RC vehicle camera, ensuring block placement and tool mining are handled exclusively by remote vehicle inputs without interference from the player's physical body.
  - Updated in-game tooltips with explicit control hints: `[Right-Click] Connect | [Shift + Right-Click] Standby`.
- **EvecualTechShader Permanent Twilight Daytime Sky**:
  - Permanently applied the requested 1-minute twilight color palette across the entire daytime sky:
    - Zenith: Dreamy pastel violet / twilight purple (`#76619E`)
    - Mid-sky: Radiant soft orchid / rose twilight (`#BF7399`)
    - Horizon: Warm peach / golden sunset blush (`#F7AD85`)
  - Tuned solar corona with warm golden sunlight illumination (`#FFE6C7`).
  - Updated procedural volumetric cloud shading and atmospheric horizon fog to harmonize with the soft rose and golden peach aesthetic.

## [1.8.0] - 2026-09-10
### Added & Overhauled
- **RC Excavator Robot Dual-Arm System**:
  - Fully implemented independent dual-arm mechanics: Right Arm for weapons and mining tools, Left Arm for block/item placement.
  - **Right Arm (LMB)**: Executes strikes, weapon attacks, block mining, and tool actions with animated hydraulic swing.
  - **Left Arm (RMB)**: Places blocks and items directly into the world with reach raycasting, block orientation awareness, placement sound effects, and block particles.
  - **Smart Inventory Refill**: Automatically pulls blocks from the 54-slot cargo chest when the equipped left arm stack is empty.
  - **Interactive Equipping**: Right-click robot with tools/weapons to equip Right Arm; right-click with blocks to equip Left Arm.
  - **3D Claw Rendering**: The equipped block/item is rendered directly in the robot's left claw clamp.
- **Perspective Toggle Key Remap (`<`)**:
  - Remapped RC vehicle perspective toggle from RMB to the `<` key (configurable via `RC_PERSPECTIVE_KEY`, supporting ISO `GLFW_KEY_WORLD_1` and US comma).
  - Frees up RMB exclusively for secondary vehicle and left-arm actions.
- **Lightning Item Fire Mode Switch**:
  - Added Sneak + Right-Click toggle between **Fire Mode** (standard fire-spawning lightning) and **No-Fire Mode** (pure lightning strike dealing damage without placing fire).
  - Displays instant actionbar alerts and detailed item tooltips.
- **Helicopter Cockpit In-Flight Item Interaction**:
  - Players seated in the EV Helicopter cockpit can now use, eat, interact with, and pair handheld inventory items (such as RC Controllers and Heli Controllers).
- **Helicopter Weapon Hardpoint Angling**:
  - Wing hardpoint weapons (miniguns, rockets, mining drills) can now be angled up and down (-60° to +30°) using Arrow Keys or configurable keybindings (`HELI_WEAPON_UP_KEY` / `HELI_WEAPON_DOWN_KEY`).
  - Rotates 3D wing weapon models and adjusts projectile trajectory pitch in real time.
- **Same-Keybind Dual-Arm Firing**:
  - Implemented direct input polling helper (`isKeyOrMousePressed`) allowing both left and right helicopter hardpoint arms to fire simultaneously when bound to the same key or button.

### Fixed
- **EvecualTechShader Sky Invariance**:
  - Fixed camera pitch and yaw altering the sky color gradient and sunlight glare. Elevation is now computed invariant to view rotation via true celestial eye-space dot products.
- **Glass Translucency & Water/Particle Visibility**:
  - Resolved invisibilities where water, mining arm particles, and sonic boom trails could not be seen through helicopter glass panes.
  - Added particle shader passes and configured `separateEntityDraws=true` and `particles.ordering=after`.
- **Helicopter Cockpit Canopy Holes**:
  - Sealed visible gaps where the front windshield, side door glass, and roof canopy meet.
- **Controls Menu Localization**:
  - Added missing localization entries in `en_us.json` and `en-US.json` for all custom keybindings and container titles.

## [1.7.0] - 2026-09-06
### Added
- **Electronic Duper Block (`electronic_duper`)**:
  - Industrial quantum replication block that duplicates any material, item, tool, or vehicle unit (Stone, Iron Ore, Diamonds, Cars, Helis, Custom Mod Items, tools with NBT) every 2 minutes (2400 ticks / 120 seconds).
  - Requires 1500 EU per duplication cycle with a maximum energy buffer of 3000 EU.
  - Full automatic hopper and energy pipe integration (`SidedInventory`): top/sides for input items, bottom for output items, and wire network automatic energy transfer.
  - Fully preserves all item NBT metadata, lore, custom names, damage states, and vehicle configurations upon duplication.
  - Features dark glassmorphic GUI with interactive progress gauge, item slots, energy status bar, particle VFX, custom audio effects, crosshair HUD overlay, and Field Guide entry.

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
