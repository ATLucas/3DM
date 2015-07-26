package edu.pitt.atl23.geometry;

import java.util.ArrayList;

/**
 * Created by Andrew T. Lucas on 3/13/2015.
 */
public class Vertex {
	public float x, y, z;
	public float nx, ny, nz;
	private ArrayList<Triangle> tris;

	public Vertex(float x, float y, float z){
		this.x = x;
		this.y = y;
		this.z = z;
		nx = 0;
		ny = 0;
		nz = 0;
		tris = new ArrayList<>();
	}

	public void addToNormal(Triangle t) {
        if(tris.indexOf(t) > -1) return;
		tris.add(t);
		calcNormal();
	}

	public void removeFromNormal(Triangle t) {
		tris.remove(t);
		calcNormal();
	}

	public void calcNormal() {
        nx = 0;
        ny = 0;
        nz = 0;
		for(Triangle t: tris) {
			nx += t.normal.x;
			ny += t.normal.y;
			nz += t.normal.z;
		}
		float mag = (float)Math.sqrt(nx*nx + ny*ny + nz*nz);
		nx /= mag;
		ny /= mag;
		nz /= mag;
	}

	public ArrayList<Triangle> getTris() {
		ArrayList<Triangle> result = new ArrayList<>();
		result.addAll(tris);
		return result;
	}

	public boolean equals(Vertex other) {
		return x==other.x && y==other.y && z==other.z;
	}

	public String toString() {
		return x + "," + y + "," + z;
	}
}
