#version 120

uniform sampler2D textureIn;
uniform vec2 texelSize, direction;
uniform float radius, weights[256];

#define offset texelSize * direction

void main() {
    vec3 color = texture2D(textureIn, gl_TexCoord[0].st).rgb * weights[0];
    float totalWeight = weights[0];

    for (float f = 1.0; f <= radius; f++) {
        color += texture2D(textureIn, gl_TexCoord[0].st + f * offset).rgb * (weights[int(abs(f))]);
        color += texture2D(textureIn, gl_TexCoord[0].st - f * offset).rgb * (weights[int(abs(f))]);

        totalWeight += (weights[int(abs(f))]) * 2.0;
    }

    gl_FragColor = vec4(color / totalWeight, 1.0);
}
