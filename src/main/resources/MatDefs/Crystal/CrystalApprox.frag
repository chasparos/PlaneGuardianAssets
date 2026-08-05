#import "Common/ShaderLib/GLSLCompat.glsllib"

uniform vec4 m_BaseColor;
uniform vec4 m_Emissive;
uniform float m_EmissionStrength;
uniform float m_CrystalOpacity;
uniform float m_CrystalFresnel;
uniform float m_CrystalRefraction;
uniform float m_CrystalNoiseScale;
uniform float m_CrystalNoiseStrength;
uniform float m_CrystalGlintStrength;
uniform float m_CrystalGlintPower;
uniform vec3 g_CameraPosition;

varying vec3 crystalWorldPosition;
varying vec3 crystalWorldNormal;

float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float cellular(vec2 p) {
    vec2 cell = floor(p);
    vec2 local = fract(p);
    float nearest = 1.0;
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 offset = vec2(float(x), float(y));
            vec2 point = vec2(hash21(cell + offset), hash21(cell + offset + 17.0));
            nearest = min(nearest, length(offset + point - local));
        }
    }
    return nearest;
}

void main() {
    vec3 normal = normalize(crystalWorldNormal);
    vec3 viewDirection = normalize(g_CameraPosition - crystalWorldPosition);
    float facing = max(dot(normal, viewDirection), 0.0);
    float edge = pow(1.0 - facing, max(m_CrystalFresnel, 0.01));

    // Cellular breakup is a cheap substitute for internal light paths.
    float cells = cellular(crystalWorldPosition.xy * max(m_CrystalNoiseScale, 0.01));
    float internalLight = 1.0 - m_CrystalNoiseStrength * cells;
    vec3 refractedTint = m_BaseColor.rgb * (internalLight + m_CrystalRefraction * edge);

    // A view-dependent glint makes silhouette edges catch light without a probe.
    float glint = pow(edge, max(m_CrystalGlintPower, 0.01)) * m_CrystalGlintStrength;
    vec3 color = refractedTint + m_Emissive.rgb * m_EmissionStrength + m_BaseColor.rgb * glint;
    gl_FragColor = vec4(color, m_CrystalOpacity * m_BaseColor.a);
}
