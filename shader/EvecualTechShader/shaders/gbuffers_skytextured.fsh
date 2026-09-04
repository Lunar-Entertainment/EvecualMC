#version 120

uniform sampler2D texture;

varying vec4 color;
varying vec2 texcoord;

/* DRAWBUFFERS:01 */

void main() {
    vec4 albedo = texture2D(texture, texcoord) * color;
    gl_FragData[0] = albedo;
    gl_FragData[1] = vec4(albedo.rgb * 0.35, 1.0); // Soft celestial glow
}
