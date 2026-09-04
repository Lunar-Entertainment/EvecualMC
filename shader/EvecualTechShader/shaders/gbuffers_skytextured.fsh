#version 120

uniform sampler2D texture;

varying vec4 color;
varying vec2 texcoord;

/* DRAWBUFFERS:01 */

void main() {
    vec4 albedo = texture2D(texture, texcoord) * color;
    if (albedo.a < 0.05) discard;

    // Distance from center of sun/moon quad
    vec2 p = texcoord - 0.5;
    float dist = length(p);

    // Radiant solar corona rays & bright core
    float corona = clamp(1.0 - dist * 1.7, 0.0, 1.0);
    float core = clamp(1.0 - dist * 2.8, 0.0, 1.0);

    vec3 sunGlow = mix(vec3(1.0, 0.88, 0.65), vec3(1.0, 1.0, 0.95), core) * (core * 1.2 + corona * 0.5);

    vec3 finalRgb = albedo.rgb + sunGlow * 0.45;
    vec3 emissive = sunGlow * 0.65;

    gl_FragData[0] = vec4(finalRgb, albedo.a);
    gl_FragData[1] = vec4(emissive, albedo.a);
}
