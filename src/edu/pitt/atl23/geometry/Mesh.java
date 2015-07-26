package edu.pitt.atl23.geometry;

import edu.pitt.atl23.ShaderProgram;
import jglsdk.glm.Mat4;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL20.*;

/**
 * Created by Andrew T. Lucas on 3/13/2015.
 */
public class Mesh {

	private IndexList indices; /** manages IBOs and VAOs and the VertexList, which has the VBO **/
	private Mat4 modelMatrix;
	private FloatBuffer mat4Buffer;

	private Skeleton skeleton;

	public Mesh() {
		indices = new IndexList();
		modelMatrix = new Mat4();
		mat4Buffer = BufferUtils.createFloatBuffer(64);
	}

	public void addSkeleton() {
		skeleton = new Skeleton(new Vertex(0f, 0f, 0f));
	}

	public void addBoneNode(Vertex v) {
		skeleton.addBoneNode(v);
	}

	public void removeBoneNode(Vertex v) {
		skeleton.removeBoneNode(v);
	}

	public void addBone(Vertex v, Vertex w) {
		skeleton.addBone(v, w);
	}

	public Mesh getSkeletonMesh() {
		return skeleton.mesh;
	}

	public Vertex add(Vertex v) {
		return add(v, false);
	}

	public Vertex add(Vertex v, boolean force) {
		return indices.add(v, force);
	}

	public Line add(Line li) {
		return add(li, false);
	}

	public Line add(Line li, boolean force) {
		return indices.add(li, force);
	}

	public Triangle add(Triangle t) {
		return add(t, false);
	}

	public Triangle add(Triangle t, boolean force) {
		t.a.addToNormal(t);
		t.b.addToNormal(t);
		t.c.addToNormal(t);
		updateVertex(t.a);
		updateVertex(t.b);
		updateVertex(t.c);

		return indices.add(t, force);
	}

	public void remove(Vertex v) {
		indices.remove(v);
	}

	public void remove(Line l) {
		indices.remove(l);
	}

	public void remove(Triangle t) {
		t.a.removeFromNormal(t);
		t.b.removeFromNormal(t);
		t.c.removeFromNormal(t);
		updateVertex(t.a);
		updateVertex(t.b);
		updateVertex(t.c);

		indices.remove(t);
	}

	public boolean moveBy(Vertex v, float x, float y, float z) {
		v.x += x;
		v.y += y;
		v.z += z;
		for(Triangle t: v.getTris()) {
			t.calcNormal();
		}
		return updateVertex(v);
	}

	public boolean updateVertex(Vertex v) {
		v.calcNormal();
		return indices.updateVertex(v);
	}

	public void translateBy(float x, float y, float z) {
		modelMatrix.matrix[12] += x;
		modelMatrix.matrix[13] += y;
		modelMatrix.matrix[14] += z;
	}

	public void translateTo(float x, float y, float z) {
		modelMatrix.matrix[12] = x;
		modelMatrix.matrix[13] = y;
		modelMatrix.matrix[14] = z;
	}

	public void applyColor(Triangle t, ColorData cd) {
        t.color = cd;
		indices.updateColor(t);
	}

	public float getx() {
		return modelMatrix.matrix[12];
	}

	public float gety() {
		return modelMatrix.matrix[13];
	}

	public float getz() {
		return modelMatrix.matrix[14];
	}

	public ArrayList<Vertex> getVerts() {
		return indices.getVerts();
	}

	public ArrayList<Line> getLines() {
		return indices.getLines();
	}

	public ArrayList<Triangle> getTris() {
		return indices.getTris();
	}

	public short[] getPointIndices() {
		return indices.getPointIndices();
	}

	public short[] getLineIndices() {
		return indices.getLineIndices();
	}

	public short[] getTriIndices() {
		return indices.getTriIndices();
	}

	public int[] getColors() {
		return indices.getColors();
	}

	public int numPoints() {
		return indices.numPoints();
	}

	public int numLines() {
		return indices.numLines();
	}

	public int numTris() {
		return indices.numTris();
	}

	public Vertex getSelectedVertex(int mouseX, int mouseY, int screenWidth, int screenHeight, int maxDistance, Mat4 viewMat, Mat4 perspMat) {
		return indices.getSelectedVertex(mouseX, mouseY, screenWidth, screenHeight, maxDistance, viewMat, perspMat);
	}

	public Line getSelectedLines(int mouseX, int mouseY, int screenWidth, int screenHeight, int maxDistance, Mat4 viewMat, Mat4 perspMat) {
		return indices.getSelectedLines(mouseX, mouseY, screenWidth, screenHeight, maxDistance, viewMat, perspMat);
	}

	public Triangle getSelectedTriangle(int mouseX, int mouseY, int screenWidth, int screenHeight, Mat4 viewMat, Mat4 perspMat) {
		return indices.getSelectedTriangle(mouseX, mouseY, screenWidth, screenHeight, viewMat, perspMat);
	}

	public Vertex getSelectedBoneEnd(int mouseX, int mouseY, int screenWidth, int screenHeight, int maxDistance, Mat4 viewMat, Mat4 perspMat) {
		return skeleton.mesh.indices.getSelectedVertex(mouseX, mouseY, screenWidth, screenHeight, maxDistance, viewMat, perspMat);
	}

	public void merge(Vertex from, Vertex to) {
		for(Triangle t: indices.getTris()) {
			if(t.a == from) {
				remove(t);
				add(new Triangle(to, t.b, t.c, new ColorData()), false);
			} else if(t.b == from) {
				remove(t);
				add(new Triangle(t.a, to, t.c, new ColorData()), false);
			} else if(t.c == from) {
				remove(t);
				add(new Triangle(t.a, t.b, to, new ColorData()), false);
			}
		}
		for(Line l: indices.getLines()) {
			if(l.a == from) {
				remove(l);
				add(new Line(to, l.b), false);
			} else if(l.b == from) {
				remove(l);
				add(new Line(l.a, to), false);
			}
		}
		remove(from);
		updateVertex(to);
	}

    public void flipNormal(Triangle t) {
        t.flipNormal();
        updateVertex(t.a);
        updateVertex(t.b);
        updateVertex(t.c);
        indices.updateTriangle(t);
    }

	public void render(ShaderProgram colorArrProgram) {
		/** Use program **/
		glUseProgram(colorArrProgram.theProgram);
		/** Load model matrix **/
		modelMatrix.fillAndFlipBuffer(mat4Buffer);
		glUniformMatrix4(colorArrProgram.modelToWorld, false, mat4Buffer);
		/** Draw triangles **/
		indices.bindColorBuffer(colorArrProgram);
		indices.drawTriangles();
	}

	public void render(ShaderProgram program, float red, float green, float blue, float alpha, int geometry) {
		/** Use program **/
		glUseProgram(program.theProgram);

		/** Load model matrix **/
		modelMatrix.fillAndFlipBuffer(mat4Buffer);
		glUniformMatrix4(program.modelToWorld, false, mat4Buffer);

		/** Set color **/
		glUniform4f(program.baseColor, red, green, blue, alpha);

		/** Draw **/
		if(geometry == 0 || geometry == 3) {
			indices.drawPoints();
		} else if(geometry == 1) {
			indices.drawLines();
		} else if(geometry == 2) {
			indices.drawTriangles();
		}
	}

	public void renderSkeleton(ShaderProgram nodeProgram, ShaderProgram boneProgram) {
		skeleton.render(nodeProgram, boneProgram);
	}

	public void delete() {
		glDisableVertexAttribArray(0);
		indices.delete();
		if(skeleton != null) skeleton.delete();
	}
}