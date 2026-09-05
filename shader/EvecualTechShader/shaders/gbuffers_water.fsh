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

    // High-Fidelity Multi-frequency Animated Water Waves
    vec3 perturbedNormal = normal;
    #ifdef WAVING_WATER
    float time = frameTimeCounter * 2.2;
    float w1 = sin(worldPos.x * 2.8 + time) * cos(worldPos.z * 2.2 + time * 0.85) * 0.10;
    float w2 = cos(worldPos.x * 1.6 - time * 0.75) * sin(worldPos.z * 3.2 + time * 1.15) * 0.08;
    float w3 = sin((worldPos.x + worldPos.z) * 4.2 + time * 1.6) * 0.04;
    perturbedNormal = normalize(normal + vec3(w1 + w3, 0.0, w2 + w3));
    #endif

    // Water sun specular glint & Fresnel reflection
    vec3 halfDir = normalize(lightDir + viewDir);
    float specSharp = pow(max(dot(perturbedNormal, halfDir), 0.0), 96.0) * 0.85;
    float specBroad = pow(max(dot(perturbedNormal, halfDir), 0.0), 24.0) * 0.25;
    float fresnel = pow(1.0 - max(dot(perturbedNormal, viewDir), 0.0), 4.2);

    // Crystal aqua-turquoise ocean scattering
    vec3 waterColor = mix(albedo.rgb, vec3(0.08, 0.52, 0.82), 0.55);
    vec3 skyReflect = mix(vec3(0.60, 0.82, 0.98), vec3(1.0, 1.0, 1.0), fresnel);

    vec3 shaded = waterColor * light.rgb * 0.92 + skyReflect * fresnel * 0.55 + vec3((specSharp + specBroad) * light.a);
    float alpha = clamp(albedo.a * 0.60 + fresnel * 0.40, 0.28, 0.88);

    gl_FragData[0] = vec4(shaded, alpha);
    gl_FragData[1] = vec4(vec3(specSharp * 0.5), 1.0);
}

