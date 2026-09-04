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

    // Emissive mask: ONLY true light sources and electric conduits
    float luma = dot(albedo.rgb, vec3(0.299, 0.587, 0.114));
    float emissiveFactor = 0.0;
    if (lmcoord.x > 0.92 && luma > 0.80) {
        emissiveFactor = clamp((lmcoord.x - 0.92) * 10.0, 0.0, 0.8);
    }

    // Neon electric conduits / wires
    float electricBoost = max(albedo.b - albedo.r, 0.0) * float(albedo.g > 0.65);
    if (electricBoost > 0.35) {
        emissiveFactor = max(emissiveFactor, 0.75);
    }

    vec3 emissive = albedo.rgb * emissiveFactor;

    gl_FragData[0] = vec4(shaded, albedo.a);
    gl_FragData[1] = vec4(emissive, 1.0);
}
