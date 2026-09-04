#version 120

/*
 * Evecual Tech Shader - Composite Pass
 * Features: Quality Settings (Low, Medium, High), ACES Filmic Tonemap, Emissive Bloom
 */

#define QUALITY 1 // [0 1 2]
#define BLOOM 1   // [0 1 2]
#define VIGNETTE  // [true false]

uniform sampler2D colortex0;
uniform sampler2D colortex1;

uniform float viewWidth;
uniform float viewHeight;

varying vec2 texcoord;

/* DRAWBUFFERS:0 */

// ACES Filmic Tonemapping Curve: prevents highlight blowout while retaining deep cyber-blacks
vec3 acesFilm(vec3 x) {
    float a = 2.51;
    float b = 0.03;
    float c = 2.43;
    float d = 0.59;
    float e = 0.14;
    return clamp((x * (a * x + b)) / (x * (c * x + d) + e), 0.0, 1.0);
}

void main() {
    vec4 baseColor = texture2D(colortex0, texcoord);
    vec2 pixelSize = vec2(1.0 / max(viewWidth, 1.0), 1.0 / max(viewHeight, 1.0));

    vec3 bloom = vec3(0.0);

#if BLOOM > 0
    #if BLOOM == 1
    // Medium Quality: 4-tap optimized cross bloom
    vec2 offsets[4] = vec2[](
        vec2(-2.0, -2.0), vec2( 2.0, -2.0),
        vec2(-2.0,  2.0), vec2( 2.0,  2.0)
    );
    for (int i = 0; i < 4; i++) {
        bloom += texture2D(colortex1, texcoord + offsets[i] * pixelSize * 2.5).rgb * 0.25;
    }
    #elif BLOOM == 2
    // High Quality: 12-tap multi-scale Gaussian bloom
    vec2 offsetsMed[8] = vec2[](
        vec2(-1.5, -1.5), vec2( 1.5, -1.5),
        vec2(-1.5,  1.5), vec2( 1.5,  1.5),
        vec2(-3.0,  0.0), vec2( 3.0,  0.0),
        vec2( 0.0, -3.0), vec2( 0.0,  3.0)
    );
    for (int i = 0; i < 8; i++) {
        bloom += texture2D(colortex1, texcoord + offsetsMed[i] * pixelSize * 2.0).rgb * 0.08;
    }

    vec2 offsetsWide[4] = vec2[](
        vec2(-5.0, -5.0), vec2( 5.0, -5.0),
        vec2(-5.0,  5.0), vec2( 5.0,  5.0)
    );
    for (int i = 0; i < 4; i++) {
        bloom += texture2D(colortex1, texcoord + offsetsWide[i] * pixelSize * 3.5).rgb * 0.09;
    }
    #endif
#endif

    // Composite bloom
    vec3 color = baseColor.rgb + bloom * 0.55;

    // High-Tech Color Grading: ACES tonemap
    vec3 graded = acesFilm(color * 1.15);

    // Tech vibrance
    float luma = dot(graded, vec3(0.299, 0.587, 0.114));
    graded = mix(vec3(luma), graded, 1.08);

#ifdef VIGNETTE
    // Subtle modern vignette
    vec2 uv = texcoord - 0.5;
    float dist = dot(uv, uv);
    float vig = clamp(1.0 - dist * 0.35, 0.85, 1.0);
    graded *= vig;
#endif

    gl_FragData[0] = vec4(graded, baseColor.a);
}
