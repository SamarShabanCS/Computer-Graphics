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

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

/**
 * Provides drawing instructions for a GLSurfaceView object. This class must
 * override the OpenGL ES drawing lifecycle methods:
 * <ul>
 * <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceCreated}</li>
 * <li>{@link android.opengl.GLSurfaceView.Renderer#onDrawFrame}</li>
 * <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceChanged}</li>
 * </ul>
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

	private static final String TAG = "MyGLRenderer";
	private Cube mCube1, mCube2;

	// mMVPMatrix is an abbreviation for "Model View Projection Matrix"
	private final float[] mMVPMatrix = new float[16];
	private final float[] mProjectionMatrix = new float[16];
	private final float[] mViewMatrix = new float[16];
	private final float[] mModelMatrix1 = new float[16];
	private final float[] mModelMatrix2 = new float[16];

	// Camera vectors forming the view matrix:
	// eye point, center of view, and an up vector.
	private float eyeX, eyeY, eyeZ;
	private float centerX, centerY, centerZ;
	private float upX, upY, upZ;

	private float mPreviousX = 0.0f;
	private float mPreviousY = 0.0f;
	
	// Camera movement directions
	private enum Direction {
		UP, DOWN, FORWARD, BACKWARD, LEFT, RIGHT
	};

	// Rotation parameters
	private boolean mRotate = true;
	private float mRotateX, mRotateY, mRotateZ;

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {

		// Set the background frame color
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		// Enable depth testing
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		// Initialize class variables
		init();
	}

	@Override
	public void onDrawFrame(GL10 unused) {

		// Draw background color
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		// Set the camera position (View matrix)
		Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, centerX, centerY,
				centerZ, upX, upY, upZ);

		// Calculate the projection and view transformation
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

		drawObjects();
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		// Adjust the viewport based on geometry changes,
		// such as screen rotation
		GLES20.glViewport(0, 0, width, height);

		float ratio = (float) width / height;

		// this projection matrix is applied to object coordinates
		// in the onDrawFrame() method
		Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 1, 10);
	}

	/*
	 * Initializing all variables used in OpenGL matrices
	 */
	private void init() {
		eyeX = 0.0f;
		eyeY = 0.0f;
		eyeZ = -2.0f;

		centerX = 0.0f;
		centerY = 0.0f;
		centerZ = 0.0f;

		upX = 0.0f;
		upY = 1.0f;
		upZ = 0.0f;

		mRotateX = 0.0f;
		mRotateY = 1.0f;
		mRotateZ = 0.0f;

		Matrix.setIdentityM(mModelMatrix1, 0);
		mCube1 = new Cube();
		mCube2 = new Cube();

	}

	private void drawObjects() {

		// FRONT CUBE
		float[] scratch1 = new float[16];
		if (mRotate) {
			rotateCube();
		}
		Matrix.multiplyMM(scratch1, 0, mMVPMatrix, 0, mModelMatrix1, 0);
		mCube1.draw(scratch1);

		// BACK CUBE
		float[] scratch2 = new float[16];
		Matrix.setIdentityM(mModelMatrix2, 0);
		Matrix.translateM(mModelMatrix2, 0, 0.0f, 0.0f, 2.0f);
		Matrix.multiplyMM(scratch2, 0, mMVPMatrix, 0, mModelMatrix2, 0);
		mCube2.draw(scratch2);
	}

	private void rotateCube() {
		// Apply Transformation
		float mAngle = 0.5f;
		Matrix.rotateM(mModelMatrix1, 0, mAngle, mRotateX, mRotateY, mRotateZ);

	}

	private void moveCamera(Direction d) {
		switch (d) {
		case LEFT:
			eyeX += 0.1f;
			break;
		case RIGHT:
			eyeX -= 0.1f;
			break;
		case UP:
			eyeY += 0.1f;
			break;
		case DOWN:
			eyeY -= 0.1f;
			break;
		case FORWARD:
			eyeZ += 0.1f;
			break;
		case BACKWARD:
			eyeZ -= 0.1f;
			break;
		}
	}

	public void handleTouchPress(float normalizedX, float normalizedY) {
		// TODO Auto-generated method stub
		mRotate = false;
		Matrix.setIdentityM(mModelMatrix1, 0);
		moveCamera(Direction.FORWARD);
	}

	public void handleTouchDrag(float normalizedX, float normalizedY) {
		// TODO Auto-generated method stub
		mRotate = true;
		
		float dx = Math.abs(normalizedX - mPreviousX);
		float dy = Math.abs(normalizedY - mPreviousY);
		
		if (dx > dy) { // Right Or Left
			if ( normalizedX > mPreviousX ) { // Right
				mRotateX = 0.0f;
				mRotateY = 1.0f;
				mRotateZ = 0.0f;
				//moveCamera(Direction.RIGHT);
			} else { // Left
				mRotateX = 0.0f;
				mRotateY = 0.0f;
				mRotateZ = 1.0f;
				//moveCamera(Direction.LEFT);
			}
		} else { // Up or Down
			if (normalizedY > mPreviousY) {
				mRotateX = 1.0f;
				mRotateY = 0.0f;
				mRotateZ = 0.0f;
				moveCamera(Direction.UP);
			} else{
				moveCamera(Direction.DOWN);
			}
		}
		
		mPreviousX = normalizedX;
		mPreviousY = normalizedY;
	}

	/**
	 * Utility method for compiling a OpenGL shader.
	 *
	 * <p>
	 * <strong>Note:</strong> When developing shaders, use the checkGlError()
	 * method to debug shader coding errors.
	 * </p>
	 *
	 * @param type
	 *            - Vertex or fragment shader type.
	 * @param shaderCode
	 *            - String containing the shader code.
	 * @return - Returns an id for the shader.
	 */
	public static int loadShader(int type, String shaderCode) {

		// create a vertex shader type (GLES20.GL_VERTEX_SHADER)
		// or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
		int shader = GLES20.glCreateShader(type);

		// add the source code to the shader and compile it
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);

		return shader;
	}

	/**
	 * Utility method for debugging OpenGL calls. Provide the name of the call
	 * just after making it:
	 *
	 * <pre>
	 * mColorHandle = GLES20.glGetUniformLocation(mProgram, &quot;vColor&quot;);
	 * MyGLRenderer.checkGlError(&quot;glGetUniformLocation&quot;);
	 * </pre>
	 *
	 * If the operation is not successful, the check throws an error.
	 *
	 * @param glOperation
	 *            - Name of the OpenGL call to check.
	 */
	public static void checkGlError(String glOperation) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(TAG, glOperation + ": glError " + error);
			throw new RuntimeException(glOperation + ": glError " + error);
		}
	}
}