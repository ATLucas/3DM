#version 330

smooth in vec4 normCamSpace;

out vec4 outputColor;

uniform vec4 dirLight;
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

	float cosAngIncidence = dot(normCamSpace, dirLight);
	cosAngIncidence = clamp(cosAngIncidence, 0, 1);
	if(cosAngIncidence > 0.8)
		cosAngIncidence = 1.0;
	else if(cosAngIncidence > 0.5)
		cosAngIncidence = 0.6;
	else
		cosAngIncidence = 0.3;

	float a = (value & 255) / 255.0;
	float b = ((value >> 8) & 255) / 255.0;
	float g = ((value >> 16) & 255) / 255.0;
	float r = ((value >> 24) & 255) / 255.0;
	vec4 diffuse = vec4(r, g, b, a);
	outputColor = diffuse * dirLightMag * cosAngIncidence + diffuse * ambient;
}