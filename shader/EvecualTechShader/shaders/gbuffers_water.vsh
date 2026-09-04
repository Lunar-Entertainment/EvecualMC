#version 120

#define WAVING_WATER

uniform float frameTimeCounter;
uniform vec3 cameraPosition;
uniform mat4 gbufferModelViewInverse;

varying vec4 color;
varying vec2 texcoord;
varying vec2 lmcoord;
varying vec3 normal;
varying vec3 worldPos;

void main() {
    texcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
    lmcoord  = (gl_TextureMatrix[1] * gl_MultiTexCoord1).xy;
    color    = gl_Color;
    normal   = normalize(gl_NormalMatrix * gl_Normal);

    vec4 localPos = gl_Vertex;

    #ifdef WAVING_WATER
    vec3 worldCoord = (gbufferModelViewInverse * (gl_ModelViewMatrix * localPos)).xyz + cameraPosition;
    float time = frameTimeCounter * 1.8;
    float wave = sin(time * 1.2 + worldCoord.x * 0.8 + worldCoord.z * 0.6) * 0.04 +
                 cos(time * 0.9 + worldCoord.x * 0.5 - worldCoord.z * 0.7) * 0.03;
    localPos.y += wave;
    #endif

    worldPos = (gbufferModelViewInverse * (gl_ModelViewMatrix * localPos)).xyz;
    gl_Position = gl_ModelViewProjectionMatrix * localPos;
}
