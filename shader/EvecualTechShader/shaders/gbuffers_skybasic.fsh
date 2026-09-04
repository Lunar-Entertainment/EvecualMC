#version 120

varying vec4 color;

/* DRAWBUFFERS:01 */

void main() {
    gl_FragData[0] = color;
    gl_FragData[1] = vec4(0.0, 0.0, 0.0, 0.0); // NO bloom on sky to prevent blown-out atmosphere
}
