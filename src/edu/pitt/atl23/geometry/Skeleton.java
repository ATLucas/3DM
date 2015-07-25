package edu.pitt.atl23.geometry;

import edu.pitt.atl23.ShaderProgram;

/**
 * Created by Andrew on 7/24/2015.
 */
public class Skeleton {
	Bone root;
	Mesh mesh;

	public Skeleton(Vertex v) {
		root = new Bone(v);
		mesh = new Mesh();
		addBoneToMesh(root);
	}

	public void addBone(Vertex head, Vertex tail) {
		System.out.println("Bone added");
	}

	public void addBoneNode(Vertex v) {
		mesh.add(v);
	}

	public void removeBoneNode(Vertex v) {
		mesh.remove(v);
	}

	private void addBoneToMesh(Bone b) {
		mesh.add(b.head);
		mesh.add(b.tail);
		mesh.add(new Line(b.head, b.tail));
	}

	public void render(ShaderProgram shaderProgram) {
		mesh.render(shaderProgram, 1.0f, 1.0f, 1.0f, 1.0f, 0);
		mesh.render(shaderProgram, 1.0f, 1.0f, 1.0f, 1.0f, 1);
	}

	public void delete() {
		mesh.delete();
	}
}
