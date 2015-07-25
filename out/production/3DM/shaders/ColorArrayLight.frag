#version 330

in float pointLight;
in float dirLight;

out vec4 outputColor;

uniform vec4 pointLightMag;
uniform vec4 dirLightMag;
uniform vec4 ambient;

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

	float factor;
	if(pointLight > 0.6)
		factor = 1.0;
	else if(pointLight > 0.1)
		factor = 0.6;
	else
		factor = 0.4;

	outputColor = diffuse * pointLightMag * factor + diffuse * dirLightMag * dirLight + diffuse * ambient;
}