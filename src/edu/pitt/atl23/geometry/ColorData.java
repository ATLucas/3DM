package edu.pitt.atl23.geometry;

import java.nio.ByteBuffer;

/**
 * Created by Andrew T. Lucas on 6/20/2015.
 */
public class ColorData {
	public int r, g, b, a;
	public int intVal;

	public ColorData(int r, int g, int b, int a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		// store the entire color in an int
		intVal = r;
		intVal <<= 8;
		intVal |= g;
		intVal <<= 8;
		intVal |= b;
		intVal <<= 8;
		intVal |= a;
	}

	public ColorData(int iv) {
		intVal = iv;
		byte[] bytes = ByteBuffer.allocate(4).putInt(iv).array();
		r = bytes[0] & 0xFF;
		g = bytes[1] & 0xFF;
		b = bytes[2] & 0xFF;
		a = bytes[3] & 0xFF;
	}

	public ColorData() {
		r = 50;
		g = 50;
		b = 100;
		a = 255;
		// store the entire color in an int
		int temp = r;
		temp <<= 8;
		temp |= g;
		temp <<= 8;
		temp |= b;
		temp <<= 8;
		temp |= a;
		intVal = temp;
	}
}
