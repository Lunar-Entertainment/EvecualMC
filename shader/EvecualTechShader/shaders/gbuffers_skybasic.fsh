#version 120

uniform vec3 sunPosition;
uniform vec3 upPosition;

varying vec4 color;
varying vec3 skyPos;

/* DRAWBUFFERS:01 */

void main() {
    vec3 npos = normalize(skyPos);
    vec3 upDir = normalize(upPosition);
    vec3 sunDir = normalize(sunPosition);

    // Altitude above horizon based on world-invariant upward zenith vector
    float h = clamp(dot(npos, upDir), 0.0, 1.0);

    // True sun elevation (altitude above horizon) invariant to camera looking angle
    float sunElev = dot(sunDir, upDir);

    // Daytime sky colors (Requested 1-minute twilight aesthetic: dreamy pastel violet -> orchid rose -> warm golden peach)
    vec3 targetDayZenith  = vec3(0.46, 0.38, 0.62); // Dreamy pastel violet / twilight purple
    vec3 targetDayMidSky  = vec3(0.75, 0.45, 0.60); // Radiant soft orchid / rose twilight
    vec3 targetDayHorizon = vec3(0.97, 0.68, 0.52); // Warm peach / sunset golden blush

    vec3 dayZenith  = mix(targetDayZenith, color.rgb * 0.7 + targetDayZenith * 0.3, 0.10);
    vec3 dayMidSky  = mix(targetDayMidSky, color.rgb * 0.7 + targetDayMidSky * 0.3, 0.10);
    vec3 dayHorizon = mix(targetDayHorizon, color.rgb * 0.7 + targetDayHorizon * 0.3, 0.10);

    // Sunset / Sunrise warm atmospheric scattering
    vec3 sunsetZenith  = vec3(0.28, 0.20, 0.46);
    vec3 sunsetMidSky  = vec3(0.85, 0.42, 0.45);
    vec3 sunsetHorizon = vec3(1.00, 0.64, 0.35);

    // Night sky
    vec3 nightZenith  = vec3(0.02, 0.03, 0.08);
    vec3 nightHorizon = vec3(0.05, 0.08, 0.16);

    // Blend based on true sun elevation
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

    // Sun disk & solar corona at true celestial sun position
    float sunDot = max(dot(npos, sunDir), 0.0);
    float sunDisk = pow(sunDot, 1024.0) * 8.0;
    float sunCorona = pow(sunDot, 32.0) * 0.45 * (1.0 - nightFactor);
    vec3 sunLight = (sunsetFactor > 0.3 ? vec3(1.0, 0.7, 0.4) : vec3(1.0, 0.90, 0.78)) * (sunDisk + sunCorona);

    // Moon corona in opposite direction
    float moonDot = max(dot(npos, -sunDir), 0.0);
    float moonCorona = pow(moonDot, 48.0) * 0.25 * nightFactor;
    vec3 moonLight = vec3(0.55, 0.75, 1.00) * moonCorona;

    vec3 finalSky = skyGrad + sunLight + moonLight;

    gl_FragData[0] = vec4(finalSky, 1.0);
    gl_FragData[1] = vec4(0.0, 0.0, 0.0, 0.0);
}
