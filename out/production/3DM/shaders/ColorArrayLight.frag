#version 330

smooth in vec3 normal_interp;

out vec4 outputColor;

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
	outputColor = vec4(r, g, b, a) * vec4(normal_interp, 1);
}