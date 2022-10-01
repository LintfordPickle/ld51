package lintfordpickle.ld51.data.ships;

public class ShipProgress {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public int shipIndex;
	public int currentLapNumber;
	public int lastVisitedNodeId;
	public int nextControlNodeId;
	public int position;

	public boolean hasShipFinished;
	public boolean isGoingWrongWay;
	public float distanceIntoRace;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public ShipProgress() {

	}
}
