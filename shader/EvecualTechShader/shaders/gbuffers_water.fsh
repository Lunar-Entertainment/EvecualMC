#version 120

#define WAVING_WATER

uniform sampler2D texture;
uniform sampler2D lightmap;
uniform float frameTimeCounter;

varying vec4 color;
varying vec2 texcoord;
varying vec2 lmcoord;
varying vec3 normal;
varying vec3 worldPos;

/* DRAWBUFFERS:01 */

void main() {
    vec4 albedo = texture2D(texture, texcoord) * color;
    vec4 light = texture2D(lightmap, lmcoord);

    vec3 viewDir = normalize(-worldPos);
    vec3 lightDir = normalize(vec3(0.35, 0.85, 0.40));

    // Animated water surface normal perturbation
    vec3 perturbedNormal = normal;
    #ifdef WAVING_WATER
    float time = frameTimeCounter * 2.0;
    float waveX = sin(worldPos.x * 2.5 + time) * cos(worldPos.z * 2.0 + time * 0.8) * 0.12;
    float waveZ = cos(worldPos.x * 2.0 - time * 0.7) * sin(worldPos.z * 2.5 + time * 1.1) * 0.12;
    perturbedNormal = normalize(normal + vec3(waveX, 0.0, waveZ));
    #endif

    // Water sun specular glint & Fresnel reflection
    vec3 halfDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(perturbedNormal, halfDir), 0.0), 64.0) * 0.65;
    float fresnel = pow(1.0 - max(dot(perturbedNormal, viewDir), 0.0), 4.0);

    // Crystal aqua-blue tint with depth translucency
    vec3 waterColor = mix(albedo.rgb, vec3(0.12, 0.58, 0.85), 0.45);
    vec3 skyReflect = mix(vec3(0.65, 0.85, 1.0), vec3(1.0, 1.0, 1.0), fresnel);

    vec3 shaded = waterColor * light.rgb * 0.90 + skyReflect * fresnel * 0.45 + vec3(spec * light.a);
    float alpha = clamp(albedo.a * 0.65 + fresnel * 0.35, 0.30, 0.85);

    gl_FragData[0] = vec4(shaded, alpha);
    gl_FragData[1] = vec4(0.0, 0.0, 0.0, 0.0);
}
