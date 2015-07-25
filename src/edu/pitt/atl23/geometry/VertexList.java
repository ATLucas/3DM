package edu.pitt.atl23.geometry;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Created by Andrew T. Lucas on 3/13/2015.
 */
public class VertexList {
	private ArrayList<Vertex> vertAL;
	private float[] verts;
	private int vbo;
	private FloatBuffer vertexBuffer;

	public VertexList(int size) {
		if(size < 3) {
			System.out.println("WARNING: VertexList initialized with size 3 instead of invalid size: " + size);
			size = 3;
		}
		verts = new float[size];
		vertAL = new ArrayList<>();
		vbo = GL15.glGenBuffers();
		loadVBO();
	}

	public VertexList() {
		this(12);
	}

	public void add(Vertex v) {
		vertAL.add(v);
		if(verts.length < (vertAL.size())*6) {
			// double size of the float array
			float[] tmp = new float[verts.length*2];
			System.arraycopy(verts,0,tmp,0,verts.length);
			verts = tmp;
			// add new vert
			int offset = (vertAL.size()-1)*6;
			verts[offset] = v.x;
			verts[offset + 1] = v.y;
			verts[offset + 2] = v.z;
			verts[offset + 3] = v.nx;
			verts[offset + 4] = v.ny;
			verts[offset + 5] = v.nz;
			// double size of the VBO (with new vert)
			loadVBO();
		} else {
			// add new coords to verts array
			int offset = (vertAL.size() - 1) * 6;
			verts[offset] = v.x;
			verts[offset + 1] = v.y;
			verts[offset + 2] = v.z;
			verts[offset + 3] = v.nx;
			verts[offset + 4] = v.ny;
			verts[offset + 5] = v.nz;
			// Create temp float buffer
			vertexBuffer = BufferUtils.createFloatBuffer(6);
			vertexBuffer.put(new float[]{v.x,v.y,v.z,v.nx,v.ny,v.nz}).flip();
			// byte offset
			offset = offset * 4;
			// load subdata
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
			GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, offset, vertexBuffer);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		}
	}

	public boolean updateVertex(Vertex v) {
		int offset = vertAL.indexOf(v)*6;
		if(offset < 0) return false;
		// update verts array
		verts[offset] = v.x;
		verts[offset + 1] = v.y;
		verts[offset + 2] = v.z;
		verts[offset + 3] = v.nx;
		verts[offset + 4] = v.ny;
		verts[offset + 5] = v.nz;
		// Create temp float buffer
		vertexBuffer = BufferUtils.createFloatBuffer(6);
		vertexBuffer.put(new float[]{v.x,v.y,v.z,v.nx,v.ny,v.nz}).flip();
		// byte offset
		offset = offset * 4;
		// load subdata
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, offset, vertexBuffer);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		return true;
	}

	public void remove(int offset) {
		vertAL.set(offset, null);
	}

	public int getVBO() {
		return vbo;
	}

	public int indexOf(Vertex v) {
		return vertAL.indexOf(v);
	}

	public int size() {
		return vertAL.size();
	}

	public void delete() {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glDeleteBuffers(vbo);
	}

	private void loadVBO() {
		vertexBuffer = BufferUtils.createFloatBuffer(verts.length);
		vertexBuffer.put(verts).flip();

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_DYNAMIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}
}
