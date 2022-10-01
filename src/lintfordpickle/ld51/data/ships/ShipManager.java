package lintfordpickle.ld51.data.ships;

import java.util.ArrayList;
import java.util.List;

public class ShipManager {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private Ship mPlayerShip;
	private final List<Ship> mShipsList = new ArrayList<>();
	private int mShipUidCounter;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	private int getShipPoolUid() {
		return mShipUidCounter++;
	}

	public Ship playerShip() {
		return mPlayerShip;
	}

	public void playerShip(Ship newShip) {
		mPlayerShip = newShip;
	}

	public int numberOfShip() {
		return mShipsList.size();
	}

	public int numberOfActiveOpponents() {
		int lnumAiControlledShips = 0;

		final int lNumberOpponents = mShipsList.size();
		for (int i = 0; i < lNumberOpponents; i++) {
			if (!mShipsList.get(i).isDestroyed && !mShipsList.get(i).isPlayerControlled) {
				lnumAiControlledShips++;
			}
		}

		return lnumAiControlledShips;
	}

	public List<Ship> ships() {
		return mShipsList;
	}

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public ShipManager() {

	}

	public void createPlayerShip() {
		final var lPlayerShip = new Ship(getShipPoolUid());
		lPlayerShip.isPlayerControlled = true;
		mPlayerShip = lPlayerShip;

		mShipsList.add(lPlayerShip);
	}

}
