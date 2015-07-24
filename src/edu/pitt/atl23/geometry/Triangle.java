package edu.pitt.atl23.geometry;

import jglsdk.glm.Vec3;

/**
 * Created by Andrew T. Lucas on 3/13/2015.
 */
public class Triangle {
	public Vertex a, b, c;
	public Line s1, s2, s3;
	public ColorData color;
	public Vec3 normal;

	public Triangle(Vertex a, Vertex b, Vertex c, ColorData cd) {
		this.a = a;
		this.b = b;
		this.c = c;
		refreshLines();
		color = cd;
		calcNormal();
	}

    public boolean contains(Vertex v) {
        return v == a || v == b || v == c;
    }

	public void refreshLines() {
		s1 = new Line(a,b);
		s2 = new Line(b,c);
		s3 = new Line(c,a);
	}

	public void calcNormal() {
		normal = Vec3.cross( new Vec3(c.x-a.x,c.y-a.y,c.z-a.z), new Vec3(b.x-a.x,b.y-a.y,b.z-a.z));
	}

    public void flipNormal() {
        Vertex temp = b;
        b = c;
        c = temp;
        calcNormal();
    }

	public boolean equals(Triangle other){
		return
			(a.equals(other.a) && b.equals(other.b) && c.equals(other.c)) ||
			(a.equals(other.a) && b.equals(other.c) && c.equals(other.b)) ||
			(a.equals(other.b) && b.equals(other.a) && c.equals(other.c)) ||
			(a.equals(other.b) && b.equals(other.c) && c.equals(other.a)) ||
			(a.equals(other.c) && b.equals(other.a) && c.equals(other.b)) ||
			(a.equals(other.c) && b.equals(other.b) && c.equals(other.a)) ;
	}

	public String toString() {
		return "[" + a + "," + b + "," + c + "]";
	}
}
