#version 330

layout(location = 0) in vec3 position;
layout(location = 1) in vec3 normal;

uniform mat4 modelToWorld;
uniform mat4 worldToCamera;
uniform mat4 cameraToClip;

void main()
{
	vec4 positionWorldSpace = modelToWorld * vec4(position.xyz, 1);
	vec4 positionCamSpace = worldToCamera * positionWorldSpace;
	gl_Position = cameraToClip * positionCamSpace;
}
