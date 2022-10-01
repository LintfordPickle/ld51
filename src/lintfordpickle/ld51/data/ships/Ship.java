package lintfordpickle.ld51.data.ships;

import net.lintford.library.core.entity.CircleEntity;
import net.lintford.library.core.maths.RandomNumbers;
import net.lintford.library.core.maths.Vector2f;

public class Ship extends CircleEntity {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = 405336221318606949L;

	public final static float SHIP_ACCELERATION = 10;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public final int shipUid;
	public final ShipProgress shipProgress = new ShipProgress();
	public final ShipInput shipInput = new ShipInput();

	// TODO: lots of overlap here
	public final Vector2f headingTo = new Vector2f();
	public final Vector2f frontWheelPosition = new Vector2f();
	public final Vector2f rearWheelPosition = new Vector2f();

	public boolean isPlayerControlled;
	public float dr;
	public float dx, dy;

	// chasis properties
	public float width;
	public float height;

	// ship properties
	public boolean isDestroyed;
	public boolean airGlide;
	public float airGlideAmt; // [-.5, .5]
	public boolean gasDown;
	public boolean isSteering;

	// progress 
	public float pointOnTrackX;
	public float pointOnTrackY;
	public float trackAngle;
	public float aiHeadingAngle;

	public float wheelBase;
	public float heading;
	public float speed;
	public float shipSpeedMax;
	public float carTurnAngleInc;
	public float carTurnAngleMax;
	public float steerFrontAngle;
	public float steerRearAngle;

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public Ship(int shipUid) {
		super();
		this.shipUid = shipUid;
		shipProgress.shipIndex = shipUid;

		width = 64;
		height = 128;
		mRadius = 48;

		shipSpeedMax = 100 * RandomNumbers.random(0.90f, 1.1f);
		carTurnAngleInc = (float) Math.toRadians(1.1f * RandomNumbers.random(0.90f, 1.1f));
		carTurnAngleMax = (float) Math.toRadians(3f * RandomNumbers.random(0.90f, 1.1f));

	}

}
