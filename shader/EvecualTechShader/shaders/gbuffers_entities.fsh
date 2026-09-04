#version 120

uniform sampler2D texture;
uniform sampler2D lightmap;
uniform vec4 entityColor;

varying vec4 color;
varying vec2 texcoord;
varying vec2 lmcoord;
varying vec3 normal;

/* DRAWBUFFERS:01 */

void main() {
    vec4 albedo = texture2D(texture, texcoord) * color;
    if (albedo.a < 0.1) discard;

    // Apply entity hurt flash
    albedo.rgb = mix(albedo.rgb, entityColor.rgb, entityColor.a);

    vec4 light = texture2D(lightmap, lmcoord);

    // Directional shading with specular gloss
    vec3 lightDir = normalize(vec3(0.35, 0.85, 0.40));
    vec3 viewDir = vec3(0.0, 0.0, 1.0);
    vec3 halfDir = normalize(lightDir + viewDir);

    float NdotL = clamp(dot(normal, lightDir), 0.0, 1.0);
    float diffuse = mix(0.75, 1.05, NdotL);

    // Automotive gloss: Specular highlight and Fresnel reflection
    float spec = pow(max(dot(normal, halfDir), 0.0), 32.0) * 0.30;
    float fresnel = pow(1.0 - max(dot(normal, viewDir), 0.0), 3.0) * 0.18;

    vec3 shaded = albedo.rgb * light.rgb * diffuse + vec3(spec + fresnel * 0.4);

    // --- Special Glowing Places on Vehicles ---
    float emissiveFactor = 0.0;

    // RC Car glowing antenna tip & headlights: bright cyan
    bool isCyanLed = (albedo.b > 0.70 && albedo.g > 0.65 && albedo.r < 0.45);
    // Red sports taillights
    bool isRedTaillight = (albedo.r > 0.88 && albedo.g < 0.18 && albedo.b < 0.18);
    // Headlights
    bool isHeadlight = (albedo.r > 0.90 && albedo.g > 0.88 && albedo.b > 0.70 && lmcoord.y > 0.5);

    if (isCyanLed) {
        emissiveFactor = 0.90;
    } else if (isRedTaillight) {
        emissiveFactor = 0.85;
    } else if (isHeadlight) {
        emissiveFactor = 0.80;
    }

    if (emissiveFactor > 0.0) {
        shaded = mix(shaded, albedo.rgb * 1.3, emissiveFactor * 0.7);
    }

    vec3 emissive = albedo.rgb * emissiveFactor;

    gl_FragData[0] = vec4(shaded, albedo.a);
    gl_FragData[1] = vec4(emissive, 1.0);
}
