package lintfordpickle.ld51.controllers;

import org.lwjgl.glfw.GLFW;

import lintfordpickle.ld51.data.ships.Ship;
import lintfordpickle.ld51.data.ships.ShipManager;
import lintfordpickle.ld51.data.tracks.Track;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.maths.MathHelper;
import net.lintford.library.core.splines.SplinePoint;

public class ShipController extends BaseController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "Ship Controller";

	public static final int DEFAULT_NUMBER_OPPONENTS = 4;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private ShipManager mShipManager;
	private TrackController mTrackController;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public ShipManager shipManager() {
		return mShipManager;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public ShipController(ControllerManager controllerManager, ShipManager shipManager, int entityGroupID) {
		super(controllerManager, CONTROLLER_NAME, entityGroupID);

		mShipManager = shipManager;
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore core) {
		super.initialize(core);

		mTrackController = (TrackController) mControllerManager.getControllerByNameRequired(TrackController.CONTROLLER_NAME, entityGroupID());

		setupPlayerShip();

		// place the ships in the correct positions
		final var lPlayerShip = mShipManager.playerShip();
		final var lTrackPosition = mTrackController.mTrack.trackSpline().points().get(0);
		lPlayerShip.worldPositionX(lTrackPosition.x);
		lPlayerShip.worldPositionY(lTrackPosition.y);
	}

	@Override
	public void unload() {

	}

	@Override
	public boolean handleInput(LintfordCore core) {
		final var lPlayerShip = mShipManager.playerShip();

		lPlayerShip.shipInput.isTurningLeft = core.input().keyboard().isKeyDown(GLFW.GLFW_KEY_LEFT);
		lPlayerShip.shipInput.isTurningRight = core.input().keyboard().isKeyDown(GLFW.GLFW_KEY_RIGHT);
		lPlayerShip.shipInput.isGas = core.input().keyboard().isKeyDown(GLFW.GLFW_KEY_UP);
		lPlayerShip.shipInput.isBrake = core.input().keyboard().isKeyDown(GLFW.GLFW_KEY_DOWN);
		lPlayerShip.shipInput.isHandBrake = core.input().keyboard().isKeyDown(GLFW.GLFW_KEY_SPACE);

		return super.handleInput(core);
	}

	@Override
	public void update(LintfordCore core) {
		super.update(core);

		final var lTrack = mTrackController.currentTrack();
		final var lShipList = mShipManager.ships();
		final int lNumShips = lShipList.size();

		for (int i = 0; i < lNumShips; i++) {
			final var lShipToUpdate = lShipList.get(i);

			updateShip(core, lShipToUpdate);
			updateShipProgress(core, lTrack, lShipToUpdate);

			if (!lShipToUpdate.isPlayerControlled)
				continue;

			updateShipAi(core, lShipToUpdate);
		}
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void updateShip(LintfordCore core, Ship shipToUpdate) {
		final float lDelta = (float) core.gameTime().elapsedTimeMilli() * 0.001f;

		final float SHIP_MAX_ACCEL = 20.f;
		shipToUpdate.shipSpeedMax = 500.f;

		if (shipToUpdate.shipInput.isGas) {
			shipToUpdate.speed += SHIP_MAX_ACCEL;
		}

		if (shipToUpdate.shipInput.isBrake) {
			shipToUpdate.speed -= SHIP_MAX_ACCEL;
		}

		if (shipToUpdate.shipInput.isTurningLeft) {
			shipToUpdate.steerFrontAngle -= 1.2f; // shipToUpdate.carTurnAngleInc;
			shipToUpdate.steerRearAngle -= 1.2f; // shipToUpdate.carTurnAngleInc;
			shipToUpdate.isSteering = true;
		}

		if (shipToUpdate.shipInput.isTurningRight) {
			shipToUpdate.steerFrontAngle += 1.2f; // shipToUpdate.carTurnAngleInc;
			shipToUpdate.steerRearAngle += 1.2f; // shipToUpdate.carTurnAngleInc;
			shipToUpdate.isSteering = true;
		}

		if (shipToUpdate.shipInput.isHandBrake) {
			shipToUpdate.airGlide = true;
			if (shipToUpdate.shipInput.isTurningLeft) {
				shipToUpdate.airGlideAmt -= 0.008f;
				if (shipToUpdate.airGlideAmt < -.5f)
					shipToUpdate.airGlideAmt = -.5f;
			}
			if (shipToUpdate.shipInput.isTurningRight) {
				shipToUpdate.airGlideAmt += 0.008f;
				if (shipToUpdate.airGlideAmt > .5f)
					shipToUpdate.airGlideAmt = .5f;
			}
		} else {
			shipToUpdate.airGlide = false;
			shipToUpdate.airGlideAmt *= 0.96f;

			if (Math.abs(shipToUpdate.airGlideAmt) < 0.001f) {
				shipToUpdate.airGlideAmt = 0;
			}
		}

		float lTurnModFront = 0.25f;
		float lTurnModRear = 0.25f;
		float lMaxSpeedMod = 1.f;

		if (shipToUpdate.airGlide) {
			lTurnModRear = 1.f + (float) Math.abs(shipToUpdate.airGlideAmt) * 0.75f;
			lMaxSpeedMod = 1.f - (float) Math.abs(shipToUpdate.airGlideAmt) * 0.5f;
		}

		final float MAX_STEER_ANGLE = 0.15f;

		shipToUpdate.wheelBase = 20;
		shipToUpdate.speed = MathHelper.clamp(shipToUpdate.speed * lMaxSpeedMod, -shipToUpdate.shipSpeedMax * 0.5f, shipToUpdate.shipSpeedMax);
		shipToUpdate.steerFrontAngle = MathHelper.clamp(shipToUpdate.steerFrontAngle, -MAX_STEER_ANGLE, MAX_STEER_ANGLE);
		shipToUpdate.steerRearAngle = MathHelper.clamp(shipToUpdate.steerRearAngle, -MAX_STEER_ANGLE, MAX_STEER_ANGLE);

		shipToUpdate.steerFrontAngle *= lTurnModFront;
		shipToUpdate.steerRearAngle *= lTurnModRear;

		shipToUpdate.frontWheelPosition.x = shipToUpdate.worldPositionX() + shipToUpdate.wheelBase / 2 * (float) Math.cos(shipToUpdate.heading);
		shipToUpdate.frontWheelPosition.y = shipToUpdate.worldPositionY() + shipToUpdate.wheelBase / 2 * (float) Math.sin(shipToUpdate.heading);

		shipToUpdate.rearWheelPosition.x = shipToUpdate.worldPositionX() - shipToUpdate.wheelBase / 2 * (float) Math.cos(shipToUpdate.heading);
		shipToUpdate.rearWheelPosition.y = shipToUpdate.worldPositionY() - shipToUpdate.wheelBase / 2 * (float) Math.sin(shipToUpdate.heading);

		// TODO: Front wheel turning
		// The wheel are what propel the vehicle forwards (with the application of gas)
		// move the rear wheels (ship heading)
		shipToUpdate.frontWheelPosition.x += shipToUpdate.speed * lDelta * (float) Math.cos(shipToUpdate.heading + shipToUpdate.steerFrontAngle);
		shipToUpdate.frontWheelPosition.y += shipToUpdate.speed * lDelta * (float) Math.sin(shipToUpdate.heading + shipToUpdate.steerFrontAngle);

		// move the front wheels (ship heading + steering direction)
		shipToUpdate.rearWheelPosition.x += shipToUpdate.speed * lDelta * (float) Math.cos(shipToUpdate.heading - shipToUpdate.steerRearAngle);
		shipToUpdate.rearWheelPosition.y += shipToUpdate.speed * lDelta * (float) Math.sin(shipToUpdate.heading - shipToUpdate.steerRearAngle);

		// extrapolate the new ship location (center of wheels)
		shipToUpdate.worldPositionX((shipToUpdate.frontWheelPosition.x + shipToUpdate.rearWheelPosition.x) / 2);
		shipToUpdate.worldPositionY((shipToUpdate.frontWheelPosition.y + shipToUpdate.rearWheelPosition.y) / 2);

		shipToUpdate.heading = (float) Math.atan2(shipToUpdate.frontWheelPosition.y - shipToUpdate.rearWheelPosition.y, shipToUpdate.frontWheelPosition.x - shipToUpdate.rearWheelPosition.x);

		shipToUpdate.speed *= 0.94f;
		shipToUpdate.steerFrontAngle *= 0.94f;

		// TODO: Health
	}

	private void updateShipProgress(LintfordCore core, Track track, Ship ship) {
		final int lNumControlNodes = track.trackSpline().numberSplineControlPoints();

		{
			final var lTrack = mTrackController.currentTrack();
			final var lProgress = ship.shipProgress;

			float lTotalDistance = 0.f;
			lTotalDistance += lTrack.getTrackDistance() * lProgress.currentLapNumber;

			float lDistanceThisRound = lTrack.trackSpline().getControlPoint(ship.shipProgress.currentNodeUid).accLength;
			lTotalDistance += lDistanceThisRound;

			float lDistanceThisSegment = getCarDistanceIntoSegment(ship);

			final int lNextNodeUId = (int) ((ship.shipProgress.currentNodeUid >= lNumControlNodes) ? 0 : ship.shipProgress.currentNodeUid);
			final var lSegment = lTrack.trackSpline().getControlPoint(lNextNodeUId);

			if (lDistanceThisSegment <= 0) {
				ship.shipProgress.currentNodeUid--;
				if (ship.shipProgress.currentNodeUid < 0) {
					// TODO: There could be a better check for this
					ship.shipProgress.currentLapNumber--;
					ship.shipProgress.currentNodeUid = lNumControlNodes - 1;
				}
			}

			if (lDistanceThisSegment >= lSegment.length) {
				ship.shipProgress.currentNodeUid++;
			}

			if (ship.shipProgress.currentNodeUid >= lNumControlNodes) { // lapped
				ship.shipProgress.currentNodeUid = 0;
				ship.shipProgress.currentLapNumber++;
			}

			lTotalDistance += lDistanceThisSegment;

			ship.shipProgress.distanceIntoRace = lTotalDistance;

			final int lCurrentNodeUid = (int) ((ship.shipProgress.currentNodeUid >= lNumControlNodes) ? 0 : ship.shipProgress.currentNodeUid);
			final float lShipPositionAlongSpling = lTrack.trackSpline().getNormalizedPositionAlongSpline(lCurrentNodeUid, ship.worldPositionX(), ship.worldPositionY());
			final float lTotalNormalizedPosition = lCurrentNodeUid + lShipPositionAlongSpling >= lNumControlNodes ? 0 : lCurrentNodeUid + lShipPositionAlongSpling;

			final var lTrackSplinePoint = lTrack.trackSpline().getPointOnSpline(lTotalNormalizedPosition);
			final var lTrackSplineGradient = lTrack.trackSpline().getSplineGradient(lTotalNormalizedPosition);

			ship.pointOnTrackX = lTrackSplinePoint.x;
			ship.pointOnTrackY = lTrackSplinePoint.y;
			ship.trackAngle = lTrackSplineGradient;
		}
	}

	private float getCarDistanceIntoSegment(Ship ship) {
		final var lTrack = mTrackController.currentTrack();

		final int lNumControlNodes = lTrack.trackSpline().numberSplineControlPoints();
		final int lLastNodeId = (int) ((ship.shipProgress.currentNodeUid >= lNumControlNodes) ? 0 : ship.shipProgress.currentNodeUid);
		final var lSegment = lTrack.trackSpline().getControlPoint(lLastNodeId);

		return lTrack.trackSpline().getNormalizedPositionAlongSpline(lLastNodeId, ship.worldPositionX(), ship.worldPositionY()) * lSegment.length;
	}

	// untested
	private void updateShipAi(LintfordCore core, Ship ship) {
		final var lTrack = mTrackController.currentTrack();
		final var lTrackSpline = lTrack.trackSpline();
		final int lNumControlNodes = lTrackSpline.numberSplineControlPoints();

		final int lCurrentNodeUid = (int) ((ship.shipProgress.currentNodeUid >= lNumControlNodes) ? 0 : ship.shipProgress.currentNodeUid);

		final float lShipPositionAlongSpling = lTrack.trackSpline().getNormalizedPositionAlongSpline(lCurrentNodeUid, ship.worldPositionX(), ship.worldPositionY());
		final float lTotalNormalizedPosition = lCurrentNodeUid + lShipPositionAlongSpling >= lNumControlNodes ? 0 : lCurrentNodeUid + lShipPositionAlongSpling;

		final var lTrackSplinePoint = lTrack.trackSpline().getPointOnSpline(lTotalNormalizedPosition);
		final var lTrackSplineGradient = lTrack.trackSpline().getSplineGradient(lTotalNormalizedPosition);

		ship.pointOnTrackX = lTrackSplinePoint.x;
		ship.pointOnTrackY = lTrackSplinePoint.y;
		ship.trackAngle = lTrackSplineGradient;

		// TODO: Move the enemy ships

	}

	private void setupPlayerShip() {
		final var lPlayerShip = mShipManager.playerShip();

		float lTrackAngle = getTrackGradientAtVehicleLocation(lPlayerShip);
		lPlayerShip.trackAngle = lTrackAngle;

	}

	private float getVehicleDistanceIntoSegmentNormalized(Ship pShip) {
		final var lTrack = mTrackController.currentTrack();

		final int lNumControlNodes = lTrack.trackSpline().numberSplineControlPoints();
		final int lLastNodeId = (int) ((pShip.shipProgress.currentNodeUid + 1 >= lNumControlNodes) ? 0 : pShip.shipProgress.currentNodeUid);

		return lTrack.trackSpline().getNormalizedPositionAlongSpline(lLastNodeId, pShip.worldPositionX(), pShip.worldPositionY());
	}

	private float getTrackGradientAtVehicleLocation(Ship pShip) {
		final var lTrack = mTrackController.currentTrack();

		final int lNumControlNodes = lTrack.trackSpline().numberSplineControlPoints();
		final int lLastNodeId = (int) ((pShip.shipProgress.currentNodeUid >= lNumControlNodes) ? 0 : pShip.shipProgress.currentNodeUid);
		final int lNextNodeId = (int) ((lLastNodeId + 1 >= lNumControlNodes) ? 0 : lLastNodeId + 1);

		final var lCarPositionAlongSpling = getVehicleDistanceIntoSegmentNormalized(pShip);

		SplinePoint lTrackSplinePoint = lTrack.trackSpline().getPointOnSpline(lLastNodeId + lCarPositionAlongSpling);

		final float lNode0X = lTrackSplinePoint.x;
		final float lNode0Y = lTrackSplinePoint.y;

		lTrackSplinePoint = lTrack.trackSpline().getControlPoint(lNextNodeId);

		final float lNode1X = lTrackSplinePoint.x;
		final float lNode1Y = lTrackSplinePoint.y;

		final float lHeadingVecX = lNode1X - lNode0X;
		final float lHeadingVecY = lNode1Y - lNode0Y;

		return (float) Math.atan2(lHeadingVecX, -lHeadingVecY);
	}

	// ---

	static float turnToFace(float pTrackHeading, float pCurrentAngle, float pTurnSpeed) {
		float difference = wrapAngle(pTrackHeading - pCurrentAngle);

		difference = MathHelper.clamp(difference, -pTurnSpeed, pTurnSpeed);

		return wrapAngle(difference);
	}

	public static float wrapAngle(float radians) {
		while (radians < -Math.PI) {
			radians += Math.PI * 2;
		}
		while (radians > Math.PI) {
			radians -= Math.PI * 2;
		}
		return radians;
	}
}
