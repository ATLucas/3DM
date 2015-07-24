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

	public Mesh() {
		indices = new IndexList();
		modelMatrix = new Mat4();
		mat4Buffer = BufferUtils.createFloatBuffer(64);
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
		return indices.add(t, force);
	}

	public void remove(Vertex v) {
		indices.remove(v);
	}

	public void remove(Line l) {
		indices.remove(l);
	}

	public void remove(Triangle t) {
		indices.remove(t);
	}

	public boolean moveBy(Vertex v, float x, float y, float z) {
		if(indices.getEquivalent(new Vertex(v.x+x,v.y+y,v.z+z)) != null) return false;
		v.x += x;
		v.y += y;
		v.z += z;
		return updateVertex(v);
	}

	public boolean moveTo(Vertex v, float x, float y, float z) {
		if(indices.getEquivalent(new Vertex(v.x+x,v.y+y,v.z+z)) != null) return false;
		v.x = x;
		v.y = y;
		v.z = z;
		return updateVertex(v);
	}

	public boolean updateVertex(Vertex v) {
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

	public void merge(Vertex from, Vertex to) {
		indices.merge(from, to);
	}

    public void flipNormal(Triangle t) {
        t.flipNormal();
        indices.updateTriangle(t);
        indices.updateNormal(t);
    }

	public void render(ShaderProgram colorArrProgram) {
		/** Use program **/
		glUseProgram(colorArrProgram.theProgram);
		/** Load model matrix **/
		modelMatrix.fillAndFlipBuffer(mat4Buffer);
		glUniformMatrix4(colorArrProgram.modelToWorldMatrix, false, mat4Buffer);
		/** Draw triangles **/
		indices.bindColorBuffer(colorArrProgram);
        indices.bindNormalBuffer(colorArrProgram);
		indices.drawTriangles();
	}

	public void render(ShaderProgram program, float red, float green, float blue, float alpha, int geometry) {
		/** Use program **/
		glUseProgram(program.theProgram);

		/** Load model matrix **/
		modelMatrix.fillAndFlipBuffer(mat4Buffer);
		glUniformMatrix4(program.modelToWorldMatrix, false, mat4Buffer);

		/** Set color **/
		glUniform4f(program.baseColor, red, green, blue, alpha);

		/** Draw **/
		if(geometry == 0) {
			indices.drawPoints();
		} else if(geometry == 1) {
			indices.drawLines();
		}else if(geometry == 2) {
			indices.drawTriangles();
		}
	}

	public void delete() {
		glDisableVertexAttribArray(0);
		indices.delete();
	}
}