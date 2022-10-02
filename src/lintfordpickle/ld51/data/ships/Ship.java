package lintfordpickle.ld51.data.ships;

import lintfordpickle.ld51.data.MoveableWorldEntity;

public class Ship extends MoveableWorldEntity {

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

	public boolean isPlayerControlled;

	// This is the driving angle
	public float steeringAngle;
	public float headingAngle;
	public float headingLength;

	// chasis properties
	public float width;
	public float height;

	// ship properties
	public boolean isDestroyed;
	public boolean gasDown;

	// progress
	public float pointOnLoResTrackX;
	public float pointOnLoResTrackY;
	public float loResTrackAngle;

	public float wheelBase;

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public Ship(int shipUid) {
		super();
		this.shipUid = shipUid;
		shipProgress.shipIndex = shipUid;

		width = 20;
		height = 10;
		r = 15;
		mass = r * 10.f;
	}
}
