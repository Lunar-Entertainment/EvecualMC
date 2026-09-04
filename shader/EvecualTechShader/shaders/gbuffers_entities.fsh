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

    // Apply entity flash (damage hurt color) if present
    albedo.rgb = mix(albedo.rgb, entityColor.rgb, entityColor.a);

    vec4 light = texture2D(lightmap, lmcoord);

    // Directional shading
    vec3 lightDir = normalize(vec3(0.35, 0.85, 0.40));
    float NdotL = clamp(dot(normal, lightDir), 0.0, 1.0);
    float diffuse = mix(0.72, 1.08, NdotL);

    vec3 shaded = albedo.rgb * light.rgb * diffuse;

    // Headlights, taillights, cyan LED strips on vehicles
    float isCyanLed = max(albedo.b - albedo.r, 0.0) * float(albedo.g > 0.6);
    float isBrightWhite = float(albedo.r > 0.9 && albedo.g > 0.9 && albedo.b > 0.9);
    float isRedTaillight = float(albedo.r > 0.85 && albedo.g < 0.2 && albedo.b < 0.2);

    float emissiveFactor = 0.0;
    if (isCyanLed > 0.2 || isBrightWhite > 0.5 || isRedTaillight > 0.5) {
        emissiveFactor = 0.85;
    } else if (lmcoord.x > 0.82) {
        emissiveFactor = clamp((lmcoord.x - 0.80) * 4.0, 0.0, 0.8);
    }

    vec3 emissive = albedo.rgb * emissiveFactor;

    gl_FragData[0] = vec4(shaded, albedo.a);
    gl_FragData[1] = vec4(emissive, 1.0);
}
