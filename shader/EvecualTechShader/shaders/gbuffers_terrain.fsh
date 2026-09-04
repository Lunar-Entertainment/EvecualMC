#version 120

#define CONNECTED_BLOCKS
#define CONNECTED_GLASS
#define EMISSIVE_ORES
#define ORE_GLOW_INTENSITY 1.00

uniform sampler2D texture;
uniform sampler2D lightmap;

varying vec4 color;
varying vec2 texcoord;
varying vec2 lmcoord;
varying vec3 normal;
varying float blockId;
varying vec2 faceUV;

/* DRAWBUFFERS:01 */

void main() {
    vec4 albedo = texture2D(texture, texcoord) * color;
    if (albedo.a < 0.05) discard;

    vec4 light = texture2D(lightmap, lmcoord);

    // Directional shading for crisp clean depth
    vec3 lightDir = normalize(vec3(0.35, 0.85, 0.40));
    float NdotL = clamp(dot(normal, lightDir), 0.0, 1.0);
    float diffuse = mix(0.78, 1.05, NdotL);

    vec3 shaded = albedo.rgb * light.rgb * diffuse;

    float borderDist = max(abs(faceUV.x), abs(faceUV.y));
    bool isOuterBorder = borderDist > 0.85;

    #ifdef CONNECTED_BLOCKS
    // --- 1. Glass & Panes (Block ID 10001) ---
    if (abs(blockId - 10001.0) < 0.5) {
        #ifdef CONNECTED_GLASS
        if (!isOuterBorder) {
            // Clean seamless crystal-clear interior (clears out ugly vanilla streaks)
            albedo.a = clamp(albedo.a * 0.40, 0.04, 0.30);
            shaded = mix(shaded, vec3(0.92, 0.96, 1.0) * light.rgb, 0.35);
        } else {
            // Sleek outer edge frame
            shaded = mix(shaded, vec3(1.0) * light.rgb, 0.25);
            albedo.a = max(albedo.a, 0.65);
        }
        #endif
    }
    // --- 2. Bookshelves (Block ID 10002) ---
    else if (abs(blockId - 10002.0) < 0.5) {
        // Connected horizontal bookshelf: Vertical inner seams soften to connect shelves
        if (abs(faceUV.x) > 0.85 && abs(faceUV.y) < 0.85) {
            shaded = mix(shaded, shaded * 1.06, 0.4);
        }
    }
    // --- 3. Mineral / Ore Blocks (Block ID 10003: Iron, Gold, Diamond, Emerald, etc.) ---
    else if (abs(blockId - 10003.0) < 0.5) {
        if (!isOuterBorder) {
            // Polished connected interior metal/gem luster
            shaded *= 1.05;
        } else {
            // Sleek perimeter bevel highlight
            shaded = mix(shaded, shaded * 1.15, 0.35);
        }
    }
    #endif

    // --- Emissive Glowing Highlights ---
    float emissiveFactor = 0.0;

    #ifdef EMISSIVE_ORES
    // --- 4. Connected Emissive Ore Veins (Block ID 10004) ---
    if (abs(blockId - 10004.0) < 0.5) {
        // Diamond: Cyan / light blue gems
        bool isDiamond = (albedo.b > 0.58 && albedo.g > 0.48 && albedo.r < 0.42);
        // Emerald: Vivid green gems
        bool isEmerald = (albedo.g > 0.48 && albedo.r < 0.38 && albedo.b < 0.38);
        // Redstone: Glowing red crystals
        bool isRedstone = (albedo.r > 0.48 && albedo.g < 0.25 && albedo.b < 0.25);
        // Gold: Warm golden nuggets
        bool isGold = (albedo.r > 0.62 && albedo.g > 0.48 && albedo.b < 0.32);
        // Copper: Electric orange copper veins
        bool isCopper = (albedo.r > 0.58 && albedo.g > 0.32 && albedo.b < 0.30);
        // Lapis: Deep cobalt lapis lazuli
        bool isLapis = (albedo.b > 0.48 && albedo.r < 0.32 && albedo.g < 0.38);
        // Quartz: Radiant white crystal
        bool isQuartz = (albedo.r > 0.80 && albedo.g > 0.78 && albedo.b > 0.75);
        // Coal: Subtle warm ember
        bool isCoal = (albedo.r < 0.22 && albedo.g < 0.22 && albedo.b < 0.22 && length(albedo.rgb) > 0.06);

        if (isDiamond) {
            emissiveFactor = 1.30 * ORE_GLOW_INTENSITY;
        } else if (isEmerald) {
            emissiveFactor = 1.25 * ORE_GLOW_INTENSITY;
        } else if (isRedstone) {
            emissiveFactor = 1.40 * ORE_GLOW_INTENSITY;
        } else if (isGold) {
            emissiveFactor = 1.15 * ORE_GLOW_INTENSITY;
        } else if (isCopper) {
            emissiveFactor = 1.15 * ORE_GLOW_INTENSITY;
        } else if (isLapis) {
            emissiveFactor = 1.20 * ORE_GLOW_INTENSITY;
        } else if (isQuartz) {
            emissiveFactor = 1.10 * ORE_GLOW_INTENSITY;
        } else if (isCoal) {
            emissiveFactor = 0.35 * ORE_GLOW_INTENSITY;
        }
    }
    #endif

    // --- Special Glowing Places on Mod Blocks & Machines (Block ID 10005 & generic) ---
    // 1. Cyan Glowing Accents: Wires, Solar Panel Traces, Combiner Hologram, Charger LEDs, RC Charger Pad
    bool isCyanGlow = (albedo.b > 0.65 && albedo.g > 0.55 && albedo.r < 0.45);

    // 2. Amber / Golden Glowing Accents: Battery Meters, Combiner Energy Gauge
    bool isAmberGlow = (albedo.r > 0.75 && albedo.g > 0.45 && albedo.b < 0.30);

    // 3. Electric Lightning & Core White/Cyan Flashes
    bool isElectricFlash = (albedo.r > 0.85 && albedo.g > 0.92 && albedo.b > 0.92 && (albedo.b - albedo.r) >= -0.05);

    // 4. Retro-reflective Luminescent Parking Bay Lines
    bool isParkingLineGlow = (albedo.r > 0.88 && albedo.g > 0.88 && albedo.b > 0.80 && lmcoord.y > 0.5);

    if (emissiveFactor <= 0.0) {
        if (isCyanGlow) {
            emissiveFactor = 0.95;
        } else if (isAmberGlow) {
            emissiveFactor = 0.90;
        } else if (isElectricFlash) {
            emissiveFactor = 1.00;
        } else if (isParkingLineGlow) {
            emissiveFactor = 0.70;
        } else if (lmcoord.x > 0.94) {
            float luma = dot(albedo.rgb, vec3(0.299, 0.587, 0.114));
            if (luma > 0.80) {
                emissiveFactor = clamp((lmcoord.x - 0.94) * 15.0, 0.0, 0.8);
            }
        }
    }

    // Add self-illumination to glowing elements
    if (emissiveFactor > 0.0) {
        shaded = mix(shaded, albedo.rgb * 1.30, clamp(emissiveFactor * 0.65, 0.0, 1.0));
    }

    vec3 emissive = albedo.rgb * emissiveFactor;

    gl_FragData[0] = vec4(shaded, albedo.a);
    gl_FragData[1] = vec4(emissive, 1.0);
}
