package com.evecual.evecualmc.client.screen;

import com.evecual.evecualmc.EvecualMC;
import net.minecraft.item.ItemStack;

import java.util.function.Supplier;

public enum TipTopic {
    SOLAR_PANEL(
            "solar_panel",
            "Solar Panel",
            "⚡ Power Generation",
            () -> new ItemStack(EvecualMC.SOLAR_PANEL_ITEM),
            "High-efficiency photovoltaic module that captures daylight to generate clean electrical energy.",
            new String[]{
                    "Place directly facing an unobstructed view of the open sky.",
                    "Generates up to 10 EU/tick during clear daylight hours.",
                    "Automatically transfers power directly into adjacent Batteries, Wires, or Chargers.",
                    "Output naturally decreases during rain, thunderstorms, sunrise, and sunset."
            },
            new String[][]{
                    {"Right-Click", "Open Diagnostics & Field Guide"},
                    {"Pickaxe/Hand", "Mineable by hand or tool (drops itself)"}
            },
            new String[]{
                    "Link multiple panels in parallel using Wires to rapidly fill large battery banks.",
                    "Ensure no solid blocks or leaves block the column of air above the panel.",
                    "Batteries can store excess daytime energy to keep your chargers running overnight."
            }
    ),

    ELECTRONIC_DUPER(
            "electronic_duper",
            "Electronic Duper",
            "⚡ Quantum Duplication",
            () -> new ItemStack(EvecualMC.ELECTRONIC_DUPER_ITEM),
            "High-tech quantum duplication chamber that replicates any material, item, tool, or vehicle unit using 1500 EU over a 2-minute cycle.",
            new String[]{
                    "Place any item (ores, minerals, electronics, tools, cars, helis, mod items) into the Input Slot (Slot 0).",
                    "Connect to Wires, Batteries, Wind Turbines, or Solar Panels up to 3000 EU max storage.",
                    "Consumes 1500 EU and begins a 2-minute (2400-tick) duplication process.",
                    "Upon completion, deposits 1 duplicate unit into the Output Slot (Slot 1) preserving all item NBT metadata!"
            },
            new String[][]{
                    {"Right-Click", "Open Duplication GUI & Progress Gauge"},
                    {"Input Slot", "Place target item to duplicate"},
                    {"Output Slot", "Collect duplicated item units"},
                    {"Sided Automation", "Hoppers / Pipes insert on top/sides, extract from bottom"}
            },
            new String[]{
                    "Connect high-output Wind Turbines or Battery clusters to supply 1500 EU continuously.",
                    "Duplication preserves all item NBT data, including customized cars, upgraded helis, and full inventories!",
                    "Use Hoppers or automation pipes to continuously feed materials into the top and pull duplicates from the bottom."
            }
    ),

    WIND_TURBINE(
            "wind_turbine",
            "Wind Turbine",
            "⚡ Power Generation",
            () -> new ItemStack(EvecualMC.WIND_TURBINE_ITEM),
            "Aerodynamic wind turbine generator that outputs a high continuous power supply of 50 EU/t directly out of its back port.",
            new String[]{
                    "Place the turbine facing your preferred direction.",
                    "Generates 50 EU/t continuously.",
                    "Energy outputs directly out of the BACK face of the block into wires, batteries, or chargers.",
                    "Connect wires or battery banks directly behind the back port for instant charging."
            },
            new String[][]{
                    {"Right-Click", "Open Diagnostics & Live Output"},
                    {"Placement", "Faces player; energy outputs out of the BACK port"}
            },
            new String[]{
                    "Connect power wires directly behind the back port to distribute electricity across your base.",
                    "Multiple Wind Turbines can feed directly into a unified multi-block battery cluster.",
                    "Combine Wind Turbines with Solar Panels for maximum day and night energy generation."
            }
    ),

    BATTERY(
            "battery",
            "Battery",
            "🔋 Energy Storage",
            () -> new ItemStack(EvecualMC.BATTERY_ITEM),
            "Advanced industrial lithium-iron storage cell holding up to 10,000 EU.",
            new String[]{
                    "Stores electrical power generated from Solar Panels and networks.",
                    "Accepts and outputs energy seamlessly across connected Wires and adjacent blocks.",
                    "Features 8 distinct visual charge states indicating current capacity.",
                    "Emits a redstone comparator signal (0 to 15) proportional to stored power."
            },
            new String[][]{
                    {"Right-Click", "Open Diagnostics & Live Capacity"},
                    {"Comparator", "Reads current charge level as redstone output"}
            },
            new String[]{
                    "Place multiple Battery blocks side-by-side to form high-capacity modular storage banks.",
                    "Use comparator outputs to automate backup generators or warning lights when low on power.",
                    "Maintains its stored energy when mined and moved in survival."
            }
    ),

    WIRE(
            "wire",
            "Power Wire",
            "🔌 Power Transmission",
            () -> new ItemStack(EvecualMC.WIRE_ITEM),
            "Heavy-duty insulated copper cabling designed for loss-free long-distance energy conduction.",
            new String[]{
                    "Dynamically forms 6-way branch connections (North, South, East, West, Up, Down).",
                    "Connects automatically with Solar Panels, Batteries, Chargers, and Combiners.",
                    "Instantly equalizes and balances energy across all connected power consumers and sources."
            },
            new String[][]{
                    {"Right-Click", "Inspect Electrical Network & Guide"},
                    {"Placement", "Attaches to any face of adjacent blocks"}
            },
            new String[]{
                    "Wires can be concealed under floors or inside walls to create clean, hidden electrical grids.",
                    "Cables transfer power reliably up to 32 blocks away from your power generators and batteries.",
                    "Single wires can bridge solar arrays on roofs straight down into basement battery rooms."
            }
    ),

    CHARGER(
            "charger",
            "Car Charger",
            "🚗 Vehicle Supercharger",
            () -> new ItemStack(EvecualMC.CHARGER_ITEM),
            "High-voltage DC fast-charging terminal capable of holding 20,000 EU.",
            new String[]{
                    "Stores high voltage power to rapid-charge parked electric passenger cars.",
                    "Placing this block broadcasts an automatic GPS waypoint HUD marker to drivers server-wide.",
                    "Connects directly with Charger Extension posts and Cables to reach parking bays up to 16m away."
            },
            new String[][]{
                    {"Right-Click", "Open Charger Diagnostics & Live Energy"},
                    {"Waypoints", "Appears on driver HUD compass automatically"}
            },
            new String[]{
                    "Keep connected to Batteries or Solar Panels so it stays primed with full 20,000 EU reserve.",
                    "Install near your garage or parking lot for convenient vehicle turnaround.",
                    "Use Charger Extension posts to wire multiple parking spots back to a single central Charger."
            }
    ),

    CHARGER_EXTENSION(
            "charger_extension",
            "Charger Extension Post",
            "🔌 Retractable Cable Post",
            () -> new ItemStack(EvecualMC.CHARGER_EXTENSION_ITEM),
            "Heavy-duty metallic bollard that houses a flexible, retractable Charger Cable.",
            new String[]{
                    "Right-click with a Charger Cable in hand to install the cable into the post.",
                    "Right-click with empty hand to plug the cable directly into your parked car within 16 blocks.",
                    "Draws power from nearby Chargers and Wires to supply cars parked in designated bays."
            },
            new String[][]{
                    {"Right-Click (Cable)", "Install Charger Cable into post"},
                    {"Right-Click (Empty)", "Connect / Disconnect cable to vehicle"},
                    {"Sneak + Right-Click", "Open Tips & Diagnostics Screen"},
                    {"'X' Key (In Car)", "Quick-plug / unplug cable while in driver's seat"}
            },
            new String[]{
                    "Cables can extend up to 16 blocks across parking lots and through open garage bays.",
                    "Pressing X while seated in the car lets you plug and unplug without ever stepping outside.",
                    "Left-clicking or breaking the post safely refunds the installed Charger Cable into your inventory."
            }
    ),

    PARKING_LINES(
            "parking_lines",
            "Parking Lines",
            "🅿️ Roadway Infrastructure",
            () -> new ItemStack(EvecualMC.PARKING_LINES_ITEM),
            "Precision reflective road markings outlining standardized 4-block automotive parking bays.",
            new String[]{
                    "Designates designated parking stalls for full-sized electric cars.",
                    "Provides navigation anchors for car autopilot systems.",
                    "Completely non-obstructive: cars and players drive and walk smoothly over the painted surface."
            },
            new String[][]{
                    {"Right-Click", "Open Parking System Guide"},
                    {"'C' Key (In Car)", "Activate Autopilot to park squarely in lines"}
            },
            new String[]{
                    "Electric cars automatically seek out and park inside these lines when pressing C.",
                    "Cars with low battery (≤5%) within 50 blocks automatically self-drive into the nearest bay.",
                    "Paint parking bays directly next to Charger Extension posts for the ultimate automated garage."
            }
    ),

    RC_CHARGER(
            "rc_charger",
            "Wireless RC Charger",
            "⚡ Wireless Power Station",
            () -> new ItemStack(EvecualMC.RC_CHARGER_ITEM),
            "High-capacity induction broadcasting station that wirelessly charges RC Parking Spots across a 16-block radius.",
            new String[]{
                    "Full block terminal that connects directly to Wires, Solar Panels, and Batteries.",
                    "Wirelessly broadcasts electricity to RC Parking Spots, Drone Helipads, and Robot Parking Spots within 16 blocks.",
                    "When powered, emits idle spark indicators on all charge-ready connected parking spots."
            },
            new String[][]{
                    {"Right-Click", "Open Live Power Diagnostics & Guide"},
                    {"Power Input", "Accepts energy seamlessly from Wires, Solar Panels, and Batteries"}
            },
            new String[]{
                    "The Wireless RC Charger itself is a power broadcast station, not a parking pad.",
                    "Park vehicles on their designated parking spots (Car, Drone, or Robot) to charge wirelessly.",
                    "A single charger can easily power an entire garage containing cars, drones, and utility robots."
            }
    ),

    RC_PARKING_SPOT(
            "rc_parking_spot",
            "RC Parking Spot",
            "🅿️ Ground Vehicle Dock",
            () -> new ItemStack(EvecualMC.RC_PARKING_SPOT_ITEM),
            "Precision ground docking bay engineered specifically for RC Cars.",
            new String[]{
                    "When an RC Car enters this spot, it powers down motors and unpairs to conserve battery.",
                    "Wirelessly receives recharge energy whenever a Wireless RC Charger is within a 16-block radius.",
                    "Provides a designated home base where vehicles stay safe from hostile mobs."
            },
            new String[][]{
                    {"Right-Click", "Open Ground Docking Guide"},
                    {"'C' Key (RC Link)", "Autopilot car directly into this spot"}
            },
            new String[]{
                    "Keeps parked cars charged at 100% so they are always ready for deployment.",
                    "To deploy a parked vehicle, simply aim the RC Controller and right-click to re-pair.",
                    "Emits electric sparks when an active Wireless RC Charger is within 16 blocks."
            }
    ),

    DRONE_PARKING_SPOT(
            "drone_parking_spot",
            "Drone Helipad",
            "🚁 Aerial Landing Pad",
            () -> new ItemStack(EvecualMC.DRONE_PARKING_SPOT_ITEM),
            "Targeted rooftop and ground helipad engineered specifically for RC Drones.",
            new String[]{
                    "When an RC Drone lands on this pad, motors safely shut down and remote connection unpairs.",
                    "Wirelessly draws energy from any active Wireless RC Charger located within a 16-block radius.",
                    "Serves as a designated high-altitude or courtyard landing zone for aerial missions."
            },
            new String[][]{
                    {"Right-Click", "Open Aerial Helipad Guide"},
                    {"'C' Key (RC Link)", "Command drone to autopilot and touch down"}
            },
            new String[]{
                    "During auto-dock, drones ascend to cruising altitude, fly straight over, and land vertically.",
                    "Build landing pads on castle towers, fortress walls, or rooftop gardens for instant deployment.",
                    "Emits electric sparks when an active Wireless RC Charger is within 16 blocks."
            }
    ),

    ROBOT_PARKING_SPOT(
            "robot_parking_spot",
            "Robot Parking Spot",
            "🤖 Robot Docking Bay",
            () -> new ItemStack(EvecualMC.ROBOT_PARKING_SPOT_ITEM),
            "Heavy-duty industrial ground docking bay engineered specifically for RC Excavator Robots.",
            new String[]{
                    "When an RC Robot enters this spot, it powers down motors and unpairs to conserve battery.",
                    "Wirelessly receives recharge power whenever a Wireless RC Charger is within a 16-block radius.",
                    "Provides an organized staging bay for automated excavation and mining operations."
            },
            new String[][]{
                    {"Right-Click", "Open Robot Docking Guide"},
                    {"'C' Key (RC Link)", "Command paired robot to navigate and dock in this spot"}
            },
            new String[]{
                    "Keeps mining robots charged at 100% capacity so they are ready for digging work.",
                    "Aim RC Controller and right-click to deploy and resume mining operations.",
                    "Features amber hazard indicators and pulses with sparks when in range of an active charger."
            }
    ),

    RC_CAR(
            "rc_car",
            "RC Car",
            "🏎️ Ground Recon Vehicle",
            () -> new ItemStack(EvecualMC.RC_CAR_ITEM),
            "High-speed agile remote-controlled rover with a 256-block operational control range.",
            new String[]{
                    "Pair by aiming with the RC Controller and right-clicking.",
                    "Press 'F' to switch your camera directly into the RC Car cockpit.",
                    "Press 'RMB' to toggle between First-Person cockpit and Third-Person exterior chase views.",
                    "Equipped with built-in trunk storage accessible by right-clicking with an empty hand."
            },
            new String[][]{
                    {"W / S", "Forward Throttle / Reverse"},
                    {"A / D or Mouse", "Steering / Cockpit Aim"},
                    {"Space", "Pneumatic Hop (Leap over obstacles)"},
                    {"F", "Enter / Exit RC Camera View"},
                    {"RMB (In Cam)", "Toggle First-Person / Third-Person view"},
                    {"Scroll Wheel", "Optical Zoom (FP) / Distance (TP)"},
                    {"L", "Toggle 15-Level Torch Headlights"},
                    {"C", "Autopilot back to Charger / Parking Spot"},
                    {"Sneak + R-Click", "Pick up vehicle into inventory"}
            },
            new String[]{
                    "Headlights project real block light (level 15) illuminating deep caves and dark ravines.",
                    "Pneumatic hop (Space) lets the car leap over blocks, fences, and low obstacles.",
                    "Pressing C automatically guides the car back to the nearest charging pad up to 50 blocks away."
            }
    ),

    RC_DRONE(
            "rc_drone",
            "RC Drone",
            "🚁 Long-Range Quadcopter",
            () -> new ItemStack(EvecualMC.RC_DRONE_ITEM),
            "High-altitude surveillance drone with an expansive 512-block operational flight range.",
            new String[]{
                    "Pair by aiming with the RC Controller and right-clicking.",
                    "Press 'F' to enter the drone's high-definition camera feed.",
                    "Press 'RMB' to toggle between First-Person FPV cockpit and exterior orbit views.",
                    "Equipped with an airborne cargo bay for transporting supplies across long distances."
            },
            new String[][]{
                    {"W / S", "Pitch Forward / Backward"},
                    {"A / D (FP Mode)", "Lateral Strafe Left / Right"},
                    {"A / D (TP Mode)", "Rotate Heading on the spot"},
                    {"Mouse", "Steer Yaw & Aim Camera Pitch"},
                    {"Space / Shift", "Ascend / Descend Altitude"},
                    {"Ctrl (Sprint)", "Turbo Jet Propulsion Boost"},
                    {"F", "Enter / Exit FPV Camera Feed"},
                    {"RMB (In Cam)", "Toggle First-Person / Third-Person view"},
                    {"Scroll Wheel", "Optical Zoom (FP) / Distance (TP)"},
                    {"L", "Toggle High-Power Spotlight (Level 15)"},
                    {"C", "Autopilot return & vertical landing"}
            },
            new String[]{
                    "In First-Person FPV mode, mouse steers heading while A and D strafe laterally with realistic bank.",
                    "Autopilot (C) locks onto the home helipad (or searches within 128m), ascends to cruise height, and lands vertically.",
                    "High-powered spotlight cuts through deep fog, night skies, and underwater depths."
            }
    ),

    PICKUP_DRONE(
            "pickup_drone",
            "Pickup Drone",
            "🛡️ Tactical Defense Harvester",
            () -> new ItemStack(EvecualMC.PICKUP_DRONE_ITEM),
            "Military-grade defense system quadcopter featuring composite armor plating, FLIR optics, automated magnetic item vacuum, special Drone Pickup station docking, and electric chute cargo unloading.",
            new String[]{
                    "Equipped with composite armor and an underslung magnetic vacuum harvester.",
                    "Automatically sweeps and vacuums nearby dropped items within 1.8m directly into its cargo bay.",
                    "Requires a special landing spot: place a Pickup Drone Parking Spot directly on top of a Drone Pickup station.",
                    "Docking on the station allows Electric Chutes to pneumatically offload cargo into high-capacity Storage Units."
            },
            new String[][]{
                    {"W / S", "Pitch Forward / Backward"},
                    {"A / D (FP Mode)", "Lateral Strafe Left / Right"},
                    {"Mouse", "Steer Yaw & Aim FLIR Camera"},
                    {"Space / Shift", "Ascend / Descend Altitude"},
                    {"Ctrl (Sprint)", "Engage High-Speed Propulsion Boost"},
                    {"F", "Toggle FLIR FPV Remote Camera Feed"},
                    {"RMB (In Cam)", "Toggle First-Person / Third-Person view"},
                    {"L", "Toggle High-Power Spotlight (Level 15)"},
                    {"C", "Autopilot return & land on Drone Pickup Station"},
                    {"Shift + Right-Click", "Retrieve drone back into inventory"}
            },
            new String[]{
                    "Press C while flying to automatically locate and dock onto the nearest Pickup Drone Station.",
                    "When docked on top of a Drone Pickup, items are rapidly sucked through connected Electric Chutes.",
                    "Operates seamlessly with both the handheld RC Controller and Stationary RC Controller terminal.",
                    "Underslung suction core emits genuine item pickup sound effects as it clears battlefield debris."
            }
    ),

    DRONE_PICKUP(
            "drone_pickup",
            "Drone Pickup Station",
            "📥 Automated Ground Base",
            () -> new ItemStack(EvecualMC.DRONE_PICKUP_ITEM),
            "Industrial logistics base engineered to receive docked Pickup Drones and interface with Electric Chutes for automated cargo offloading.",
            new String[]{
                    "Place on the ground as the foundation for the Pickup Drone landing dock.",
                    "Requires a Pickup Drone Parking Spot placed directly on top of it.",
                    "Detects when a Pickup Drone touches down and anchors its cargo bay for extraction.",
                    "Attach Electric Chutes to the sides to pneumatically transfer items to nearby Storage Units."
            },
            new String[][]{
                    {"Right-Click", "View Station Diagnostics & Cargo State"},
                    {"Top Placement", "Place Pickup Drone Parking Spot on top"},
                    {"Sides", "Attach Electric Chutes for item routing"}
            },
            new String[]{
                    "Electric Chutes connected to this base will automatically siphon cargo into Storage Units.",
                    "Emits electric cyan beacon particles when a Pickup Drone is securely docked.",
                    "Can be combined with Wireless RC Chargers to keep docked drones fully powered."
            }
    ),

    PICKUP_DRONE_PARKING_SPOT(
            "pickup_drone_parking_spot",
            "Pickup Drone Landing Pad",
            "🛡️ Specialized Helipad",
            () -> new ItemStack(EvecualMC.PICKUP_DRONE_PARKING_SPOT_ITEM),
            "High-precision magnetic landing plate engineered exclusively to sit on top of a Drone Pickup station.",
            new String[]{
                    "MUST be placed directly on top of a Drone Pickup block.",
                    "Provides millimeter-accurate alignment and magnetic grounding for the Pickup Drone.",
                    "Serves as the primary homing target for the drone's automated Return-to-Base (C) autopilot."
            },
            new String[][]{
                    {"Place on Drone Pickup", "Locks onto station below and activates landing beacon"},
                    {"'C' Key (Drone Flight)", "Autopilot homing and vertical touchdown"}
            },
            new String[]{
                    "If the Drone Pickup beneath is destroyed, this landing pad safely breaks and drops.",
                    "Pickup Drones prioritize this special helipad over standard civilian drone landing pads."
            }
    ),

    ELECTRIC_CHUTE(
            "electric_chute",
            "Electric Chute",
            "⚡ Pneumatic Conduit",
            () -> new ItemStack(EvecualMC.ELECTRIC_CHUTE_ITEM),
            "High-voltage pneumatic transfer conduit that extracts items from the Drone Pickup station and pipes them into Storage Units.",
            new String[]{
                    "Connect between a Drone Pickup station and a Storage Unit (or chain multiple chutes).",
                    "Requires electricity to operate (consumes 5 EU per transfer).",
                    "Automatically draws power from adjacent Wires, Batteries, Generators, or Storage Units.",
                    "Transfers items at rapid pneumatic speeds with sound and electrical particle effects."
            },
            new String[][]{
                    {"Right-Click", "Check power status, upstream source, and downstream storage"},
                    {"Placement", "Faces in the direction of placement to route item flow"}
            },
            new String[]{
                    "Can form continuous conduit pipelines up to 8 blocks long between stations and vaults.",
                    "Draws energy directly from the destination Storage Unit if it has stored charge."
            }
    ),

    STORAGE_UNIT(
            "storage_unit",
            "Storage Unit",
            "🔋 Quantum Multi-Space Vault",
            () -> new ItemStack(EvecualMC.STORAGE_UNIT_ITEM),
            "Colossal multi-block expandable electric storage vault with 108 slots per unit. Placing adjacent units merges them into a massive unified bank. Acts as a high-capacity Shulker Box when mined while charged.",
            new String[]{
                    "Provides 108 slots per single unit—more space than anything in vanilla Minecraft.",
                    "Place multiple Storage Units adjacent to each other to combine them into an expandable bank.",
                    "Use the [◀] and [▶] page buttons in the vault screen to browse through all connected compartments.",
                    "Charged Shulker: When broken while powered (≥ 200 EU), retains all contents inside its drop item.",
                    "Uncharged warning: If broken with 0 EU, the quantum field collapses and spills all items.",
                    "Requires 200 EU initialization electricity when placed down with stored items."
            },
            new String[][]{
                    {"Right-Click", "Open Quantum Multi-Space Vault UI"},
                    {"[◀] / [▶] Buttons", "Navigate pages across connected storage units"},
                    {"Shift + Click", "Quick-transfer items into vault compartments"}
            },
            new String[]{
                    "Connect Wires, Solar Panels, or Batteries to ensure the unit stays charged before mining.",
                    "When placed down with stored items, simply connect electricity to initialize and unlock.",
                    "Electric Chutes directly deposit vacuumed drone cargo into this vault automatically."
            }
    ),

    RC_ROBOT(
            "rc_robot",
            "RC Robot",
            "🤖 Heavy Autonomous Worker",
            () -> new ItemStack(EvecualMC.RC_ROBOT_ITEM),
            "Armored industrial caterpillar-tracked robot with 256-block control range and a 54-slot cargo bay.",
            new String[]{
                    "Equip any tool (axe, pickaxe, shovel, sword) by right-clicking the robot while holding it.",
                    "Hold Left Mouse Button (LMB) while piloting to swing the tool where your crosshair aims.",
                    "Mined blocks and drops are automatically vacuumed into its 54-slot Double Chest cargo bay.",
                    "Press 'Z' while controlling (or right-click in-world) to open the cargo inventory anytime."
            },
            new String[][]{
                    {"W / S", "Drive Forward / Reverse"},
                    {"A / D or Mouse", "Tank Tread Steering & Swivel"},
                    {"Mouse Y", "Tilt Sensor Dome & Aim Tool Arm"},
                    {"LMB (Hold)", "Mine blocks / Attack mobs with equipped tool"},
                    {"Z", "Open 54-Slot Double Chest Cargo"},
                    {"Space", "Suspension Step-Assist (Climbs 1-block steps)"},
                    {"F", "Enter / Exit Robot Sensor Camera"},
                    {"RMB (In Cam)", "Toggle First-Person / Third-Person view"},
                    {"Scroll Wheel", "Optical Zoom (FP) / Distance (TP)"},
                    {"L", "Toggle Powerful Work Halogen Light"},
                    {"C", "Autopilot return to RC Charger"},
                    {"Sneak + R-Click", "Retrieve robot into inventory"}
            },
            new String[]{
                    "Axes swiftly harvest whole trees, pickaxes mine ores, and shovels clear dirt/gravel.",
                    "All mined items automatically vacuum straight into the 54-slot inventory without cluttering ground.",
                    "Tank treads feature full differential steering and animated rubber cleat tracks."
            }
    ),

    CAR(
            "car",
            "Electric Passenger Car",
            "🚘 High-Performance Vehicle",
            () -> new ItemStack(EvecualMC.CAR_ITEM),
            "Full-sized luxury electric automobile featuring responsive physics, customizable paint, and turbo boost.",
            new String[]{
                    "Right-click with empty hand to enter the driver's seat.",
                    "Sneak + Right-Click to open the rear trunk (18 standard slots, upgradeable to 27).",
                    "Can be painted in all 16 Minecraft colors using standard dyes.",
                    "Recharge at any Car Charger by connecting a Charger Cable."
            },
            new String[][]{
                    {"W / S", "Accelerate Throttle / Reverse Brake"},
                    {"A / D or Mouse", "Steering Wheel Controls"},
                    {"Space", "Hydraulic Handbrake (Power Drift)"},
                    {"Ctrl", "Turbo Supercharger Boost"},
                    {"L", "Toggle Xenon Headlights & Tail Lights"},
                    {"C", "Auto-Park into designated Parking Lines"},
                    {"X", "Plug / Unplug nearby Charger Cable"},
                    {"R", "Car Stereo Radio / Music Player"},
                    {"H", "Open Vehicle Guide & Tip Menu"},
                    {"Shift", "Exit Vehicle"}
            },
            new String[]{
                    "Install Turbo Engine and Expanded Trunk upgrades in the Electronic Combiner.",
                    "Pressing X while seated instantly connects to nearby Charger Extension posts within 16 blocks.",
                    "Auto-park (C) smoothly steers the vehicle directly into any nearby painted parking stall."
            }
    ),

    EV_HELI(
            "ev_heli",
            "EV Helicopter",
            "🚁 High-Speed VTOL Aircraft",
            () -> new ItemStack(EvecualMC.HELI_ITEM),
            "High-speed passenger VTOL electric helicopter featuring 12 m/s cruise speed, 20 m/s boost, dual rotors, and rapid helipad charging.",
            new String[]{
                    "Right-click with empty hand to board the cockpit as pilot.",
                    "Cruises at 12 blocks/second (0.60 bps) and reaches 20 blocks/second (1.00 bps) on Sprint/Boost.",
                    "Holds a generous 27-slot internal cargo bay accessible by pressing 'Z'.",
                    "Customizable with Dyes for body livery and Stained Glass for cockpit bubble tint.",
                    "Charges rapidly by landing on a Heli Charger placed on a Vehicle Charger Base."
            },
            new String[][]{
                    {"W / S", "Forward Cruise / Pitch Back Brake"},
                    {"A / D", "Yaw Heading & Banking Turn"},
                    {"Space", "Ascend (Altitude Up)"},
                    {"Shift", "Descend (Altitude Down / Land)"},
                    {"Ctrl / Sprint", "Engage 20 m/s Turbine Boost"},
                    {"C", "Helipad Autopilot return & vertical landing"},
                    {"F", "Exit / Dismount Helicopter"},
                    {"Z", "Open 27-Slot Internal Cargo Bay"}
            },
            new String[]{
                    "Place a Heli Charger directly on top of a Vehicle Charger Base for automatic high-voltage docking.",
                    "When hovering without vertical input, the onboard flight computer actively locks your altitude.",
                    "Turbine boost consumes power faster but allows rapid traversal across hundreds of blocks in seconds."
            }
    ),

    HELI_CHARGER(
            "heli_charger",
            "Heli Charger",
            "⚡ High-Voltage Helipad",
            () -> new ItemStack(EvecualMC.HELI_CHARGER_ITEM),
            "Rapid inductive charging helipad designed to be placed directly on top of or adjacent to a Vehicle Charger Base.",
            new String[]{
                    "Place directly on top of a Vehicle Charger Base to supply high-voltage charging power.",
                    "Automatically detects landed EV Helis and rapid-charges their battery cells at 300 EU/s.",
                    "Emits electric charging sparks and sound effects during active power transfer."
            },
            new String[][]{
                    {"Right-Click", "Open Helipad Diagnostics & Guide"},
                    {"Landing", "Touch down with EV Heli to initiate rapid charging"}
            },
            new String[]{
                    "Place on rooftops or helipad towers connected down to battery banks and solar arrays.",
                    "Works seamlessly with multiple Vehicle Charger Bases for massive high-speed aircraft charging."
            }
    ),

    ELECTRONIC_COMBINER(
            "electronic_combiner",
            "Electronic Combiner",
            "⚙️ Advanced Fabricator",
            () -> new ItemStack(EvecualMC.ELECTRONIC_COMBINER_ITEM),
            "High-precision computerized assembly workstation featuring a wide CAD blueprint display with machine side-views.",
            new String[]{
                    "Fabricates complete vehicles, RC units, and high-tech components from raw materials.",
                    "Wide CAD blueprint renders technical side-profile silhouettes of each craftable machine.",
                    "Crafting slots are positioned directly onto their corresponding physical mounting points on the machine.",
                    "Requires continuous power supply via adjacent Batteries, Wires, or Solar Panels."
            },
            new String[][]{
                    {"Right-Click", "Open Wide CAD Blueprint Interface"},
                    {"Shift + Click", "Quick-transfer items into corresponding machine sockets"}
            },
            new String[]{
                    "Hover over any machine socket to view the component name, accepted item, and required count.",
                    "Add any dye into the color socket to customize the factory coat paint of the finished vehicle.",
                    "Can fabricate Electric Cars, RC Cars, RC Drones, RC Robots, Controllers, Senders, and Receivers."
            }
    ),

    RC_CONTROLLER(
            "rc_controller",
            "RC Controller",
            "📡 Remote Transmitter",
            () -> new ItemStack(EvecualMC.RC_CONTROLLER_ITEM),
            "Handheld digital transceiver providing precision telemetry and remote command for all RC vehicles.",
            new String[]{
                    "Aim crosshair at an RC Car, Drone, or Robot and right-click to pair.",
                    "Press 'F' to project your vision directly into the paired vehicle's onboard camera.",
                    "Holds long-range radio link up to 256 blocks (or 512 blocks for RC Drones).",
                    "Press 'C' to transmit an autonomous return-to-base docking command."
            },
            new String[][]{
                    {"Right-Click", "Pair / Disconnect with aimed RC vehicle"},
                    {"F", "Engage / Disengage remote camera feed"},
                    {"C", "Send automatic return-to-base homing signal"}
            },
            new String[]{
                    "Hovering over the controller shows the paired vehicle's ID, type, and live telemetry.",
                    "Unpair instantly by parking the vehicle in an RC or Drone Parking Spot.",
                    "One controller can be easily re-paired to switch between car, drone, and excavator robot."
            }
    ),

    STEEL_INGOT(
            "steel_ingot",
            "Steel Ingot",
            "🔨 Industrial Alloy",
            () -> new ItemStack(EvecualMC.STEEL_INGOT),
            "Heavy reinforced metallurgical alloy engineered for vehicle chassis and high-stress mechanical frameworks.",
            new String[]{
                    "Smelt Iron Ingots in a Blast Furnace or forge with coal to produce high-strength Steel.",
                    "Serves as the core structural building block for vehicle chassis, frames, and motors.",
                    "Essential material for building the Electronic Combiner and Car Chargers."
            },
            new String[][]{
                    {"Blast Furnace", "Smelt Iron Ingot with Coal / Carbon to forge Steel"},
                    {"Crafting Table", "Craft into engines, frames, chargers, and combiner workstations"}
            },
            new String[]{
                    "Stockpile plenty of Steel Ingots before constructing your first fleet of electric vehicles.",
                    "Significantly more blast and impact resistant than ordinary iron.",
                    "Used across both ground vehicle chassis and drone airframe fabrication."
            }
    ),

    ENGINE(
            "electric_engine",
            "Electric Engine",
            "⚡ Powertrain Core",
            () -> new ItemStack(EvecualMC.ENGINE),
            "High-torque brushless AC induction motor designed for silent, instantaneous acceleration.",
            new String[]{
                    "Core propulsion component required to craft Electric Cars, RC Cars, and Robots.",
                    "Delivers instant torque with 100% efficiency and zero emissions.",
                    "Can be upgraded into an Advanced Turbo Engine in the Electronic Combiner."
            },
            new String[][]{
                    {"Electronic Combiner", "Insert into the Engine blueprint slot to fabricate vehicles"},
                    {"Crafting Table", "Assembled from Copper Wire, Steel Ingots, and Redstone"}
            },
            new String[]{
                    "Provides excellent low-end torque capable of climbing steep 45-degree hills.",
                    "Pair with upgraded battery cells to maximize continuous operating range.",
                    "Zero maintenance required—no oil, spark plugs, or fuel canisters."
            }
    ),

    UPGRADED_ENGINE(
            "upgraded_electric_engine",
            "Turbo Engine",
            "🚀 High-Output Motor",
            () -> new ItemStack(EvecualMC.UPGRADED_ENGINE),
            "Hyper-tuned electric motor featuring dual stator coils delivering +40% top speed and supercharged boost.",
            new String[]{
                    "Fabricate in the Electronic Combiner or assemble with gold and advanced coils.",
                    "Right-click an existing Electric Passenger Car while holding this item to install.",
                    "Drastically boosts acceleration and top cruising speed."
            },
            new String[][]{
                    {"Right-Click on Car", "Install Turbo Engine directly onto placed vehicle"},
                    {"Combiner Slot", "Insert into Combiner for factory-turbo vehicle builds"}
            },
            new String[]{
                    "Installed Turbo status is preserved permanently even when the car is picked up as an item.",
                    "Enables high-speed highway cruising and aggressive power drifting on curves.",
                    "Compatible with standard Vehicle Chargers."
            }
    ),

    TRUNK_UPGRADE(
            "trunk_upgrade",
            "Trunk Expansion",
            "📦 Cargo Capacity Mod",
            () -> new ItemStack(EvecualMC.TRUNK_UPGRADE),
            "Modular luggage rack and reinforced cargo partition that expands vehicle trunk storage to 27 full slots.",
            new String[]{
                    "Right-click on any standard Electric Passenger Car to instantly expand trunk space.",
                    "Upgrades trunk capacity from 18 slots to a full 27-slot chest size.",
                    "Can also be applied at manufacturing time in the Electronic Combiner."
            },
            new String[][]{
                    {"Right-Click on Car", "Install Trunk Upgrade on vehicle"},
                    {"Sneak + Right-Click Car", "Access expanded 27-slot storage compartment"}
            },
            new String[]{
                    "Stored items are safely preserved inside the car even when picked up into inventory.",
                    "Ideal for long-distance mining expeditions, supply runs, and nomadic journeys.",
                    "Visual indicator in the driver HUD confirms expanded trunk tier."
            }
    ),

    CHARGER_CABLE(
            "charger_cable",
            "Charger Cable",
            "⚡ Heavy-Duty Tether",
            () -> new ItemStack(EvecualMC.CHARGER_CABLE),
            "Ultra-flexible high-amperage charging tether connecting Charger Extensions directly to vehicles.",
            new String[]{
                    "Right-click an empty Charger Extension post to equip and install the cable.",
                    "When equipped, press 'X' while in a parked car to automatically connect and charge.",
                    "Can also be right-clicked directly onto a nearby car to begin rapid charging."
            },
            new String[][]{
                    {"Right-Click Extension", "Equip cable onto Charger Extension post"},
                    {"'X' Key (In Car)", "Remotely plug in or disconnect cable from driver seat"},
                    {"Empty Hand on Cable", "Manually disconnect tether"}
            },
            new String[]{
                    "Breaking a Charger Extension automatically drops the installed cable safely.",
                    "Has an extended 16-block reach, allowing one post to charge multiple parking bays.",
                    "Visual high-voltage cable renders seamlessly between the post and vehicle port."
            }
    ),

    RC_SENDER(
            "rc_sender",
            "RC Sender",
            "📡 Telecommand Module",
            () -> new ItemStack(EvecualMC.RC_SENDER_ITEM),
            "Micro-scale RF broadcast transceiver module required to remotely control drones, rovers, and worker robots.",
            new String[]{
                    "Essential electronic component for crafting the RC Car, RC Drone, and RC Robot in the Electronic Combiner.",
                    "Broadcasts telecommand signals and high-bandwidth sensor feeds back to paired handheld controllers.",
                    "Can be fabricated in the Electronic Combiner under the RC category."
            },
            new String[][]{
                    {"Electronic Combiner", "Craft using Lightning Rod, Redstone, Steel Ingot, and Wire"},
                    {"Vehicle Blueprint", "Placed into Slot 0 when fabricating RC vehicles"}
            },
            new String[]{
                    "Every remote vehicle (Car, Drone, Robot) requires one RC Sender in its construction.",
                    "Transmits long-range telecommand packets seamlessly through walls and solid obstacles."
            }
    ),

    RC_RECEIVER(
            "rc_receiver",
            "RC Receiver",
            "📟 Crystal Decoder Module",
            () -> new ItemStack(EvecualMC.RC_RECEIVER_ITEM),
            "High-precision multi-channel telemetry crystal receiver tuned for remote command transmitters.",
            new String[]{
                    "Core telemetry processing unit required to assemble the RC Controller in the Electronic Combiner.",
                    "Decodes multi-frequency radio signals into directional steering and actuator inputs.",
                    "Fabricated in the Electronic Combiner under the RC category."
            },
            new String[][]{
                    {"Electronic Combiner", "Craft using Redstone Comparator, Quartz, Steel Ingot, and Wire"},
                    {"Controller Blueprint", "Placed into Slot 0 when fabricating the RC Controller"}
            },
            new String[]{
                    "Without an RC Receiver, handheld remote controllers cannot decode vehicle telemetry.",
                    "Features crystal oscillators for high-reliability low-latency radio communication."
            }
    ),

    STATIONARY_RC_CONTROLLER(
            "stationary_rc_controller",
            "Stationary RC Controller",
            "📡 Ground Station Terminal",
            () -> new ItemStack(EvecualMC.STATIONARY_RC_CONTROLLER_ITEM),
            "Heavy-duty ground command terminal that pairs with RC vehicles before placement to provide locked-in remote operations.",
            new String[]{
                    "Before placing, right-click any RC Car, RC Drone, or RC Robot while holding the terminal item to link telemetry.",
                    "Place the terminal onto solid ground and right-click to enter fixed remote control mode.",
                    "While operating the terminal, the player remains stationary and locked in the vehicle's perspective.",
                    "Press Sneak (Shift) or F to disconnect and exit terminal mode."
            },
            new String[][]{
                    {"Item Right-Click on RC", "Pairs terminal with targeted vehicle"},
                    {"Right-Click Placed Terminal", "Operate vehicle from stationary console"},
                    {"WASD / Movement Keys", "Steer and drive paired vehicle remotely"},
                    {"Sneak (Shift) / F", "Exit terminal view and return to body"}
            },
            new String[]{
                    "Must be linked to a vehicle while in item form before placing it down.",
                    "Player camera is locked into vehicle perspective while operating the terminal.",
                    "Break the terminal to retrieve the item; pairing data is retained when picked up."
            }
    ),

    ICE_CREAM(
            "ice_cream",
            "Artisan Ice Cream",
            "🍦 Chilled Refreshment",
            () -> new ItemStack(EvecualMC.VANILLA_ICE_CREAM),
            "Delicious handcrafted frozen dairy treats available in Vanilla, Chocolate, Sweet Berry, and Electric flavor.",
            new String[]{
                    "Crafted using Milk, Sugar, Ice, and flavor ingredients (Cocoa, Sweet Berries, or Redstone).",
                    "Provides substantial nutrition and soothing saturation for weary drivers.",
                    "Electric Ice Cream infuses the player with an energizing Speed and Haste boost."
            },
            new String[][]{
                    {"Right-Click (Eat)", "Consume delicious ice cream for hunger and buffs"},
                    {"Crafting Table", "Combine Milk, Sugar, and Snow/Ice with flavorings"}
            },
            new String[]{
                    "Keep a stack in your vehicle's glovebox or trunk for road trip snacks.",
                    "Electric flavor grants a burst of energetic speed perfect for pit stops.",
                    "Enjoyable treat during long journeys across hot desert biomes."
            }
    ),

    HELI_CONTROLLER(
            "heli_controller",
            "Heli Controller",
            "📡 Long-Range Telecommand",
            () -> new ItemStack(EvecualMC.HELI_CONTROLLER_ITEM),
            "Aerospace-grade flight controller capable of remote piloting the EV Heli from up to 1024 blocks away.",
            new String[]{
                    "Aim at an EV Heli within 12 blocks and right-click with the controller to pair.",
                    "Right-click anytime to toggle the active telecommand link online/standby.",
                    "Press F to switch camera view between player and the remote helicopter.",
                    "Operates seamlessly at extreme ranges up to 1024 blocks."
            },
            new String[][]{
                    {"Right-Click Heli", "Pair controller with EV Heli"},
                    {"Right-Click", "Toggle Active Remote Link ON/OFF"},
                    {"Shift + Right-Click", "Unpair / Re-pair"},
                    {"WASD + Space/Shift", "Fly and maneuver helicopter remotely"},
                    {"LMB (Attack)", "Trigger hardpoint arms remotely"},
                    {"C", "Trigger 3x3 Helipad Autopilot remotely"},
                    {"F", "Toggle 3rd Person / FPV remote camera view"}
            },
            new String[]{
                    "With 1024m range, you can scout entire maps and mine distant terrain safely.",
                    "Combine with Mining Arms to create an autonomous aerial mining drone.",
                    "Helicopter chunk remains loaded during active flight operations."
            }
    ),

    STATIONARY_TURRET(
            "stationary_turret",
            "Stationary Turret",
            "🛡️ Automated Defense Sentry",
            () -> new ItemStack(EvecualMC.STATIONARY_TURRET_ITEM),
            "Heavy automated defense sentry that tracks and engages hostile mobs, intruders, and aerial threats with high-velocity kinetic projectiles.",
            new String[]{
                    "Place down in an open perimeter or defense vantage point.",
                    "Must be linked to a Turret Ammo Container using the Turret Linker tool.",
                    "Right-click the turret to open the Target Filter GUI (adjust detection radius from 8m up to 256m).",
                    "Configurable to target Hostile Monsters, Players, Animals, and Bosses.",
                    "Smoothly rotates yaw and pitch to lead moving targets and fires automatically."
            },
            new String[][]{
                    {"Right-Click", "Open Target Filter & Radius GUI"},
                    {"Turret Linker", "Link to Ammo Container (Right-click container, then turret)"},
                    {"Pickaxe / Hand", "Mineable to retrieve"}
            },
            new String[]{
                    "Link multiple turrets to a single Turret Ammo Container for synchronized perimeter defense.",
                    "Supports Copper, Iron, and Diamond ammunition types for scaled projectile damage.",
                    "Has an effective operational range of up to 256 blocks (512x512 area coverage)!"
            }
    ),

    TURRET_AMMO_CONTAINER(
            "turret_ammo_container",
            "Turret Ammo Container",
            "📦 Heavy Ammunition Magazine",
            () -> new ItemStack(EvecualMC.TURRET_AMMO_CONTAINER_ITEM),
            "Armored 27-slot ammunition supply depot that feeds linked Stationary Turrets wirelessly with ammunition.",
            new String[]{
                    "Place near your base or defense installations.",
                    "Load with Copper Ammo, Iron Ammo, or Diamond Ammo (up to 27 full stacks).",
                    "Right-click with the Turret Linker tool to select this container as the ammo source.",
                    "Feeds ammo directly to any number of linked Stationary Turrets across the perimeter."
            },
            new String[][]{
                    {"Right-Click", "Open 27-Slot Ammo Storage Matrix"},
                    {"Turret Linker", "Right-click to select as source for turrets"},
                    {"Hopper / Pipe", "Automated ammo replenishment from bottom/sides"}
            },
            new String[]{
                    "Automatically supplies whichever ammo type is loaded, prioritizing higher tier ammo.",
                    "Breaking the container safely preserves items or spills them if broken in survival.",
                    "One ammo container can supply an entire network of defensive turrets."
            }
    ),

    TURRET_LINKER(
            "turret_linker",
            "Turret Linker",
            "🔧 Defense Configuration Tool",
            () -> new ItemStack(EvecualMC.TURRET_LINKER),
            "Tactical RF calibration tool used to bind Stationary Turrets to Turret Ammo Containers.",
            new String[]{
                    "Step 1: Right-click on a Turret Ammo Container to store its frequency.",
                    "Step 2: Right-click on any Stationary Turret to establish the telemetry link.",
                    "The turret will now draw ammunition wirelessly from the linked container."
            },
            new String[][]{
                    {"Right-Click Ammo Container", "Select and store ammo depot position"},
                    {"Right-Click Turret", "Bind turret to stored ammo container"},
                    {"Shift + Right-Click Air", "Clear saved container position"}
            },
            new String[]{
                    "Link status is confirmed with chime sounds and action bar confirmations.",
                    "A single container can be linked to indefinitely many turrets."
            }
    );

    public final String id;
    public final String title;
    public final String category;
    public final Supplier<ItemStack> iconSupplier;
    public final String overview;
    public final String[] setupSteps;
    public final String[][] controls;
    public final String[] proTips;

    TipTopic(String id, String title, String category, Supplier<ItemStack> iconSupplier,
             String overview, String[] setupSteps, String[][] controls, String[] proTips) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.iconSupplier = iconSupplier;
        this.overview = overview;
        this.setupSteps = setupSteps;
        this.controls = controls;
        this.proTips = proTips;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public ItemStack getIcon() {
        return iconSupplier.get();
    }

    public String getOverview() {
        return overview;
    }

    public String[] getSetupSteps() {
        return setupSteps;
    }

    public String[][] getControls() {
        return controls;
    }

    public String[] getProTips() {
        return proTips;
    }

    public static TipTopic fromId(String id) {
        if (id == null) return SOLAR_PANEL;
        for (TipTopic topic : values()) {
            if (topic.id.equalsIgnoreCase(id)) {
                return topic;
            }
        }
        return SOLAR_PANEL;
    }

    public static TipTopic fromBlock(net.minecraft.block.Block block) {
        if (block == null) return null;
        if (block == EvecualMC.SOLAR_PANEL_BLOCK) return SOLAR_PANEL;
        if (block == EvecualMC.WIND_TURBINE_BLOCK) return WIND_TURBINE;
        if (block == EvecualMC.BATTERY_BLOCK) return BATTERY;
        if (block == EvecualMC.WIRE_BLOCK) return WIRE;
        if (block == EvecualMC.CHARGER_BLOCK) return CHARGER;
        if (block == EvecualMC.CHARGER_EXTENSION_BLOCK) return CHARGER_EXTENSION;
        if (block == EvecualMC.PARKING_LINES_BLOCK) return PARKING_LINES;
        if (block == EvecualMC.RC_CHARGER_BLOCK) return RC_CHARGER;
        if (block == EvecualMC.RC_PARKING_SPOT_BLOCK) return RC_PARKING_SPOT;
        if (block == EvecualMC.DRONE_PARKING_SPOT_BLOCK) return DRONE_PARKING_SPOT;
        if (block == EvecualMC.ROBOT_PARKING_SPOT_BLOCK) return ROBOT_PARKING_SPOT;
        if (block == EvecualMC.ELECTRONIC_COMBINER_BLOCK) return ELECTRONIC_COMBINER;
        if (block == EvecualMC.ELECTRONIC_DUPER_BLOCK) return ELECTRONIC_DUPER;
        if (block == EvecualMC.STATIONARY_RC_CONTROLLER_BLOCK) return STATIONARY_RC_CONTROLLER;
        if (block == EvecualMC.HELI_CHARGER_BLOCK) return HELI_CHARGER;
        if (block == EvecualMC.DRONE_PICKUP_BLOCK) return DRONE_PICKUP;
        if (block == EvecualMC.PICKUP_DRONE_PARKING_SPOT_BLOCK) return PICKUP_DRONE_PARKING_SPOT;
        if (block == EvecualMC.ELECTRIC_CHUTE_BLOCK) return ELECTRIC_CHUTE;
        if (block == EvecualMC.STORAGE_UNIT_BLOCK) return STORAGE_UNIT;
        if (block == EvecualMC.STATIONARY_TURRET_BLOCK) return STATIONARY_TURRET;
        if (block == EvecualMC.TURRET_AMMO_CONTAINER_BLOCK) return TURRET_AMMO_CONTAINER;
        return null;
    }

    public static TipTopic fromItem(net.minecraft.item.Item item) {
        if (item == null) return null;
        if (item == EvecualMC.CAR_ITEM) return CAR;
        if (item == EvecualMC.HELI_ITEM) return EV_HELI;
        if (item == EvecualMC.HELI_CONTROLLER_ITEM) return HELI_CONTROLLER;
        if (item == EvecualMC.HELI_MINING_ARM || item == EvecualMC.HELI_WEAPON_ARM) return EV_HELI;
        if (item == EvecualMC.RC_CAR_ITEM) return RC_CAR;
        if (item == EvecualMC.PICKUP_DRONE_ITEM) return PICKUP_DRONE;
        if (item == EvecualMC.RC_DRONE_ITEM) return RC_DRONE;
        if (item == EvecualMC.DRONE_PICKUP_ITEM) return DRONE_PICKUP;
        if (item == EvecualMC.PICKUP_DRONE_PARKING_SPOT_ITEM) return PICKUP_DRONE_PARKING_SPOT;
        if (item == EvecualMC.ELECTRIC_CHUTE_ITEM) return ELECTRIC_CHUTE;
        if (item == EvecualMC.STORAGE_UNIT_ITEM) return STORAGE_UNIT;
        if (item == EvecualMC.STATIONARY_TURRET_ITEM) return STATIONARY_TURRET;
        if (item == EvecualMC.TURRET_AMMO_CONTAINER_ITEM) return TURRET_AMMO_CONTAINER;
        if (item == EvecualMC.TURRET_LINKER) return TURRET_LINKER;
        if (item == EvecualMC.RC_ROBOT_ITEM) return RC_ROBOT;
        if (item == EvecualMC.RC_CONTROLLER_ITEM) return RC_CONTROLLER;
        if (item == EvecualMC.STATIONARY_RC_CONTROLLER_ITEM) return STATIONARY_RC_CONTROLLER;
        if (item == EvecualMC.RC_SENDER_ITEM) return RC_SENDER;
        if (item == EvecualMC.RC_RECEIVER_ITEM) return RC_RECEIVER;
        if (item == EvecualMC.SOLAR_PANEL_ITEM) return SOLAR_PANEL;
        if (item == EvecualMC.WIND_TURBINE_ITEM) return WIND_TURBINE;
        if (item == EvecualMC.BATTERY_ITEM) return BATTERY;
        if (item == EvecualMC.WIRE_ITEM) return WIRE;
        if (item == EvecualMC.CHARGER_ITEM) return CHARGER;
        if (item == EvecualMC.CHARGER_EXTENSION_ITEM) return CHARGER_EXTENSION;
        if (item == EvecualMC.CHARGER_CABLE) return CHARGER_CABLE;
        if (item == EvecualMC.PARKING_LINES_ITEM) return PARKING_LINES;
        if (item == EvecualMC.RC_CHARGER_ITEM) return RC_CHARGER;
        if (item == EvecualMC.HELI_CHARGER_ITEM) return HELI_CHARGER;
        if (item == EvecualMC.RC_PARKING_SPOT_ITEM) return RC_PARKING_SPOT;
        if (item == EvecualMC.DRONE_PARKING_SPOT_ITEM) return DRONE_PARKING_SPOT;
        if (item == EvecualMC.ROBOT_PARKING_SPOT_ITEM) return ROBOT_PARKING_SPOT;
        if (item == EvecualMC.ELECTRONIC_COMBINER_ITEM) return ELECTRONIC_COMBINER;
        if (item == EvecualMC.ELECTRONIC_DUPER_ITEM) return ELECTRONIC_DUPER;
        if (item == EvecualMC.STEEL_INGOT) return STEEL_INGOT;
        if (item == EvecualMC.ENGINE) return ENGINE;
        if (item == EvecualMC.UPGRADED_ENGINE) return UPGRADED_ENGINE;
        if (item == EvecualMC.TRUNK_UPGRADE) return TRUNK_UPGRADE;
        if (item == EvecualMC.VANILLA_ICE_CREAM || item == EvecualMC.CHOCOLATE_ICE_CREAM
                || item == EvecualMC.SWEET_BERRY_ICE_CREAM || item == EvecualMC.ELECTRIC_ICE_CREAM) {
            return ICE_CREAM;
        }
        return null;
    }
}
