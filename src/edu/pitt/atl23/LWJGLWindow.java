package edu.pitt.atl23;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.*;

import static org.lwjgl.opengl.GL11.*;

/**
 * Visit https://github.com/integeruser/jgltut for info, updates and license terms.
 *
 * @author integeruser
 */
public class LWJGLWindow {

    // Measured in milliseconds
    private float elapsedTime;
    private float lastFrameDuration;

    private double lastFrameTimestamp, now;
    private boolean continueMainLoop;

    ////////////////////////////////
    public final void start() {
        start( 500, 500 );
    }

    public final void start(int width, int height) {
        try {
            Display.setTitle("3D Modeller");
            Display.setDisplayMode(new DisplayMode(width, height));
            Display.setResizable(true);
            Display.setVSyncEnabled(true);

            String osName = System.getProperty( "os.name" ).toLowerCase();
            if ( osName.startsWith( "mac os x" ) ) {
                Display.create(new PixelFormat(), new ContextAttribs(3, 2).withProfileCore(true));
            } else {
                Display.create();
            }

            printInfo();
        } catch ( LWJGLException e ) {
            e.printStackTrace();
            System.exit( -1 );
        }

        long startTime = System.nanoTime();
        continueMainLoop = true;

        onStart();
        reshape( width, height );

        while ( continueMainLoop && !Display.isCloseRequested() ) {
            elapsedTime = (float) ((System.nanoTime() - startTime) / 1000000.0);

            now = System.nanoTime();
            lastFrameDuration = (float) ((now - lastFrameTimestamp) / 1000000.0);
            lastFrameTimestamp = now;

            update();
            render();

            Display.update();
	        Display.sync(100);

            if ( Display.wasResized() ) {
                reshape( Display.getWidth(), Display.getHeight() );
            }
        }
		onStop();
        Display.destroy();
    }

    private void printInfo() {
        System.out.println();
        System.out.println("-----------------------------------------------------------");

        System.out.format("%-18s%s\n", "Running:", getClass().getName());
        System.out.println("GL_VENDOR: " + glGetString(GL11.GL_VENDOR));
        System.out.println("GL_RENDERER: " + glGetString(GL11.GL_RENDERER));
        System.out.println("GL_VERSION: " + glGetString(GL11.GL_VERSION));
        System.out.println("GL_SHADING_LANGUAGE: " + GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION));
    }

    ////////////////////////////////
    protected void onStart() {
    }

	protected void update() {
		while ( Keyboard.next() ) {
			if ( Keyboard.getEventKeyState()) {
				if ( Keyboard.getEventKey() == Keyboard.KEY_ESCAPE ) {
					leaveMainLoop();
				}
			}
		}
	}

    protected void render() {
        glClearColor( 0.0f, 0.0f, 0.0f, 0.0f );
        glClear( GL_COLOR_BUFFER_BIT );
    }

	protected void onStop() {}

	protected void reshape(int w, int h) {
		glViewport( 0, 0, w, h );
	}

    ////////////////////////////////
    protected final float getElapsedTime() {
        return elapsedTime;
    }

    protected final float getLastFrameDuration() {
        return lastFrameDuration;
    }

    protected final void leaveMainLoop() {
        continueMainLoop = false;
    }
}
