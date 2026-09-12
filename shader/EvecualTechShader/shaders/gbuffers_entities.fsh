#version 120

uniform sampler2D texture;
uniform sampler2D lightmap;
uniform vec4 entityColor;
uniform vec3 sunPosition;
uniform vec3 upPosition;

varying vec4 color;
varying vec2 texcoord;
varying vec2 lmcoord;
varying vec3 normal;

/* DRAWBUFFERS:01 */

void main() {
    vec4 albedo = texture2D(texture, texcoord) * color;
    if (albedo.a < 0.08) discard;

    // Apply entity hurt flash
    albedo.rgb = mix(albedo.rgb, entityColor.rgb, entityColor.a);

    vec4 light = texture2D(lightmap, lmcoord);

    // Directional lighting with dynamic sun/moon vector and specular automotive sheen
    float sunElev = dot(normalize(sunPosition), normalize(upPosition));
    vec3 lightDir = normalize(sunElev > -0.05 ? sunPosition : -sunPosition);
    vec3 viewDir = vec3(0.0, 0.0, 1.0);
    vec3 halfDir = normalize(lightDir + viewDir);

    float NdotL = clamp(dot(normal, lightDir), 0.0, 1.0);
    float diffuse = mix(0.72, 1.08, pow(NdotL * 0.5 + 0.5, 1.25));

    // High-tech automotive clearcoat: Specular highlight and Fresnel rim reflection
    float NdotH = max(dot(normal, halfDir), 0.0);
    float spec = pow(NdotH, 36.0) * 0.45;
    float specBroad = pow(NdotH, 12.0) * 0.15;
    float fresnel = pow(1.0 - max(dot(normal, viewDir), 0.0), 3.2) * 0.25;

    vec3 gloss = vec3(spec + specBroad + fresnel * 0.4) * light.rgb;
    vec3 shaded = albedo.rgb * light.rgb * diffuse + gloss;

    // --- Special Glowing Places on Vehicles & Mobs ---
    float emissiveFactor = 0.0;

    // 1. Cyan LED indicators & antenna beacon
    bool isCyanLed = (albedo.b > 0.65 && albedo.g > 0.60 && albedo.r < 0.48);
    // 2. Red sports taillights & brake lamps
    bool isRedTaillight = (albedo.r > 0.82 && albedo.g < 0.22 && albedo.b < 0.22);
    // 3. Headlights & Xenon lamps
    bool isHeadlight = (albedo.r > 0.85 && albedo.g > 0.85 && albedo.b > 0.65 && lmcoord.y > 0.40);
    // 4. Amber hazard indicators & drone status lights
    bool isAmberLed = (albedo.r > 0.85 && albedo.g > 0.55 && albedo.b < 0.25);

    if (isCyanLed) {
        emissiveFactor = 1.25;
    } else if (isRedTaillight) {
        emissiveFactor = 1.20;
    } else if (isHeadlight) {
        emissiveFactor = 1.30;
    } else if (isAmberLed) {
        emissiveFactor = 1.15;
    }

    if (emissiveFactor > 0.0) {
        shaded = mix(shaded, albedo.rgb * 1.40, clamp(emissiveFactor * 0.75, 0.0, 1.0));
    }

    vec3 emissive = albedo.rgb * emissiveFactor;

    gl_FragData[0] = vec4(shaded, albedo.a);
    gl_FragData[1] = vec4(emissive, 1.0);
}
