#version 330

layout(location = 0) in vec3 position;
layout(location = 1) in vec3 normal;

uniform mat4 modelToWorld;
uniform mat4 worldToCamera;
uniform mat4 cameraToClip;

uniform vec4 pointLightPos;
uniform vec4 dirLightDir;

out float pointLight;
out float dirLight;

void main()
{
	vec4 positionWorldSpace = modelToWorld * vec4(position.xyz, 1);
	vec4 positionCamSpace = worldToCamera * positionWorldSpace;
	gl_Position = cameraToClip * positionCamSpace;

	vec4 dirToLight = normalize(pointLightPos - positionWorldSpace);
	pointLight = dot(vec4(normal.xyz, 0.0), dirToLight);
	pointLight = clamp(pointLight, 0, 1);

	dirLight = dot(vec4(normal, 0.0), dirLightDir);
	dirLight = clamp(dirLight, 0, 1);
}
