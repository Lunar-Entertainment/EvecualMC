#version 120

uniform sampler2D colortex0;
uniform sampler2D colortex1;

uniform float viewWidth;
uniform float viewHeight;

varying vec2 texcoord;

/* DRAWBUFFERS:0 */

// Tonemap helper
vec3 filmicTone(vec3 x) {
    vec3 a = vec3(0.004);
    vec3 d = vec3(0.20);
    return max(vec3(0.0), x - a) / (x * (1.0 + d) + 0.18);
}

void main() {
    vec4 baseColor = texture2D(colortex0, texcoord);
    vec2 pixelSize = vec2(1.0 / max(viewWidth, 1.0), 1.0 / max(viewHeight, 1.0));

    // Multi-tap Gaussian Bloom sampling on colortex1 (emissive)
    vec3 bloom = vec3(0.0);
    float totalWeight = 0.0;

    // Small radius taps
    vec2 offsets[8] = vec2[](
        vec2(-1.5, -1.5), vec2( 1.5, -1.5),
        vec2(-1.5,  1.5), vec2( 1.5,  1.5),
        vec2(-3.0,  0.0), vec2( 3.0,  0.0),
        vec2( 0.0, -3.0), vec2( 0.0,  3.0)
    );

    for (int i = 0; i < 8; i++) {
        vec2 tapCoord = texcoord + offsets[i] * pixelSize * 2.5;
        bloom += texture2D(colortex1, tapCoord).rgb * 0.10;
        totalWeight += 0.10;
    }

    // Wide radius taps for soft haze
    vec2 wideOffsets[4] = vec2[](
        vec2(-6.0, -6.0), vec2( 6.0, -6.0),
        vec2(-6.0,  6.0), vec2( 6.0,  6.0)
    );

    for (int i = 0; i < 4; i++) {
        vec2 tapCoord = texcoord + wideOffsets[i] * pixelSize * 3.5;
        bloom += texture2D(colortex1, tapCoord).rgb * 0.05;
        totalWeight += 0.05;
    }

    bloom /= totalWeight;

    // Combine base with bloom
    vec3 color = baseColor.rgb + bloom * 1.35;

    // High-tech vibrance
    float luma = dot(color, vec3(0.299, 0.587, 0.114));
    vec3 sat = mix(vec3(luma), color, 1.14);

    // Modern contrast S-curve
    vec3 contrasted = pow(max(sat, vec3(0.0)), vec3(1.06));

    // Cinematic vignette
    vec2 uv = texcoord - 0.5;
    float dist = dot(uv, uv);
    float vignette = 1.0 - dist * 0.45;
    contrasted *= clamp(vignette, 0.75, 1.0);

    gl_FragData[0] = vec4(contrasted, baseColor.a);
}
