#version 120

#define CONNECTED_BLOCKS
#define CONNECTED_GLASS
#define EMISSIVE_ORES
#define ORE_GLOW_INTENSITY 1.50

uniform sampler2D texture;
uniform sampler2D lightmap;

varying vec4 color;
varying vec2 texcoord;
varying vec2 lmcoord;
varying vec3 normal;
varying float blockId;
varying vec2 faceUV;
varying vec3 worldPos;

/* DRAWBUFFERS:01 */

void main() {
    vec4 albedo = texture2D(texture, texcoord) * color;
    if (albedo.a < 0.05) discard;

    vec4 light = texture2D(lightmap, lmcoord);

    // Dynamic sun/ambient directional lighting
    vec3 lightDir = normalize(vec3(0.35, 0.85, 0.40));
    vec3 viewDir = normalize(-worldPos);
    vec3 halfDir = normalize(lightDir + viewDir);

    float NdotL = clamp(dot(normal, lightDir), 0.0, 1.0);
    // Half-Lambert wrap diffuse for rich volumetric shadows without pitch-black faces
    float wrapDiffuse = pow(NdotL * 0.5 + 0.5, 1.3);
    float diffuse = mix(0.72, 1.08, wrapDiffuse);

    vec3 shaded = albedo.rgb * light.rgb * diffuse;

    float borderDist = max(abs(faceUV.x), abs(faceUV.y));
    bool isOuterBorder = borderDist > 0.85;

    #ifdef CONNECTED_BLOCKS
    // --- 1. Glass & Panes (Block ID 10001) ---
    if (abs(blockId - 10001.0) < 0.5) {
        #ifdef CONNECTED_GLASS
        if (!isOuterBorder) {
            // Clean seamless crystal-clear interior
            albedo.a = clamp(albedo.a * 0.35, 0.03, 0.25);
            shaded = mix(shaded, vec3(0.94, 0.97, 1.0) * light.rgb, 0.38);
        } else {
            // Sleek outer edge frame
            shaded = mix(shaded, vec3(1.0) * light.rgb, 0.30);
            albedo.a = max(albedo.a, 0.70);
        }
        #endif
    }
    // --- 2. Bookshelves (Block ID 10002) ---
    else if (abs(blockId - 10002.0) < 0.5) {
        if (abs(faceUV.x) > 0.85 && abs(faceUV.y) < 0.85) {
            shaded = mix(shaded, shaded * 1.08, 0.45);
        }
    }
    // --- 3. Mineral / Ore Blocks (Block ID 10003: Iron, Gold, Diamond, Copper, Netherite) ---
    else if (abs(blockId - 10003.0) < 0.5) {
        float spec = pow(max(dot(normal, halfDir), 0.0), 24.0) * 0.35 * light.a;
        if (!isOuterBorder) {
            shaded = shaded * 1.06 + vec3(spec);
        } else {
            shaded = mix(shaded, shaded * 1.20, 0.40) + vec3(spec * 1.5);
        }
    }
    #endif

    // --- Emissive Glowing Highlights ---
    float emissiveFactor = 0.0;

    #ifdef EMISSIVE_ORES
    // --- 4. Connected Emissive Ore Veins (Block ID 10004) ---
    if (abs(blockId - 10004.0) < 0.5) {
        // Diamond: Cyan / light blue gems
        bool isDiamond = (albedo.b > 0.55 && albedo.g > 0.45 && albedo.r < 0.45);
        // Emerald: Vivid green gems
        bool isEmerald = (albedo.g > 0.46 && albedo.r < 0.40 && albedo.b < 0.40);
        // Redstone: Glowing red crystals
        bool isRedstone = (albedo.r > 0.46 && albedo.g < 0.28 && albedo.b < 0.28);
        // Gold: Warm golden nuggets
        bool isGold = (albedo.r > 0.60 && albedo.g > 0.45 && albedo.b < 0.35);
        // Copper: Electric orange copper veins
        bool isCopper = (albedo.r > 0.56 && albedo.g > 0.30 && albedo.b < 0.32);
        // Lapis: Deep cobalt lapis lazuli
        bool isLapis = (albedo.b > 0.46 && albedo.r < 0.35 && albedo.g < 0.40);
        // Quartz: Radiant white crystal
        bool isQuartz = (albedo.r > 0.78 && albedo.g > 0.76 && albedo.b > 0.74);
        // Coal: Subtle warm ember
        bool isCoal = (albedo.r < 0.24 && albedo.g < 0.24 && albedo.b < 0.24 && length(albedo.rgb) > 0.05);

        if (isDiamond) {
            emissiveFactor = 1.35 * ORE_GLOW_INTENSITY;
        } else if (isEmerald) {
            emissiveFactor = 1.30 * ORE_GLOW_INTENSITY;
        } else if (isRedstone) {
            emissiveFactor = 1.45 * ORE_GLOW_INTENSITY;
        } else if (isGold) {
            emissiveFactor = 1.20 * ORE_GLOW_INTENSITY;
        } else if (isCopper) {
            emissiveFactor = 1.20 * ORE_GLOW_INTENSITY;
        } else if (isLapis) {
            emissiveFactor = 1.25 * ORE_GLOW_INTENSITY;
        } else if (isQuartz) {
            emissiveFactor = 1.15 * ORE_GLOW_INTENSITY;
        } else if (isCoal) {
            emissiveFactor = 0.40 * ORE_GLOW_INTENSITY;
        }
    }
    #endif

    // --- Special Glowing Places on Mod Blocks & Machines (Block ID 10005 & generic) ---
    // 1. Cyan Glowing Accents: Wires, Solar Panel Traces, Combiner Hologram, Charger LEDs, RC Charger Station, Terminal Radar
    bool isCyanGlow = (albedo.b > 0.60 && albedo.g > 0.50 && albedo.r < 0.48);

    // 2. Amber / Golden Glowing Accents: Battery Meters, Combiner Energy Gauge, Robot Station Hazard LEDs
    bool isAmberGlow = (albedo.r > 0.72 && albedo.g > 0.42 && albedo.b < 0.32);

    // 3. Electric Lightning & Core White/Cyan Flashes
    bool isElectricFlash = (albedo.r > 0.82 && albedo.g > 0.88 && albedo.b > 0.88 && (albedo.b - albedo.r) >= -0.06);

    // 4. Retro-reflective Luminescent Parking Bay Lines
    bool isParkingLineGlow = (albedo.r > 0.85 && albedo.g > 0.85 && albedo.b > 0.75 && lmcoord.y > 0.45);

    if (emissiveFactor <= 0.0) {
        if (isCyanGlow) {
            emissiveFactor = 1.10;
        } else if (isAmberGlow) {
            emissiveFactor = 1.05;
        } else if (isElectricFlash) {
            emissiveFactor = 1.20;
        } else if (isParkingLineGlow) {
            emissiveFactor = 0.80;
        } else if (lmcoord.x > 0.92) {
            float luma = dot(albedo.rgb, vec3(0.299, 0.587, 0.114));
            if (luma > 0.75) {
                emissiveFactor = clamp((lmcoord.x - 0.92) * 16.0, 0.0, 1.0);
            }
        }
    }

    // Add self-illumination to glowing elements
    if (emissiveFactor > 0.0) {
        shaded = mix(shaded, albedo.rgb * 1.35, clamp(emissiveFactor * 0.70, 0.0, 1.0));
    }

    vec3 emissive = albedo.rgb * emissiveFactor;

    gl_FragData[0] = vec4(shaded, albedo.a);
    gl_FragData[1] = vec4(emissive, 1.0);
}
