package edu.pitt.atl23.geometry;

/**
 * Created by Andrew T. Lucas on 3/13/2015.
 */
public class Vertex {
	public float x, y, z;

	public Vertex(float x, float y, float z){
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public boolean equals(Vertex other) {
		return x==other.x && y==other.y && z==other.z;
	}

	public String toString() {
		return x + "," + y + "," + z;
	}
}
