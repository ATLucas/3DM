package edu.pitt.atl23.gui;

import edu.pitt.atl23.ShaderProgram;
import edu.pitt.atl23.geometry.*;

import java.util.ArrayList;

/**
 * Created by Andrew T. Lucas on 5/29/2015.
 */
public class Cursor {
	private Mesh mesh;

	// how much to move on the next update (set in Main.java)
	public float dx,dy,dz;

	public Cursor() {
		mesh = new Mesh();
		dx = 0;
		dy = 0;
		dz = 0;

		Vertex a,b,c,d,e,f,g,h,i,j,k,l;
		
		// points on diamond
		a = new Vertex( 0f, 0f, -0.1f);
		b = new Vertex( 0.1f, 0f, 0f);
		c = new Vertex( 0f, 0f, 0.1f);
		d = new Vertex( -0.1f, 0f, 0f);
		e = new Vertex( 0f, -0.1f, 0f);
		f = new Vertex( 0f, 0.1f, 0f);
		// tips of the lines
		g = new Vertex( 0f, 0f, -1.0f);
		h = new Vertex( 1.0f, 0f, 0f);
		i = new Vertex( 0f, 0f, 1.0f);
		j = new Vertex( -1.0f, 0f, 0f);
		k = new Vertex( 0f, -1.0f, 0f);
		l = new Vertex( 0f, 1.0f, 0f);
		
		// add vertices
		mesh.add(a); mesh.add(b); mesh.add(c);
		mesh.add(d); mesh.add(e); mesh.add(f);
		mesh.add(g); mesh.add(h); mesh.add(i);
		mesh.add(j); mesh.add(k); mesh.add(l);
		
		// add triangles
		mesh.add(new Triangle(c, d, f, new ColorData()));
		mesh.add(new Triangle(d, a, f, new ColorData()));
		mesh.add(new Triangle(a, b, f, new ColorData()));
		mesh.add(new Triangle(b, c, f, new ColorData()));
		mesh.add(new Triangle(e, d, c, new ColorData()));
		mesh.add(new Triangle(e, a, d, new ColorData()));
		mesh.add(new Triangle(e, b, a, new ColorData()));
		mesh.add(new Triangle(e, c, b, new ColorData()));
		
		// add lines
		mesh.add(new Line(a, g));
		mesh.add(new Line(b, h));
		mesh.add(new Line(c, i));
		mesh.add(new Line(d, j));
		mesh.add(new Line(e, k));
		mesh.add(new Line(f, l));
	}

	public float getx() {
		return mesh.getx();
	}

	public float gety() {
		return mesh.gety();
	}

	public float getz() {
		return mesh.getz();
	}

	public void update(Mesh model, Mesh selection) {
		dy = dy + mesh.gety() >  0.0f ? dy : 0.0f - mesh.gety();
		dy = dy + mesh.gety() >=  80.0f ?  80.0f - mesh.gety() : dy;
		dx = dx + mesh.getx() >=  80.0f ?  80.0f - mesh.getx() : dx;
		dx = dx + mesh.getx() <= -80.0f ? -80.0f - mesh.getx() : dx;
		dz = dz + mesh.getz() >=  80.0f ?  80.0f - mesh.getz() : dz;
		dz = dz + mesh.getz() <= -80.0f ? -80.0f - mesh.getz() : dz;
		mesh.translateBy(dx,dy,dz);
		ArrayList<Vertex> verts = selection.getVerts();
		for(Vertex v: verts) {
			model.moveBy(v, dx, dy, dz);
			selection.updateVertex(v);
		}
	}

	public void update() {
		dy = dy + mesh.gety() >  0.0f ? dy : 0.0f - mesh.gety();
		dy = dy + mesh.gety() >=  80.0f ?  80.0f - mesh.gety() : dy;
		dx = dx + mesh.getx() >=  80.0f ?  80.0f - mesh.getx() : dx;
		dx = dx + mesh.getx() <= -80.0f ? -80.0f - mesh.getx() : dx;
		dz = dz + mesh.getz() >=  80.0f ?  80.0f - mesh.getz() : dz;
		dz = dz + mesh.getz() <= -80.0f ? -80.0f - mesh.getz() : dz;
		mesh.translateBy(dx,dy,dz);
	}

	public boolean translateToX(float f) {
		if(Math.abs(f) <= 80.0f) {
			mesh.translateTo(f, mesh.gety(), mesh.getz());
			return true;
		}
		return false;
	}

	public boolean translateToY(float f) {
		if(f >= 0.0f && f <= 80.0f) {
			mesh.translateTo(mesh.getx(), f, mesh.getz());
			return true;
		}
		return false;
	}

	public boolean translateToZ(float f) {
		if(Math.abs(f) <= 80.0f) {
			mesh.translateTo(mesh.getx(), mesh.gety(), f);
			return true;
		}
		return false;
	}
	
	public void render(ShaderProgram shaderProgram) {
		mesh.render(shaderProgram, 0.793f, 0.375f, 0.176f, 1.0f, 2);
		mesh.render(shaderProgram, 0.406f, 0.590f, 0.730f, 1.0f, 1);
	}

	public void destroy() {
		mesh.delete();
	}
}
