package lintfordpickle.ld51.controllers;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import lintfordpickle.ld51.ConstantsGame;
import lintfordpickle.ld51.data.MoveableWorldEntity;
import lintfordpickle.ld51.data.ships.Ship;
import lintfordpickle.ld51.data.ships.ShipManager;
import lintfordpickle.ld51.data.tracks.Track;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.maths.MathHelper;
import net.lintford.library.core.maths.Vector2f;
import net.lintford.library.core.splines.SplinePoint;

public class ShipController extends BaseController {

	public static boolean USE_DYNAMIC_COLLISION_RESPONSE = true;

	// RECFACTOR
	public class CollisionShipPair {
		public boolean isActive;
		public MoveableWorldEntity obj0;
		public MoveableWorldEntity obj1;

		public void objectsHaveCollided(MoveableWorldEntity o0, MoveableWorldEntity o1) {
			obj0 = o0;
			obj1 = o1;
			isActive = true;
		}
	}

	public CollisionShipPair getFreeCollisionPair() {
		for (int i = 0; i < MAX_COLLIDERS; i++) {
			if (collidingObjects.get(i).isActive == false)
				return collidingObjects.get(i);
		}
		return null;
	}

	private final int MAX_COLLIDERS = 16;
	private final List<CollisionShipPair> collidingObjects = new ArrayList<>();

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

	private final MoveableWorldEntity wallCollisionBall = new MoveableWorldEntity();
	// END

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "Ship Controller";

	public static final int DEFAULT_NUMBER_OPPONENTS = 1;

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

		// refactor
		for (int i = 0; i < MAX_COLLIDERS; i++) {
			collidingObjects.add(new CollisionShipPair());
		}
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

			lShip.x(lTrackPosition.x);
			lShip.y(lTrackPosition.y);
			lShip.headingAngle = lTrackGradient;
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

		if (core.input().keyboard().isKeyDown(GLFW.GLFW_KEY_U)) {
			final var lShip = mShipManager.playerShip();
			lShip.headingAngle += Math.toRadians(1.f);
		}

		if (core.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_R)) {
			final var lShip = mShipManager.playerShip();
			final var lTrack = mTrackController.currentTrack();
			final var lTrackSpline = lTrack.trackSpline();
			final var lFirstPoint = lTrackSpline.points().get(0);
			final var lGradiantValue = lTrackSpline.getSplineGradient(0.f);

			lShip.x(lFirstPoint.x);
			lShip.y(lFirstPoint.y);
			lShip.headingAngle = lGradiantValue;
			lShip.speed = 0.f;
			lShip.v.set(0, 0);
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

		handleShipOnShipCollisions(core);
		handleDynamicCollisions(core);
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void updateShip(LintfordCore core, Ship ship) {
		final float lDelta = (float) core.gameTime().elapsedTimeMilli() * 0.001f;

		final float SHIP_MAX_ACCEL_PER_FRAME = 20.f;

		final float MAX_STEER_ANGLE_IN_RADIANS = (float) Math.toRadians(35f);
		final float INC_STEER_ANGLE_IN_RADIANS = (float) Math.toRadians(2f);

		if (ship.shipInput.isGas) {
			ship.speed += SHIP_MAX_ACCEL_PER_FRAME;
		}

		ship.v.x += ship.speed * (float) Math.cos(ship.headingAngle + ship.steeringAngle) * lDelta;
		ship.v.y += ship.speed * (float) Math.sin(ship.headingAngle + ship.steeringAngle) * lDelta;

		if (ship.shipInput.isTurningLeft) {
			ship.steeringAngle -= INC_STEER_ANGLE_IN_RADIANS;
		}

		if (ship.shipInput.isTurningRight) {
			ship.steeringAngle += INC_STEER_ANGLE_IN_RADIANS;
		}

		ship.steeringAngle = MathHelper.clamp(ship.steeringAngle, -MAX_STEER_ANGLE_IN_RADIANS, MAX_STEER_ANGLE_IN_RADIANS);

		final boolean isSteering = ship.shipInput.isTurningLeft || ship.shipInput.isTurningRight;
		if (isSteering)
			ship.headingAngle += turnToFace(ship.headingAngle, ship.headingAngle - ship.steeringAngle, 0.025f);

		ship.x += ship.v.x * lDelta;
		ship.y += ship.v.y * lDelta;

		ship.v.x *= 0.97f;
		ship.v.y *= 0.97f;

		ship.speed *= 0.97f;
		ship.steeringAngle *= 0.90f;

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
			final float lShipPositionAlongSpling = lTrack.trackSpline().getNormalizedPositionAlongSpline(lCurrentNodeUid, ship.x(), ship.y());
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

		return lTrack.trackSpline().getNormalizedPositionAlongSpline(lLastNodeId, ship.x(), ship.y()) * lSegment.length;
	}

	// untested
	private void updateShipAi(LintfordCore core, Ship ship) {
		final var lTrack = mTrackController.currentTrack();
		final var lTrackSpline = lTrack.trackSpline();
		final int lNumControlNodes = lTrackSpline.numberSplineControlPoints();

		final int lCurrentNodeUid = (int) ((ship.shipProgress.currentNodeUid >= lNumControlNodes) ? 0 : ship.shipProgress.currentNodeUid);

		final float lShipPositionAlongSpling = lTrack.trackSpline().getNormalizedPositionAlongSpline(lCurrentNodeUid, ship.x(), ship.y());
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

			final float lOurPositionX = ship.x();
			final float lOurPositionY = ship.y();

			final float lHeadingVecX = lProjectedPointOnSpline.x - lOurPositionX;
			final float lHeadingVecY = lProjectedPointOnSpline.y - lOurPositionY;

			final float headingTowards = (float) Math.atan2(lHeadingVecY, lHeadingVecX);
			ship.headingAngle = headingTowards;
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

		return lTrack.trackSpline().getNormalizedPositionAlongSpline(lLastNodeId, pShip.x(), pShip.y());
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

	// --- Collisions

	private boolean doCirclesOverlap(float x1, float y1, float r1, float x2, float y2, float r2) {
		return Math.abs((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)) < (r1 + r2) * (r1 + r2);
	}

	private void handleShipOnShipCollisions(LintfordCore core) {
		final var lShipList = mShipManager.ships();
		final int lNumShips = lShipList.size();

		for (int i = 0; i < lNumShips; i++) {
			final var ship = lShipList.get(i);
			for (int j = 0; j < lNumShips; j++) {
				if (i == j)
					continue;

				final var target = lShipList.get(j);

				if (doCirclesOverlap(ship.x(), ship.y(), ship.radius(), target.x(), target.y(), target.radius())) {
					final var lCollisionPairObject = getFreeCollisionPair();
					if (lCollisionPairObject == null) {
						// cannot handle more collision
						// This shouldn't ever happen - either the collision pairs are not being freed (collisions not handled?) or
						// you have more ships collidiing than in the pool.
						Debug.debugManager().logger().e(getClass().getSimpleName(), "cannot handle more collision. No more CollisionPair objects!");
						// return;

					} else {
						lCollisionPairObject.objectsHaveCollided(ship, target);
					}

					// distance between centers
					float dist = (float) Math.sqrt((ship.x() - target.x()) * (ship.x() - target.x()) + (ship.y() - target.y()) * (ship.y() - target.y()));
					float overlap = (dist - ship.radius() - target.radius()) * .5f;

					// Resovle static collision
					ship.x -= overlap * (ship.x - target.x) / dist;
					ship.y -= overlap * (ship.y - target.y) / dist;

					target.x += overlap * (ship.x - target.x) / dist;
					target.y += overlap * (ship.y - target.y) / dist;
				}
			}
		}
	}

	private void handleShipsOnLevelCollisions(LintfordCore core, Track track, Ship ship) {
		buildCollisionWallsAroundShip(core, track, ship);

		{ // Inner edge
			float lLineX1 = innerWallSegment.ex - innerWallSegment.sx;
			float lLineY1 = innerWallSegment.ey - innerWallSegment.sy;

			float lLineX2 = ship.x() - innerWallSegment.sx;
			float lLineY2 = ship.y() - innerWallSegment.sy;

			float lEdgeLength = lLineX1 * lLineX1 + lLineY1 * lLineY1;

			float v = lLineX1 * lLineX2 + lLineY1 * lLineY2;
			float t = MathHelper.clamp(v, 0.f, lEdgeLength) / lEdgeLength;

			float lClosestPointX = innerWallSegment.sx + t * lLineX1;
			float lClosestPointY = innerWallSegment.sy + t * lLineY1;

			float distance = (float) Math.sqrt((ship.x() - lClosestPointX) * (ship.x() - lClosestPointX) + (ship.y() - lClosestPointY) * (ship.y() - lClosestPointY));

			if (distance <= (innerWallSegment.radius + ship.radius())) {
				// Collision detected
				wallCollisionBall.x = lClosestPointX;
				wallCollisionBall.y = lClosestPointY;
				wallCollisionBall.v.x = -ship.v.x;
				wallCollisionBall.v.y = -ship.v.y;
				wallCollisionBall.mass = ship.mass * 1.f;
				wallCollisionBall.r = innerWallSegment.radius;

//				final var lCollisionPairObject = getFreeCollisionPair();
//				if (lCollisionPairObject != null) {
//					lCollisionPairObject.objectsHaveCollided(ship, wallCollisionBall);
//				}

				// Static collision (keeps you out of the wall)
				float lOverlap = 1.0f * (distance - ship.radius() - wallCollisionBall.radius());
				ship.x -= lOverlap * (ship.x - wallCollisionBall.x) / distance;
				ship.y -= lOverlap * (ship.y - wallCollisionBall.y) / distance;
			}
		}

		{ // Out edge
			float lLineX1 = outerWallSegment.ex - outerWallSegment.sx;
			float lLineY1 = outerWallSegment.ey - outerWallSegment.sy;

			float lLineX2 = ship.x() - outerWallSegment.sx;
			float lLineY2 = ship.y() - outerWallSegment.sy;

			float lEdgeLength = lLineX1 * lLineX1 + lLineY1 * lLineY1;

			float v = lLineX1 * lLineX2 + lLineY1 * lLineY2;
			float t = MathHelper.clamp(v, 0.f, lEdgeLength) / lEdgeLength;

			float lClosestPointX = outerWallSegment.sx + t * lLineX1;
			float lClosestPointY = outerWallSegment.sy + t * lLineY1;

			float distance = (float) Math.sqrt((ship.x() - lClosestPointX) * (ship.x() - lClosestPointX) + (ship.y() - lClosestPointY) * (ship.y() - lClosestPointY));

			if (distance <= (outerWallSegment.radius + ship.radius())) {
				// Collision detected
				wallCollisionBall.x = lClosestPointX;
				wallCollisionBall.y = lClosestPointY;
				wallCollisionBall.v.x = -ship.v.x;
				wallCollisionBall.v.y = -ship.v.y;
				wallCollisionBall.mass = ship.mass * 1.f;
				wallCollisionBall.r = outerWallSegment.radius;

//				final var lCollisionPairObject = getFreeCollisionPair();
//				if (lCollisionPairObject != null) {
//					lCollisionPairObject.objectsHaveCollided(ship, wallCollisionBall);
//				}

				// Static collision (keeps you out of the wall)
				float lOverlap = 1.0f * (distance - ship.radius() - wallCollisionBall.radius());
				ship.x -= lOverlap * (ship.x - wallCollisionBall.x) / distance;
				ship.y -= lOverlap * (ship.y - wallCollisionBall.y) / distance;
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
		final float lShipPositionAlongSpling = lTrackSpline.getNormalizedPositionAlongSpline(lCurrentNodeUid, ship.x(), ship.y());

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

	private void handleDynamicCollisions(LintfordCore core) {
		for (int i = 0; i < MAX_COLLIDERS; i++) {
			final var c = collidingObjects.get(i);
			if (c.isActive == false)
				continue;

			final var b1 = c.obj0;
			final var b2 = c.obj1;

			float dist = (float) Math.sqrt((b1.x - b2.x) * (b1.x - b2.x) + (b1.y - b2.y) * (b1.y - b2.y));

			// normal
			float nx = (b2.x - b1.x) / dist;
			float ny = (b2.y - b1.y) / dist;

			// tangental
			float tx = -ny;
			float ty = nx;

			// float dp tangent
			float dpTan1 = b1.v.x * tx + b1.v.y * ty;
			float dpTan2 = b1.v.y * tx + b2.v.y * ty;

			float dpNor1 = b1.v.x * nx + b1.v.y * ny;
			float dpNor2 = b1.v.y * nx + b2.v.y * ny;

			// conservation of momemtum (1d)
			float m1 = (dpNor1 * (b1.mass - b2.mass) + 2.f * b2.mass * dpNor2) / (b1.mass + b2.mass);
			float m2 = (dpNor2 * (b1.mass - b2.mass) + 2.f * b1.mass * dpNor2) / (b1.mass + b2.mass);

			b1.v.x = tx * dpTan1 + nx * m1;
			b1.v.y = ty * dpTan1 + ny * m1;
			b2.v.x = tx * dpTan2 + nx * m2;
			b2.v.y = ty * dpTan2 + ny * m2;

			c.obj0 = null;
			c.obj1 = null;
			c.isActive = false;
		}
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
