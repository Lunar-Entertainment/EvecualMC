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
                    "No power loss over distance—build your power plant anywhere and cable it to your garage.",
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
            "RC Charger Pad",
            "⚡ Wireless Inductive Pad",
            () -> new ItemStack(EvecualMC.RC_CHARGER_ITEM),
            "High-frequency wireless inductive charging pad storing up to 5,000 EU.",
            new String[]{
                    "Rapidly recharges RC Cars, RC Drones, and RC Robots positioned squarely on the pad.",
                    "Wirelessly broadcasts charging energy to all RC and Drone Parking Spots within a 16-block radius.",
                    "Functions as an autonomous homing beacon for remote vehicles across a 50-block range."
            },
            new String[][]{
                    {"Right-Click", "Open Live Power Diagnostics & Guide"},
                    {"'C' Key (RC Link)", "Command RC vehicle to auto-dock on pad"}
            },
            new String[]{
                    "A single RC Charger can wirelessly power an entire fleet parked across multiple parking spots.",
                    "Vehicles automatically return to this pad if battery reaches 5% while within 50 blocks.",
                    "Supplied via standard Wires, Solar Panels, or Battery storage networks."
            }
    ),

    RC_PARKING_SPOT(
            "rc_parking_spot",
            "RC Parking Spot",
            "🅿️ Ground Vehicle Dock",
            () -> new ItemStack(EvecualMC.RC_PARKING_SPOT_ITEM),
            "Precision ground docking bay for RC Cars and RC Robots.",
            new String[]{
                    "When an RC Car or Robot enters this spot, it powers down motors and unpairs to save power.",
                    "Wirelessly receives recharge energy whenever an RC Charger is within a 16-block radius.",
                    "Provides a designated home base where vehicles stay safe from hostile mobs."
            },
            new String[][]{
                    {"Right-Click", "Open Ground Docking Guide"},
                    {"'C' Key (RC Link)", "Autopilot car/robot directly into this spot"}
            },
            new String[]{
                    "Keeps parked vehicles charged at 100% so they are always ready for deployment.",
                    "To deploy a parked vehicle, simply aim the RC Controller and right-click to re-pair.",
                    "Place multiple spots in a row to create an organized staging depot for your mining robot fleet."
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
                    "Wirelessly draws energy from any active RC Charger located within a 16-block radius.",
                    "Serves as a designated high-altitude or courtyard landing zone for aerial missions."
            },
            new String[][]{
                    {"Right-Click", "Open Aerial Helipad Guide"},
                    {"'C' Key (RC Link)", "Command drone to autopilot and touch down"}
            },
            new String[]{
                    "During auto-dock, drones ascend to cruising altitude, fly straight over, and land vertically.",
                    "Build landing pads on castle towers, fortress walls, or rooftop gardens for instant deployment.",
                    "Charges drones quietly with zero emissions or cable clutter."
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
                    "Autopilot (C) ascends to safe cruising height, flies across mountains, and lands automatically.",
                    "High-powered spotlight cuts through deep fog, night skies, and underwater depths."
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

    ELECTRONIC_COMBINER(
            "electronic_combiner",
            "Electronic Combiner",
            "⚙️ Advanced Fabricator",
            () -> new ItemStack(EvecualMC.ELECTRONIC_COMBINER_ITEM),
            "High-precision computerized assembly workstation powered by electric energy.",
            new String[]{
                    "Fabricates complete vehicles, RC units, and high-tech components from raw ingredients.",
                    "Requires continuous power supply via adjacent Batteries, Wires, or Solar Panels.",
                    "Features intuitive slot blueprint indicators for Engine, Hull, Glass, Leather, and Dye."
            },
            new String[][]{
                    {"Right-Click", "Open Combiner Crafting Interface"},
                    {"Shift + Click", "Quick-transfer crafting ingredients"}
            },
            new String[]{
                    "Keep supplied with at least 500 EU in its internal buffer before starting an assembly cycle.",
                    "Add any dye into the color slot to customize the factory coat paint of the finished vehicle.",
                    "Can fabricate Electric Cars, RC Cars, RC Drones, RC Robots, and Turbo Engine upgrades."
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
        if (block == EvecualMC.BATTERY_BLOCK) return BATTERY;
        if (block == EvecualMC.WIRE_BLOCK) return WIRE;
        if (block == EvecualMC.CHARGER_BLOCK) return CHARGER;
        if (block == EvecualMC.CHARGER_EXTENSION_BLOCK) return CHARGER_EXTENSION;
        if (block == EvecualMC.PARKING_LINES_BLOCK) return PARKING_LINES;
        if (block == EvecualMC.RC_CHARGER_BLOCK) return RC_CHARGER;
        if (block == EvecualMC.RC_PARKING_SPOT_BLOCK) return RC_PARKING_SPOT;
        if (block == EvecualMC.DRONE_PARKING_SPOT_BLOCK) return DRONE_PARKING_SPOT;
        if (block == EvecualMC.ELECTRONIC_COMBINER_BLOCK) return ELECTRONIC_COMBINER;
        return null;
    }

    public static TipTopic fromItem(net.minecraft.item.Item item) {
        if (item == null) return null;
        if (item == EvecualMC.CAR_ITEM) return CAR;
        if (item == EvecualMC.RC_CAR_ITEM) return RC_CAR;
        if (item == EvecualMC.RC_DRONE_ITEM) return RC_DRONE;
        if (item == EvecualMC.RC_ROBOT_ITEM) return RC_ROBOT;
        if (item == EvecualMC.RC_CONTROLLER_ITEM) return RC_CAR;
        if (item == EvecualMC.SOLAR_PANEL_ITEM) return SOLAR_PANEL;
        if (item == EvecualMC.BATTERY_ITEM) return BATTERY;
        if (item == EvecualMC.WIRE_ITEM) return WIRE;
        if (item == EvecualMC.CHARGER_ITEM) return CHARGER;
        if (item == EvecualMC.CHARGER_EXTENSION_ITEM) return CHARGER_EXTENSION;
        if (item == EvecualMC.CHARGER_CABLE) return CHARGER_EXTENSION;
        if (item == EvecualMC.PARKING_LINES_ITEM) return PARKING_LINES;
        if (item == EvecualMC.RC_CHARGER_ITEM) return RC_CHARGER;
        if (item == EvecualMC.RC_PARKING_SPOT_ITEM) return RC_PARKING_SPOT;
        if (item == EvecualMC.DRONE_PARKING_SPOT_ITEM) return DRONE_PARKING_SPOT;
        if (item == EvecualMC.ELECTRONIC_COMBINER_ITEM) return ELECTRONIC_COMBINER;
        return null;
    }
}
