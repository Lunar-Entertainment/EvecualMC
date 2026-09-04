#version 120

#define WAVING_PLANTS

attribute vec4 mc_Entity;
attribute vec4 mc_midTexCoord;

uniform float frameTimeCounter;
uniform vec3 cameraPosition;
uniform mat4 gbufferModelViewInverse;

varying vec4 color;
varying vec2 texcoord;
varying vec2 lmcoord;
varying vec3 normal;
varying float blockId;
varying vec2 faceUV;
varying vec3 worldPos;

void main() {
    texcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
    lmcoord  = (gl_TextureMatrix[1] * gl_MultiTexCoord1).xy;
    color    = gl_Color;
    normal   = normalize(gl_NormalMatrix * gl_Normal);
    blockId  = mc_Entity.x;

    vec2 halfSize = abs(gl_MultiTexCoord0.st - mc_midTexCoord.st);
    if (halfSize.x > 0.00001 && halfSize.y > 0.00001) {
        faceUV = (gl_MultiTexCoord0.st - mc_midTexCoord.st) / halfSize;
    } else {
        faceUV = vec2(0.0);
    }

    vec4 localPos = gl_Vertex;

    #ifdef WAVING_PLANTS
    // Check if foliage / leaves / crops (Block ID 10006)
    if (abs(blockId - 10006.0) < 0.5) {
        // Wind wave based on world position and time
        vec3 worldCoord = (gbufferModelViewInverse * (gl_ModelViewMatrix * localPos)).xyz + cameraPosition;
        float time = frameTimeCounter * 2.2;
        float wave = sin(time + worldCoord.x * 0.75 + worldCoord.z * 0.5) * cos(time * 0.7 + worldCoord.z * 0.6);

        // Displace top of plants / foliage
        if (gl_MultiTexCoord0.t < mc_midTexCoord.t) {
            localPos.x += wave * 0.08;
            localPos.z += wave * 0.06;
        }
    }
    #endif

    worldPos = (gbufferModelViewInverse * (gl_ModelViewMatrix * localPos)).xyz;
    gl_Position = gl_ModelViewProjectionMatrix * localPos;
}
