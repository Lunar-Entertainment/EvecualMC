#version 120

#define CONNECTED_GLASS

uniform sampler2D texture;
uniform sampler2D lightmap;

varying vec4 color;
varying vec2 texcoord;
varying vec2 lmcoord;
varying float blockId;
varying vec2 faceUV;

/* DRAWBUFFERS:01 */

void main() {
    vec4 albedo = texture2D(texture, texcoord) * color;
    if (albedo.a < 0.03) discard;

    vec4 light = texture2D(lightmap, lmcoord);

    #ifdef CONNECTED_GLASS
    // Connected Stained Glass & Tinted Glass (Block ID 10001)
    if (abs(blockId - 10001.0) < 0.5) {
        float borderDist = max(abs(faceUV.x), abs(faceUV.y));
        if (borderDist < 0.85) {
            // Clean crystal interior
            albedo.a = clamp(albedo.a * 0.45, 0.08, 0.45);
        } else {
            // Crisp frame edge
            albedo.a = max(albedo.a, 0.70);
            albedo.rgb = mix(albedo.rgb, vec3(1.0), 0.20);
        }
    }
    #endif

    vec3 shaded = albedo.rgb * light.rgb;

    // Electric sparks / flames / bright glowing elements
    float luma = dot(albedo.rgb, vec3(0.299, 0.587, 0.114));
    vec3 emissive = albedo.rgb * clamp((luma - 0.50) * 1.8, 0.0, 1.0);

    gl_FragData[0] = vec4(shaded, albedo.a);
    gl_FragData[1] = vec4(emissive, 1.0);
}
