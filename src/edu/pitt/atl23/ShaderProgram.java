package edu.pitt.atl23;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static org.lwjgl.opengl.ARBUniformBufferObject.glGetUniformBlockIndex;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

/**
 * Created by Andrew T. Lucas on 3/10/2015.
 */
public class ShaderProgram {

	public static String COMMON_DATAPATH = "/shaders/";
	public static String CURRENT_DATAPATH = null;

	public int theProgram;
	public int modelToWorldMatrix;
	public int worldToClipMatrix;
    public int worldToCamera;
	public int orthogonalMatrix;
	public int baseColor;
	public int colorBlock;
    public int normalBlock;
    public int dirLight;
    public int dirLightMag;
    public int ambient;

	public ShaderProgram(String vertexShaderFileName, String fragmentShaderFileName) {
		ArrayList<Integer> shaderList = new ArrayList<>();
		shaderList.add( loadShader(GL_VERTEX_SHADER, vertexShaderFileName) );
		shaderList.add( loadShader(GL_FRAGMENT_SHADER, fragmentShaderFileName) );

		theProgram = createProgram(shaderList);
		modelToWorldMatrix = glGetUniformLocation(theProgram, "modelToWorldMatrix");
		worldToClipMatrix = glGetUniformLocation(theProgram, "worldToClipMatrix" );
        worldToCamera = glGetUniformLocation(theProgram, "worldToCamera" );
		baseColor = glGetUniformLocation(theProgram, "baseColor" );
        dirLight = glGetUniformLocation(theProgram, "dirLight");
        dirLightMag = glGetUniformLocation(theProgram, "dirLightMag");
        ambient = glGetUniformLocation(theProgram, "ambient");

		colorBlock = glGetUniformBlockIndex(theProgram, "colorBlock");
        normalBlock = glGetUniformBlockIndex(theProgram, "normalBlock");

		orthogonalMatrix = glGetUniformLocation(theProgram, "orthogonalMatrix");
	}

	private int loadShader(int shaderType, String shaderFilename) {
		String filepath = ShaderProgram.findFileOrThrow( shaderFilename );
		String shaderCode = loadShaderFile( filepath );

		return compileShader(shaderType, shaderCode);
	}

	private int createProgram(ArrayList<Integer> shaders) {
		try {
			int program = glCreateProgram();
			return linkProgram(program, shaders);
		} finally {
			for ( Integer shader : shaders ) {
				glDeleteShader( shader );
			}
		}
	}

	private int compileShader(int shaderType, String shaderCode) {
		int shader = glCreateShader(shaderType);

		glShaderSource(shader, shaderCode);
		glCompileShader(shader);

		int status = glGetShaderi(shader, GL_COMPILE_STATUS);
		if (status == GL_FALSE) {
            glGetShaderInfoLog(shader, glGetShaderi(shader, GL_INFO_LOG_LENGTH));
            glDeleteShader(shader);
			throw new CompileLinkShaderException(shader);
		}

		return shader;
	}

	private int linkProgram(int program, ArrayList<Integer> shaders) {
		for (Integer shader : shaders) {
			glAttachShader(program, shader);
		}

		glLinkProgram(program);

		int status = glGetProgrami(program, GL_LINK_STATUS);
		if (status == GL_FALSE) {
            glGetProgramInfoLog(program, glGetProgrami(program, GL_INFO_LOG_LENGTH));
			glDeleteProgram(program);
			throw new CompileLinkProgramException(program);
		}

		for (Integer shader : shaders) {
			glDetachShader(program, shader);
		}

		return program;
	}

	private String loadShaderFile(String shaderFilepath) {
		StringBuilder text = new StringBuilder();

		try {
			BufferedReader reader = new BufferedReader( new InputStreamReader( ClassLoader.class.getResourceAsStream( shaderFilepath ) ) );
			String line;

			while ( (line = reader.readLine()) != null ) {
				text.append( line ).append( "\n" );
			}

			reader.close();
		} catch ( IOException e ) {
			e.printStackTrace();
		}

		return text.toString();
	}

	public static String findFileOrThrow(String filename) {
		InputStream fileStream = ClassLoader.class.getResourceAsStream( CURRENT_DATAPATH + filename );
		if ( fileStream != null ) {
			return CURRENT_DATAPATH + filename;
		}

		fileStream = ClassLoader.class.getResourceAsStream( COMMON_DATAPATH + filename );
		if ( fileStream != null ) {
			return COMMON_DATAPATH + filename;
		}

		throw new RuntimeException( "Could not find the file " + filename );
	}

	private static class CompileLinkShaderException extends RuntimeException {
		private static final long serialVersionUID = 5490603440382398244L;

		CompileLinkShaderException(int shader) {
			super("\n" + glGetShaderInfoLog(shader, glGetShaderi(shader, GL_INFO_LOG_LENGTH)));
		}
	}

	private static class CompileLinkProgramException extends RuntimeException {
		private static final long serialVersionUID = 7321217286524434327L;

		CompileLinkProgramException(int program) {
			super("\n" + glGetShaderInfoLog(program, glGetShaderi(program, GL_INFO_LOG_LENGTH)));
		}
	}
}
