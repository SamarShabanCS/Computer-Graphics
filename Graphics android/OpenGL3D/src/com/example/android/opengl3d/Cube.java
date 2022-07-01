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
package com.example.android.opengl3d;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import android.opengl.GLES20;

/**
 * A three-dimensional cube for use as a drawn object in OpenGL ES 2.0.
 */
public class Cube {

	private final String vertexShaderCode =
	// This matrix member variable provides a hook to manipulate
	// the coordinates of the objects that use this vertex shader
	"uniform mat4 uMVPMatrix;" + "attribute vec4 vPosition;"
			+ "attribute vec4 aColor;" + "varying vec4 vColor;"
			+ "void main() {" + "  vColor = aColor;" +
			// the matrix must be included as a modifier of gl_Position
			// Note that the uMVPMatrix factor *must be first* in order
			// for the matrix multiplication product to be correct.
			"  gl_Position = uMVPMatrix * vPosition;" + "}";

	private final String fragmentShaderCode = "precision mediump float;"
			+ "varying vec4 vColor;" + "void main() {"
			+ "  gl_FragColor = vColor;" + "}";

	private final FloatBuffer vertexBuffer;
	private final FloatBuffer colorBuffer;

	private final int mProgram;
	private int mPositionHandle;
	private int mColorHandle;
	private int mMVPMatrixHandle;

	// number of coordinates per vertex in the array
	static final int COORDS_PER_VERTEX = 3;
	// number of attributes per color in the array
	static final int ATTRIBS_PER_COLOR = 4;

	// 8 vertices of the cube
	static float v[][] = {
		{-0.5f, -0.5f,  0.5f}, 	// V0. 	Front-Bottom-Left
		{ 0.5f, -0.5f,  0.5f}, 	// V1.	Front-Bottom-Right
		{-0.5f,  0.5f,  0.5f}, 	// V2.	Front-Top-Left
		{ 0.5f,  0.5f,  0.5f}, 	// V3.	Front-Top-Right
		{-0.5f, -0.5f, -0.5f}, 	// V4.	Back-Bottom-Left
		{ 0.5f, -0.5f, -0.5f}, 	// V5.	Back-Bottom-Right
		{-0.5f,  0.5f, -0.5f}, 	// V6.	Back-Top-Left
		{ 0.5f,  0.5f, -0.5f}, 	// V7.	Back-Top-Right
	};
	
	static float cubeCoords[] = {
		 // in counterclockwise order:
		 // Front Face (V0,V1,V2,V3)
		 v[2][0],v[2][1],v[2][2],
		 v[0][0],v[0][1],v[0][2],
		 v[3][0],v[3][1],v[3][2],
		 v[3][0],v[3][1],v[3][2],
		 v[0][0],v[0][1],v[0][2],
		 v[1][0],v[1][1],v[1][2],
	  	
		 // Back Face (V4,V5,V6,V7)
		 v[4][0],v[4][1],v[4][2],
		 v[5][0],v[5][1],v[5][2],
		 v[6][0],v[6][1],v[6][2],
		 v[6][0],v[6][1],v[6][2],
		 v[5][0],v[5][1],v[5][2],
		 v[7][0],v[7][1],v[7][2],
	  	
		 // Left Face (V0,V2,V4,V6)  	  
		 v[0][0],v[0][1],v[0][2],
		 v[2][0],v[2][1],v[2][2],
		 v[4][0],v[4][1],v[4][2],
		 v[4][0],v[4][1],v[4][2],
		 v[2][0],v[2][1],v[2][2],
		 v[6][0],v[6][1],v[6][2],
		 
		 // Right Face (V1,V3,V5,V7) 	 
		 v[1][0],v[1][1],v[1][2],
		 v[5][0],v[5][1],v[5][2],
		 v[3][0],v[3][1],v[3][2],
		 v[3][0],v[3][1],v[3][2],
	   	 v[5][0],v[5][1],v[5][2],
	   	 v[7][0],v[7][1],v[7][2],
	   	 
	   	 // Top Face (V2,V3,V6,V7) 
	   	 v[2][0],v[2][1],v[2][2],
	     v[3][0],v[3][1],v[3][2],
	     v[6][0],v[6][1],v[6][2],
	     v[6][0],v[6][1],v[6][2],
	   	 v[3][0],v[3][1],v[3][2],
	   	 v[7][0],v[7][1],v[7][2],
	   	 
	   	 // Bottom Face (V0,V1,V4,V5)   
	   	 v[0][0],v[0][1],v[0][2],
	     v[1][0],v[1][1],v[1][2],
	     v[4][0],v[4][1],v[4][2],
	     v[4][0],v[4][1],v[4][2],
	     v[1][0],v[1][1],v[1][2],
	     v[5][0],v[5][1],v[5][2]
	};

	private final int vertexCount = cubeCoords.length / COORDS_PER_VERTEX;
	private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per
															// vertex
	// Six Colors for the 6 faces of the cube
	static float c[][] = {
		{1.0f, 0.0f, 0.0f, 1.0f}, 	// Red
		{0.0f, 1.0f, 0.0f, 1.0f}, 	// Green
		{0.0f, 0.0f, 1.0f, 1.0f}, 	// Blue
		{1.0f, 1.0f, 0.0f, 1.0f}, 	// Yellow
		{0.0f, 1.0f, 1.0f, 1.0f}, 	// Cyan
		{1.0f, 0.0f, 1.0f, 1.0f} 	// Magenta
	};
		
	float colors[] = new float[vertexCount*ATTRIBS_PER_COLOR];
	/**
	 * Sets up the drawing object data for use in an OpenGL ES context.
	 */
	public Cube() {
		// Set vertices colors
		int k = 0;
		for(int i = 0; i < c.length; i = k/24){
			for(int j = 0; j < ATTRIBS_PER_COLOR; j++){
				colors[k] = c[i][j];
				k++;
			}
		}
		
		// initialize vertex byte buffer for shape coordinates
		// (number of coordinate values * 4 bytes per float)
		ByteBuffer bb = ByteBuffer.allocateDirect(cubeCoords.length * 4);
		// use the device hardware's native byte order
		bb.order(ByteOrder.nativeOrder());
		// create a floating point buffer from the ByteBuffer
		vertexBuffer = bb.asFloatBuffer();
		// add the coordinates to the FloatBuffer
		vertexBuffer.put(cubeCoords);
		// set the buffer to read the first coordinate
		vertexBuffer.position(0);

		// initialize color byte buffer
		ByteBuffer cb = ByteBuffer.allocateDirect(colors.length * 4);
		cb.order(ByteOrder.nativeOrder());
		colorBuffer = cb.asFloatBuffer();
		colorBuffer.put(colors);
		colorBuffer.position(0);

		// prepare shaders and OpenGL program
		int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
				vertexShaderCode);
		int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
				fragmentShaderCode);

		mProgram = GLES20.glCreateProgram(); // create empty OpenGL Program
		GLES20.glAttachShader(mProgram, vertexShader); // add the vertex shader to program
		GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
		GLES20.glLinkProgram(mProgram); // create OpenGL program executables
	}

	/**
	 * Encapsulates the OpenGL ES instructions for drawing this shape.
	 *
	 * @param mvpMatrix
	 *            - The Model View Project matrix in which to draw this shape.
	 */

	public void draw(float[] mvpMatrix) {
		// Add program to OpenGL environment
		GLES20.glUseProgram(mProgram);

		// get handle to vertex shader's vPosition member
		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
		// Enable a handle to the triangle vertices
		GLES20.glEnableVertexAttribArray(mPositionHandle);
		// Prepare the triangle coordinate data
		GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
		
		// get handle to fragment shader's aColor member
		mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
		GLES20.glEnableVertexAttribArray(mColorHandle);
		GLES20.glVertexAttribPointer(mColorHandle, ATTRIBS_PER_COLOR, 
				GLES20.GL_FLOAT, false, 0, colorBuffer);

		// get handle to shape's transformation matrix
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
		MyGLRenderer.checkGlError("glGetUniformLocation");

		// Apply the projection and view transformation
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
		MyGLRenderer.checkGlError("glUniformMatrix4fv");

		// Draw the cube
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

		// Disable vertex array
		GLES20.glDisableVertexAttribArray(mPositionHandle);
	}
}
