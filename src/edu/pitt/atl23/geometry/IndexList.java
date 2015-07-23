package edu.pitt.atl23.geometry;

import edu.pitt.atl23.ShaderProgram;
import jglsdk.glm.Mat4;
import jglsdk.glm.Vec2;
import jglsdk.glm.Vec3;
import jglsdk.glm.Vec4;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBUniformBufferObject;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import static org.lwjgl.opengl.ARBUniformBufferObject.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Created by Andrew T. Lucas on 3/13/2015.
 */
public class IndexList {
	private VertexList vertexList;

	private ArrayList<Vertex> pointAL;
	private ArrayList<Line> lineAL;
	private ArrayList<Triangle> triAL;
	private ArrayList<ColorData> colorAL;
	private short[] tris, lines, points;
	private int[] colors;
	private int triIBO, lineIBO, pointIBO;
	private int triVAO, lineVAO, pointVAO;
	private int colorUBO;

	private ShortBuffer indexBuffer;
	private IntBuffer colorBuffer;

	public IndexList(int size) {
		if(size < 3) {
			System.out.println("WARNING: IndexList initialized with size 3 instead of invalid size: " + size);
			size = 3;
		}
		vertexList = new VertexList();
		tris = new short[size*3];
		lines = new short[size*2];
		points = new short[size];
		colors = new int[1024];
		triAL = new ArrayList<>();
		lineAL = new ArrayList<>();
		pointAL = new ArrayList<>();
		colorAL = new ArrayList<>();

		triIBO = glGenBuffers();
		loadTriIBO();
		lineIBO = glGenBuffers();
		loadLineIBO();
		pointIBO = glGenBuffers();
		loadPointIBO();

		triVAO = glGenVertexArrays();
		loadTriVAO();
		lineVAO = glGenVertexArrays();
		loadLineVAO();
		pointVAO = glGenVertexArrays();
		loadPointVAO();

		colorUBO = glGenBuffers();
		loadColorBuffer();
	}

	public IndexList() {
		this(6);
	}

	public Triangle add(Triangle t, boolean force) {
		if(!force) {
			for (Triangle tri : triAL) {
				if (t.equals(tri)) return tri;
			}
		}
		triAL.add(t);

		/** Color **/
		colorAL.add(t.color);
		int offset = colorAL.size() - 1;
		colors[offset] = t.color.intVal;
		IntBuffer cb = BufferUtils.createIntBuffer(1);
		cb.put(new int[]{t.color.intVal}).flip();
		offset *= 4;
		glBindBuffer(GL_UNIFORM_BUFFER, colorUBO);
		glBufferSubData(GL_UNIFORM_BUFFER, offset, cb);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		/** End Color **/

		if(tris.length < (triAL.size())*3) {
			// double size of the float array
			short[] tmp = new short[tris.length*2];
			System.arraycopy(tris,0,tmp,0,tris.length);
			tris = tmp;
			// add new tri
			offset = (triAL.size()-1)*3;
			tris[offset] = (short)vertexList.indexOf(t.a);
			tris[offset + 1] = (short)vertexList.indexOf(t.b);
			tris[offset + 2] = (short)vertexList.indexOf(t.c);
			// double size of the IBO (with new tri)
			loadTriIBO();
		} else {
			// add new coords to tris array
			offset = (triAL.size()-1)*3;
			tris[offset] = (short)vertexList.indexOf(t.a);
			tris[offset + 1] = (short)vertexList.indexOf(t.b);
			tris[offset + 2] = (short)vertexList.indexOf(t.c);
			// Create temp short buffer
			indexBuffer = BufferUtils.createShortBuffer(3);
			indexBuffer.put(new short[]{tris[offset],tris[offset + 1],tris[offset + 2]}).flip();
			// byte offset
			offset = offset * 2;
			// load subdata
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, triIBO);
			glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, offset, indexBuffer);
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		}
		return t;
	}

	public Line add(Line li, boolean force) {
		if(!force) {
			for (Line line : lineAL) {
				if (li.equals(line)) return line;
			}
		}
		lineAL.add(li);
		if(lines.length < (lineAL.size())*2) {
			// double size of the float array
			short[] tmp = new short[lines.length*2];
			System.arraycopy(lines,0,tmp,0,lines.length);
			lines = tmp;
			// add new line
			int offset = (lineAL.size()-1)*2;
			lines[offset] = (short)vertexList.indexOf(li.a);
			lines[offset + 1] = (short)vertexList.indexOf(li.b);
			// double size of the IBO (with new line)
			loadLineIBO();
		} else {
			// add new coords to lines array
			int offset = (lineAL.size()-1)*2;
			lines[offset] = (short)vertexList.indexOf(li.a);
			lines[offset + 1] = (short)vertexList.indexOf(li.b);
			// Create temp short buffer
			indexBuffer = BufferUtils.createShortBuffer(2);
			indexBuffer.put(new short[]{lines[offset],lines[offset + 1]}).flip();
			// byte offset
			offset = offset * 2;
			// load subdata
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, lineIBO);
			glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, offset, indexBuffer);
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		}
		return li;
	}

	public Vertex add(Vertex v, boolean force) {
		if(!force) {
			for (Vertex ve : pointAL) {
				if (ve.equals(v)) return ve;
			}
		}
		vertexList.add(v);
		pointAL.add(v);
		if(points.length < (pointAL.size())) {
			// double size of the float array
			short[] tmp = new short[points.length*2];
			System.arraycopy(points,0,tmp,0,points.length);
			points = tmp;
			// add new vert
			int offset = (pointAL.size()-1);
			points[offset] = (short)(vertexList.size()-1);
			// double size of the IBO (with new vert)
			loadPointIBO();
		} else {
			// add new vert
			int offset = (pointAL.size()-1);
			points[offset] = (short)(vertexList.size()-1);
			// Create temp short buffer
			indexBuffer = BufferUtils.createShortBuffer(1);
			indexBuffer.put(new short[]{points[offset]}).flip();
			// byte offset
			offset = offset * 2;
			// load subdata
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, pointIBO);
			glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, offset, indexBuffer);
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		}
		return v;
	}

	public boolean remove(Triangle t) {

		int offset = triAL.indexOf(t);
		if(offset < 0) return false;

		// remove tri from tri ArrayList
		triAL.remove(offset);

		/** Color **/
		colorAL.remove(offset);
		int last = colorAL.size();
		for(int i=offset; i<last; i++) {
			colors[i] = colors[i+1];
		}
		colors[last] = 0;
		IntBuffer cb = BufferUtils.createIntBuffer(last - offset);
		cb.put(Arrays.copyOfRange(colors, offset, last)).flip();
		glBindBuffer(GL_UNIFORM_BUFFER, colorUBO);
		glBufferSubData(GL_UNIFORM_BUFFER, offset*4, cb);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
		/** End Color **/

		offset = offset*3;

		// remove tri indices from tri array
		last = triAL.size()*3;
		for(int i=offset; i<last; i++) {
			tris[i] = tris[i+3];
		}
		// Create temp short buffer
		indexBuffer = BufferUtils.createShortBuffer(last-offset);
		indexBuffer.put(Arrays.copyOfRange(tris, offset, last)).flip();

		// byte offset
		offset = offset * 2;

		// load subdata
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, triIBO);
		glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, offset, indexBuffer);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

		return true;
	}

	public boolean remove(Line li) {

		/** First remove the line **/

		int offset = lineAL.indexOf(li);
		if(offset < 0) return false;

		// remove line from line ArrayList
		lineAL.remove(offset);
		offset = offset*2;

		// remove line indices from line array
		int last = lineAL.size()*2;
		for(int i=offset; i<last; i++) {
			lines[i] = lines[i+2];
		}

		// Create temp short buffer
		indexBuffer = BufferUtils.createShortBuffer(last-offset);
		indexBuffer.put(Arrays.copyOfRange(lines, offset, last)).flip();

		// byte offset
		offset = offset * 2;

		// load subdata
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, lineIBO);
		glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, offset, indexBuffer);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

		/** Now remove all triangles that contain the line **/
		ArrayList<Triangle> trisToRemove = new ArrayList<>();
		for(Triangle tri: triAL) {
			if(li.containsVerts(tri.s1) || li.containsVerts(tri.s2) || li.containsVerts(tri.s3)) {
				trisToRemove.add(tri);
			}
		}
		for(Triangle tri: trisToRemove) {
			remove(tri);
		}

		return true;
	}

	public boolean remove(Vertex v) {

		/** First remove the vertex **/

		int offset = pointAL.indexOf(v);
		if(offset < 0) return false;

		// remove point from point ArrayList
		pointAL.remove(offset);

		// remove point indices from point array
		int last = pointAL.size();
		for(int i=offset; i<last; i++) {
			points[i] = points[i+1];
		}

		// Create temp short buffer
		indexBuffer = BufferUtils.createShortBuffer(last-offset);
		indexBuffer.put(Arrays.copyOfRange(points, offset, last)).flip();

		// byte offset
		offset = offset * 2;

		// load subdata
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, pointIBO);
		glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, offset, indexBuffer);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

		/** Now remove all triangles that contain the vertex **/
		ArrayList<Triangle> trisToRemove = v.getTris();
		for(Triangle tri: trisToRemove) {
			remove(tri);
		}

		/** Now remove all lines that contain the vertex **/
		ArrayList<Line> linesToRemove = new ArrayList<>();
		for(Line line: lineAL) {
			if(line.a.equals(v) || line.b.equals(v)) {
				linesToRemove.add(line);
			}
		}
		for(Line line: linesToRemove) {
			remove(line);
		}

		return true;
	}

	public boolean updateVertex(Vertex v) {
		return vertexList.updateVertex(v);
	}

	public boolean applyColor(Triangle t) {
		for(int offset = 0; offset < triAL.size(); offset++) {
			if(triAL.get(offset) == t) {
				colorAL.set(offset, t.color);
				colors[offset] = t.color.intVal;
				IntBuffer cb = BufferUtils.createIntBuffer(1);
				cb.put(new int[]{t.color.intVal}).flip();
				offset *= 4;
				glBindBuffer(GL_UNIFORM_BUFFER, colorUBO);
				glBufferSubData(GL_UNIFORM_BUFFER, offset, cb);
				glBindBuffer(GL_UNIFORM_BUFFER, 0);
				return true;
			}
		}
		return false;
	}

	public void merge(Vertex from, Vertex to) {
		ArrayList<Triangle> checkTris = new ArrayList<>();
		checkTris.addAll(triAL);
		for(Triangle t: checkTris) {
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
		ArrayList<Line> checkLines = new ArrayList<>();
		checkLines.addAll(lineAL);
		for(Line l: checkLines) {
			if(l.a == from) {
				remove(l);
				add(new Line(to, l.b), false);
			} else if(l.b == from) {
				remove(l);
				add(new Line(l.a, to), false);
			}
		}
		remove(from);
	}

	public ArrayList<Vertex> getVerts() {
		return vertexList.getVerts();
	}

	public ArrayList<Line> getLines() {
		ArrayList<Line> result = new ArrayList<>();
		result.addAll(lineAL);
		return result;
	}

	public ArrayList<Triangle> getTris() {
		ArrayList<Triangle> result = new ArrayList<>();
		result.addAll(triAL);
		return result;
	}

	public short[] getPointIndices() {
		return points;
	}

	public short[] getLineIndices() {
		return lines;
	}

	public short[] getTriIndices() {
		return tris;
	}

	public int[] getColors() {
		return colors;
	}

	public int numPoints() {
		return pointAL.size();
	}

	public int numLines() {
		return lineAL.size();
	}

	public int numTris() {
		return triAL.size();
	}

	public Vertex getSelectedVertex(int mouseX, int mouseY, int screenWidth, int screenHeight, int maxDistance, Mat4 viewMat, Mat4 perspMat) {
		Vertex result = null;
		float resZ = 1;
		for(Vertex v: pointAL) {
			Vec4 res = Mat4.mul(Mat4.mul(perspMat, viewMat), new Vec4(v.x, v.y, v.z, 1));
			res.scale(1/res.w);
			res = new Vec4((res.x + 1) * screenWidth / 2, (res.y + 1) * screenHeight / 2, res.z, 1);
			if( Math.abs(res.x - mouseX) <= maxDistance && Math.abs(res.y - mouseY) <= maxDistance &&
				(result == null || res.z < resZ) ) {
				result = v;
				resZ = res.z;
			}
		}
		return result;
	}

	public Line getSelectedLines(int mouseX, int mouseY, int screenWidth, int screenHeight, int maxDistance, Mat4 viewMat, Mat4 perspMat) {
		Line result = null;
		Vec3 r0 = null;
		Vec3 r1 = null;
		for(Line l: lineAL) {
			Vec4 trans = Mat4.mul(viewMat, new Vec4(l.a.x, l.a.y, l.a.z, 1));
			Vec4 temp = Mat4.mul(perspMat, trans);
			temp.scale(1/temp.w);
			Vec3 p0 = new Vec3((temp.x + 1) * screenWidth / 2, (temp.y + 1) * screenHeight / 2, trans.z);

			trans = Mat4.mul(viewMat, new Vec4(l.b.x, l.b.y, l.b.z, 1));
			temp = Mat4.mul(perspMat, trans);
			temp.scale(1/temp.w);
			Vec3 p1 = new Vec3((temp.x + 1) * screenWidth / 2, (temp.y + 1) * screenHeight / 2, trans.z);

			int distance = distFromLineSegment(mouseX, mouseY, p0.x, p0.y, p1.x, p1.y);

			if(distance <= maxDistance) {
				if(result != null) {
					if((mouseX - p0.x) * (p0.z - p1.z) / (p0.x - p1.x) + p0.z > (mouseX - r0.x) * (r0.z - r1.z) / (r0.x - r1.x) + r0.z) {
						result = l;
						r0 = p0;
						r1 = p1;
					}
				} else {
					result = l;
					r0 = p0;
					r1 = p1;
				}
			}
		}
		return result;
	}

	public Triangle getSelectedTriangle(int mouseX, int mouseY, int screenWidth, int screenHeight, Mat4 viewMat, Mat4 perspMat) {
		Triangle result = null;
		Vec3 r0 = null;
		Vec3 r1 = null;
		Vec3 r2 = null;
		for(Triangle t: triAL) {
			Vec4 trans = Mat4.mul(viewMat, new Vec4(t.a.x, t.a.y, t.a.z, 1));
			Vec4 temp = Mat4.mul(perspMat, trans);
			temp.scale(1/temp.w);
			Vec3 p0 = new Vec3((temp.x + 1) * screenWidth / 2, (temp.y + 1) * screenHeight / 2, trans.z);

			trans = Mat4.mul(viewMat, new Vec4(t.b.x, t.b.y, t.b.z, 1));
			temp = Mat4.mul(perspMat, trans);
			temp.scale(1/temp.w);
			Vec3 p1 = new Vec3((temp.x + 1) * screenWidth / 2, (temp.y + 1) * screenHeight / 2, trans.z);

			trans = Mat4.mul(viewMat, new Vec4(t.c.x, t.c.y, t.c.z, 1));
			temp = Mat4.mul(perspMat, trans);
			temp.scale(1/temp.w);
			Vec3 p2 = new Vec3((temp.x + 1) * screenWidth / 2, (temp.y + 1) * screenHeight / 2, trans.z);

			if(triangleContainsPoint(p0.x, p0.y, p1.x, p1.y, p2.x, p2.y, mouseX, mouseY)) {
				if(result == null || triangleIsInFront(mouseX, mouseY, p0, p1, p2, r0, r1, r2)) {
					result = t;
					r0 = p0;
					r1 = p1;
					r2 = p2;
				}
			}
		}
		return result;
	}

	public Vertex getEquivalent(Vertex v) {
		for(Vertex point: pointAL) {
			if(v.equals(point)) return point;
		}
		return null;
	}

	public Line getEquivalent(Line l) {
		for(Line line: lineAL) {
			if(l.equals(line)) return line;
		}
		return null;
	}

	public Triangle getEquivalent(Triangle t) {
		for(Triangle tri: triAL) {
			if(t.equals(tri)) return tri;
		}
		return null;
	}

	public void drawTriangles() {
		glBindVertexArray(triVAO);
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, triIBO);
		glDrawElements(GL_TRIANGLES, triAL.size()*3, GL_UNSIGNED_SHORT, 0);
	}

	public void drawLines() {
		glBindVertexArray(lineVAO);
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, lineIBO);
		glDrawElements(GL_LINES, lineAL.size()*2, GL_UNSIGNED_SHORT, 0);
	}

	public void drawPoints() {
		glBindVertexArray(pointVAO);
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, pointIBO);
		glDrawElements(GL_POINTS, pointAL.size(), GL_UNSIGNED_SHORT, 0);
	}

	public void delete() {
		vertexList.delete();
		/** Delete the ibos **/
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		glDeleteBuffers(triIBO);
		glDeleteBuffers(lineIBO);
		glDeleteBuffers(pointIBO);
		/** Delete the vaos **/
		glBindVertexArray(0);
		glDeleteVertexArrays(triVAO);
		glDeleteVertexArrays(lineVAO);
		glDeleteVertexArrays(pointVAO);
		/** Delete ubo**/
		glDeleteBuffers(colorUBO);
	}

	private void loadTriIBO() {
		indexBuffer = BufferUtils.createShortBuffer(tris.length);
		indexBuffer.put(tris).flip();

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, triIBO);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_DYNAMIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	private void loadLineIBO() {
		indexBuffer = BufferUtils.createShortBuffer(lines.length);
		indexBuffer.put(lines).flip();

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, lineIBO);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_DYNAMIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	private void loadPointIBO() {
		indexBuffer = BufferUtils.createShortBuffer(points.length);
		indexBuffer.put(points).flip();

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, pointIBO);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_DYNAMIC_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	private void loadTriVAO() {
		glBindVertexArray(triVAO);

		glBindBuffer(GL_ARRAY_BUFFER, vertexList.getVBO());
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 24, 0);
		glVertexAttribPointer(1, 3, GL_FLOAT, false, 24, 12);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, triIBO);

		glBindVertexArray(0);
	}

	private void loadLineVAO() {
		glBindVertexArray(lineVAO);

		glBindBuffer(GL_ARRAY_BUFFER, vertexList.getVBO());
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 24, 0);
		glVertexAttribPointer(1, 3, GL_FLOAT, false, 24, 12);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, lineIBO);

		glBindVertexArray(0);
	}

	private void loadPointVAO() {
		glBindVertexArray(pointVAO);

		glBindBuffer(GL_ARRAY_BUFFER, vertexList.getVBO());
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 24, 0);
		glVertexAttribPointer(1, 3, GL_FLOAT, false, 24, 12);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, pointIBO);

		glBindVertexArray(0);
	}

	private void loadColorBuffer() {
		colorBuffer = BufferUtils.createIntBuffer(colors.length);
		colorBuffer.put(colors).flip();

		glBindBuffer(GL_UNIFORM_BUFFER, colorUBO);
		glBufferData(GL_UNIFORM_BUFFER, colorBuffer, GL_DYNAMIC_DRAW);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}

	public void bindColorBuffer(ShaderProgram program) {
		glUniformBlockBinding(program.theProgram, program.colorBlock, 0);
		ARBUniformBufferObject.glBindBufferBase(GL_UNIFORM_BUFFER, 0, colorUBO);
	}

	private int distFromLineSegment(int px, int py, float ax, float ay, float bx, float by) {
		float lengthSquared = distanceSquared(ax, ay, bx, by);
		if (lengthSquared == 0) return (int)distanceSquared(px, py, bx, by);
		float t = ((px - bx) * (ax - bx) + (py - by) * (ay - by)) / lengthSquared;
		if (t < 0) return (int)distanceSquared(px, py, bx, by);
		if (t > 1) return (int)distanceSquared(px, py, ax, ay);
		Vec2 projection = Vec2.add( new Vec2(bx,by), Vec2.sub(new Vec2(ax,ay), new Vec2(bx,by)).scale(t) );
		return (int)distanceSquared(px, py, projection.x, projection.y);
	}

	private float distanceSquared(float ax, float ay, float bx, float by) {
		return (bx - ax) * (bx - ax) + (by - ay) * (by - ay);
	}

	private boolean triangleContainsPoint(float ax, float ay, float bx, float by, float cx, float cy, float px, float py) {
		if( (px > ax && px > bx && px > cx) || (px < ax && px < bx && px < cx) ||
			(py > ay && py > by && py > cy) || (py < ay && py < by && py < cy) ) {
			return false;
		}

		// Compute vectors
		Vec2 v0 = new Vec2(cx-ax,cy-ay);
		Vec2 v1 = new Vec2(bx-ax,by-ay);
		Vec2 v2 = new Vec2(px-ax,py-ay);

		// Compute dot products
		float dot00 = Vec2.dot(v0, v0);
		float dot01 = Vec2.dot(v0, v1);
		float dot02 = Vec2.dot(v0, v2);
		float dot11 = Vec2.dot(v1, v1);
		float dot12 = Vec2.dot(v1, v2);

		// Compute barycentric coordinates
		float invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
		float u = (dot11 * dot02 - dot01 * dot12) * invDenom;
		float v = (dot00 * dot12 - dot01 * dot02) * invDenom;

		// Check if point is in triangle
		return (u >= 0) && (v >= 0) && (u + v < 1);
	}

	private boolean triangleIsInFront(int x, int y, Vec3 a1, Vec3 a2, Vec3 a3, Vec3 b1, Vec3 b2, Vec3 b3) {
		Vec3 PQ = Vec3.sub(a2, a1);
		Vec3 PR = Vec3.sub(a3, a1);
		Vec3 N = Vec3.cross(PQ, PR);
		// N.x * (x-a1.x) + N.y * (y-a1.y) + N.z * (z-a1.z) = 0
		float z1 = ( N.x * (x - a1.x) + N.y * (y - a1.y) ) / -N.z + a1.z;

		PQ = Vec3.sub(b2, b1);
		PR = Vec3.sub(b3, b1);
		N = Vec3.cross(PQ, PR);
		float z2 = ( N.x * (x - b1.x) + N.y * (y - b1.y) ) / -N.z + b1.z;

		return z1 > z2;
	}

}
