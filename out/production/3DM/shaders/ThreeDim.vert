#version 330

layout(location = 0) in vec3 position;
layout(location = 1) in vec3 normal;

uniform mat4 worldToClipMatrix;
uniform mat4 modelToWorldMatrix;

smooth out vec3 normal_interp;

void main()
{
	vec4 worldCoords = modelToWorldMatrix * vec4(position.xyz, 1);
	gl_Position = worldToClipMatrix * worldCoords;

	normal_interp = normal;
}
