package edu.pitt.atl23;

//TODO transition from Glm
import jglsdk.glm.Glm;
import jglsdk.glm.Mat4;
import jglsdk.glm.Vec3;
import jglsdk.glm.Vec4;

/**
 * Created by Andrew T. Lucas on 3/10/2015.
 */
public class Camera {
	private Mat4 viewMatrix, perspectiveMatrix;
	private float zNear, zFar;
	protected Vec3 target;
	protected Vec3 spherePos;
	protected Vec3 cameraPt;

	private static final float degToRad = 3.14159f * 2.0f / 360.0f;

	public Camera(float zn, float zf) {
		target = new Vec3( 0.0f, 0.0f, 0.0f );
		spherePos = new Vec3( 67.5f, -46.0f, 20.0f );
		zNear = zn;
		zFar = zf;
		cameraPt = resolveCamPosition();
	}

	public void validate() {
		spherePos.y = spherePos.y > -89.99f ? spherePos.y : -89.99f;
		spherePos.y = spherePos.y < 90.00f ? spherePos.y : 90.00f;
		spherePos.z = spherePos.z > 1.0f ? spherePos.z : 1.0f;
		target.y = target.y >  0.0f ? target.y :  0.0f;
		target.y = target.y >=  80.0f ?  80.0f : target.y;
		target.x = target.x >=  80.0f ?  80.0f : target.x;
		target.x = target.x <= -80.0f ? -80.0f : target.x;
		target.z = target.z >=  80.0f ?  80.0f : target.z;
		target.z = target.z <= -80.0f ? -80.0f : target.z;
	}

	public Mat4 getViewMatrix() {
		return viewMatrix;
	}

	public Mat4 getPerspectiveMatrix() {
		return perspectiveMatrix;
	}

	public void calcViewMatrix() {
		cameraPt = resolveCamPosition();
		Vec3 lookDir = Glm.normalize(Vec3.sub(target, cameraPt));
		Vec3 upDir = Glm.normalize( new Vec3(0f,1f,0f) );

		Vec3 rightDir = Glm.normalize( Glm.cross( lookDir, upDir ) );
		Vec3 perpUpDir = Glm.cross( rightDir, lookDir );

		Mat4 rotMat = new Mat4( 1.0f );
		rotMat.setColumn( 0, new Vec4( rightDir, 0.0f ) );
		rotMat.setColumn( 1, new Vec4( perpUpDir, 0.0f ) );
		rotMat.setColumn( 2, new Vec4( Vec3.negate( lookDir ), 0.0f ) );

		rotMat = Glm.transpose( rotMat );

		Mat4 transMat = new Mat4( 1.0f );
		transMat.setColumn( 3, new Vec4( Vec3.negate( cameraPt ), 1.0f ) );

		viewMatrix = rotMat.mul( transMat );
	}

	public void calcPerspectiveMatrix(float fovY, float aspect) {
		float range = (float) (Math.tan(Math.toRadians(fovY / 2.0f)) * zNear);
		float left = -range * aspect;
		float right = range * aspect;
		float bottom = -range;
		float top = range;

		perspectiveMatrix = new Mat4(0.0f);
		perspectiveMatrix.matrix[0] 	=  (2.0f * zNear) / (right - left);
		perspectiveMatrix.matrix[5] 	=  (2.0f * zNear) / (top - bottom);
		perspectiveMatrix.matrix[10] 	= -(zFar + zNear) / (zFar - zNear);
		perspectiveMatrix.matrix[11] 	= -1.0f;
		perspectiveMatrix.matrix[14] 	= -(2.0f * zFar * zNear) / (zFar - zNear);
	}

	private Vec3 resolveCamPosition() {
		float phi = degToRad( spherePos.x );
		float theta = degToRad( spherePos.y + 90.0f );

		float sinTheta = (float) Math.sin( theta );
		float cosTheta = (float) Math.cos( theta );
		float cosPhi = (float) Math.cos( phi );
		float sinPhi = (float) Math.sin( phi );

		Vec3 dirToCamera = new Vec3( sinTheta * cosPhi, cosTheta, sinTheta * sinPhi );
		return (dirToCamera.scale( spherePos.z )).add( target );
	}

	public static float degToRad(float angDeg) {
		return angDeg * degToRad;
	}
}
