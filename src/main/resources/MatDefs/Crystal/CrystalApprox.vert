#import "Common/ShaderLib/GLSLCompat.glsllib"

attribute vec3 inPosition;
attribute vec3 inNormal;

uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldMatrix;

varying vec3 crystalWorldPosition;
varying vec3 crystalWorldNormal;

void main() {
    vec4 modelPosition = vec4(inPosition, 1.0);
    crystalWorldPosition = (g_WorldMatrix * modelPosition).xyz;
    crystalWorldNormal = normalize((g_WorldMatrix * vec4(inNormal, 0.0)).xyz);
    gl_Position = g_WorldViewProjectionMatrix * modelPosition;
}
