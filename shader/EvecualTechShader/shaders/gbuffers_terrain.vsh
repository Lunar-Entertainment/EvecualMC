#version 120

attribute vec4 mc_Entity;
attribute vec4 mc_midTexCoord;

varying vec4 color;
varying vec2 texcoord;
varying vec2 lmcoord;
varying vec3 normal;
varying float blockId;
varying vec2 faceUV;

void main() {
    gl_Position = ftransform();
    texcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
    lmcoord  = (gl_TextureMatrix[1] * gl_MultiTexCoord1).xy;
    color = gl_Color;
    normal = normalize(gl_NormalMatrix * gl_Normal);
    blockId = mc_Entity.x;

    vec2 halfSize = abs(gl_MultiTexCoord0.st - mc_midTexCoord.st);
    if (halfSize.x > 0.00001 && halfSize.y > 0.00001) {
        faceUV = (gl_MultiTexCoord0.st - mc_midTexCoord.st) / halfSize;
    } else {
        faceUV = vec2(0.0);
    }
}
