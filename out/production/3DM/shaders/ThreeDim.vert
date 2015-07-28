#version 330 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec3 normal;

uniform mat4 modelToWorld;
uniform mat4 worldToCamera;
uniform mat4 cameraToClip;

out VS_OUT {
    vec4 position;
    vec4 normal;
} vs_out;

void main()
{
	vec4 normalWorldSpace = modelToWorld * vec4(normal.xyz, 0);
	vec4 positionWorldSpace = modelToWorld * vec4(position.xyz, 1);
	vec4 positionCamSpace = worldToCamera * positionWorldSpace;
	gl_Position = cameraToClip * positionCamSpace;

	vs_out.position = positionWorldSpace;
	vs_out.normal = normalWorldSpace;
}
