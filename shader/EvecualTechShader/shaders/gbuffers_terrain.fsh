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

    // Directional shading for crisp tech depth
    vec3 lightDir = normalize(vec3(0.35, 0.85, 0.40));
    float NdotL = clamp(dot(normal, lightDir), 0.0, 1.0);
    float diffuse = mix(0.70, 1.08, NdotL);

    vec3 shaded = albedo.rgb * light.rgb * diffuse;

    // Emissive mask extraction for neon bloom
    // Block light (lmcoord.x) > 0.82 or high self-luminance
    float blockLight = lmcoord.x;
    float luma = dot(albedo.rgb, vec3(0.299, 0.587, 0.114));
    
    // Check for electric cyan/neon colors
    float electricBoost = max(albedo.b - albedo.r, 0.0) * float(albedo.g > 0.4);
    float emissiveFactor = clamp((blockLight - 0.75) * 4.0, 0.0, 1.0);
    if (electricBoost > 0.3) {
        emissiveFactor = max(emissiveFactor, 0.7);
    }

    vec3 emissive = albedo.rgb * emissiveFactor;

    gl_FragData[0] = vec4(shaded, albedo.a);
    gl_FragData[1] = vec4(emissive, 1.0);
}
