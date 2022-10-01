package lintfordpickle.ld51.controllers;

import org.lwjgl.glfw.GLFW;

import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.camera.Camera;
import net.lintford.library.core.camera.ICamera;
import net.lintford.library.core.geometry.Rectangle;
import net.lintford.library.core.maths.Vector2f;

public class CameraMovementController extends BaseController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "Camera Movement Controller";

	private static final float CAMERA_MAN_MOVE_SPEED = 15.f;
	private static final float CAMERA_MAN_MOVE_SPEED_MAX = 10f;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private Rectangle mPlayArea;
	private ICamera mGameCamera;
	private Vector2f mVelocity;
	private boolean mIsEditorMode;
	private Vector2f mPointToFollow;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public boolean isEditorMode() {
		return mIsEditorMode;
	}

	public void isEditorMode(boolean isEditorMode) {
		mIsEditorMode = isEditorMode;
	}

	public boolean isFollowingPoint() {
		return mPointToFollow != null;
	}

	public void setPointToFollow(Vector2f pointToFollow) {
		mPointToFollow = pointToFollow;
	}

	public Rectangle playArea() {
		return mPlayArea;
	}

	public void setPlayArea(float pX, float pY, float pWidth, float pHeight) {
		mPlayArea.set(pX, pY, pWidth, pHeight);
	}

	public ICamera gameCamera() {
		return mGameCamera;
	}

	@Override
	public boolean isInitialized() {
		return mGameCamera != null;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public CameraMovementController(ControllerManager controllerManager, ICamera camera, boolean isEditorMode, int entityGroupUid) {
		super(controllerManager, CONTROLLER_NAME, entityGroupUid);

		mVelocity = new Vector2f();
		mPlayArea = new Rectangle();
		if (camera instanceof Camera) {
			final var lCamera = (Camera) camera;
			lCamera.setIsChaseCamera(true, 0.06f);
		}

		mIsEditorMode = isEditorMode;
		mGameCamera = camera;
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
	}

	@Override
	public void unload() {
	}

	@Override
	public boolean handleInput(LintfordCore pCore) {
		if (mGameCamera == null)
			return false;

		final float lElapsed = (float) pCore.appTime().elapsedTimeMilli() * 0.001f;
		final float lOneOverCameraZoom = mGameCamera.getZoomFactorOverOne();
		final float speed = CAMERA_MAN_MOVE_SPEED * lOneOverCameraZoom;

		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL))
			return false; // editor controls
		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_A)) {
			mVelocity.x -= speed * lElapsed;
			mPointToFollow = null; // stop auto follow

		}
		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_D)) {
			mVelocity.x += speed * lElapsed;
			mPointToFollow = null; // stop auto follow

		}
		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_S)) {
			mVelocity.y += speed * lElapsed;
			mPointToFollow = null; // stop auto follow

		}
		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_W)) {
			mVelocity.y -= speed * lElapsed;
			mPointToFollow = null; // stop auto follow

		}
		return false;
	}

	@Override
	public void update(LintfordCore pCore) {
		if (mGameCamera == null)
			return;

		if (mPointToFollow != null) {
			mGameCamera.setPosition(mPointToFollow.x, mPointToFollow.y);

		} else {
			// Cap
			if (mVelocity.x < -CAMERA_MAN_MOVE_SPEED_MAX)
				mVelocity.x = -CAMERA_MAN_MOVE_SPEED_MAX;
			if (mVelocity.x > CAMERA_MAN_MOVE_SPEED_MAX)
				mVelocity.x = CAMERA_MAN_MOVE_SPEED_MAX;
			if (mVelocity.y < -CAMERA_MAN_MOVE_SPEED_MAX)
				mVelocity.y = -CAMERA_MAN_MOVE_SPEED_MAX;
			if (mVelocity.y > CAMERA_MAN_MOVE_SPEED_MAX)
				mVelocity.y = CAMERA_MAN_MOVE_SPEED_MAX;

			float elapsed = (float) pCore.appTime().elapsedTimeMilli();

			// Applys
			float lCurX = mGameCamera.getPosition().x;
			float lCurY = mGameCamera.getPosition().y;
			if (mIsEditorMode == false && mPlayArea != null && !mPlayArea.isEmpty()) {
				if (lCurX - mGameCamera.getWidth() * .5f < mPlayArea.left()) {
					lCurX = mPlayArea.left() + mGameCamera.getWidth() * .5f;
					if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_A)) // kill velocity
						mVelocity.x = 0;
				}
				if (lCurX + mGameCamera.getWidth() * .5f > mPlayArea.right()) {
					lCurX = mPlayArea.right() - mGameCamera.getWidth() * .5f;
					if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_D)) // kill velocity
						mVelocity.x = 0;
				}
				if (lCurY - mGameCamera.getHeight() * .5f < mPlayArea.top()) {
					lCurY = mPlayArea.top() + mGameCamera.getHeight() * .5f;
					if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_W)) // kill velocity
						mVelocity.y = 0;
				}
				if (lCurY + mGameCamera.getHeight() * .5f > mPlayArea.bottom()) {
					lCurY = mPlayArea.bottom() - mGameCamera.getHeight() * .5f;
					if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_S)) // kill velocity
						mVelocity.y = 0;
				}
			}

			mGameCamera.setPosition(lCurX + mVelocity.x * elapsed, lCurY + mVelocity.y * elapsed);
		}

		// DRAG
		mVelocity.x *= 0.857f;
		mVelocity.y *= 0.857f;
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void zoomIn(float pZoomFactor) {
		mGameCamera.setZoomFactor(pZoomFactor);
	}

}
