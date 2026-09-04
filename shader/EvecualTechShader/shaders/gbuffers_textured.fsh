#version 120

uniform sampler2D texture;
uniform sampler2D lightmap;

varying vec4 color;
varying vec2 texcoord;
varying vec2 lmcoord;

/* DRAWBUFFERS:01 */

void main() {
    vec4 albedo = texture2D(texture, texcoord) * color;
    if (albedo.a < 0.05) discard;

    vec4 light = texture2D(lightmap, lmcoord);
    vec3 shaded = albedo.rgb * light.rgb;

    // Electric sparks / flames are emissive
    float luma = dot(albedo.rgb, vec3(0.299, 0.587, 0.114));
    vec3 emissive = albedo.rgb * clamp((luma - 0.45) * 1.8, 0.0, 1.0);

    gl_FragData[0] = vec4(shaded, albedo.a);
    gl_FragData[1] = vec4(emissive, 1.0);
}
