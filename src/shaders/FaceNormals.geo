#version 330 core

layout (triangles) in;
layout (triangle_strip, max_vertices = 3) out;

in VS_OUT {
    vec4 position;
    vec4 normal;
} gs_in[];

out vec4 position;
out vec4 normal;
out vec4 normal_interp;

void main() {
	normal = normalize(vec4(cross(gs_in[2].position.xyz-gs_in[0].position.xyz,
		gs_in[1].position.xyz-gs_in[0].position.xyz), 1));

	gl_Position = gl_in[0].gl_Position;
	position = gs_in[0].position;
	normal_interp = gs_in[0].normal;
	EmitVertex();
	gl_Position = gl_in[1].gl_Position;
	position = gs_in[1].position;
	normal_interp = gs_in[1].normal;
	EmitVertex();
	gl_Position = gl_in[2].gl_Position;
	position = gs_in[2].position;
	normal_interp = gs_in[2].normal;
	EmitVertex();
	EndPrimitive();
}