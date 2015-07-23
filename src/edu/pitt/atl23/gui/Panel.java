package edu.pitt.atl23.gui;

import edu.pitt.atl23.ShaderProgram;

import edu.pitt.atl23.geometry.*;
import jglsdk.glm.Mat4;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.Color;

import java.awt.Font;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * Created by Andrew T. Lucas on 5/29/2015.
 */
public class Panel {
	private IndexList indices;
	private Cursor cursor;
	private TrueTypeFont monospacedBold20, monospacedBold16;
	private Color orange, blue;
	private ColorData shadowColor, topColor;
	private int width, height;
	private Mat4 orthogonalMatrix;
	private FloatBuffer mat4Buffer;

	public Pane taskBarPane, viewModePane, manipulationPane, selectionPane, editCursorPane, trackPadPane, editColorPane;
	// task bar
	public Field save, load, shortcuts;
	// view mode
	public Field normalView, colorView, wireFrameView;
	// manipulations
	public Field place, remove, merge, fill, extrude, grab;
	// selection mode
	public Field vSelect, lSelect, tSelect;
	// track pad
	public Field trackX, trackY, trackZ;
	// cursor edit
	public EditText editX, editY, editZ;
	public Field hideCursor;
	// color
	public EditText editRed, editGreen, editBlue, editAlpha;
	public ColorData triColor;
	public Field applyColor;


	/** NOTE: the (x,y) origin is the top left corner (while mouse origin is bottom left) **/

	public Panel(int width, int height, Cursor cursor) {
		this.width = width;
		this.height = height;
		this.cursor = cursor;

		// init orthogonal view matrix
		mat4Buffer = BufferUtils.createFloatBuffer(16);
		orthogonalMatrix = new Mat4(1f);
		orthogonalMatrix.matrix[0] = 2f / (float)width;
		orthogonalMatrix.matrix[5] = -2f / (float)height;
		orthogonalMatrix.matrix[12] = -1f;
		orthogonalMatrix.matrix[13] = 1f;

		// init fonts
		monospacedBold20 = new TrueTypeFont(new Font("Monospaced", Font.BOLD, 20), true);
		monospacedBold16 = new TrueTypeFont(new Font("Monospaced", Font.BOLD, 16), true);
		// init colors
		orange = new Color(0.797f, 0.469f, 0.195f);
		blue = new Color(0.406f, 0.590f, 0.730f);
		// init ColorData
		shadowColor = new ColorData(52,52,52,255);
		topColor = new ColorData(60,60,60,255);

		// begin with no triangle selected
		triColor = new ColorData(0,0,0,0);

		initPanes();
	}

	private void initPanes() {
		indices = new IndexList();

		/** Task Bar **/
		taskBarPane = new Pane(0, 0, 565, 50);
		save = new Field(taskBarPane.x+10, taskBarPane.y+10, 100, 30, shadowColor, indices);
		save.addText(32, 5, "Save", monospacedBold16);
		load = new Field(taskBarPane.x+120, taskBarPane.y+10, 100, 30, shadowColor, indices);
		load.addText(32, 5, "Load", monospacedBold16);
		shortcuts = new Field(taskBarPane.x+230, taskBarPane.y+10, 100, 30, shadowColor, indices);
		shortcuts.addText(4, 5, "Shortcuts", monospacedBold16);

		/** View Mode Pane **/
		viewModePane = new Pane(580, -10, 340, 60);
		normalView = new Field(viewModePane.x+10, viewModePane.y+20, 100, 30, shadowColor, indices);
		normalView.addText(20, 5, "Normal", monospacedBold16);
		colorView = new Field(viewModePane.x+120, viewModePane.y+20, 100, 30, shadowColor, indices);
		colorView.addText(26, 5, "Color", monospacedBold16);
		wireFrameView = new Field(viewModePane.x+230, viewModePane.y+20, 100, 30, shadowColor, indices);
		wireFrameView.addText(30, 5, "Wire", monospacedBold16);

		/** Manipulations Pane **/
		manipulationPane = new Pane(15, height-470, 140, 260);
		manipulationPane.addText(10, 5, "Manipulate", monospacedBold20);
		place = new Field(manipulationPane.x+20, manipulationPane.y+40, 100, 30, shadowColor, indices);
		place.addText(24, 5, "Place", monospacedBold16);
		remove = new Field(manipulationPane.x+20, manipulationPane.y+75, 100, 30, shadowColor, indices);
		remove.addText(18, 5, "Remove", monospacedBold16);
		merge = new Field(manipulationPane.x+20, manipulationPane.y+110, 100, 30, shadowColor, indices);
		merge.addText(24, 5, "Merge", monospacedBold16);
		fill = new Field(manipulationPane.x+20, manipulationPane.y+145, 100, 30, shadowColor, indices);
		fill.addText(28, 5, "Fill", monospacedBold16);
		extrude = new Field(manipulationPane.x+20, manipulationPane.y+180, 100, 30, shadowColor, indices);
		extrude.addText(16, 5, "Extrude", monospacedBold16);
		grab = new Field(manipulationPane.x+20, manipulationPane.y+215, 100, 30, shadowColor, indices);

		/** Selection Mode Pane **/
		selectionPane = new Pane(15, height-195, 140, 180);
		selectionPane.addText(18, 5, "Selection", monospacedBold20);
		selectionPane.addText(48, 25, "Mode", monospacedBold20);
		vSelect = new Field(selectionPane.x+20, selectionPane.y+60, 100, 30, shadowColor, indices);
		vSelect.addText(22, 5, "Vertex", monospacedBold16);
		lSelect = new Field(selectionPane.x+20, selectionPane.y+95, 100, 30, shadowColor, indices);
		lSelect.addText(30, 5, "Line", monospacedBold16);
		tSelect = new Field(selectionPane.x+20, selectionPane.y+130, 100, 30, shadowColor, indices);
		tSelect.addText(12, 5, "Triangle", monospacedBold16);

		/** Color Edit Pane **/
		editColorPane = new Pane(width-215, height-590, 210, 175);
		editColorPane.addText(30, 5, "Triangle Color", monospacedBold20);
		editRed = new EditText(editColorPane.x + 115, editColorPane.y + 35, 85, 18, shadowColor, indices);
		editGreen = new EditText(editColorPane.x + 115, editColorPane.y + 60, 85, 18, shadowColor, indices);
		editBlue = new EditText(editColorPane.x + 115, editColorPane.y + 85, 85, 18, shadowColor, indices);
		editAlpha = new EditText(editColorPane.x + 115, editColorPane.y + 110, 85, 18, shadowColor, indices);
		applyColor = new Field(editColorPane.x + 35, editColorPane.y + 135, 140, 30, shadowColor, indices);
		applyColor.addText(16, 5, "Apply Color", monospacedBold16);

		/** Cursor Edit Pane **/
		editCursorPane = new Pane(width-215, height-400, 210, 150);
		editCursorPane.addText(15, 0, "Cursor Position", monospacedBold20);
		editX = new EditText(editCursorPane.x + 115, editCursorPane.y + 35, 85, 18, shadowColor, indices);
		editY = new EditText(editCursorPane.x + 115, editCursorPane.y + 60, 85, 18, shadowColor, indices);
		editZ = new EditText(editCursorPane.x + 115, editCursorPane.y + 85, 85, 18, shadowColor, indices);
		hideCursor = new Field(editCursorPane.x + 35, editCursorPane.y + 110, 140, 30, shadowColor, indices);

		/** Track Pads Pane **/
		trackPadPane = new Pane(width-215, height-235, 210, 220);
		trackX = new Field(trackPadPane.x+10, trackPadPane.y+10, 60, 200, shadowColor, indices);
		trackX.addText(25, 45, "X", monospacedBold20);
		trackY = new Field(trackPadPane.x+75, trackPadPane.y+10, 60, 200, shadowColor, indices);
		trackY.addText(25, 45, "Y", monospacedBold20);
		trackZ = new Field(trackPadPane.x+140, trackPadPane.y+10, 60, 200, shadowColor, indices);
		trackZ.addText(25, 45, "Z", monospacedBold20);
	}

	private void addBox(int x, int y, int w, int h, ColorData cd, IndexList i) {
		Vertex a,b,c,d;
		a = new Vertex( x, y, 0.0f);
		b = new Vertex( x+w, y, 0.0f);
		c = new Vertex( x+w, y+h, 0.0f);
		d = new Vertex( x, y+h, 0.0f);
		// add vertices
		a = i.add(a, false);
		b = i.add(b, false);
		c = i.add(c, false);
		d = i.add(d, false);
		// add triangles
		i.add(new Triangle(a, b, c, cd), false);
		i.add(new Triangle(c, d, a, cd), false);
		// add lines
		i.add(new Line(a, b), false);
		i.add(new Line(b, c), false);
		i.add(new Line(c, d), false);
		i.add(new Line(d, a), false);
	}

	public boolean contains(int x, int y) {
		return viewModePane.contains(x,y) || editCursorPane.contains(x,y) || selectionPane.contains(x,y) ||
			manipulationPane.contains(x,y) || trackPadPane.contains(x,y) || taskBarPane.contains(x,y) ||
			editColorPane.contains(x,y);
	}

	public void render(ShaderProgram program) {
		/** Use program **/
		GL20.glUseProgram(program.theProgram);

		/** Load orthogonal matrix **/
		glUniformMatrix4(program.orthogonalMatrixUnif, false,
				orthogonalMatrix.fillAndFlipBuffer(mat4Buffer));

		/** Draw triangles **/
		indices.bindColorBuffer(program);
		indices.drawTriangles();

		/** Draw text **/
		glUseProgram(0);
		// uses the projection matrix that is built into openGL
		manipulationPane.renderText(orange);
		selectionPane.renderText(orange);
		editCursorPane.renderText(orange);
		editColorPane.renderText(orange);
		editX.renderText();
		editY.renderText();
		editZ.renderText();
		editRed.renderText();
		editGreen.renderText();
		editBlue.renderText();
		editAlpha.renderText();
	}

	public void renderFocus(ShaderProgram program, int focus, boolean grabbing, boolean renderCursor) {
		glUseProgram(0);
		if(focus == 1) {
			monospacedBold16.drawString(editX.x-105, editX.y-2, "X:" + String.format("%+.4f", cursor.getx()), orange);
		} else {
			monospacedBold16.drawString(editX.x-105, editX.y-2, "X:" + String.format("%+.4f", cursor.getx()), blue);
		}
		if(focus == 2) {
			monospacedBold16.drawString(editY.x-105, editY.y-2, "Y:" + String.format("%+.4f", cursor.gety()), orange);
		} else {
			monospacedBold16.drawString(editY.x-105, editY.y-2, "Y:" + String.format("%+.4f", cursor.gety()), blue);
		}
		if(focus == 3) {
			monospacedBold16.drawString(editZ.x - 105, editZ.y - 2, "Z:" + String.format("%+.4f", cursor.getz()), orange);
		} else {
			monospacedBold16.drawString(editZ.x - 105, editZ.y - 2, "Z:" + String.format("%+.4f", cursor.getz()), blue);
		}
		if(focus == 4) {
			trackX.renderText(orange);
		} else {
			trackX.renderText(blue);
		}
		if(focus == 5) {
			trackY.renderText(orange);
		} else {
			trackY.renderText(blue);
		}
		if(focus == 6) {
			trackZ.renderText(orange);
		} else {
			trackZ.renderText(blue);
		}
		if(focus == 7) {
			place.renderText(orange);
		} else {
			place.renderText(blue);
		}
		if(focus == 8) {
			remove.renderText(orange);
		} else {
			remove.renderText(blue);
		}
		if(focus == 9) {
			merge.renderText(orange);
		} else {
			merge.renderText(blue);
		}
		if(focus == 10) {
			fill.renderText(orange);
		} else {
			fill.renderText(blue);
		}
		if(focus == 11) {
			extrude.renderText(orange);
		} else {
			extrude.renderText(blue);
		}
		if(focus == 12) {
			if(grabbing) {
				monospacedBold16.drawString(grab.x+14, grab.y+5, "Release", orange);
				grab.renderOutline(program);
			} else {
				monospacedBold16.drawString(grab.x+28, grab.y+5, "Grab", orange);
			}
		} else {
			if(grabbing) {
				monospacedBold16.drawString(grab.x+18, grab.y+5, "Release", blue);
				grab.renderOutline(program);
			} else {
				monospacedBold16.drawString(grab.x+28, grab.y+5, "Grab", blue);
			}
		}
		if(focus == 13) {
			vSelect.renderText(orange);
		} else {
			vSelect.renderText(blue);
		}
		if(focus == 14) {
			lSelect.renderText(orange);
		} else {
			lSelect.renderText(blue);
		}
		if(focus == 15) {
			tSelect.renderText(orange);
		} else {
			tSelect.renderText(blue);
		}
		if(focus == 16) {
			normalView.renderText(orange);
		} else {
			normalView.renderText(blue);
		}
		if(focus == 17) {
			colorView.renderText(orange);
		} else {
			colorView.renderText(blue);
		}
		if(focus == 18) {
			wireFrameView.renderText(orange);
		} else {
			wireFrameView.renderText(blue);
		}
		if(focus == 19) {
			if(!renderCursor) {
				monospacedBold16.drawString(hideCursor.x+16, hideCursor.y+5, "Show Cursor", orange);
				hideCursor.renderOutline(program);
			} else {
				monospacedBold16.drawString(hideCursor.x+16, hideCursor.y+5, "Hide Cursor", orange);
			}
		} else {
			if(!renderCursor) {
				monospacedBold16.drawString(hideCursor.x+16, hideCursor.y+5, "Show Cursor", blue);
				hideCursor.renderOutline(program);
			} else {
				monospacedBold16.drawString(hideCursor.x+16, hideCursor.y+5, "Hide Cursor", blue);
			}
		}
		if(focus == 20) {
			save.renderText(orange);
		} else {
			save.renderText(blue);
		}
		if(focus == 21) {
			load.renderText(orange);
		} else {
			load.renderText(blue);
		}
		if(focus == 22) {
			shortcuts.renderText(orange);
		} else {
			shortcuts.renderText(blue);
		}
		if(focus == 23) {
			monospacedBold16.drawString(editRed.x - 105, editRed.y - 2, "Red:   " + String.format("%03d", triColor.r), orange);
		} else {
			monospacedBold16.drawString(editRed.x - 105, editRed.y - 2, "Red:   " + String.format("%03d", triColor.r), blue);
		}
		if(focus == 24) {
			monospacedBold16.drawString(editGreen.x - 105, editGreen.y - 2, "Green: " + String.format("%03d", triColor.g), orange);
		} else {
			monospacedBold16.drawString(editGreen.x - 105, editGreen.y - 2, "Green: " + String.format("%03d", triColor.g), blue);
		}
		if(focus == 25) {
			monospacedBold16.drawString(editBlue.x - 105, editBlue.y - 2, "Blue:  " + String.format("%03d", triColor.b), orange);
		} else {
			monospacedBold16.drawString(editBlue.x - 105, editBlue.y - 2, "Blue:  " + String.format("%03d", triColor.b), blue);
		}
		if(focus == 26) {
			monospacedBold16.drawString(editAlpha.x - 105, editAlpha.y - 2, "Alpha: " + String.format("%03d", triColor.a), orange);
		} else {
			monospacedBold16.drawString(editAlpha.x - 105, editAlpha.y - 2, "Alpha: " + String.format("%03d", triColor.a), blue);
		}
		if(focus == 27) {
			applyColor.renderText(orange);
		} else {
			applyColor.renderText(blue);
		}
	}

	public void renderViewMode(ShaderProgram program, int mode) {
		switch (mode) {
			case 0:
				normalView.renderOutline(program);
				break;
			case 1:
				colorView.renderOutline(program);
				break;
			case 2:
				wireFrameView.renderOutline(program);
				break;
		}
	}

	public void renderSelectionMode(ShaderProgram program, int mode) {
		switch (mode) {
			case 0:
				vSelect.renderOutline(program);
				break;
			case 1:
				lSelect.renderOutline(program);
				break;
			case 2:
				tSelect.renderOutline(program);
				break;
		}
	}

	public void renderEditOutline(ShaderProgram program, int cursorEditOutline) {
		switch (cursorEditOutline) {
			case 0:
				editX.renderOutline(program);
				break;
			case 1:
				editY.renderOutline(program);
				break;
			case 2:
				editZ.renderOutline(program);
				break;
			case 3:
				editRed.renderOutline(program);
				break;
			case 4:
				editGreen.renderOutline(program);
				break;
			case 5:
				editBlue.renderOutline(program);
				break;
			case 6:
				editAlpha.renderOutline(program);
				break;
		}
	}

	public void resize(int w, int h) {
		width = w;
		height = h;
		initPanes();

		orthogonalMatrix.matrix[0] = 2f / (float)width;
		orthogonalMatrix.matrix[5] = -2f / (float)height;

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, width, height, 0, 1, -1);
	}

	public void delete() {
		glDisableVertexAttribArray(0);
		indices.delete();
		trackX.delete();
		trackY.delete();
		trackZ.delete();
		editX.delete();
		editY.delete();
		editZ.delete();
		vSelect.delete();
		lSelect.delete();
		tSelect.delete();
		place.delete();
		remove.delete();
		merge.delete();
		fill.delete();
		extrude.delete();
		grab.delete();
		normalView.delete();
		colorView.delete();
		wireFrameView.delete();
		save.delete();
		load.delete();
		shortcuts.delete();
		hideCursor.delete();
	}

	public class Pane {
		public int x, y, w, h;
		ArrayList<TextBox> textBoxes;

		public Pane(int x, int y, int w, int h) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			textBoxes = new ArrayList<>();

			Vertex a,b,c;
			// Shadow
			addBox(x-10, y+10, w, h, shadowColor, indices);
			a = new Vertex(x-10, y+10, 0);
			b = new Vertex(x, y, 0);
			c = new Vertex(x, y+10, 0);
			a = indices.add(a, false);
			b = indices.add(b, false);
			c = indices.add(c, false);
			indices.add(new Triangle(a, b, c, shadowColor), false);
			a = new Vertex(x+w, y+h, 0);
			b = new Vertex(x+w-10, y+h+10, 0);
			c = new Vertex(x+w-10, y+h, 0);
			a = indices.add(a, false);
			b = indices.add(b, false);
			c = indices.add(c, false);
			indices.add(new Triangle(a, b, c, shadowColor), false);
			// Top
			addBox(x, y, w, h, topColor, indices);
		}

		public boolean contains(int x, int y) {
			return((x>this.x)&&(x<this.x+w)&&(height-y>this.y)&&(height-y<this.y+h));
		}

		public void addText(int x, int y, String s, TrueTypeFont f) {
			textBoxes.add(new TextBox(x,y,s,f));
		}

		public void renderText(Color c) {
			for(TextBox tb: textBoxes) {
				tb.render(x,y,c);
			}
		}
	}

	public class Field {
		int x, y, w, h;
		IndexList indices;
		TextBox text;

		public Field(int x, int y, int w, int h, ColorData cd, IndexList i) {
			indices = new IndexList();

			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			// input box
			addBox(x, y, w, h, cd, i);
			// focus box (only renders lines)
			addBox(x - 1, y - 1, w + 2, h + 2, cd, indices);
		}

		public boolean contains(int x, int y) {
			return (x > this.x) && (x < this.x + w) && (height - y > this.y) && (height - y < this.y + h);
		}

		public void addText(int x, int y, String s, TrueTypeFont f) {
			text = new TextBox(x,y,s,f);
		}

		public void renderText(Color c) {
			text.render(x,y,c);
		}

		public void renderOutline(ShaderProgram program) {
			GL20.glUseProgram(program.theProgram);

			/** Load orthogonal matrix **/
			glUniformMatrix4(program.orthogonalMatrixUnif, false,
					orthogonalMatrix.fillAndFlipBuffer(mat4Buffer));

			glUniform4f(program.baseColorUnif, 0.797f, 0.469f, 0.195f, 1.0f);
			indices.drawLines();
			GL20.glUseProgram(0);
		}

		public void delete() {
			indices.delete();
		}
	}

	public class EditText extends Field {
		private StringBuilder sb;

		public EditText(int x, int y, int w, int h, ColorData cd, IndexList i) {
			super(x, y, w, h, cd, i);

			sb = new StringBuilder("");
		}

		public void renderText() {
			monospacedBold16.drawString(x+2, y-2, sb.toString(), orange);
		}

		public void add(char c) {
			if(sb.length() < 8) {
				sb.append(c);
			}
		}

		public float read() {
			String s = sb.toString();
			if(s.length() == 0){
				return 0f;
			} else {
				float result = Float.parseFloat(s);
				sb = new StringBuilder("");
				return result;
			}
		}

		public void backspace() {
			if(sb.length() > 0) sb = new StringBuilder(sb.subSequence(0, sb.length() - 1));
		}

		public void delete() {
			indices.delete();
		}
	}

	public class TextBox {
		int textX,textY;
		String s;
		TrueTypeFont font;

		public TextBox(int x, int y, String s, TrueTypeFont f) {
			textX = x;
			textY = y;
			this.s = s;
			font = f;
		}

		public void render(int x, int y, Color c){
			font.drawString(x + textX, y + textY, s, c);
		}
	}
}
