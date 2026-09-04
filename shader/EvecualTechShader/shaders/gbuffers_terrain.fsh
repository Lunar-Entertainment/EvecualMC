#version 120

uniform sampler2D texture;
uniform sampler2D lightmap;

varying vec4 color;
varying vec2 texcoord;
varying vec2 lmcoord;
varying vec3 normal;

/* DRAWBUFFERS:01 */

void main() {
    vec4 albedo = texture2D(texture, texcoord) * color;
    if (albedo.a < 0.1) discard;

    vec4 light = texture2D(lightmap, lmcoord);

    // Directional shading for crisp clean depth
    vec3 lightDir = normalize(vec3(0.35, 0.85, 0.40));
    float NdotL = clamp(dot(normal, lightDir), 0.0, 1.0);
    float diffuse = mix(0.78, 1.05, NdotL);

    vec3 shaded = albedo.rgb * light.rgb * diffuse;

    // --- Special Glowing Places on Mod Blocks & Machines ---
    float emissiveFactor = 0.0;

    // 1. Cyan Glowing Accents: Wires, Solar Panel Traces, Combiner Hologram, Charger LEDs, RC Charger Pad
    bool isCyanGlow = (albedo.b > 0.65 && albedo.g > 0.55 && albedo.r < 0.45);

    // 2. Amber / Golden Glowing Accents: Battery Meters, Combiner Energy Gauge
    bool isAmberGlow = (albedo.r > 0.75 && albedo.g > 0.45 && albedo.b < 0.30);

    // 3. Electric Lightning & Core White/Cyan Flashes
    bool isElectricFlash = (albedo.r > 0.85 && albedo.g > 0.92 && albedo.b > 0.92 && (albedo.b - albedo.r) >= -0.05);

    // 4. Retro-reflective Luminescent Parking Bay Lines
    bool isParkingLineGlow = (albedo.r > 0.88 && albedo.g > 0.88 && albedo.b > 0.80 && lmcoord.y > 0.5);

    if (isCyanGlow) {
        emissiveFactor = 0.95;
    } else if (isAmberGlow) {
        emissiveFactor = 0.90;
    } else if (isElectricFlash) {
        emissiveFactor = 1.00;
    } else if (isParkingLineGlow) {
        emissiveFactor = 0.70;
    } else if (lmcoord.x > 0.94) {
        // Vanilla bright light blocks (glowstone, lanterns, beacons)
        float luma = dot(albedo.rgb, vec3(0.299, 0.587, 0.114));
        if (luma > 0.80) {
            emissiveFactor = clamp((lmcoord.x - 0.94) * 15.0, 0.0, 0.8);
        }
    }

    // Add self-illumination to glowing elements
    if (emissiveFactor > 0.0) {
        shaded = mix(shaded, albedo.rgb * 1.25, emissiveFactor * 0.6);
    }

    vec3 emissive = albedo.rgb * emissiveFactor;

    gl_FragData[0] = vec4(shaded, albedo.a);
    gl_FragData[1] = vec4(emissive, 1.0);
}
