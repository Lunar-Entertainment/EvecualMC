#version 120

/*
 * Evecual Tech Shader - Composite Pass (Next-Gen Cyber-Tech Engine v2.0)
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
#define TECH_CONTRAST 1.08    // [0.90 1.00 1.08 1.15]
#define TECH_VIBRANCE 1.15    // [0.90 1.00 1.15 1.25]
#define ATMOSPHERIC_FOG       // [true false]

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

// ACES Filmic Tonemapping Curve (Cinematic Academy Color Encoding System)
vec3 acesFilm(vec3 x) {
    float a = 2.51;
    float b = 0.03;
    float c = 2.43;
    float d = 0.59;
    float e = 0.14;
    return clamp((x * (a * x + b)) / (x * (c * x + d) + e), 0.0, 1.0);
}

// Procedural 2D noise for organic drifting clouds and atmospheric turbulence
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
    v += 0.125 * noise(p); p *= 2.01;
    v += 0.062 * noise(p);
    return v;
}

// Linearize depth buffer
float linearizeDepth(float d) {
    return (2.0 * 0.1) / (120.0 + 0.1 - d * (120.0 - 0.1));
}

void main() {
    float depth = texture2D(depthtex0, texcoord).r;
    vec2 pixelSize = vec2(1.0 / max(viewWidth, 1.0), 1.0 / max(viewHeight, 1.0));

    vec4 baseColor = texture2D(colortex0, texcoord);

    #ifdef CHROMATIC_ABERRATION
    // Subtle chromatic dispersion towards screen borders
    vec2 distFromCenter = texcoord - 0.5;
    float caDist = dot(distFromCenter, distFromCenter) * 0.0032;
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
            vec2 velocity = (texcoord - prevCoord) * 0.35;
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
    // High-Fidelity 16-sample Depth-Aware Screen Space Ambient Occlusion
    if (depth < 0.999) {
        float centerLinDepth = linearizeDepth(depth);
        float totalAO = 0.0;

        float d1  = linearizeDepth(texture2D(depthtex0, clamp(texcoord + vec2(-1.5,  0.5) * pixelSize * 2.5, 0.0, 1.0)).r);
        float d2  = linearizeDepth(texture2D(depthtex0, clamp(texcoord + vec2( 1.5, -0.5) * pixelSize * 2.5, 0.0, 1.0)).r);
        float d3  = linearizeDepth(texture2D(depthtex0, clamp(texcoord + vec2( 0.5,  1.5) * pixelSize * 2.5, 0.0, 1.0)).r);
        float d4  = linearizeDepth(texture2D(depthtex0, clamp(texcoord + vec2(-0.5, -1.5) * pixelSize * 2.5, 0.0, 1.0)).r);
        float d5  = linearizeDepth(texture2D(depthtex0, clamp(texcoord + vec2(-2.5, -2.5) * pixelSize * 2.5, 0.0, 1.0)).r);
        float d6  = linearizeDepth(texture2D(depthtex0, clamp(texcoord + vec2( 2.5,  2.5) * pixelSize * 2.5, 0.0, 1.0)).r);
        float d7  = linearizeDepth(texture2D(depthtex0, clamp(texcoord + vec2( 3.0, -1.0) * pixelSize * 2.5, 0.0, 1.0)).r);
        float d8  = linearizeDepth(texture2D(depthtex0, clamp(texcoord + vec2(-3.0,  1.0) * pixelSize * 2.5, 0.0, 1.0)).r);
        float d9  = linearizeDepth(texture2D(depthtex0, clamp(texcoord + vec2(-1.0,  3.0) * pixelSize * 3.2, 0.0, 1.0)).r);
        float d10 = linearizeDepth(texture2D(depthtex0, clamp(texcoord + vec2( 1.0, -3.0) * pixelSize * 3.2, 0.0, 1.0)).r);
        float d11 = linearizeDepth(texture2D(depthtex0, clamp(texcoord + vec2(-3.5, -1.5) * pixelSize * 3.2, 0.0, 1.0)).r);
        float d12 = linearizeDepth(texture2D(depthtex0, clamp(texcoord + vec2( 3.5,  1.5) * pixelSize * 3.2, 0.0, 1.0)).r);

        if (centerLinDepth - d1  > 0.00015 && centerLinDepth - d1  < 0.016) totalAO += 1.0;
        if (centerLinDepth - d2  > 0.00015 && centerLinDepth - d2  < 0.016) totalAO += 1.0;
        if (centerLinDepth - d3  > 0.00015 && centerLinDepth - d3  < 0.016) totalAO += 1.0;
        if (centerLinDepth - d4  > 0.00015 && centerLinDepth - d4  < 0.016) totalAO += 1.0;
        if (centerLinDepth - d5  > 0.00015 && centerLinDepth - d5  < 0.016) totalAO += 1.0;
        if (centerLinDepth - d6  > 0.00015 && centerLinDepth - d6  < 0.016) totalAO += 1.0;
        if (centerLinDepth - d7  > 0.00015 && centerLinDepth - d7  < 0.016) totalAO += 1.0;
        if (centerLinDepth - d8  > 0.00015 && centerLinDepth - d8  < 0.016) totalAO += 1.0;
        if (centerLinDepth - d9  > 0.00015 && centerLinDepth - d9  < 0.016) totalAO += 0.8;
        if (centerLinDepth - d10 > 0.00015 && centerLinDepth - d10 < 0.016) totalAO += 0.8;
        if (centerLinDepth - d11 > 0.00015 && centerLinDepth - d11 < 0.016) totalAO += 0.8;
        if (centerLinDepth - d12 > 0.00015 && centerLinDepth - d12 < 0.016) totalAO += 0.8;

        float aoFactor = clamp(1.0 - (totalAO / 11.2) * 0.42, 0.58, 1.0);
        sceneColor *= aoFactor;
    }
    #endif

    #ifdef CLOUDS
    // Procedural multi-layer drifting volumetric clouds in the sky
    if (depth >= 0.9999) {
        vec2 uv = (texcoord - 0.5) * vec2(viewWidth / max(viewHeight, 1.0), 1.0);
        float time = frameTimeCounter * 0.014;
        vec2 cloudCoord = uv * 2.8 + vec2(time, time * 0.35);

        float cloudDensity = fbm(cloudCoord);
        float cloudMask = smoothstep(0.38, 0.76, cloudDensity);

        if (cloudMask > 0.01) {
            float cloudShading = smoothstep(0.40, 0.84, fbm(cloudCoord + vec2(0.02, 0.04)));
            vec3 sunDir = normalize(sunPosition);
            float sunFacing = max(dot(vec3(uv, 0.5), sunDir), 0.0);
            vec3 silverLining = vec3(1.0, 0.95, 0.85) * pow(sunFacing, 8.0) * 0.45;

            vec3 cloudCol = mix(vec3(0.80, 0.86, 0.96), vec3(1.00, 1.00, 1.00), cloudShading) + silverLining;
            sceneColor = mix(sceneColor, cloudCol, cloudMask * 0.92);
        }
    }
    #endif

    #ifdef VOLUMETRIC_RAYS
    // Sun & Moon Volumetric Crepuscular Light Shafts (God Rays)
    vec4 sunClip = gbufferProjection * vec4(sunPosition, 1.0);
    if (sunClip.w > 0.0) {
        vec2 sunScreen = (sunClip.xy / sunClip.w) * 0.5 + 0.5;
        if (sunScreen.x >= -0.4 && sunScreen.x <= 1.4 && sunScreen.y >= -0.4 && sunScreen.y <= 1.4) {
            vec2 rayDir = (sunScreen - texcoord);
            float rayDist = length(rayDir);
            vec2 stepUV = rayDir / 14.0;
            float rayDensity = 0.0;
            vec2 currentUV = texcoord;

            for (int r = 0; r < 14; r++) {
                currentUV += stepUV;
                if (currentUV.x < 0.0 || currentUV.x > 1.0 || currentUV.y < 0.0 || currentUV.y > 1.0) break;
                float sampleD = texture2D(depthtex0, currentUV).r;
                if (sampleD >= 0.9999) {
                    rayDensity += (1.0 - float(r) / 14.0);
                }
            }

            float sunElev = normalize(sunPosition).y;
            vec3 rayColor;
            if (sunElev > 0.15) {
                rayColor = vec3(1.00, 0.96, 0.84); // Bright Golden Sunlight
            } else if (sunElev > -0.10) {
                rayColor = vec3(1.00, 0.62, 0.30); // Warm Sunset/Sunrise Ray
            } else {
                rayColor = vec3(0.45, 0.65, 1.00); // Cool Moonlight Ray
            }

            float rayIntensity = clamp((rayDensity / 14.0) * 0.32 * (1.0 - clamp(rayDist * 0.55, 0.0, 1.0)), 0.0, 0.40);
            sceneColor += rayColor * rayIntensity;
        }
    }
    #endif

    #ifdef ATMOSPHERIC_FOG
    // Depth-aware atmospheric Rayleigh/Mie horizon fog
    if (depth < 0.9999) {
        float linD = linearizeDepth(depth);
        float fogFactor = clamp(pow(linD * 2.8, 1.8), 0.0, 0.65);
        vec3 horizonColor = mix(vec3(0.68, 0.82, 0.98), vec3(0.95, 0.70, 0.45), clamp(1.0 - abs(normalize(sunPosition).y) * 4.0, 0.0, 1.0));
        sceneColor = mix(sceneColor, horizonColor, fogFactor);
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
    // High 16-tap multi-scale Gaussian bloom with anamorphic glare
    bloom += texture2D(colortex1, texcoord + vec2(-1.5, -1.5) * pixelSize * 1.8).rgb * 0.07;
    bloom += texture2D(colortex1, texcoord + vec2( 1.5, -1.5) * pixelSize * 1.8).rgb * 0.07;
    bloom += texture2D(colortex1, texcoord + vec2(-1.5,  1.5) * pixelSize * 1.8).rgb * 0.07;
    bloom += texture2D(colortex1, texcoord + vec2( 1.5,  1.5) * pixelSize * 1.8).rgb * 0.07;
    bloom += texture2D(colortex1, texcoord + vec2(-3.0,  0.0) * pixelSize * 1.8).rgb * 0.08;
    bloom += texture2D(colortex1, texcoord + vec2( 3.0,  0.0) * pixelSize * 1.8).rgb * 0.08;
    bloom += texture2D(colortex1, texcoord + vec2( 0.0, -3.0) * pixelSize * 1.8).rgb * 0.08;
    bloom += texture2D(colortex1, texcoord + vec2( 0.0,  3.0) * pixelSize * 1.8).rgb * 0.08;

    bloom += texture2D(colortex1, texcoord + vec2(-5.0, -5.0) * pixelSize * 3.5).rgb * 0.08;
    bloom += texture2D(colortex1, texcoord + vec2( 5.0, -5.0) * pixelSize * 3.5).rgb * 0.08;
    bloom += texture2D(colortex1, texcoord + vec2(-5.0,  5.0) * pixelSize * 3.5).rgb * 0.08;
    bloom += texture2D(colortex1, texcoord + vec2( 5.0,  5.0) * pixelSize * 3.5).rgb * 0.08;

    // Horizontal anamorphic sci-fi bloom flare
    bloom += texture2D(colortex1, texcoord + vec2(-8.0, 0.0) * pixelSize * 4.0).rgb * 0.06;
    bloom += texture2D(colortex1, texcoord + vec2( 8.0, 0.0) * pixelSize * 4.0).rgb * 0.06;
    #endif

    // Composite scene with bloom
    vec3 color = sceneColor + bloom * 0.75;

    // ACES Filmic Tonemapping
    vec3 graded = acesFilm(color * 1.08);

    // Color Grading: Tech contrast and vibrance
    float luma = dot(graded, vec3(0.299, 0.587, 0.114));
    graded = mix(vec3(luma), graded, TECH_VIBRANCE);

    #ifdef VIGNETTE
    vec2 uvc = texcoord - 0.5;
    float vig = clamp(1.0 - dot(uvc, uvc) * 0.35, 0.84, 1.0);
    graded *= vig;
    #endif

    gl_FragData[0] = vec4(graded, baseColor.a);
}


