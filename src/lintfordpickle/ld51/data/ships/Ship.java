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

	public float wallCollTimer;

	// This is the driving angle
	public float steeringAngle; // vector we are steering toward (heading+steering)
	public float headingAngle; // vector we are facing
	public float speed;

	// chasis properties
	public float width;
	public float height;

	public float zHeight;

	// ship properties
	public boolean isDestroyed;
	public boolean gasDown;

	// progress
	public float pointOnLoResTrackX;
	public float pointOnLoResTrackY;
	public float loResTrackAngle;

	public float wheelBase;

	public float tiltAmount; // [-1,1]
	public int tiltLevel; // [0-4]

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public Ship(int shipUid) {
		super();
		this.shipUid = shipUid;
		shipProgress.shipIndex = shipUid;

		zHeight = 0.5f;
		width = 20;
		height = 10;
		radius = 10;
		mass = radius * 10.f;
	}
}
