#version 120

uniform vec2 location, rectSize;
uniform vec4 color;
uniform float radius;
uniform bool blur;

float roundSDF(vec2 p, vec2 b, float r) {
	return length(max(abs(p) - b, 0.0)) - r;
}

void main() {
	vec2 rectHalf = rectSize * .5;
	float smoothedAlpha =  (1.0-smoothstep(0.0, 1.0, roundSDF(rectHalf - (gl_TexCoord[0].st * rectSize), rectHalf - radius - 1., radius))) * color.a;

	gl_FragColor = vec4(color.rgb, smoothedAlpha);
}