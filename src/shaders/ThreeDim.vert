#version 330

layout(location = 0) in vec3 position;
layout(location = 1) in vec3 normal;

uniform mat4 modelToWorld;
uniform mat4 worldToCamera;
uniform mat4 cameraToClip;

out vec4 pos_interp;
out vec4 normal_interp;
flat out vec4 normal_flat;

void main()
{
	vec4 positionWorldSpace = modelToWorld * vec4(position.xyz, 1);
	vec4 positionCamSpace = worldToCamera * positionWorldSpace;
	gl_Position = cameraToClip * positionCamSpace;

	pos_interp = positionWorldSpace;
	normal_interp = vec4(normal, 0.0);
	normal_flat = vec4(normal, 0.0);
}
