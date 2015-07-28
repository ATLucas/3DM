#version 330 core

layout (lines) in;
layout (triangle_strip, max_vertices = 12) out;

void main() {
	vec4 dv = gl_in[1].gl_Position - gl_in[0].gl_Position;
	float factor = 0.05 * length(dv);

	gl_Position = gl_in[0].gl_Position;
	EmitVertex();
	gl_Position = gl_in[0].gl_Position + dv / 4 + vec4(0,factor,0,0);
	EmitVertex();
	gl_Position = gl_in[0].gl_Position + dv / 4 - vec4(0,factor,0,0);
	EmitVertex();
	gl_Position = gl_in[1].gl_Position;
    EmitVertex();

	gl_Position = gl_in[0].gl_Position;
	EmitVertex();
	gl_Position = gl_in[0].gl_Position + dv / 4 + vec4(factor,0,0,0);
	EmitVertex();
	gl_Position = gl_in[0].gl_Position + dv / 4 - vec4(factor,0,0,0);
	EmitVertex();
	gl_Position = gl_in[1].gl_Position;
	EmitVertex();

	gl_Position = gl_in[0].gl_Position;
	EmitVertex();
	gl_Position = gl_in[0].gl_Position + dv / 4 + vec4(0,0,factor,0);
	EmitVertex();
	gl_Position = gl_in[0].gl_Position + dv / 4 - vec4(0,0,factor,0);
	EmitVertex();
	gl_Position = gl_in[1].gl_Position;
	EmitVertex();
	EndPrimitive();
}