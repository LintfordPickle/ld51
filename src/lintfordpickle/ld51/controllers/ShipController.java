package lintfordpickle.ld51.controllers;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import lintfordpickle.ld51.ConstantsGame;
import lintfordpickle.ld51.data.ships.Ship;
import lintfordpickle.ld51.data.ships.ShipManager;
import lintfordpickle.ld51.data.tracks.Track;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.geometry.Circle;
import net.lintford.library.core.maths.MathHelper;
import net.lintford.library.core.maths.Vector2f;
import net.lintford.library.core.splines.SplinePoint;

public class ShipController extends BaseController {

	// RECFACTOR

	// lines as capsules (line with radius)
	public class LineSegment {

		public float sx, sy;
		public float ex, ey;
		public float radius;
	}

	public List<LineSegment> mLineSegments = new ArrayList<>();

	// DEBUG to draw
	private final Vector2f lTempSideVector = new Vector2f();
	private final SplinePoint tempSideSpline = new SplinePoint();
	public final LineSegment innerWallSegment = new LineSegment();
	public final LineSegment outerWallSegment = new LineSegment();
	private final Circle wallCollisionBall = new Circle();
	// END

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

		float fMarkerMod = 0.f;

		final var lShips = mShipManager.ships();
		final int lNumShips = lShips.size();
		for (int i = 0; i < lNumShips; i++) {
			fMarkerMod += 0.05f;
			final var lShip = lShips.get(i);

			final var lTrackPosition = mTrackController.mTrack.trackSpline().getPointOnSpline(fMarkerMod);
			final var lTrackGradient = mTrackController.mTrack.trackSpline().getSplineGradient(fMarkerMod);

			lShip.worldPositionX(lTrackPosition.x);
			lShip.worldPositionY(lTrackPosition.y);
			lShip.heading = lTrackGradient;
		}
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

		if (core.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_R)) {
			final var lShip = mShipManager.playerShip();
			final var lTrack = mTrackController.currentTrack();
			final var lTrackSpline = lTrack.trackSpline();
			final var lFirstPoint = lTrackSpline.points().get(0);
			final var lGradiantValue = lTrackSpline.getSplineGradient(0.f);

			lShip.worldPositionX(lFirstPoint.x);
			lShip.worldPositionY(lFirstPoint.y);
			lShip.heading = lGradiantValue;
		}

		return super.handleInput(core);
	}

	@Override
	public void update(LintfordCore core) {
		super.update(core);

		final var lTrack = mTrackController.currentTrack();
		final var lShipList = mShipManager.ships();
		final int lNumShips = lShipList.size();

		for (int i = 0; i < lNumShips; i++) {
			final var lShip = lShipList.get(i);

			if (!lShip.isPlayerControlled)
				updateShipAi(core, lShip);

			updateShip(core, lShip);
			updateShipProgress(core, lTrack, lShip);

			if (lShip.isPlayerControlled)
				handleShipsOnLevelCollisions(core, mTrackController.mTrack, lShip);

		}
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void updateShip(LintfordCore core, Ship shipToUpdate) {
		final float lDelta = (float) core.gameTime().elapsedTimeMilli() * 0.001f;

		final float MAX_STEER_ANGLE_IN_RADIANS = 0.43f;
		final float INC_STEER_ANGLE_IN_RADIANS = (float) Math.toRadians(1f);

		final float SHIP_MAX_ACCEL_PER_FRAME = 30.f;
		final float SHIP_MAX_SPEED = 300.f;

		if (shipToUpdate.shipInput.isGas)
			shipToUpdate.speed += SHIP_MAX_ACCEL_PER_FRAME;

		if (shipToUpdate.shipInput.isTurningLeft) {
//			shipToUpdate.steerFrontAngle -= INC_STEER_ANGLE_IN_RADIANS; // shipToUpdate.carTurnAngleInc;
			shipToUpdate.steerRearAngle -= INC_STEER_ANGLE_IN_RADIANS; // shipToUpdate.carTurnAngleInc;
			shipToUpdate.isSteering = true;
		}

		if (shipToUpdate.shipInput.isTurningRight) {
//			shipToUpdate.steerFrontAngle += INC_STEER_ANGLE_IN_RADIANS; // shipToUpdate.carTurnAngleInc;
			shipToUpdate.steerRearAngle += INC_STEER_ANGLE_IN_RADIANS; // shipToUpdate.carTurnAngleInc;
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

			if (Math.abs(shipToUpdate.airGlideAmt) < 0.001f)
				shipToUpdate.airGlideAmt = 0;
		}

		float lTurnModFront = 1.f;
		float lTurnModRear = 1.f;
		float lMaxSpeedMod = 1.f;

		if (shipToUpdate.airGlide) {
//			lTurnModRear = 0.65f + (float) Math.abs(shipToUpdate.airGlideAmt) * 0.055f;
//			lTurnModFront = 0.45f + (float) Math.abs(shipToUpdate.airGlideAmt) * 0.055f;
//			lMaxSpeedMod = 1.f - (float) Math.abs(shipToUpdate.airGlideAmt) * 0.5f;
//			shipToUpdate.steerFrontAngle *= lTurnModFront;
//			shipToUpdate.steerRearAngle *= lTurnModRear;
		}

		shipToUpdate.wheelBase = 20;
		shipToUpdate.speed = MathHelper.clamp(shipToUpdate.speed * lMaxSpeedMod, -SHIP_MAX_SPEED * 0.5f, SHIP_MAX_SPEED);
		shipToUpdate.steerFrontAngle = MathHelper.clamp(shipToUpdate.steerFrontAngle, -MAX_STEER_ANGLE_IN_RADIANS, MAX_STEER_ANGLE_IN_RADIANS);
		shipToUpdate.steerRearAngle = MathHelper.clamp(shipToUpdate.steerRearAngle, -MAX_STEER_ANGLE_IN_RADIANS, MAX_STEER_ANGLE_IN_RADIANS);

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
		shipToUpdate.steerRearAngle *= 0.94f;

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

			ship.pointOnLoResTrackX = lTrackSplinePoint.x;
			ship.pointOnLoResTrackY = lTrackSplinePoint.y;
			ship.loResTrackAngle = lTrackSplineGradient;
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

		final var lOurPositionOnSpline = lTrack.trackSpline().getPointOnSpline(lTotalNormalizedPosition);
		final var lOurGradientOnSpline = lTrack.trackSpline().getSplineGradient(lTotalNormalizedPosition);

		ship.pointOnLoResTrackX = lOurPositionOnSpline.x;
		ship.pointOnLoResTrackY = lOurPositionOnSpline.y;
		ship.loResTrackAngle = lOurGradientOnSpline;

		{
			final float lLookAheadAmount = lCurrentNodeUid + lShipPositionAlongSpling + 0.15f;
			final float lProjectedNormalizedPosition = (lLookAheadAmount >= lNumControlNodes) ? lLookAheadAmount - lNumControlNodes : lLookAheadAmount;
			final var lProjectedPointOnSpline = lTrack.trackSpline().getPointOnSpline(lProjectedNormalizedPosition);

			final float lOurPositionX = ship.worldPositionX();
			final float lOurPositionY = ship.worldPositionY();

			final float lHeadingVecX = lProjectedPointOnSpline.x - lOurPositionX;
			final float lHeadingVecY = lProjectedPointOnSpline.y - lOurPositionY;

			final float headingTowards = (float) Math.atan2(lHeadingVecY, lHeadingVecX);
			ship.headingTowards = headingTowards;

			float tempAngle = turnToFace(headingTowards, ship.loResTrackAngle, 0.325f);
			ship.steerFrontAngle = tempAngle;
		}

		ship.shipInput.isGas = true; // just go
	}

	private void setupPlayerShip() {
		final var lPlayerShip = mShipManager.playerShip();

		float lTrackAngle = getTrackGradientAtVehicleLocation(lPlayerShip);
		lPlayerShip.loResTrackAngle = lTrackAngle;

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

	private void handleShipOnShipCollisions(LintfordCore core) {

	}

	private void handleShipsOnLevelCollisions(LintfordCore core, Track track, Ship ship) {
		buildCollisionWallsAroundShip(core, track, ship);

		{ // Inner edge
			float lLineX1 = innerWallSegment.ex - innerWallSegment.sx;
			float lLineY1 = innerWallSegment.ey - innerWallSegment.sy;

			float lLineX2 = ship.worldPositionX() - innerWallSegment.sx;
			float lLineY2 = ship.worldPositionY() - innerWallSegment.sy;

			float lEdgeLength = lLineX1 * lLineX1 + lLineY1 * lLineY1;

			float v = lLineX1 * lLineX2 + lLineY1 * lLineY2;
			float t = MathHelper.clamp(v, 0.f, lEdgeLength) / lEdgeLength;

			float lClosestPointX = innerWallSegment.sx + t * lLineX1;
			float lClosestPointY = innerWallSegment.sy + t * lLineY1;

			float distance = (float) Math.sqrt((ship.worldPositionX() - lClosestPointX) * (ship.worldPositionX() - lClosestPointX) + (ship.worldPositionY() - lClosestPointY) * (ship.worldPositionY() - lClosestPointY));

			if (distance <= (innerWallSegment.radius + ship.radius())) {
				// Collision
				System.out.println("inner collision");
				wallCollisionBall.set(lClosestPointX, lClosestPointY, innerWallSegment.radius);
			}
		}

		{ // Out edge
			float lLineX1 = outerWallSegment.ex - outerWallSegment.sx;
			float lLineY1 = outerWallSegment.ey - outerWallSegment.sy;

			float lLineX2 = ship.worldPositionX() - outerWallSegment.sx;
			float lLineY2 = ship.worldPositionY() - outerWallSegment.sy;

			float lEdgeLength = lLineX1 * lLineX1 + lLineY1 * lLineY1;

			float v = lLineX1 * lLineX2 + lLineY1 * lLineY2;
			float t = MathHelper.clamp(v, 0.f, lEdgeLength) / lEdgeLength;

			float lClosestPointX = outerWallSegment.sx + t * lLineX1;
			float lClosestPointY = outerWallSegment.sy + t * lLineY1;

			float distance = (float) Math.sqrt((ship.worldPositionX() - lClosestPointX) * (ship.worldPositionX() - lClosestPointX) + (ship.worldPositionY() - lClosestPointY) * (ship.worldPositionY() - lClosestPointY));

			if (distance <= (outerWallSegment.radius + ship.radius())) {
				// Collision
				System.out.println("Outer collision");
				wallCollisionBall.set(lClosestPointX, lClosestPointY, outerWallSegment.radius);
			}
		}
	}

	private void buildCollisionWallsAroundShip(LintfordCore core, Track track, Ship ship) {
		float len = ConstantsGame.TRACK_SEG_REG_WIDTH / 2.f;
		final float lWallLineRadius = 2.f;
		final float lLeadAmount = 0.15f;

		final var lTrackSpline = track.trackSpline();
		final int lNumControlNodes = lTrackSpline.numberSplineControlPoints();

		final int lCurrentNodeUid = (int) ((ship.shipProgress.currentNodeUid >= lNumControlNodes) ? 0 : ship.shipProgress.currentNodeUid);
		final float lShipPositionAlongSpling = lTrackSpline.getNormalizedPositionAlongSpline(lCurrentNodeUid, ship.worldPositionX(), ship.worldPositionY());

		{
			// behind part

			final float lLeadBehindDistance = lCurrentNodeUid + lShipPositionAlongSpling - lLeadAmount;
			final float lTotalNormalizedBehindPosition = lLeadBehindDistance < 0 ? lLeadBehindDistance + lNumControlNodes : lLeadBehindDistance;
			final var lTrackSplineBehindPoint = track.trackSpline().getPointOnSpline(lTotalNormalizedBehindPosition);
			final var lTrackSplineBehindGradient = track.trackSpline().getSplineGradientPoint(lTotalNormalizedBehindPosition);

			tempSideSpline.x = lTrackSplineBehindGradient.y;
			tempSideSpline.y = -lTrackSplineBehindGradient.x;

			lTempSideVector.set(tempSideSpline.x, tempSideSpline.y);
			lTempSideVector.nor();

			innerWallSegment.sx = lTrackSplineBehindPoint.x - lTempSideVector.x * len;
			innerWallSegment.sy = lTrackSplineBehindPoint.y - lTempSideVector.y * len;

			outerWallSegment.sx = lTrackSplineBehindPoint.x + lTempSideVector.x * len;
			outerWallSegment.sy = lTrackSplineBehindPoint.y + lTempSideVector.y * len;
		}

		{
			final float lLeadAheadDistance = lCurrentNodeUid + lShipPositionAlongSpling + lLeadAmount;
			final float lTotalNormalizedAheadPosition = lLeadAheadDistance >= lNumControlNodes ? lLeadAheadDistance - lNumControlNodes : lLeadAheadDistance;

			final var lTrackSplineAheadPoint = track.trackSpline().getPointOnSpline(lTotalNormalizedAheadPosition);
			final var lTrackSplineAheadGradient = track.trackSpline().getSplineGradientPoint(lTotalNormalizedAheadPosition);

			tempSideSpline.x = lTrackSplineAheadGradient.y;
			tempSideSpline.y = -lTrackSplineAheadGradient.x;

			lTempSideVector.set(tempSideSpline.x, tempSideSpline.y);
			lTempSideVector.nor();

			innerWallSegment.ex = lTrackSplineAheadPoint.x - lTempSideVector.x * len;
			innerWallSegment.ey = lTrackSplineAheadPoint.y - lTempSideVector.y * len;

			outerWallSegment.ex = lTrackSplineAheadPoint.x + lTempSideVector.x * len;
			outerWallSegment.ey = lTrackSplineAheadPoint.y + lTempSideVector.y * len;
		}

		innerWallSegment.radius = lWallLineRadius;
		outerWallSegment.radius = lWallLineRadius;
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
