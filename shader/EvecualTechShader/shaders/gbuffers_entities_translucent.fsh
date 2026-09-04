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
    if (albedo.a < 0.02) discard;

    vec4 light = texture2D(lightmap, lmcoord);

    // Subtle specular reflection highlight on glass
    vec3 lightDir = normalize(vec3(0.3, 0.9, 0.4));
    vec3 viewDir = vec3(0.0, 0.0, 1.0);
    vec3 halfDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfDir), 0.0), 32.0) * 0.25;

    vec3 finalRgb = albedo.rgb * light.rgb + vec3(spec);

    // Beautiful translucent blend: write color + alpha to buffer 0, ZERO bloom to buffer 1
    gl_FragData[0] = vec4(finalRgb, albedo.a);
    gl_FragData[1] = vec4(0.0, 0.0, 0.0, 0.0);
}
