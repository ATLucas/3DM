#version 330

layout(location = 0) in vec3 position;
layout(location = 1) in vec3 normal;

uniform mat4 modelToWorld;
uniform mat4 worldToCamera;
uniform mat4 cameraToClip;

smooth out vec4 normCamSpace;

void main()
{
	vec4 worldCoords = modelToWorld * vec4(position.xyz, 1);
	gl_Position = cameraToClip * worldToCamera * worldCoords;

	normCamSpace = normalize(worldToCamera * vec4(normal.xyz, 0.0));
}
