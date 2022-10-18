package lintfordpickle.ld51.controllers;

import org.lwjgl.glfw.GLFW;

import lintfordpickle.ld51.data.ships.Ship;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.camera.ICamera;
import net.lintford.library.core.maths.MathHelper;
import net.lintford.library.core.maths.Vector2f;

public class CameraShipChaseController extends BaseController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "Camera Ship Chase Controller";

	private static final float CAMERA_MAN_MOVE_SPEED = 0.2f;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private ICamera mGameCamera;
	private Ship mTrackedEntity;
	private boolean mAllowManualControl;
	private boolean mIsTrackingPlayer;

	private Vector2f mVelocity;
	public Vector2f mDesiredPosition;
	public Vector2f mPosition;
	public Vector2f mLookAhead;

	public float mZoomFactor;
	public float mZoomVelocity;

	private float mStiffness = 18.0f;
	private float mDamping = 6.0f;
	private float mMass = .5f;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public ICamera gameCamera() {
		return mGameCamera;
	}

	public boolean trackPlayer() {
		return mIsTrackingPlayer;
	}

	public void trackPlayer(boolean pNewValue) {
		mIsTrackingPlayer = pNewValue;
	}

	public boolean allowManualControl() {
		return mAllowManualControl;
	}

	public void allowManualControl(boolean pNewValue) {
		mAllowManualControl = pNewValue;
	}

	@Override
	public boolean isInitialized() {
		return mGameCamera != null;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public CameraShipChaseController(ControllerManager controllerManager, ICamera camera, Ship trackedCar, int entityGroupUid) {
		super(controllerManager, CONTROLLER_NAME, entityGroupUid);

		mVelocity = new Vector2f();
		mDesiredPosition = new Vector2f();
		mPosition = new Vector2f();
		mLookAhead = new Vector2f();

		mPosition.x = trackedCar.x;
		mPosition.y = trackedCar.y;

		mGameCamera = camera;
		mTrackedEntity = trackedCar;
		mIsTrackingPlayer = true;
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore core) {

	}

	public void setTrackedEntity(ICamera cameCamera, Ship trackedEntity) {
		mGameCamera = cameCamera;
		mTrackedEntity = trackedEntity;
	}

	@Override
	public void unload() {

	}

	@Override
	public boolean handleInput(LintfordCore core) {
		if (mGameCamera == null)
			return false;

		if (true || mAllowManualControl) {
			final float speed = CAMERA_MAN_MOVE_SPEED;

			// Just listener for clicks - couldn't be easier !!?!
			if (core.input().keyboard().isKeyDown(GLFW.GLFW_KEY_A)) {
				mVelocity.x -= speed;
				mIsTrackingPlayer = false;
			}

			if (core.input().keyboard().isKeyDown(GLFW.GLFW_KEY_D)) {
				mVelocity.x += speed;
				mIsTrackingPlayer = false;
			}

			if (core.input().keyboard().isKeyDown(GLFW.GLFW_KEY_S)) {
				mVelocity.y += speed;
				mIsTrackingPlayer = false;
			}

			if (core.input().keyboard().isKeyDown(GLFW.GLFW_KEY_W)) {
				mVelocity.y -= speed;
				mIsTrackingPlayer = false;
			}
		}

		return false;
	}

	@Override
	public void update(LintfordCore pCore) {
		if (mGameCamera == null)
			return;

		if (mTrackedEntity != null) {
			updateSpring(pCore);

			mGameCamera.setPosition(-mTrackedEntity.x, -mTrackedEntity.y);
		}
	}

	private void updateSpring(LintfordCore core) {
		mStiffness = 10000.0f;
		mDamping = 1000.0f;
		mMass = 100.f;

		updatewWorldPositions(core);
		updateWorldZoomFactor(core);

		float elapsed = (float) core.gameTime().elapsedTimeMilli() * 0.001f;

		// Calculate spring force
		float stretchX = mPosition.x - mDesiredPosition.x;
		float stretchY = mPosition.y - mDesiredPosition.y;

		float forceX = -mStiffness * stretchX - mDamping * mVelocity.x;
		float forceY = -mStiffness * stretchY - mDamping * mVelocity.y;

		// Apply acceleration
		float accelerationX = forceX / mMass;
		float accelerationY = forceY / mMass;

		mVelocity.x += accelerationX * elapsed;
		mVelocity.y += accelerationY * elapsed;

		// Apply velocity
		mPosition.x += mVelocity.x * elapsed;
		mPosition.y += mVelocity.y * elapsed;
	}

	private void updatewWorldPositions(LintfordCore core) {
		float lAngle = mTrackedEntity.headingAngle;
		mLookAhead.x = (float) Math.cos(lAngle);
		mLookAhead.y = (float) Math.sin(lAngle);

		float lSpeedMod = mTrackedEntity.speed * 0.2f;
		mDesiredPosition.x = mTrackedEntity.x + mLookAhead.x * lSpeedMod;
		mDesiredPosition.y = mTrackedEntity.y + mLookAhead.y * lSpeedMod;
	}

	private void updateWorldZoomFactor(LintfordCore core) {

		final float lZoomInLimit = 0.5f;
		final float lZoomOutLimit = 1.5f;
		final float lDefaultZoom = 1.f;

		float lTargetZoom = lDefaultZoom - mTrackedEntity.speed * 0.05f;
		lTargetZoom = MathHelper.clamp(lTargetZoom, lZoomInLimit, lZoomOutLimit);

		final float lVelStepSize = 0.175f;

		if (lTargetZoom > mZoomFactor)
			mZoomVelocity += lVelStepSize;
		else
			mZoomVelocity -= lVelStepSize;

		mZoomFactor += mZoomVelocity * core.gameTime().elapsedTimeMilli() * 0.001f;

		mZoomVelocity = MathHelper.clamp(mZoomVelocity, -0.025f, 0.025f);
		mZoomFactor = MathHelper.clamp(mZoomFactor, lZoomInLimit, lZoomOutLimit);
		mGameCamera.setZoomFactor(mZoomFactor);

		mZoomVelocity *= 0.0987f;
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void zoomIn(float zoomFactor) {
		mGameCamera.setZoomFactor(zoomFactor);
	}
}
