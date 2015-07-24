#version 330

layout(location = 0) in vec3 position;

uniform mat4 worldToClipMatrix;
uniform mat4 modelToWorldMatrix;

void main()
{
	vec4 worldCoords = modelToWorldMatrix * vec4(position.xyz, 1);
	gl_Position = worldToClipMatrix * worldCoords;
}
