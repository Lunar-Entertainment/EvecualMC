#version 120

/*
 * Evecual Tech Shader - Composite Pass (Next-Gen Cyber-Tech Engine)
 */

#define QUALITY 2             // [0 1 2 3]
#define VOLUMETRIC_RAYS       // [true false]
#define SSAO                  // [true false]
#define BLOOM 2               // [0 1 2]
#define CLOUDS                // [true false]
#define VIGNETTE              // [true false]
#define CHROMATIC_ABERRATION  // [true false]
#define MOTION_BLUR           // [true false]
#define MOTION_BLUR_SAMPLES 7 // [3 5 7]
#define TECH_CONTRAST 1.06    // [0.90 1.00 1.06 1.12]
#define TECH_VIBRANCE 1.12    // [0.90 1.00 1.12 1.20]

uniform sampler2D colortex0;
uniform sampler2D colortex1;
uniform sampler2D depthtex0;

uniform float viewWidth;
uniform float viewHeight;
uniform float frameTimeCounter;
uniform vec3 sunPosition;

uniform mat4 gbufferProjection;
uniform mat4 gbufferProjectionInverse;
uniform mat4 gbufferModelViewInverse;
uniform mat4 gbufferPreviousModelView;
uniform mat4 gbufferPreviousProjection;

varying vec2 texcoord;

/* DRAWBUFFERS:0 */

// ACES Filmic Tonemapping Curve
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

// Linearize depth buffer
float linearizeDepth(float d) {
    return (2.0 * 0.1) / (100.0 + 0.1 - d * (100.0 - 0.1));
}

void main() {
    float depth = texture2D(depthtex0, texcoord).r;
    vec2 pixelSize = vec2(1.0 / max(viewWidth, 1.0), 1.0 / max(viewHeight, 1.0));

    vec4 baseColor = texture2D(colortex0, texcoord);

    #ifdef CHROMATIC_ABERRATION
    // Subtle chromatic dispersion towards screen borders
    vec2 distFromCenter = texcoord - 0.5;
    float caDist = dot(distFromCenter, distFromCenter) * 0.0035;
    float rChannel = texture2D(colortex0, texcoord + distFromCenter * caDist).r;
    float bChannel = texture2D(colortex0, texcoord - distFromCenter * caDist).b;
    baseColor.r = rChannel;
    baseColor.b = bChannel;
    #endif

    #ifdef MOTION_BLUR
    // Velocity-vector camera motion blur
    vec4 currentClip = vec4(texcoord.x * 2.0 - 1.0, texcoord.y * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 viewPos = gbufferProjectionInverse * currentClip;
    if (abs(viewPos.w) > 0.0001) {
        viewPos /= viewPos.w;
        vec4 feetPos = gbufferModelViewInverse * viewPos;

        vec4 prevViewPos = gbufferPreviousModelView * feetPos;
        vec4 prevClip = gbufferPreviousProjection * prevViewPos;
        if (abs(prevClip.w) > 0.0001) {
            prevClip /= prevClip.w;
            vec2 prevCoord = prevClip.xy * 0.5 + 0.5;
            vec2 velocity = (texcoord - prevCoord) * 0.38;
            float speed = length(velocity);
            if (speed > 0.0005) {
                velocity = clamp(velocity, vec2(-0.035), vec2(0.035));
                vec4 accumColor = baseColor;
                float totalWeight = 1.0;
                for (int s = 1; s <= MOTION_BLUR_SAMPLES; s++) {
                    float t = float(s) / float(MOTION_BLUR_SAMPLES);
                    vec2 sampleCoord = clamp(texcoord + velocity * t, 0.0, 1.0);
                    float sampleDepth = texture2D(depthtex0, sampleCoord).r;
                    if (abs(sampleDepth - depth) < 0.12) {
                        accumColor += texture2D(colortex0, sampleCoord);
                        totalWeight += 1.0;
                    }
                }
                baseColor = accumColor / totalWeight;
            }
        }
    }
    #endif

    vec3 sceneColor = baseColor.rgb;

    #ifdef SSAO
    // Screen Space Ambient Occlusion for grounded contact shadows (unrolled for GLSL 120 compatibility)
    if (depth < 0.999) {
        float centerLinDepth = linearizeDepth(depth);
        float totalAO = 0.0;

        float d1 = linearizeDepth(texture2D(depthtex0, clamp(texcoord + vec2(-1.5,  0.5) * pixelSize * 2.8, 0.0, 1.0)).r);
        float d2 = linearizeDepth(texture2D(depthtex0, clamp(texcoord + vec2( 1.5, -0.5) * pixelSize * 2.8, 0.0, 1.0)).r);
        float d3 = linearizeDepth(texture2D(depthtex0, clamp(texcoord + vec2( 0.5,  1.5) * pixelSize * 2.8, 0.0, 1.0)).r);
        float d4 = linearizeDepth(texture2D(depthtex0, clamp(texcoord + vec2(-0.5, -1.5) * pixelSize * 2.8, 0.0, 1.0)).r);
        float d5 = linearizeDepth(texture2D(depthtex0, clamp(texcoord + vec2(-2.5, -2.5) * pixelSize * 2.8, 0.0, 1.0)).r);
        float d6 = linearizeDepth(texture2D(depthtex0, clamp(texcoord + vec2( 2.5,  2.5) * pixelSize * 2.8, 0.0, 1.0)).r);
        float d7 = linearizeDepth(texture2D(depthtex0, clamp(texcoord + vec2( 3.0, -1.0) * pixelSize * 2.8, 0.0, 1.0)).r);
        float d8 = linearizeDepth(texture2D(depthtex0, clamp(texcoord + vec2(-3.0,  1.0) * pixelSize * 2.8, 0.0, 1.0)).r);

        if (centerLinDepth - d1 > 0.0002 && centerLinDepth - d1 < 0.018) totalAO += 1.0;
        if (centerLinDepth - d2 > 0.0002 && centerLinDepth - d2 < 0.018) totalAO += 1.0;
        if (centerLinDepth - d3 > 0.0002 && centerLinDepth - d3 < 0.018) totalAO += 1.0;
        if (centerLinDepth - d4 > 0.0002 && centerLinDepth - d4 < 0.018) totalAO += 1.0;
        if (centerLinDepth - d5 > 0.0002 && centerLinDepth - d5 < 0.018) totalAO += 1.0;
        if (centerLinDepth - d6 > 0.0002 && centerLinDepth - d6 < 0.018) totalAO += 1.0;
        if (centerLinDepth - d7 > 0.0002 && centerLinDepth - d7 < 0.018) totalAO += 1.0;
        if (centerLinDepth - d8 > 0.0002 && centerLinDepth - d8 < 0.018) totalAO += 1.0;

        float aoFactor = clamp(1.0 - (totalAO / 8.0) * 0.45, 0.55, 1.0);
        sceneColor *= aoFactor;
    }
    #endif

    #ifdef CLOUDS
    // Procedural multi-layer drifting clouds in the sky
    if (depth >= 0.9999) {
        vec2 uv = (texcoord - 0.5) * vec2(viewWidth / max(viewHeight, 1.0), 1.0);
        float time = frameTimeCounter * 0.012;
        vec2 cloudCoord = uv * 2.6 + vec2(time, time * 0.3);

        float cloudDensity = fbm(cloudCoord);
        float cloudMask = smoothstep(0.40, 0.74, cloudDensity);

        if (cloudMask > 0.01) {
            float cloudShading = smoothstep(0.42, 0.82, fbm(cloudCoord + vec2(0.02, 0.04)));
            vec3 cloudCol = mix(vec3(0.82, 0.88, 0.96), vec3(1.00, 1.00, 1.00), cloudShading);
            sceneColor = mix(sceneColor, cloudCol, cloudMask * 0.88);
        }
    }
    #endif

    #ifdef VOLUMETRIC_RAYS
    // Sun God Rays / Crepuscular Rays
    vec4 sunClip = gbufferProjection * vec4(sunPosition, 1.0);
    if (sunClip.w > 0.0) {
        vec2 sunScreen = (sunClip.xy / sunClip.w) * 0.5 + 0.5;
        if (sunScreen.x >= -0.3 && sunScreen.x <= 1.3 && sunScreen.y >= -0.3 && sunScreen.y <= 1.3) {
            vec2 rayDir = (sunScreen - texcoord);
            float rayDist = length(rayDir);
            vec2 stepUV = rayDir / 12.0;
            float rayDensity = 0.0;
            vec2 currentUV = texcoord;

            for (int r = 0; r < 12; r++) {
                currentUV += stepUV;
                if (currentUV.x < 0.0 || currentUV.x > 1.0 || currentUV.y < 0.0 || currentUV.y > 1.0) break;
                float sampleD = texture2D(depthtex0, currentUV).r;
                if (sampleD >= 0.9999) {
                    rayDensity += (1.0 - float(r) / 12.0);
                }
            }

            float sunWarmth = clamp(1.0 - abs(sunPosition.y) * 0.015, 0.0, 1.0);
            vec3 rayColor = mix(vec3(1.0, 0.95, 0.82), vec3(1.0, 0.65, 0.35), sunWarmth);
            float rayIntensity = clamp((rayDensity / 12.0) * 0.28 * (1.0 - clamp(rayDist * 0.6, 0.0, 1.0)), 0.0, 0.35);
            sceneColor += rayColor * rayIntensity;
        }
    }
    #endif

    vec3 bloom = vec3(0.0);

    #if BLOOM == 1
    // Medium 4-tap bloom
    bloom += texture2D(colortex1, texcoord + vec2(-2.0, -2.0) * pixelSize * 2.2).rgb * 0.25;
    bloom += texture2D(colortex1, texcoord + vec2( 2.0, -2.0) * pixelSize * 2.2).rgb * 0.25;
    bloom += texture2D(colortex1, texcoord + vec2(-2.0,  2.0) * pixelSize * 2.2).rgb * 0.25;
    bloom += texture2D(colortex1, texcoord + vec2( 2.0,  2.0) * pixelSize * 2.2).rgb * 0.25;
    #endif

    #if BLOOM == 2
    // High 12-tap multi-scale Gaussian bloom
    bloom += texture2D(colortex1, texcoord + vec2(-1.5, -1.5) * pixelSize * 2.0).rgb * 0.08;
    bloom += texture2D(colortex1, texcoord + vec2( 1.5, -1.5) * pixelSize * 2.0).rgb * 0.08;
    bloom += texture2D(colortex1, texcoord + vec2(-1.5,  1.5) * pixelSize * 2.0).rgb * 0.08;
    bloom += texture2D(colortex1, texcoord + vec2( 1.5,  1.5) * pixelSize * 2.0).rgb * 0.08;
    bloom += texture2D(colortex1, texcoord + vec2(-3.0,  0.0) * pixelSize * 2.0).rgb * 0.08;
    bloom += texture2D(colortex1, texcoord + vec2( 3.0,  0.0) * pixelSize * 2.0).rgb * 0.08;
    bloom += texture2D(colortex1, texcoord + vec2( 0.0, -3.0) * pixelSize * 2.0).rgb * 0.08;
    bloom += texture2D(colortex1, texcoord + vec2( 0.0,  3.0) * pixelSize * 2.0).rgb * 0.08;

    bloom += texture2D(colortex1, texcoord + vec2(-5.0, -5.0) * pixelSize * 3.8).rgb * 0.09;
    bloom += texture2D(colortex1, texcoord + vec2( 5.0, -5.0) * pixelSize * 3.8).rgb * 0.09;
    bloom += texture2D(colortex1, texcoord + vec2(-5.0,  5.0) * pixelSize * 3.8).rgb * 0.09;
    bloom += texture2D(colortex1, texcoord + vec2( 5.0,  5.0) * pixelSize * 3.8).rgb * 0.09;
    #endif

    // Composite scene with bloom
    vec3 color = sceneColor + bloom * 0.70;

    // ACES Filmic Tonemapping
    vec3 graded = acesFilm(color * 1.06);

    // Color Grading: Tech contrast and vibrance
    float luma = dot(graded, vec3(0.299, 0.587, 0.114));
    graded = mix(vec3(luma), graded, 1.12);

    #ifdef VIGNETTE
    vec2 uvc = texcoord - 0.5;
    float vig = clamp(1.0 - dot(uvc, uvc) * 0.38, 0.82, 1.0);
    graded *= vig;
    #endif

    gl_FragData[0] = vec4(graded, baseColor.a);
}

