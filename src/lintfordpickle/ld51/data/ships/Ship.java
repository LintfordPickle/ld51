package lintfordpickle.ld51.data.ships;

import net.lintford.library.core.entity.CircleEntity;
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
	public float pointOnLoResTrackX;
	public float pointOnLoResTrackY;
	public float loResTrackAngle;

	public float pointOnHiResTrackX;
	public float pointOnHiResTrackY;
	public float hiResTrackAngle;

	public float wheelBase;
	public float heading;
	public float headingTowards;
	public float speed;

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
	}
}
