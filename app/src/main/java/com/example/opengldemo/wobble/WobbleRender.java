 package com.example.opengldemo.wobble;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.example.BasicRender;
import com.example.opengldemo.R;
import com.example.shaderUtil.MatrixHelper;
import com.example.shaderUtil.ObjLoader;
import com.example.shaderUtil.TextureHelper;
import com.example.shaderUtil.TextureShaderProgram;
import com.example.object.Vertices3;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;

@SuppressLint("NewApi")
public class WobbleRender extends BasicRender {
	private Activity activity;
	private int meshTexture;

	private float[] projectionMatrix = new float[16];
	private float[] viewMatrix = new float[16];
	private final float[] modelMatrix = new float[16];
	private float[] viewProjectMatrix = new float[16];

	private WobbleShaderProgram textureShader;
	private Vertices3 mesh;
	private float xAngle = 0;
	private float yAngle = 0;

	Bitmap bitmap[] = new Bitmap[5];

	public WobbleRender(Activity activity) {
		this.activity = activity;
	}

	@Override
	public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
		GLES20.glClearColor(1F, 1F, 1F, 0F);

		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		// GLES20.glEnable(GLES20.GL_CULL_FACE);

		meshTexture = TextureHelper.loadTexture(activity, R.drawable.face);
		
		textureShader = new WobbleShaderProgram(activity,
				R.raw.wobble_vertex_shader, R.raw.rotate_fragment_shader);
		mesh = ObjLoader.load(activity, R.raw.monkey);
	}

	@Override
	public void onSurfaceChanged(GL10 arg0, int width, int height) {

		GLES20.glViewport(0, 0, width, height);

		float fov = 120;
		float aspect = ((float) width / (float) height);
		float near = 0.1f;
		float far = 200f;
		MatrixHelper.perspectiveM(projectionMatrix, fov, aspect, near, far);

		Matrix.setLookAtM(viewMatrix, 0,
				0f, 0f, 1f,
				0f, 0.5f, -1f, 
				0f, 1f, 0f);

		Matrix.translateM(viewMatrix, 0, 0, 0, -2);
	}

	@Override
	public void onDrawFrame(GL10 arg0) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		drawWobble();
	}

	private void drawWobble(){
		float[] matrix = getWobbleMatrix();
		textureShader.useProgram();
		textureShader.setUniform(matrix, meshTexture);

		mesh.bind(textureShader.getPositionLocation(),
				textureShader.getTextureCoordinatesLocation());
		mesh.draw();
	}

	private float[] getWobbleMatrix() {
		Matrix.setIdentityM(modelMatrix, 0);

		Matrix.translateM(modelMatrix, 0, 0f, -2f, -5);
		Matrix.rotateM(modelMatrix, 0, xAngle, 1f, 0f, 0f);
		Matrix.rotateM(modelMatrix, 0, yAngle, 0f, 1f, 0f);
		float[] temp = new float[16];
		float[] resultMatrix = new float[16];
		Matrix.multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
		System.arraycopy(temp, 0, resultMatrix, 0, temp.length);
		// xAngle += 0.5;

		return resultMatrix;
	}

	public void rotate(float xAngle, float yAngle) {
		this.xAngle += xAngle;
		this.yAngle += yAngle;
	}

	public void release(){
		int[] textureArray = {meshTexture};
		GLES20.glDeleteTextures(1, textureArray, 0);
	}
}
