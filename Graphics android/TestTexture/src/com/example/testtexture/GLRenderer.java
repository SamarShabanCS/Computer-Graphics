package com.example.testtexture;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;

public class GLRenderer implements Renderer {

	// Our matrices
	private final float[] mProjectionMatrix = new float[16];
	private final float[] mViewMatrix = new float[16];
	private final float[] mMVPMatrix = new float[16];
	private final float[] mModelMatrix1 = new float[16];

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
	private boolean mRotate = false;
	private float mRotateX, mRotateY, mRotateZ;
	
	private Context context;
	
	private Cube mCube1;//, mCube2;
	
	public GLRenderer(Context context) {
		this.context = context;
	}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
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
		
		// Draw the scene
		drawObjects();
	}
	
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// Adjust the viewport based on geometry changes,
		// such as screen rotation
		GLES20.glViewport(0, 0, width, height);

		float ratio = (float) width / height;

		// this projection matrix is applied to object coordinates
		// in the onDrawFrame() method
		Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 1, 100);
	}
	
	/*
	 * Initializing all variables used in OpenGL matrices
	 */
	private void init(){
		eyeX = 0.0f;
		eyeY = 0.0f;
		eyeZ = 4.0f;

		centerX = 0.0f;
		centerY = 0.0f;
		centerZ = 0.0f;

		upX = 0.0f;
		upY = 1.0f;
		upZ = 0.0f;
		
		Matrix.setIdentityM(mModelMatrix1, 0);
		mCube1 = new Cube(context, R.drawable.text3);

	}
	
	private void drawObjects(){
		// Update our example
		float[] scratch1 = new float[16];
		float mAngle = 0.5f;
		Matrix.rotateM(mModelMatrix1, 0, mAngle, 1.0f, 1.0f, 1.0f);
		Matrix.multiplyMM(scratch1, 0, mMVPMatrix, 0, mModelMatrix1, 0);
		mCube1.Draw(scratch1);

	}

	public void handleTouchPress(float normalizedX, float normalizedY) {
		// TODO Auto-generated method stub
	}

	public void handleTouchDrag(float normalizedX, float normalizedY) {
		// TODO Auto-generated method stub
	}
}
