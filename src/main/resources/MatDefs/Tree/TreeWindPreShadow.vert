#import "Common/ShaderLib/GLSLCompat.glsllib"
#import "Common/ShaderLib/Instancing.glsllib"
#import "Common/ShaderLib/Skinning.glsllib"
#import "Common/ShaderLib/MorphAnim.glsllib"

attribute vec3 inPosition;
attribute vec2 inTexCoord;

uniform float m_WindWeight;
uniform float m_WindPhase;
uniform float m_WindFrequency;
uniform vec3 m_WindDirection;
uniform float m_WindIntensity;
uniform float m_WindTime;

varying vec2 texCoord;

void main(){
    vec4 modelSpacePos = vec4(inPosition, 1.0);

   #ifdef NUM_MORPH_TARGETS
           Morph_Compute(modelSpacePos);
   #endif

   #ifdef NUM_BONES
       Skinning_Compute(modelSpacePos);
   #endif
    float wind = sin(m_WindTime * m_WindFrequency + m_WindPhase * 6.28318530718)
            * m_WindIntensity * m_WindWeight;
    modelSpacePos.xyz += m_WindDirection * wind;
    gl_Position = TransformWorldViewProjection(modelSpacePos);
    texCoord = inTexCoord;
}
