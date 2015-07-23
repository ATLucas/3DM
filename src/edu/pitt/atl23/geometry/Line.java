package edu.pitt.atl23.geometry;

/**
 * Created by Andrew T. Lucas on 3/14/2015.
 */
public class Line {
	public Vertex a, b;

	public Line(Vertex a, Vertex b) {
		this.a = a;
		this.b = b;
	}

	public boolean equals(Line other){
		return (a.equals(other.a) && b.equals(other.b)) || (a.equals(other.b) && b.equals(other.a));
	}

	public boolean containsVerts(Line l) {
		return (a==l.a && b==l.b) || (a==l.b && b==l.a);
	}

	public String toString() {
		return "[" + a + "," + b + "]";
	}
}
