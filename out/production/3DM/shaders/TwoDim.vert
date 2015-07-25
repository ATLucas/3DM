#version 330

layout(location = 0) in vec3 position;

uniform mat4 orthogonalMatrix;

void main()
{
	gl_Position = orthogonalMatrix * vec4(position.xyz, 1.0);
}
