#version 330

layout(location = 0) in vec3 position;
layout(location = 1) in vec3 normal;

uniform mat4 projectionViewMatrix;
uniform mat4 modelMatrix;
uniform vec3 ambientLight;
uniform vec3 dirToLight;
uniform vec3 lightIntensity;

smooth out vec3 normal_interp;

void main()
{
	vec4 worldCoords = modelMatrix * vec4(position.xyz, 1);
	gl_Position = projectionViewMatrix * worldCoords;

	float cosAngIncidence = dot(normal, dirToLight);
	cosAngIncidence = clamp(cosAngIncidence, 0, 1);
    normal_interp = lightIntensity * cosAngIncidence;// + ambientLight;
}
