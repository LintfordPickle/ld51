package lintfordpickle.ld51.data.tracks;

import java.io.Serializable;
import java.util.Arrays;

import com.google.gson.annotations.SerializedName;

public class TrackDefinition implements Serializable {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = -4688017667605284050L;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	@SerializedName(value = "TrackName")
	private String mTrackName;

	@SerializedName(value = "ControlPointsX")
	private float[] mControlPointsX;

	@SerializedName(value = "ControlPointsY")
	private float[] mControlPointsY;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public String trackName() {
		return mTrackName;
	}

	public float[] copyOfControlPointsX() {
		return Arrays.copyOf(mControlPointsX, mControlPointsX.length);
	}

	public float[] copyOfControlPointsY() {
		return Arrays.copyOf(mControlPointsY, mControlPointsY.length);
	}

	public int numControlPoints() {
		return mControlPointsX.length;
	}

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public TrackDefinition(float[] controlPointsX, float[] controlPointsY) {
		mControlPointsX = controlPointsX;
		mControlPointsY = controlPointsY;
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public boolean isTrackDefinitionValid() {
		if (mControlPointsX == null || mControlPointsX.length == 0 || mControlPointsY == null || mControlPointsY.length == 0)
			return false;

		if (mControlPointsX.length != mControlPointsY.length)
			return false;

		return true;
	}
}
