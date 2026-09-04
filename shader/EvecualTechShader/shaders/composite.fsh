#version 120

/*
 * Evecual Tech Shader - Composite Pass
 * Features: Quality Settings (Low, Medium, High), Procedural Drifting Clouds, ACES Filmic Tonemap, Emissive Bloom
 */

#define QUALITY 1 // [0 1 2]
#define BLOOM 1   // [0 1 2]
#define CLOUDS    // [true false]
#define VIGNETTE  // [true false]

uniform sampler2D colortex0;
uniform sampler2D colortex1;
uniform sampler2D depthtex0;

uniform float viewWidth;
uniform float viewHeight;
uniform float frameTimeCounter;

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

// Procedural 2D noise for organic drifting clouds
float hash21(vec2 p) {
    p = fract(p * vec2(234.34, 435.345));
    p += dot(p, p + 34.23);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash21(i);
    float b = hash21(i + vec2(1.0, 0.0));
    float c = hash21(i + vec2(0.0, 1.0));
    float d = hash21(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    v += 0.500 * noise(p); p *= 2.02;
    v += 0.250 * noise(p); p *= 2.03;
    v += 0.125 * noise(p);
    return v;
}

void main() {
    vec4 baseColor = texture2D(colortex0, texcoord);
    float depth = texture2D(depthtex0, texcoord).r;
    vec2 pixelSize = vec2(1.0 / max(viewWidth, 1.0), 1.0 / max(viewHeight, 1.0));

    vec3 skyColor = baseColor.rgb;

#ifdef CLOUDS
    // If pixel is background sky (depth == 1.0), render beautiful drifting clouds
    if (depth >= 0.9999) {
        vec2 uv = (texcoord - 0.5) * vec2(viewWidth / max(viewHeight, 1.0), 1.0);
        float time = frameTimeCounter * 0.015;
        vec2 cloudCoord = uv * 2.8 + vec2(time, time * 0.35);

        float cloudDensity = fbm(cloudCoord);
        float cloudMask = smoothstep(0.42, 0.72, cloudDensity);

        if (cloudMask > 0.01) {
            // Cloud top highlight & soft underside shadow
            float cloudShading = smoothstep(0.45, 0.80, fbm(cloudCoord + vec2(0.02, 0.04)));
            vec3 cloudCol = mix(vec3(0.85, 0.90, 0.95), vec3(1.00, 1.00, 1.00), cloudShading);
            skyColor = mix(skyColor, cloudCol, cloudMask * 0.85);
        }
    }
#endif

    vec3 bloom = vec3(0.0);

#if BLOOM > 0
    #if BLOOM == 1
    // Medium: 4-tap cross bloom
    vec2 offsets[4] = vec2[](
        vec2(-2.0, -2.0), vec2( 2.0, -2.0),
        vec2(-2.0,  2.0), vec2( 2.0,  2.0)
    );
    for (int i = 0; i < 4; i++) {
        bloom += texture2D(colortex1, texcoord + offsets[i] * pixelSize * 2.2).rgb * 0.25;
    }
    #elif BLOOM == 2
    // High: 12-tap multi-scale Gaussian bloom
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

    // Combine base/clouds with bloom
    vec3 color = skyColor + bloom * 0.65;

    // ACES Filmic Tonemapping
    vec3 graded = acesFilm(color * 1.10);

    // Tech vibrance
    float luma = dot(graded, vec3(0.299, 0.587, 0.114));
    graded = mix(vec3(luma), graded, 1.08);

#ifdef VIGNETTE
    vec2 uvc = texcoord - 0.5;
    float vig = clamp(1.0 - dot(uvc, uvc) * 0.35, 0.85, 1.0);
    graded *= vig;
#endif

    gl_FragData[0] = vec4(graded, baseColor.a);
}
