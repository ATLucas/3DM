#version 330

layout(location = 0) in vec4 position;

uniform mat4 orthogonalMatrix;

void main()
{
	gl_Position = orthogonalMatrix * position;
}
