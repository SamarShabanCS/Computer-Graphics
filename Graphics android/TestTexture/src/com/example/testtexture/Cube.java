package com.example.testtexture;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

public class Cube {
	// Geometric variables
	private static float vertices[];
	private static short indices[];
	private static float textureCoords[];
	
	private FloatBuffer vertexBuffer;
	private ShortBuffer drawListBuffer;
	private FloatBuffer textureBuffer;
	
	private int mTextureDataHandle;
	private int mProgram;

	public Cube(Context context, int resourceId) {
		// TODO Auto-generated constructor stub
		// Create the cube
		SetupCube();

		// Create the texture information
		SetupTexture(context, resourceId);

		//  prepare shaders and OpenGL program
		int vertexShader = Shaders.loadShader(GLES20.GL_VERTEX_SHADER, Shaders.vs_Texture);
		int fragmentShader = Shaders.loadShader( GLES20.GL_FRAGMENT_SHADER, Shaders.fs_Texture);
		
		// create empty OpenGL ES Program
		mProgram = GLES20.glCreateProgram(); 
		// add the vertex shader to program
		GLES20.glAttachShader(mProgram, vertexShader); 
		// add the fragment shader to program
		GLES20.glAttachShader(mProgram, fragmentShader); 
		// create OpenGL program executables
		GLES20.glLinkProgram(mProgram); 
		// Add program to OpenGL environment
		GLES20.glUseProgram(mProgram);
	}

	public void SetupTexture(Context context, int resourceId) {
		// Create our UV coordinates.
		textureCoords = new float[] { 
				0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f,
				0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f 
		};

		// The texture buffer
		ByteBuffer bb = ByteBuffer.allocateDirect(textureCoords.length * 4);
		bb.order(ByteOrder.nativeOrder());
		textureBuffer = bb.asFloatBuffer();
		textureBuffer.put(textureCoords);
		textureBuffer.position(0);

		// Generate Textures, if more needed, alter these numbers.
		int[] textureObjectIds = new int[1];
		GLES20.glGenTextures(1, textureObjectIds, 0);

		// Temporary create a bitmap
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
				resourceId);

		// Bind texture to texture object
		//GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjectIds[0]);

		// Set filtering: a default must be set, or the texture will be black
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		
		// Load the bitmap into the bound texture.
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

		// We are done using the bitmap so we should recycle it.
		bitmap.recycle();
		
		// Unbind from the texture.
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		
		mTextureDataHandle = textureObjectIds[0];

	}

	public void SetupCube() {
		// We have to create the vertices of our triangle.
		vertices = new float[] { 
				-1.0f,  1.0f,  1.0f, // Front-Top-Left
				-1.0f, -1.0f,  1.0f, // Front-Bottom-Left
				 1.0f, -1.0f,  1.0f, // Front-Bottom-Right
				 1.0f,  1.0f,  1.0f, // Front-Top-Right
				-1.0f,  1.0f, -1.0f, // Back-Top-Left
				-1.0f, -1.0f, -1.0f, // Back-Bottom-Left
				 1.0f, -1.0f, -1.0f, // Back-Bottom-Right
				 1.0f,  1.0f, -1.0f // Back-Top-Right
		};
		
		// The order of vertex rendering.
		indices = new short[] { 
				0, 1, 2, 0, 2, 3, 
				4, 5, 6, 4, 6, 7, 
				3, 2, 6, 3, 6, 7, 
				4, 5, 1, 4, 1, 0, 
				4, 0, 3, 4, 3, 7, 
				5, 1, 2, 5, 2, 6 
		}; 
																			
		// The vertex buffer.
		ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
		bb.order(ByteOrder.nativeOrder());
		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);

		// initialize byte buffer for the draw list
		ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);
		dlb.order(ByteOrder.nativeOrder());
		drawListBuffer = dlb.asShortBuffer();
		drawListBuffer.put(indices);
		drawListBuffer.position(0);

	}

	public void Draw(float[] mvpMatrix) {

		// Get handle to vertex shader's vPosition member
		int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
		// Enable generic vertex attribute array
		GLES20.glEnableVertexAttribArray(mPositionHandle);
		// Prepare the coordinate data
		GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT,
				false, 0, vertexBuffer);

		// Get handle to texture coordinates location
		int mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_texCoord");
		// Enable generic vertex attribute array
		GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
		// Prepare the texture coordinates
		GLES20.glVertexAttribPointer(mTextureCoordHandle, 2, GLES20.GL_FLOAT, false,
				0, textureBuffer);

		// Get handle to shape's transformation matrix
		int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
		// Apply the projection and view transformation
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

		// Get handle to textures locations
		int mTextureLocHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");
		
		// Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
		// Set the sampler texture unit to 0, where we have saved the texture.
		GLES20.glUniform1i(mTextureLocHandle, 0);

		// Draw the triangles
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length,
				GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

		// Disable vertex array
		GLES20.glDisableVertexAttribArray(mPositionHandle);
		GLES20.glDisableVertexAttribArray(mTextureCoordHandle);
	}
}
