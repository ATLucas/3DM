#version 330

in vec4 pos_interp;
in vec4 normal_interp;
flat in vec4 normal_flat;

out vec4 outputColor;

uniform vec4 pointLightPos;
uniform vec4 pointLightColor;
uniform vec4 dirLightDir;
uniform vec4 dirLightMag;
uniform vec4 ambient;

uniform float pointLightRadius;
uniform float pointLightFade;

uniform float maxIntensity;
uniform float gamma;

struct block {
	int first;
	int second;
	int third;
	int fourth;
};

layout (std140) uniform colorBlock {
  block colors[1024];
};

void main()
{
	int value;
	int d = gl_PrimitiveID / 4;
	int m = gl_PrimitiveID % 4;
	if( m == 0)
		value = colors[d].first;
	else if(m == 1)
		value = colors[d].second;
	else if(m == 2)
		value = colors[d].third;
	else
		value = colors[d].fourth;

	float a = (value & 255) / 255.0;
	float b = ((value >> 8) & 255) / 255.0;
	float g = ((value >> 16) & 255) / 255.0;
	float r = ((value >> 24) & 255) / 255.0;
	vec4 diffuse = vec4(r, g, b, a);

	float dirLight = dot(normal_interp, dirLightDir);
	dirLight = clamp(dirLight, 0, 1) * dirLightMag;

	vec4 pointLightDiff = pointLightPos - pos_interp;
	float dist = length(pointLightDiff);

	float pointLightMag = clamp(1.0 - dist*dist/(pointLightRadius*pointLightRadius), 0.0, 1.0);
	pointLightMag *= pointLightMag;

    float pointLight = pointLightFade * dot(normal_interp, normalize(pointLightDiff)) * pointLightMag;
    pointLight = clamp(pointLight, 0, 1);

	float celFactor = 0;
	if(pointLight > 0.6)
		celFactor = 0.6;
	else if(pointLight > 0.3)
		celFactor = pointLight;
	else if(pointLight > 0.1)
		celFactor = 0.3;
	else
		celFactor = pointLight * 3;

	//vec4 accumLighting = (pointLightColor + diffuse) * pointLight + diffuse * dirLight + diffuse * ambient;
	vec4 accumLighting = pointLightColor * celFactor + diffuse * dirLight + diffuse * ambient;
    outputColor = pow(accumLighting, vec4(gamma, gamma, gamma, 1.0));
}