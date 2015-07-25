package edu.pitt.atl23;

/**
 * Created by Andrew T. Lucas on 3/8/2015.
 */

import edu.pitt.atl23.geometry.*;
import edu.pitt.atl23.gui.Cursor;
import edu.pitt.atl23.gui.Panel;
import jglsdk.glm.Mat4;
import jglsdk.glm.Vec3;
import jglsdk.glm.Vec4;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL32;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;

import javax.swing.*;
import java.awt.Font;
import java.io.*;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Main extends LWJGLWindow {

	private static int WIDTH = 1200;
	private static int HEIGHT = 600;
	private static int xoffset = -100;
	private static int yoffset = 0;

	private int viewerWidth, viewerHeight, guiWidth, guiHeight;

	private Camera camera;
	private Cursor cursor;
	private Panel panel;
	private Mesh plane;
	private Mesh axes;
	private Mesh theModel;
	private Mesh selection;
	private Mesh colorSelection;
	private ShaderProgram colorUniformProgram, skeletonProgram, colorArrayProgram, colorUniform2DProgram, colorArray2DProgram;
	private FloatBuffer mat4Buffer = BufferUtils.createFloatBuffer(16);

	// font
	private TrueTypeFont monospacedBold36;
	private Color orange, blue;

	// for coloring triangles
	private Triangle currentTri;

	/** 0: viewer
	 1: cursor edit x
	 2: cursor edit y
	 3: cursor edit z
	 4: track x
	 5: track y
	 6: track z
	 7: place
	 8: remove
	 9: merge
	 10: fill
	 11: extrude
	 12: grab/release
	 13: vertex selection
	 14: line selection
	 15: triangle selection
	 16: normal view
	 17: color view
	 18: wireframe view
	 19: hide/show cursor
	 20: save
	 21: load
	 22: shortcuts
	 23: color edit red
	 24: color edit green
	 25: color edit blue
	 26: color edit alpha
	 27: add color
	 **/
	private int focus;

	/** 0: vertex selection
	 1: line selection
	 2: triangle selection
	 3: bone selection
	 **/
	private int selectionMode;

	/** 0: normal
	 1: color
	 2: wireframe
	 **/
	private int viewMode;

	/** 0: x
	 1: y
	 2: z
	 3: red
	 4: green
	 5: blue
	 6: alpha
	 **/
	private int editOutline;

	/** -1: none
	 0: edit color
	 1: edit cursor
	 **/
	private int currentPane;

	private boolean grabbing;
	private boolean renderCursor;

	private int tickCount;

	public static void main(String[] args) {
		new Main().start(WIDTH, HEIGHT);
	}

	@Override
	protected void onStart() {
		// currently the gui viewport must occupy the entire screen
		guiWidth = WIDTH;
		guiHeight = HEIGHT;

		tickCount = 0;

		focus = 0;
		selectionMode = 0;
		editOutline = 0;
		currentPane = -1;
		grabbing = false;
		renderCursor = true;

		camera = new Camera(0.5f, 100.0f);
		cursor = new Cursor();
		panel = new Panel(guiWidth, guiHeight, cursor);
		colorUniformProgram = new ShaderProgram("ThreeDim.vert", null, "ColorUniform.frag");
		colorArrayProgram = new ShaderProgram("ThreeDim.vert", null, "ColorArrayLight.frag");
		colorUniform2DProgram = new ShaderProgram("TwoDim.vert", null, "ColorUniform.frag");
		colorArray2DProgram = new ShaderProgram("TwoDim.vert", null, "ColorArray.frag");
        skeletonProgram = new ShaderProgram("Bones.vert", "Bones.geo", "ColorUniform.frag");

		monospacedBold36 = new TrueTypeFont(new Font("Monospaced", Font.BOLD, 36), true);
		orange = new Color(0.793f, 0.375f, 0.176f);
		blue = new Color(0.406f, 0.590f, 0.730f);

		selection = new Mesh();
		colorSelection = new Mesh();

		/** Init the model itself **/
		theModel = new Mesh();
		theModel.addSkeleton();

		/********** Initialize meshes **********/
		Vertex a,b,c,d,e,f,g,h,ua,ub,uc,ud,ue,uf,ug,uh;

		/** Init plane and axes **/
		plane = new Mesh();
		axes = new Mesh();

		a = new Vertex(-10.0f, 0.0f,-10.0f);
		b = new Vertex( 10.0f, 0.0f,-10.0f);
		c = new Vertex( 10.0f, 0.0f, 10.0f);
		d = new Vertex(-10.0f, 0.0f, 10.0f);

		e = new Vertex(  0.0f, 0.0f,-10.0f);
		f = new Vertex( 10.0f, 0.0f,  0.0f);
		g = new Vertex(  0.0f, 0.0f, 10.0f);
		h = new Vertex(-10.0f, 0.0f,  0.0f);

		ua = new Vertex(-10.0f, 0.05f,-10.0f);
		ub = new Vertex( 10.0f, 0.05f,-10.0f);
		uc = new Vertex( 10.0f, 0.05f, 10.0f);
		ud = new Vertex(-10.0f, 0.05f, 10.0f);

		ue = new Vertex(  0.0f, 0.05f,-10.0f);
		uf = new Vertex( 10.0f, 0.05f,  0.0f);
		ug = new Vertex(  0.0f, 0.05f, 10.0f);
		uh = new Vertex(-10.0f, 0.05f,  0.0f);

		// add vertices
		plane.add(a);
		plane.add(b);
		plane.add(c);
		plane.add(d);

		axes.add(a);
		axes.add(b);
		axes.add(c);
		axes.add(d);
		axes.add(e);
		axes.add(f);
		axes.add(g);
		axes.add(h);
		axes.add(ua);
		axes.add(ub);
		axes.add(uc);
		axes.add(ud);
		axes.add(ue);
		axes.add(uf);
		axes.add(ug);
		axes.add(uh);

		// add triangles
		plane.add(new Triangle(a, b, c, new ColorData()));
		plane.add(new Triangle(c, d, a, new ColorData()));

		axes.add(new Triangle(a,b,ub, new ColorData()));
		axes.add(new Triangle(ub,ua,a, new ColorData()));
		axes.add(new Triangle(b,c,uc, new ColorData()));
		axes.add(new Triangle(uc,ub,b, new ColorData()));
		axes.add(new Triangle(c,d,ud, new ColorData()));
		axes.add(new Triangle(ud,uc,c, new ColorData()));
		axes.add(new Triangle(d,a,ua, new ColorData()));
		axes.add(new Triangle(ua,ud,d, new ColorData()));
		axes.add(new Triangle(e,g,ug, new ColorData()));
		axes.add(new Triangle(ug,ue,e, new ColorData()));
		axes.add(new Triangle(f,h,uh, new ColorData()));
		axes.add(new Triangle(uh,uf,f, new ColorData()));

		/********** OpenGL enables **********/
		glEnable(GL_VERTEX_PROGRAM_POINT_SIZE);
		glPointSize(4.0f);

		/*GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_LIGHTING);*/


	}

	@Override
	protected void update() {
		float lastFrameDuration = getLastFrameDuration() * 5 / 1000.0f;
		if(tickCount++ > 100) {
			tickCount = 0;
		}
		cursor.dx = 0;
		cursor.dy = 0;
		cursor.dz = 0;

		while(Mouse.next()) {

			int x = Mouse.getEventX();
			int y = Mouse.getEventY();

			// Check focus
			if(!panel.contains(x, y)) {
				focus = 0;
			} else {
				focus = -1;
			}

			// for tabbing
			if(panel.editColorPane.contains(x,y)) {
				currentPane = 0;
			} else if(panel.editCursorPane.contains(x,y)) {
				currentPane = 1;
			} else {
				currentPane = -1;
			}

			// set focus
			if (panel.editX.contains(x, y)) {
				focus = 1;
			} else if (panel.editY.contains(x, y)) {
				focus = 2;
			} else if (panel.editZ.contains(x, y)) {
				focus = 3;
			} else if (panel.trackX.contains(x, y)) {
				focus = 4;
			} else if (panel.trackY.contains(x, y)) {
				focus = 5;
			} else if (panel.trackZ.contains(x, y)) {
				focus = 6;
			} else if (panel.place.contains(x, y)) {
				focus = 7;
			} else if (panel.remove.contains(x, y)) {
				focus = 8;
			} else if (panel.merge.contains(x, y)) {
				focus = 9;
			} else if (panel.fill.contains(x, y)) {
				focus = 10;
			} else if (panel.extrude.contains(x, y)) {
				focus = 11;
			} else if (panel.grab.contains(x, y)) {
				focus = 12;
			} else if (panel.vSelect.contains(x, y)) {
				focus = 13;
			} else if (panel.lSelect.contains(x, y)) {
				focus = 14;
			} else if (panel.tSelect.contains(x, y)) {
				focus = 15;
			} else if (panel.normalView.contains(x, y)) {
				focus = 16;
			} else if (panel.colorView.contains(x, y)) {
				focus = 17;
			} else if (panel.wireFrameView.contains(x, y)) {
				focus = 18;
			} else if (panel.hideCursor.contains(x, y)) {
				focus = 19;
			} else if (panel.save.contains(x, y)) {
				focus = 20;
			} else if (panel.load.contains(x, y)) {
				focus = 21;
			} else if (panel.shortcuts.contains(x, y)) {
				focus = 22;
			} else if (panel.editRed.contains(x, y)) {
				focus = 23;
			} else if (panel.editGreen.contains(x, y)) {
				focus = 24;
			} else if (panel.editBlue.contains(x, y)) {
				focus = 25;
			} else if (panel.editAlpha.contains(x, y)) {
				focus = 26;
			} else if (panel.applyColor.contains(x, y)) {
				focus = 27;
			}

			if(Mouse.isButtonDown(0)) {
				int d = Mouse.getEventDY();
				int sx = x - xoffset;
				int sy = y - yoffset;

				if(focus == 0) {
					if (selectionMode == 0) {
						Vertex selected = theModel.getSelectedVertex(sx, sy, viewerWidth, viewerHeight, 6,
							camera.getViewMatrix(), camera.getPerspectiveMatrix());
						if (selected != null) {
							if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
								deselectVertex(selected);
							} else {
								selectVertex(selected);
							}
						}
					} else if (selectionMode == 1) {
						Line selected = theModel.getSelectedLines(sx, sy, viewerWidth, viewerHeight, 6,
							camera.getViewMatrix(), camera.getPerspectiveMatrix());
						if (selected != null) {
							if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
								deselectLine(selected);
							} else {
								selectLine(selected);
							}
						}
					} else if (selectionMode == 2) {
						Triangle selected = theModel.getSelectedTriangle(sx, sy, viewerWidth, viewerHeight, camera.getViewMatrix(), camera.getPerspectiveMatrix());
						if (selected != null) {
							if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
								deselectTriangle(selected);
							} else {
								selectTriangle(selected);
							}
						}
					} else if (selectionMode == 3) {
						Vertex selected = theModel.getSelectedBoneEnd(sx, sy, viewerWidth, viewerHeight, 6,
								camera.getViewMatrix(), camera.getPerspectiveMatrix());
						if (selected != null) {
							if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
								deselectVertex(selected);
							} else {
								selectVertex(selected);
							}
						}
					}
				} else if(focus == 1) {
					editOutline = 0;
				} else if(focus == 2) {
					editOutline = 1;
				} else if(focus == 3) {
					editOutline = 2;
				} else if(focus == 4) {
					if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
						camera.target.x = camera.target.x + 0.1f * d * lastFrameDuration;
						cursor.dx = d * 0.1f * lastFrameDuration;
					} else {
						camera.target.x = camera.target.x + 1.0f * d * lastFrameDuration;
						cursor.dx = d * 1.0f * lastFrameDuration;
					}
				} else if(focus == 5) {
					if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
						camera.target.y = camera.target.y + 0.1f * d * lastFrameDuration;
						cursor.dy = d * 0.1f * lastFrameDuration;
					} else {
						camera.target.y = camera.target.y + 1.0f * d * lastFrameDuration;
						cursor.dy = d * 1.0f * lastFrameDuration;
					}
				} else if(focus == 6) {
					if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
						camera.target.z = camera.target.z + 0.1f * d * lastFrameDuration;
						cursor.dz = d * 0.1f * lastFrameDuration;
					} else {
						camera.target.z = camera.target.z + 1.0f * d * lastFrameDuration;
						cursor.dz = d * 1.0f * lastFrameDuration;
					}
				} else if(focus == 7) {
					Vertex a = new Vertex(cursor.getx(), cursor.gety(), cursor.getz());
					theModel.add(a);
				} else if(focus == 8) {
					if(selectionMode == 0) {
						removeSelectedVertices();
					} else if(selectionMode == 1) {
						removeSelectedLines();
					} else if(selectionMode == 2) {
						removeSelectedTriangles();
					} else if(selectionMode == 3) {
						removeSelectedBoneNodes();
					}
				} else if(focus == 9) {
					if(selectionMode == 0) {
						mergeSelectedVertices();
					}
				} else if(focus == 10) {
					if(selectionMode == 0) {
						fillSelectedVertices();
					} else if(selectionMode == 1) {
						fillSelectedLines();
					}
				} else if(focus == 11) {
					if(selectionMode == 0) {
						extrudeSelectedVertices();
					} else if(selectionMode == 1) {
						extrudeSelectedLines();
					} else if(selectionMode == 2) {
						extrudeSelectedTriangles();
					}
				} else if(focus == 12) {
					grabbing = !grabbing;
				} else if(focus == 13) {
					selectionMode = 0;
					clearSelection();
				} else if(focus == 14) {
					selectionMode = 1;
					clearSelection();
				} else if(focus == 15) {
					selectionMode = 2;
					clearSelection();
				} else if(focus == 16) {
					viewMode = 0;
				} else if(focus == 17) {
					viewMode = 1;
				} else if(focus == 18) {
					viewMode = 2;
				} else if(focus == 19) {
					renderCursor = !renderCursor;
				} else if(focus == 20) {
					save();
				} else if(focus == 21) {
					load();
				} else if(focus == 23) {
					editOutline = 3;
				} else if(focus == 24) {
					editOutline = 4;
				} else if(focus == 25) {
					editOutline = 5;
				} else if(focus == 26) {
					editOutline = 6;
				} else if(focus == 27) {
					int r = (int)panel.editRed.read();
					r = r >= 0 ? r : 0;
					r = r < 256 ? r : 255;
					int g = (int)panel.editGreen.read();
					g = g >= 0 ? g : 0;
					g = g < 256 ? g : 255;
					int b = (int)panel.editBlue.read();
					b = b >= 0 ? b : 0;
					b = b < 256 ? b : 255;
					int a = (int)panel.editAlpha.read();
					a = a >= 0 ? a : 0;
					a = a < 256 ? a : 255;
					ArrayList<Triangle> at = selection.getTris();
					if(at.size() > 0) {
						panel.triColor = new ColorData(r, g, b, a);
						for (Triangle t: at) {
							theModel.applyColor(t, panel.triColor);
						}
					}
				}
				camera.validate();
				if(grabbing) {
					if(selectionMode == 3) {
						cursor.update(theModel.getSkeletonMesh(), selection);
					} else {
						cursor.update(theModel, selection);
					}
				} else {
					cursor.update();
				}
			} else if (Mouse.isButtonDown(1)) {
				if(focus == 0) {
					if (grabbing) {
						grabbing = false;
						clearSelection();
					} else {
						grabbing = true;
					}
				}
			} else if(Mouse.isButtonDown(2)) {
				if(focus == 0) {
					int dy = Mouse.getEventDY();
					int dx = Mouse.getEventDX();
					if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
						camera.spherePos.y = camera.spherePos.y + 1.0f * dy * lastFrameDuration;
					} else {
						camera.spherePos.y = camera.spherePos.y + 2.0f * dy * lastFrameDuration;
					}
					if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
						camera.spherePos.x = camera.spherePos.x + 1.0f * dx * lastFrameDuration;
					} else {
						camera.spherePos.x = camera.spherePos.x + 2.0f * dx * lastFrameDuration;
					}
				}
			}

			// Check zoom
			int dwheel = Mouse.getDWheel();
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
				camera.spherePos.z = camera.spherePos.z - 0.005f * dwheel * lastFrameDuration;
				camera.validate();
			} else {
				camera.spherePos.z = camera.spherePos.z - 0.02f * dwheel * lastFrameDuration;
				camera.validate();
			}
		}

		while ( Keyboard.next() ) {
			if ( Keyboard.getEventKeyState() ) {
				char c = Keyboard.getEventCharacter();
				if(Character.isDigit(c) || c=='.' || c=='-' || c=='+') {
					if(editOutline == 0) {
						panel.editX.add(c);
					} else if(editOutline == 1) {
						panel.editY.add(c);
					} else if(editOutline == 2) {
						panel.editZ.add(c);
					}
					if(c != '.') {
						if(editOutline == 3) {
							panel.editRed.add(c);
						} else if(editOutline == 4) {
							panel.editGreen.add(c);
						} else if(editOutline == 5) {
							panel.editBlue.add(c);
						} else if(editOutline == 6) {
							panel.editAlpha.add(c);
						}
					}
				}

				switch ( Keyboard.getEventKey() ) {
					case Keyboard.KEY_SPACE:
						Vertex v = new Vertex(cursor.getx(), cursor.gety(), cursor.getz());
						if(selectionMode == 0) {
							theModel.add(v);
						} else if(selectionMode == 3) {
							theModel.addBoneNode(v);
						}
						break;
					case Keyboard.KEY_TAB:
						if(focus == 0) {
							clearSelection();
							if(selectionMode < 3) {
								selectionMode++;
							} else {
								selectionMode = 0;
							}
						} else if(currentPane == 0) {
							if(editOutline < 3) {
								editOutline = 3;
							} else {
								editOutline++;
								if (editOutline == 7) {
									editOutline = 3;
								}
							}
						} else if(currentPane == 1) {
							if(editOutline > 2) {
								editOutline = 0;
							} else {
								editOutline++;
								if (editOutline == 3) {
									editOutline = 0;
								}
							}
						}
						break;
					case Keyboard.KEY_RETURN:
						if (editOutline == 0) {
							float f = panel.editX.read();
							if(cursor.translateToX(f)) {
								camera.target.x = f;
							}
							editOutline++;
						} else if (editOutline == 1) {
							float f = panel.editY.read();
							if(cursor.translateToY(f)) {
								camera.target.y = f;
							}
							editOutline++;
						} else if (editOutline == 2) {
							float f = panel.editZ.read();
							if(cursor.translateToZ(f)) {
								camera.target.z = f;
							}
							editOutline = 0;
						} else if (editOutline == 3) {
							int i = (int)panel.editRed.read();
							if(currentTri != null) {
								if (i >= 0 && i < 256) {
									panel.triColor.r = i;
									for(Triangle t: selection.getTris()) {
										theModel.applyColor(t, new ColorData(i, currentTri.color.g, currentTri.color.b, currentTri.color.a));
									}
								}
							}
							editOutline++;
						} else if (editOutline == 4) {
							int i = (int)panel.editGreen.read();
							if(currentTri != null) {
								if (i >= 0 && i < 256) {
									panel.triColor.g = i;
									for(Triangle t: selection.getTris()) {
										theModel.applyColor(t, new ColorData(currentTri.color.r, i, currentTri.color.b, currentTri.color.a));
									}
								}
							}
							editOutline++;
						} else if (editOutline == 5) {
							int i = (int)panel.editBlue.read();
							if(currentTri != null) {
								if (i >= 0 && i < 256) {
									panel.triColor.b = i;
									for(Triangle t: selection.getTris()) {
										theModel.applyColor(t, new ColorData(currentTri.color.r, currentTri.color.g, i, currentTri.color.a));
									}
								}
							}
							editOutline++;
						} else if (editOutline == 6) {
							int i = (int)panel.editAlpha.read();
							if(currentTri != null) {
								if (i >= 0 && i < 256) {
									panel.triColor.a = i;
									for(Triangle t: selection.getTris()) {
										theModel.applyColor(t, new ColorData(currentTri.color.r, currentTri.color.g, currentTri.color.b, i));
									}
								}
							}
							editOutline = 3;
						}
						break;
					case Keyboard.KEY_BACK:
						if (editOutline == 0) {
							panel.editX.backspace();
						} else if (editOutline == 1) {
							panel.editY.backspace();
						} else if (editOutline == 2) {
							panel.editZ.backspace();
						} else if(editOutline == 3) {
							panel.editRed.backspace();
						} else if(editOutline == 4) {
							panel.editGreen.backspace();
						} else if(editOutline == 5) {
							panel.editBlue.backspace();
						} else if(editOutline == 6) {
							panel.editAlpha.backspace();
						}
						break;
					case Keyboard.KEY_F:
						if(focus == 0) {
							if(selectionMode == 0) {
								fillSelectedVertices();
							} else if(selectionMode == 1) {
								fillSelectedLines();
							} else if(selectionMode == 3) {
								fillSelectedBoneNodes();
							}
						}
						break;
					case Keyboard.KEY_R:
						if(focus == 0) {
							if(selectionMode == 0) {
								removeSelectedVertices();
							} else if(selectionMode == 1) {
								removeSelectedLines();
							} else if(selectionMode == 2) {
								removeSelectedTriangles();
							} else if(selectionMode == 3) {
								removeSelectedBoneNodes();
							}
						}
						break;
					case Keyboard.KEY_X:
						if(focus == 0) {
							if(selectionMode == 0) {
								extrudeSelectedVertices();
							} else if(selectionMode == 1) {
								extrudeSelectedLines();
							} else if(selectionMode == 2) {
								extrudeSelectedTriangles();
							}
						}
						break;
					case Keyboard.KEY_V:
						if(Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU)) {
							viewMode = (viewMode + 1) % 3;
						} else {
							if(focus == 0 && selectionMode == 0) {
								mergeSelectedVertices();
							}
						}
						break;
					case Keyboard.KEY_C:
						if(Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU)) {
							renderCursor = !renderCursor;
						} else {
							// change tri color
						}
						break;
					case Keyboard.KEY_N:
						if(focus == 0) {
							flipNormals();
						}
						break;
					case Keyboard.KEY_ESCAPE:
						leaveMainLoop();
						break;
				}
			}
		}

		if(focus == 0) {
			// Move cursor with qawsed

			// for moving the cursor relative to the camera
			float dirX = camera.target.x - camera.cameraPt.x;
			float dirZ = camera.target.z - camera.cameraPt.z;
			float mag = (float) Math.sqrt(dirX * dirX + dirZ * dirZ);
			dirX = dirX / mag;
			dirZ = dirZ / mag;
			float perpDirX = -dirZ;
			float perpDirZ = dirX;

			if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
					camera.target.x = camera.target.x + 0.1f * dirX * lastFrameDuration;
					cursor.dx = 0.1f * dirX * lastFrameDuration;
					camera.target.z = camera.target.z + 0.1f * dirZ * lastFrameDuration;
					cursor.dz = 0.1f * dirZ * lastFrameDuration;
				} else {
					camera.target.x = camera.target.x + 1.0f * dirX * lastFrameDuration;
					cursor.dx = 1.0f * dirX * lastFrameDuration;
					camera.target.z = camera.target.z + 1.0f * dirZ * lastFrameDuration;
					cursor.dz = 1.0f * dirZ * lastFrameDuration;
				}
			} else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
					camera.target.x = camera.target.x - 0.1f * dirX * lastFrameDuration;
					cursor.dx = -0.1f * dirX * lastFrameDuration;
					camera.target.z = camera.target.z - 0.1f * dirZ * lastFrameDuration;
					cursor.dz = -0.1f * dirZ * lastFrameDuration;
				} else {
					camera.target.x = camera.target.x - 1.0f * dirX * lastFrameDuration;
					cursor.dx = -1.0f * dirX * lastFrameDuration;
					camera.target.z = camera.target.z - 1.0f * dirZ * lastFrameDuration;
					cursor.dz = -1.0f * dirZ * lastFrameDuration;
				}
			} else if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
					camera.target.x = camera.target.x + 0.1f * perpDirX * lastFrameDuration;
					cursor.dx = 0.1f * perpDirX * lastFrameDuration;
					camera.target.z = camera.target.z + 0.1f * perpDirZ * lastFrameDuration;
					cursor.dz = 0.1f * perpDirZ * lastFrameDuration;
				} else {
					camera.target.x = camera.target.x + 1.0f * perpDirX * lastFrameDuration;
					cursor.dx = 1.0f * perpDirX * lastFrameDuration;
					camera.target.z = camera.target.z + 1.0f * perpDirZ * lastFrameDuration;
					cursor.dz = 1.0f * perpDirZ * lastFrameDuration;
				}
			} else if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
					camera.target.x = camera.target.x - 0.1f * perpDirX * lastFrameDuration;
					cursor.dx = -0.1f * perpDirX * lastFrameDuration;
					camera.target.z = camera.target.z - 0.1f * perpDirZ * lastFrameDuration;
					cursor.dz = -0.1f * perpDirZ * lastFrameDuration;
				} else {
					camera.target.x = camera.target.x - 1.0f * perpDirX * lastFrameDuration;
					cursor.dx = -1.0f * perpDirX * lastFrameDuration;
					camera.target.z = camera.target.z - 1.0f * perpDirZ * lastFrameDuration;
					cursor.dz = -1.0f * perpDirZ * lastFrameDuration;
				}
			} else if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
					camera.target.y = camera.target.y - 0.1f * lastFrameDuration;
					cursor.dy = -0.1f * lastFrameDuration;
				} else {
					camera.target.y = camera.target.y - 1.0f * lastFrameDuration;
					cursor.dy = -1.0f * lastFrameDuration;
				}
			} else if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
					camera.target.y = camera.target.y + 0.1f * lastFrameDuration;
					cursor.dy = 0.1f * lastFrameDuration;
				} else {
					camera.target.y = camera.target.y + 1.0f * lastFrameDuration;
					cursor.dy = 1.0f * lastFrameDuration;
				}
			} else if (Keyboard.isKeyDown(Keyboard.KEY_Z)) {
				if(selectionMode < 3) {
					ArrayList<Vertex> verts = selection.getVerts();
					if (focus == 0 && verts.size() > 0) {
						float avgx = 0;
						float avgy = 0;
						float avgz = 0;
						for (Vertex v : verts) {
							avgx += v.x;
							avgy += v.y;
							avgz += v.z;
						}
						avgx /= verts.size();
						avgy /= verts.size();
						avgz /= verts.size();
						for (Vertex v : verts) {
							Vec3 dir = new Vec3(v.x - avgx, v.y - avgy, v.z - avgz);
							if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
								if (Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU)) {
									theModel.moveBy(v, -dir.x * 0.03f * lastFrameDuration, -dir.y * 0.03f * lastFrameDuration, -dir.z * 0.03f * lastFrameDuration);
									selection.updateVertex(v);
								} else {
									theModel.moveBy(v, dir.x * 0.03f * lastFrameDuration, dir.y * 0.03f * lastFrameDuration, dir.z * 0.03f * lastFrameDuration);
									selection.updateVertex(v);
								}
							} else {
								if (Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU)) {
									theModel.moveBy(v, -dir.x * 0.06f * lastFrameDuration, -dir.y * 0.06f * lastFrameDuration, -dir.z * 0.06f * lastFrameDuration);
									selection.updateVertex(v);
								} else {
									theModel.moveBy(v, dir.x * 0.06f * lastFrameDuration, dir.y * 0.06f * lastFrameDuration, dir.z * 0.06f * lastFrameDuration);
									selection.updateVertex(v);
								}
							}
						}
					}
				}
			}

			camera.validate();
			if(grabbing) {
				if(selectionMode == 3) {
					cursor.update(theModel.getSkeletonMesh(), selection);
				} else {
					cursor.update(theModel, selection);
				}
			} else {
				cursor.update();
			}
		}
	}

	@Override
	protected void render() {
		/*********************/
		/**** RENDER PREP ****/
		/*********************/

		// clear buffers
		glClearColor( 0.168f, 0.168f, 0.168f, 1.0f );
		glClearDepth( 1.0f );
		glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );

		// update the view matrix
		camera.calcViewMatrix();

		// multiply perspective * view
		Mat4 cam2clip = camera.getPerspectiveMatrix();
		Mat4 world2cam = camera.getViewMatrix();
		Mat4 world2clip = Mat4.mul(cam2clip, world2cam);

		// Set uniforms for the 3d shaders
        // plane and cursor
		glUseProgram(colorUniformProgram.theProgram);
		glUniformMatrix4(colorUniformProgram.worldToCamera, false, world2cam.fillAndFlipBuffer(mat4Buffer));
		glUniformMatrix4(colorUniformProgram.cameraToClip, false, cam2clip.fillAndFlipBuffer(mat4Buffer));
		glUseProgram(0);
        // skeleton
        glUseProgram(skeletonProgram.theProgram);
        glUniformMatrix4(skeletonProgram.worldToCamera, false, world2cam.fillAndFlipBuffer(mat4Buffer));
        glUniformMatrix4(skeletonProgram.cameraToClip, false, cam2clip.fillAndFlipBuffer(mat4Buffer));
        glUseProgram(0);
        // model
		glUseProgram(colorArrayProgram.theProgram);
		glUniformMatrix4(colorArrayProgram.worldToCamera, false, world2cam.fillAndFlipBuffer(mat4Buffer));
		glUniformMatrix4(colorArrayProgram.cameraToClip, false, cam2clip.fillAndFlipBuffer(mat4Buffer));
		glUniform4f(colorArrayProgram.pointLightPos, 10f, 10f, 0f, 1.0f);
		glUniform4f(colorArrayProgram.pointLightMag, 1.0f, 0.8f, 0.7f, 1.0f);
        glUniform4f(colorArrayProgram.dirLightDir, 1f, 0f, 1f, 1.0f);
        glUniform4f(colorArrayProgram.dirLightMag, 0.2f, 0.2f, 0.2f, 1.0f);
		glUniform4f(colorArrayProgram.ambient, 0.5f, 0.5f, 0.5f, 1.0f);
		glUseProgram( 0 );

		/************************/
		/**** VIEWER RENDERS ****/
		/************************/

		// OpenGL state
		glEnable(GL_DEPTH_TEST);
		glDepthMask(true);
		glDepthFunc(GL_LEQUAL);
		glDepthRange(0.0f, 1.0f);
		glEnable(GL32.GL_DEPTH_CLAMP);
		glViewport(xoffset, yoffset, viewerWidth, viewerHeight);
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		glFrontFace(GL_CW);

		// render plane
		plane.render(colorUniformProgram, 0.150f, 0.150f, 0.150f, 1.0f, 2);
		// render axes
		glDisable(GL_CULL_FACE);
		if(focus == 0) {
			axes.render(colorUniformProgram, 0.793f, 0.375f, 0.176f, 1.0f, 2);
		} else {
			axes.render(colorUniformProgram, 0.406f, 0.590f, 0.730f, 1.0f, 2);
		}
		// render axis labels
		glUseProgram(0);
		glDisable(GL_DEPTH_TEST);
		if(focus == 0) {
			Vec4 temp = Mat4.mul(world2clip, new Vec4(10.5f, 0.18f, 0.0f, 1f));
			temp.scale(1 / temp.w);
			monospacedBold36.drawString(((temp.x + 1) * viewerWidth / 2)+xoffset/2,
					(viewerHeight - (temp.y + 1) * viewerHeight / 2)+yoffset/2, "X", orange);
			temp = Mat4.mul(world2clip, new Vec4(0f, 0.18f, 10.5f, 1f));
			temp.scale(1 / temp.w);
			monospacedBold36.drawString(((temp.x + 1) * viewerWidth / 2)+xoffset/2,
					(viewerHeight - (temp.y + 1) * viewerHeight / 2)+yoffset/2, "Z", orange);
		} else {
			Vec4 temp = Mat4.mul(world2clip, new Vec4(10.5f, 0.18f, 0.0f, 1f));
			temp.scale(1 / temp.w);
			monospacedBold36.drawString(((temp.x + 1) * viewerWidth / 2)+xoffset/2,
					(viewerHeight - (temp.y + 1) * viewerHeight / 2)+yoffset/2, "X", blue);
			temp = Mat4.mul(world2clip, new Vec4(0f, 0.18f, 10.5f, 1f));
			temp.scale(1 / temp.w);
			monospacedBold36.drawString(((temp.x + 1) * viewerWidth / 2)+xoffset/2,
					(viewerHeight - (temp.y + 1) * viewerHeight / 2)+yoffset/2, "Z", blue);
		}
		glEnable(GL_DEPTH_TEST);
		// render model
		glDisable(GL_BLEND);
		glEnable(GL_CULL_FACE);
		if(selectionMode == 0){
			if(viewMode == 0) {
				theModel.render(colorUniformProgram, 0.050f, 0.072f, 0.090f, 1.0f, 0);
				theModel.render(colorUniformProgram, 0.406f, 0.590f, 0.730f, 1.0f, 1);
				theModel.render(colorUniformProgram, 0.167f, 0.242f, 0.300f, 1.0f, 2);
			} else if(viewMode == 1){
				//theModel.render(colorUniformProgram, 0.050f, 0.072f, 0.090f, 1.0f, 0);
				//theModel.render(colorUniformProgram, 0.406f, 0.590f, 0.730f, 1.0f, 1);
				theModel.render(colorArrayProgram);
			} else {
				theModel.render(colorUniformProgram, 0.050f, 0.072f, 0.090f, 1.0f, 0);
				theModel.render(colorUniformProgram, 0.406f, 0.590f, 0.730f, 1.0f, 1);
			}
		} else if(selectionMode == 1) {
			if(viewMode == 0) {
				theModel.render(colorUniformProgram, 0.406f, 0.590f, 0.730f, 1.0f, 1);
				theModel.render(colorUniformProgram, 0.167f, 0.242f, 0.300f, 1.0f, 2);
			} else if(viewMode == 1){
				theModel.render(colorUniformProgram, 0.406f, 0.590f, 0.730f, 1.0f, 1);
				theModel.render(colorArrayProgram);
			} else {
				theModel.render(colorUniformProgram, 0.406f, 0.590f, 0.730f, 1.0f, 1);
			}
		} else {
			if(viewMode == 0) {
				theModel.render(colorUniformProgram, 0.406f, 0.590f, 0.730f, 1.0f, 1);
				theModel.render(colorUniformProgram, 0.167f, 0.242f, 0.300f, 1.0f, 2);
			} else if(viewMode == 1){
				theModel.render(colorUniformProgram, 0.406f, 0.590f, 0.730f, 1.0f, 1);
				theModel.render(colorArrayProgram);
			} else {
				theModel.render(colorUniformProgram, 0.406f, 0.590f, 0.730f, 1.0f, 1);
			}
			if(selectionMode == 3) {
				glDisable(GL_DEPTH_TEST);
                glDisable(GL_CULL_FACE);
                theModel.renderSkeleton(colorUniformProgram, skeletonProgram);
                glEnable(GL_CULL_FACE);
				glEnable(GL_DEPTH_TEST);
			}
		}

		// render cursor
		glDisable(GL_CULL_FACE);
		if(renderCursor) {
			cursor.render(colorUniformProgram);
		}
		// render selection mesh
		if(viewMode == 1 && selectionMode == 2) {
			selection.render(colorUniformProgram, 0.793f, 0.375f, 0.176f, 1.0f, 1);
			if(currentTri != null) {
				colorSelection.render(colorUniformProgram, 1f, 0.094f, 0.044f, 1.0f, 1);
			}
		} else {
			selection.render(colorUniformProgram, 0.793f, 0.375f, 0.176f, 1.0f, selectionMode);
		}

		/*********************/
		/**** GUI RENDERS ****/
		/*********************/

		// OpenGL state
		glDisable(GL_CULL_FACE);
		glDisable(GL_DEPTH_TEST);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_BLEND);
		glViewport(0, 0, guiWidth, guiHeight);

		// render gui
		panel.render(colorArray2DProgram);
		panel.renderFocus(colorUniform2DProgram, focus, grabbing, renderCursor);
		panel.renderViewMode(colorUniform2DProgram, viewMode);
		panel.renderSelectionMode(colorUniform2DProgram, selectionMode);
		panel.renderEditOutline(colorUniform2DProgram, editOutline);


		/*************************/
		/**** RENDER DISPOSAL ****/
		/*************************/

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		glDisableVertexAttribArray(0);
		glBindVertexArray(0);
		glUseProgram(0);
	}

	@Override
	protected void onStop() {
		colorSelection.delete();
		selection.delete();
		plane.delete();
		cursor.destroy();
		panel.delete();
		theModel.delete();
	}

	@Override
	protected void reshape(int w, int h) {
		WIDTH = w;
		HEIGHT = h;

		// currently the gui viewport must occupy the entire screen
		guiWidth = w;
		guiHeight = h;
		panel.resize(guiWidth, guiHeight);

		// can be less than the full screen
		viewerWidth = w-xoffset;
		viewerHeight = h-yoffset;

		// update the perspective matrix
		camera.calcPerspectiveMatrix(45.0f, (viewerWidth / (float) viewerHeight));
	}

	private void clearSelection() {
		clearColorSelection();
		selection.delete();
		selection = new Mesh();
	}

	private void clearColorSelection() {
		colorSelection.delete();
		colorSelection = new Mesh();
	}

	private void selectVertex(Vertex v) {
		selection.add(v);
	}

	private void deselectVertex(Vertex v) {
		selection.remove(v);
	}

	private void fillSelectedVertices() {
		ArrayList<Vertex> verts = selection.getVerts();
		if(verts.size() < 2) {
			clearSelection();
			return;
		}
		Vertex previousVert = verts.remove(0);
		for(Vertex v: verts) {
			theModel.add(new Line(previousVert, v));
			previousVert = v;
		}
		clearSelection();
	}

	private void removeSelectedVertices() {
		ArrayList<Vertex> verts = selection.getVerts();
		for(Vertex v: verts) {
			theModel.remove(v);
		}
		clearSelection();
	}

	private void extrudeSelectedVertices() {
		ArrayList<Vertex> verts = selection.getVerts();
		clearSelection();
		for(Vertex v: verts) {
			//Vertex n = new Vertex(v.x+0.01f,v.y+0.01f,v.z+0.01f);
			Vertex n = new Vertex(v.x,v.y,v.z);
			theModel.add(n, true);
			theModel.add(new Line(v,n), true);
			selectVertex(n);
		}
		grabbing = true;
	}

	private void mergeSelectedVertices() {
		ArrayList<Vertex> verts = selection.getVerts();
		clearSelection();
		if(verts.size() != 2) return;
		theModel.merge(verts.get(0), verts.get(1));
	}

	private void selectLine(Line l){
		l.a = selection.add(l.a);
		l.b = selection.add(l.b);
		selection.add(l);
	}

	private void deselectLine(Line l) {
		selection.remove(l);
	}

	private void fillSelectedLines() {
		ArrayList<Line> lines = selection.getLines();

		// fill one tri at a time
		if(lines.size() != 3) {
			clearSelection();
			return;
		}

		// we assume no two lines are equal or of zero length
		Vertex a=null,b=null,c;
		// match first point on first line against any point on another line
		if(lines.get(0).a == lines.get(1).a) a = lines.get(0).a;
		else if(lines.get(0).a == lines.get(1).b) a = lines.get(0).a;
		else if(lines.get(0).a == lines.get(2).a) a = lines.get(0).a;
		else if(lines.get(0).a == lines.get(2).b) a = lines.get(0).a;
		if(a == null) {
			clearSelection();
			return;
		}
		// match second point on first line against any other point on another line
		if(lines.get(0).b == lines.get(1).a) b = lines.get(0).b;
		else if(lines.get(0).b == lines.get(1).b) b = lines.get(0).b;
		else if(lines.get(0).b == lines.get(2).a) b = lines.get(0).b;
		else if(lines.get(0).b == lines.get(2).b) b = lines.get(0).b;
		if(b == null || a == b) {
			clearSelection();
			return;
		}
		// find the point on the second line that was not matched
		if(lines.get(1).a != a && lines.get(1).a != b) c = lines.get(1).a;
		else c = lines.get(1).b;
		// match this point to a point on the third line
		if(c != lines.get(2).a && c != lines.get(2).b) {
			clearSelection();
			return;
		}

		theModel.add(new Triangle(a, b, c, new ColorData()));
		clearSelection();
	}

	private void removeSelectedLines() {
		ArrayList<Line> lines = selection.getLines();
		for(Line l: lines) {
			theModel.remove(l);
		}
		clearSelection();
	}

	private void extrudeSelectedLines() {
		ArrayList<Line> lines = selection.getLines();
		clearSelection();
		// note that every individual line extrusion involves two vertex extrusions
		// and the addition of a line.
		// in the case that two connected lines are being extruded,
		// we don't want to extrude the same vertex twice
		ArrayList<Vertex> wereExtruded = new ArrayList<>();
		for(Line l: lines) {
			Vertex na = new Vertex(l.a.x,l.a.y,l.a.z);
			Vertex nb = new Vertex(l.b.x,l.b.y,l.b.z);
			boolean afound = false;
			boolean bfound = false;
			for(Vertex v: wereExtruded) {
				if(na.equals(v)) {
					na = v;
					afound = true;
				}
				if(nb.equals(v)) {
					nb = v;
					bfound = true;
				}
			}
			if(!afound) {
				theModel.add(na, true);
				theModel.add(new Line(l.a, na), true);
				wereExtruded.add(na);
			}
			if(!bfound) {
				theModel.add(nb, true);
				theModel.add(new Line(l.b, nb), true);
				wereExtruded.add(nb);
			}
			Line n = new Line(na,nb);
			theModel.add(n, true);
			selectLine(n);
		}
		grabbing = true;
	}

	private void selectTriangle(Triangle t){
		t.refreshLines();
		t.a = selection.add(t.a);
		t.b = selection.add(t.b);
		t.c = selection.add(t.c);
		t.s1 = selection.add(t.s1);
		t.s2 = selection.add(t.s2);
		t.s3 = selection.add(t.s3);
		selection.add(t);

		currentTri = t;
		panel.triColor.r = currentTri.color.r;
		panel.triColor.g = currentTri.color.g;
		panel.triColor.b = currentTri.color.b;
		panel.triColor.a = currentTri.color.a;

		clearColorSelection();
		colorSelection.add(t.a);
		colorSelection.add(t.b);
		colorSelection.add(t.c);
		colorSelection.add(t.s1);
		colorSelection.add(t.s2);
		colorSelection.add(t.s3);
	}

	private void deselectTriangle(Triangle t) {
		selection.remove(t);
		boolean s1found = false;
		boolean s2found = false;
		boolean s3found = false;
		for(Triangle tri: selection.getTris()) {
			if(t.s1.containsVerts(tri.s1) || t.s1.containsVerts(tri.s2) || t.s1.containsVerts(tri.s3)) {
				s1found = true;
			}
			if(t.s2.containsVerts(tri.s1) || t.s2.containsVerts(tri.s2) || t.s2.containsVerts(tri.s3)) {
				s2found = true;
			}
			if(t.s3.containsVerts(tri.s1) || t.s3.containsVerts(tri.s2) || t.s3.containsVerts(tri.s3)) {
				s3found = true;
			}
		}
		if(!s1found) selection.remove(t.s1);
		if(!s2found) selection.remove(t.s2);
		if(!s3found) selection.remove(t.s3);
		if(t == currentTri) {
			ArrayList<Triangle> tris = selection.getTris();
			clearColorSelection();
			if(tris.size() != 0) {
				currentTri = tris.get(tris.size()-1);
				panel.triColor.r = currentTri.color.r;
				panel.triColor.g = currentTri.color.g;
				panel.triColor.b = currentTri.color.b;
				panel.triColor.a = currentTri.color.a;
				colorSelection.add(currentTri.a);
				colorSelection.add(currentTri.b);
				colorSelection.add(currentTri.c);
				colorSelection.add(currentTri.s1);
				colorSelection.add(currentTri.s2);
				colorSelection.add(currentTri.s3);
			} else {
				currentTri = null;
				panel.triColor.r = 0;
				panel.triColor.g = 0;
				panel.triColor.b = 0;
				panel.triColor.a = 0;
			}
		}
	}

	private void removeSelectedTriangles() {
		ArrayList<Triangle> tris = selection.getTris();
		for(Triangle t: tris) {
			theModel.remove(t);
		}
		clearSelection();
	}

	private void extrudeSelectedTriangles() {
		ArrayList<Triangle> tris = selection.getTris();
		clearSelection();
		ArrayList<Vertex> vertsExtruded = new ArrayList<>();
		ArrayList<Line> linesExtruded = new ArrayList<>();
		for(Triangle t: tris) {
			Vertex na = new Vertex(t.a.x,t.a.y,t.a.z);
			Vertex nb = new Vertex(t.b.x,t.b.y,t.b.z);
			Vertex nc = new Vertex(t.c.x,t.c.y,t.c.z);
			boolean afound = false;
			boolean bfound = false;
			boolean cfound = false;
			for(Vertex v: vertsExtruded) {
				if(na.equals(v)) {
					na = v;
					afound = true;
				}
				if(nb.equals(v)) {
					nb = v;
					bfound = true;
				}
				if(nc.equals(v)) {
					nc = v;
					cfound = true;
				}
			}
			// add new verts
			if(!afound) {
				theModel.add(na, true);
				theModel.add(new Line(t.a, na), true);
				vertsExtruded.add(na);
			}
			if(!bfound) {
				theModel.add(nb, true);
				theModel.add(new Line(t.b, nb), true);
				vertsExtruded.add(nb);
			}
			if(!cfound) {
				theModel.add(nc, true);
				theModel.add(new Line(t.c, nc), true);
				vertsExtruded.add(nc);
			}
			Line ab = new Line(na,t.b);
			Line ac = new Line(na,t.c);
			Line bc = new Line(nb,t.c);
			Line sab = null;
			Line sac = null;
			Line sbc = null;
			for(Line l: linesExtruded) {
				if(ab.equals(l)) {
					sab = l;
				} else if(ac.equals(l)) {
					sac = l;
				} else if(bc.equals(l)) {
					sbc = l;
				}
			}
			// a to b
			if(sab != null) {
				theModel.remove(sab);
			}
			else {
				theModel.add(ab, true);
				theModel.add(new Line(na,nb), true);
				theModel.add(new Triangle(na,nb,t.b, new ColorData()), true);
				theModel.add(new Triangle(na,t.a,t.b, new ColorData()), true);
				linesExtruded.add(ab);
			}
			// a to c
			if(sac != null) {
				theModel.remove(sac);
			}
			else {
				theModel.add(ac, true);
				theModel.add(new Line(na,nc), true);
				theModel.add(new Triangle(na,nc,t.c, new ColorData()), true);
				theModel.add(new Triangle(na,t.a,t.c, new ColorData()), true);
				linesExtruded.add(ac);
			}
			// b to c
			if(sbc != null) {
				theModel.remove(sbc);
			}
			else {
				theModel.add(bc, true);
				theModel.add(new Line(nb,nc), true);
				theModel.add(new Triangle(nb,nc,t.c, new ColorData()), true);
				theModel.add(new Triangle(nb,t.b,t.c, new ColorData()), true);
				linesExtruded.add(bc);
			}
			// extruded face
			Triangle n = new Triangle(na,nb,nc, new ColorData());
			theModel.add(n, true);
			selectTriangle(n);
		}
		grabbing = true;
	}

	private void flipNormals() {
		ArrayList<Triangle> tris = selection.getTris();
		clearSelection();

		for(Triangle t: tris) {
			theModel.flipNormal(t);
		}
	}

	private void fillSelectedBoneNodes() {
		ArrayList<Vertex> verts = selection.getVerts();
		if(verts.size() < 2) {
			clearSelection();
			return;
		}
		Vertex previousVert = verts.remove(0);
		for(Vertex v : verts) {
			theModel.addBone(previousVert, v);
			previousVert = v;
		}
		clearSelection();
	}

	private void removeSelectedBoneNodes() {
		ArrayList<Vertex> verts = selection.getVerts();
		for(Vertex v: verts) {
			theModel.removeBoneNode(v);
		}
		clearSelection();
	}

	private void save() {
		try {
			String fileName = JOptionPane.showInputDialog(null,
					"What would you like to name your model?", "Save", JOptionPane.QUESTION_MESSAGE);
			if(fileName == null || fileName.equals("")) {
				JOptionPane.showMessageDialog(null, "Not Saved", "Status", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			if(!fileName.matches("[a-zA-Z0-9_-]+")) {
				JOptionPane.showMessageDialog(null, "Invalid name -- use letters, numbers, dashes, and underscores",
						"Status", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			File file = new File("models/" + fileName + ".3df");

			if (file.exists()) {
				if(file.isDirectory()) {
					JOptionPane.showMessageDialog(null, "Not Saved", "Status", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				int overwrite = JOptionPane.showConfirmDialog(null,
						"Overwrite save file?", "File Exists", JOptionPane.YES_NO_OPTION);
				if(overwrite == JOptionPane.YES_OPTION) {
					file.delete();
					file.createNewFile();
				} else {
					JOptionPane.showMessageDialog(null, "Not Saved", "Status", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
			} else {
				File parentFile = file.getParentFile();
				parentFile.mkdirs();
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write("Vertices:");
			ArrayList<Vertex> verts = theModel.getVerts();
			for(int i=0; i<verts.size(); i++) {
				if(i % 3 == 0) bw.write("\n");
				bw.write(verts.get(i) + " ");
			}
			bw.write("\nLines:");
			short[] lines = theModel.getLineIndices();
			for(int i=0; i<theModel.numLines()*2; i=i+2) {
				if(i % 24 == 0) bw.write("\n");
				bw.write(lines[i] + "," + lines[i+1] + " ");
			}
			bw.write("\nTriangles:");
			short[] tris = theModel.getTriIndices();
			int[] colors = theModel.getColors();
			for(int i=0; i<theModel.numTris()*3; i=i+3) {
				if(i % 18 == 0) bw.write("\n");
				bw.write(tris[i] + "," + tris[i+1] + "," + tris[i+2] + "," + colors[i / 3] + " ");
			}
			bw.close();
			JOptionPane.showMessageDialog(null, "Saved Successfully", "Status", JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Not Saved", "Status", JOptionPane.INFORMATION_MESSAGE);
			e.printStackTrace();
		}
	}

	private void load() {
		try {
			File folder = new File("models/");
			File[] listOfFiles = folder.listFiles();
			ArrayList<String> fileNames = new ArrayList<>();

			for (int i = 0; i < listOfFiles.length; i++) {
				String name = listOfFiles[i].getName();
				if(name.matches("[a-zA-Z0-9_-]+\\.3df")) {
					fileNames.add(name.substring(0, name.length()-4));
				}
			}

			if(fileNames.size() < 1) {
				JOptionPane.showMessageDialog(null, "No models to load", "Status", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			String fileName = (String) JOptionPane.showInputDialog(null,
					"What model would you like to load?", "Load",
					JOptionPane.QUESTION_MESSAGE, null, fileNames.toArray(), fileNames.get(0));

			if(fileName == null || fileName.equals("")) {
				JOptionPane.showMessageDialog(null, "Not Loaded", "Status", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			int overwrite = JOptionPane.showConfirmDialog(null,
					"Are you sure you want to load?\nUnsaved changes will be lost.",
					"Load", JOptionPane.YES_NO_OPTION);
			if(overwrite != JOptionPane.YES_OPTION) {
				JOptionPane.showMessageDialog(null, "Not Loaded", "Status", JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			clearSelection();
			clearColorSelection();
			theModel.delete();
			theModel = new Mesh();
			theModel.addSkeleton();

			File file = new File("models/" + fileName + ".3df");
			BufferedReader br = new BufferedReader(new FileReader(file.getAbsoluteFile()));

			boolean readingVerts = true, readingLines = false;
			String[] line;
			String[] item;
			ArrayList<Vertex> verts = new ArrayList<>(); // for compilation
			if (br.ready()) br.readLine();
			while (br.ready()) {
				line = br.readLine().split("[ ]");
				if (readingVerts) {
					if (line[0].equals("Lines:")) {
						readingVerts = false;
						readingLines = true;
						verts = theModel.getVerts();
					} else {
						for (int i = 0; i < line.length; i++) {
							item = line[i].split("[,]");
							theModel.add(new Vertex(Float.parseFloat(item[0]), Float.parseFloat(item[1]), Float.parseFloat(item[2])));
						}
					}
				} else if (readingLines) {
					if (line[0].equals("Triangles:")) {
						readingLines = false;
					} else {
						for (int i = 0; i < line.length; i++) {
							item = line[i].split("[,]");
							theModel.add(new Line(verts.get(Short.parseShort(item[0])), verts.get(Short.parseShort(item[1]))));
						}
					}
				} else {
					for (int i = 0; i < line.length; i++) {
						item = line[i].split("[,]");
						theModel.add(new Triangle(verts.get(Short.parseShort(item[0])), verts.get(Short.parseShort(item[1])),
								verts.get(Short.parseShort(item[2])), new ColorData(Integer.parseInt(item[3]))));
					}
				}
			}
			br.close();

		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Not Loaded", "Status", JOptionPane.INFORMATION_MESSAGE);
		}
	}
}
