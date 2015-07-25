package edu.pitt.atl23.geometry;

import java.util.ArrayList;

/**
 * Created by Andrew on 7/24/2015.
 */
public class Bone {
	Bone parent;
	ArrayList<Bone> children;
	Vertex head, tail;

	public Bone(Bone p, Vertex v) {
		parent = p;
		children = new ArrayList<>();
		head = parent.tail;
		tail = v;
	}

	public Bone(Vertex v) {
		children = new ArrayList<>();
		head = v;
		parent = null;
		tail = new Vertex(head.x, head.y + 1.0f, head.z);
	}
}