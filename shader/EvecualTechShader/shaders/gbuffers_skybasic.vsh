#version 120

varying vec4 color;
varying vec3 skyPos;

void main() {
    gl_Position = ftransform();
    color = gl_Color;
    skyPos = (gl_ModelViewMatrix * gl_Vertex).xyz;
}
