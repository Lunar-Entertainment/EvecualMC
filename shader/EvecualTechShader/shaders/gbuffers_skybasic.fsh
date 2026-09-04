#version 120

uniform vec3 sunPosition;
uniform vec3 upPosition;

varying vec4 color;
varying vec3 skyPos;

/* DRAWBUFFERS:01 */

void main() {
    vec3 npos = normalize(skyPos);
    float h = clamp(npos.y, 0.0, 1.0);

    // Sun altitude for Rayleigh atmospheric time-of-day coloring
    vec3 sunDir = normalize(sunPosition);
    float sunElev = sunDir.y;

    // Daytime sky colors
    vec3 dayZenith  = mix(vec3(0.08, 0.35, 0.88), color.rgb, 0.45);
    vec3 dayMidSky  = mix(vec3(0.30, 0.65, 0.98), color.rgb, 0.65);
    vec3 dayHorizon = mix(vec3(0.68, 0.86, 1.00), color.rgb, 0.80);

    // Sunset / Sunrise warm atmospheric scattering
    vec3 sunsetZenith  = vec3(0.12, 0.16, 0.38);
    vec3 sunsetMidSky  = vec3(0.85, 0.42, 0.28);
    vec3 sunsetHorizon = vec3(1.00, 0.68, 0.32);

    // Night sky
    vec3 nightZenith  = vec3(0.02, 0.03, 0.08);
    vec3 nightHorizon = vec3(0.05, 0.08, 0.16);

    // Blend based on sun elevation
    float sunsetFactor = clamp(1.0 - abs(sunElev) * 3.5, 0.0, 1.0);
    float nightFactor  = clamp(-sunElev * 3.0, 0.0, 1.0);

    vec3 skyZenith  = mix(dayZenith, sunsetZenith, sunsetFactor);
    skyZenith       = mix(skyZenith, nightZenith, nightFactor);

    vec3 skyMidSky  = mix(dayMidSky, sunsetMidSky, sunsetFactor);
    skyMidSky       = mix(skyMidSky, nightHorizon, nightFactor);

    vec3 skyHorizon = mix(dayHorizon, sunsetHorizon, sunsetFactor);
    skyHorizon      = mix(skyHorizon, nightHorizon, nightFactor);

    vec3 skyGrad = mix(skyHorizon, skyMidSky, clamp(h * 2.2, 0.0, 1.0));
    skyGrad = mix(skyGrad, skyZenith, pow(h, 0.75));

    // Sun disk & lunar glow
    float sunDot = max(dot(npos, sunDir), 0.0);
    float sunDisk = pow(sunDot, 1024.0) * 8.0;
    float sunCorona = pow(sunDot, 32.0) * 0.45 * (1.0 - nightFactor);
    vec3 sunLight = (sunsetFactor > 0.3 ? vec3(1.0, 0.7, 0.4) : vec3(1.0, 0.98, 0.85)) * (sunDisk + sunCorona);

    vec3 finalSky = skyGrad + sunLight;

    gl_FragData[0] = vec4(finalSky, 1.0);
    gl_FragData[1] = vec4(0.0, 0.0, 0.0, 0.0);
}
