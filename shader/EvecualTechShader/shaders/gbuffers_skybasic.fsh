#version 120

varying vec4 color;
varying vec3 skyPos;

/* DRAWBUFFERS:01 */

void main() {
    vec3 npos = normalize(skyPos);
    float h = clamp(npos.y, 0.0, 1.0);

    // Deep modern zenith blue transitioning into soft atmospheric haze
    vec3 zenith = mix(vec3(0.12, 0.38, 0.82), color.rgb, 0.55);
    vec3 midSky = mix(vec3(0.35, 0.65, 0.95), color.rgb, 0.70);
    vec3 horizon = mix(vec3(0.70, 0.85, 1.00), color.rgb, 0.85);

    vec3 skyGrad = mix(horizon, midSky, clamp(h * 2.5, 0.0, 1.0));
    skyGrad = mix(skyGrad, zenith, pow(h, 0.8));

    gl_FragData[0] = vec4(skyGrad, 1.0);
    gl_FragData[1] = vec4(0.0, 0.0, 0.0, 0.0);
}
