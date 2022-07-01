/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.opengltext;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

/**
 * A three-dimensional cube for use as a drawn object in OpenGL ES 2.0.
 */
public class Cube {

	private final String vertexShaderCode = "uniform mat4 u_MVPMatrix;"
			+ "attribute vec4 a_Position;" + "attribute vec2 a_TexCoordinate;"
			+ "varying vec2 v_TexCoordinate;" + "void main() {"
			+ "  gl_Position = u_MVPMatrix * a_Position;"
			+ "  v_TexCoordinate = a_TexCoordinate;" + "}";

	private final String fragmentShaderCode = "precision mediump float;"
			+ "varying vec2 v_TexCoordinate;"
			+ "uniform sampler2D u_TextureUnit;" + "void main() {"
			+ "  gl_FragColor = texture2D(u_TextureUnit, v_TexCoordinate);"
			+ "}";

	// number of bytes in float
	static final int BYTES_IN_FLOAT = 4;
	// number of coordinates per vertex in the array
	static final int COORDS_PER_VERTEX = 3;
	// number of coordinates per texel in the array
	static final int TEXTURE_COORDS = 2;

	private final FloatBuffer vertexBuffer;
	private final FloatBuffer textureBuffer;

	private final int mProgram;
	private int mPositionHandle;
	private int mMVPMatrixHandle;
	private int mTextureCoordHandle;
	private int mTextureUniformHandle;
	private int mTextureDataHandle;
	

	// 8 vertices of the cube
		static float v[][] = {
			{-1.0f,  1.0f,  1.0f}, 	// V0. Front-Top-Left
			{-1.0f, -1.0f,  1.0f}, 	// V1. Front-Bottom-Left
			{ 1.0f, -1.0f,  1.0f}, 	// V2. Front-Bottom-Right
			{ 1.0f,  1.0f,  1.0f}, 	// V3. Front-Top-Right
			{-1.0f,  1.0f, -1.0f}, 	// V4. Back-Top-Left
			{-1.0f, -1.0f, -1.0f}, 	// V5. Back-Bottom-Left
			{ 1.0f, -1.0f, -1.0f}, 	// V6. Back-Bottom-Right
			{ 1.0f,  1.0f, -1.0f} 	// V7. Back-Top-Right
		};
		
		static float cubeCoords[] = {
			 // in counterclockwise order:
			 // Front Face (V0,V1,V2,V3)
			 v[0][0],v[0][1],v[0][2],
			 v[1][0],v[1][1],v[1][2],
			 v[2][0],v[2][1],v[2][2],
			 v[0][0],v[0][1],v[0][2],
			 v[2][0],v[2][1],v[2][2],
			 v[3][0],v[3][1],v[3][2],
		  	
			 // Back Face (V4,V5,V6,V7)
			 v[4][0],v[4][1],v[4][2],
			 v[5][0],v[5][1],v[5][2],
			 v[6][0],v[6][1],v[6][2],
			 v[4][0],v[4][1],v[4][2],
			 v[6][0],v[6][1],v[6][2],
			 v[7][0],v[7][1],v[7][2],
		  	
			 // Left Face (V3,V2,V6,V7)  	  
			 v[3][0],v[3][1],v[3][2],
			 v[2][0],v[2][1],v[2][2],
			 v[6][0],v[6][1],v[6][2],
			 v[3][0],v[3][1],v[3][2],
			 v[6][0],v[6][1],v[6][2],
			 v[7][0],v[7][1],v[7][2],
			 
			 // Right Face (V4,V5,V1,V0) 	 
			 v[4][0],v[4][1],v[4][2],
			 v[5][0],v[5][1],v[5][2],
			 v[1][0],v[1][1],v[1][2],
			 v[4][0],v[4][1],v[4][2],
			 v[1][0],v[1][1],v[1][2],
			 v[0][0],v[0][1],v[0][2],
		   	 
		   	 // Top Face (V4,V0,V3,V7) 
			 v[4][0],v[4][1],v[4][2],
			 v[0][0],v[0][1],v[0][2],
			 v[3][0],v[3][1],v[3][2],
		     v[4][0],v[4][1],v[4][2],
		   	 v[3][0],v[3][1],v[3][2],
		   	 v[7][0],v[7][1],v[7][2],
		   	 
		   	 // Bottom Face (V5,V1,V2,V6)   
		   	 v[5][0],v[5][1],v[5][2],
		     v[1][0],v[1][1],v[1][2],
		     v[2][0],v[2][1],v[2][2],
		     v[5][0],v[5][1],v[5][2],
		     v[2][0],v[2][1],v[2][2],
		     v[6][0],v[6][1],v[6][2]
		};

	private final int vertexCount = cubeCoords.length / COORDS_PER_VERTEX;
	private final int vertexStride = COORDS_PER_VERTEX * BYTES_IN_FLOAT;

	/*
	 * S, T (or X, Y) Texture coordinate data. Because images have a Y axis
	 * pointing downward (values increase as you move down the image) while
	 * OpenGL has a Y axis pointing upward, we adjust for that here by flipping
	 * the Y axis. What's more is that the texture coordinates are the same for
	 * every face.
	 */
	static float[] textureCoords = { 
			0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f,
			0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f,
		};

	/**
	 * Sets up the drawing object data for use in an OpenGL ES context.
	 */
	public Cube(Context context, int resourceId) {
		// initialize vertex byte buffer for shape coordinates
		// (number of coordinate values * 4 bytes per float)
		ByteBuffer bb = ByteBuffer.allocateDirect(cubeCoords.length
				* BYTES_IN_FLOAT);
		// use the device hardware's native byte order
		bb.order(ByteOrder.nativeOrder());
		// create a floating point buffer from the ByteBuffer
		vertexBuffer = bb.asFloatBuffer();
		// add the coordinates to the FloatBuffer
		vertexBuffer.put(cubeCoords);
		// set the buffer to read the first coordinate
		vertexBuffer.position(0);

		// initialize texture byte buffer
		ByteBuffer tb = ByteBuffer.allocateDirect(textureCoords.length
				* BYTES_IN_FLOAT);
		tb.order(ByteOrder.nativeOrder());
		textureBuffer = tb.asFloatBuffer();
		textureBuffer.put(textureCoords);
		textureBuffer.position(0);

		// Load the texture
		mTextureDataHandle = loadTexture(context, resourceId);

		// prepare shaders and OpenGL program
		int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
				vertexShaderCode);
		int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
				fragmentShaderCode);

		mProgram = GLES20.glCreateProgram(); // create empty OpenGL Program
		GLES20.glAttachShader(mProgram, vertexShader); // add the vertex shader
														// to program
		GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment
															// shader to program
		GLES20.glLinkProgram(mProgram); // create OpenGL program executables
		
		// Add program to OpenGL environment
		GLES20.glUseProgram(mProgram);
	}

	/**
	 * Encapsulates the OpenGL ES instructions for drawing this shape.
	 *
	 * @param mvpMatrix
	 *            - The Model View Project matrix in which to draw this shape.
	 */

	public void draw(float[] mvpMatrix) {
		
		// get handle to vertex shader's a_Position member
		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");
		// Enable a handle to the triangle vertices
		GLES20.glEnableVertexAttribArray(mPositionHandle);
		// Prepare the triangle coordinate data
		GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

		// get handle to vertex shader's a_TexCoordinate member
		mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram,
				"a_TexCoordinate");
		GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
		GLES20.glVertexAttribPointer(mTextureCoordHandle, TEXTURE_COORDS,
				GLES20.GL_FLOAT, false, 0, textureBuffer);

		// get handle to shape's transformation matrix
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix");
		MyGLRenderer.checkGlError("glGetUniformLocation");

		// Apply the projection and view transformation
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
		MyGLRenderer.checkGlError("glUniformMatrix4fv");

		// get handle to shape's transformation matrix
		mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram,
				"u_TextureUnit");
		// Tell the texture uniform sampler to use this texture in the shader by
		// binding to texture unit 0.
		// Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
		GLES20.glUniform1i(mTextureUniformHandle, 0);

		// Draw the cube
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

		// Disable vertex array
		GLES20.glDisableVertexAttribArray(mPositionHandle);
		GLES20.glDisableVertexAttribArray(mTextureCoordHandle);
	}

	private int loadTexture(Context context, int resourceId) {
		final int[] textureObjectIds = new int[1];
		GLES20.glGenTextures(1, textureObjectIds, 0);

		/*
		 * if (textureObjectIds[0] == 0) {
		 * MyGLRenderer.checkGlError("glGenTextures"); return 0; }
		 */
		// final BitmapFactory.Options options = new BitmapFactory.Options();
		// options.inScaled = false;

		// Read in the resource
		final Bitmap bitmap = BitmapFactory.decodeResource(
				context.getResources(), resourceId/* , options */);

		/*
		 * if (bitmap == null) { MyGLRenderer.checkGlError("decodeResource");
		 * 
		 * GLES20.glDeleteTextures(1, textureObjectIds, 0); return 0; }
		 */
		// Bind to the texture in OpenGL
		//GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjectIds[0]);

		// Set filtering: a default must be set, or the texture will be black
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		
		// Load the bitmap into the bound texture.
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

		// Note: Following code may cause an error to be reported in the
		// ADB log as follows: E/IMGSRV(20095): :0: HardwareMipGen:
		// Failed to generate texture mipmap levels (error=3)
		// No OpenGL error will be encountered (glGetError() will return
		// 0). If this happens, just squash the source image to be
		// square. It will look the same because of texture coordinates,
		// and mipmap generation will work.

		GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

		// Recycle the bitmap, since its data has been loaded into
		// OpenGL.
		bitmap.recycle();

		// Unbind from the texture.
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		
		return textureObjectIds[0];
	}
}
