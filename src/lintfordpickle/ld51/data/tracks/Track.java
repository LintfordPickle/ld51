package lintfordpickle.ld51.data.tracks;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.splines.Spline;
import net.lintford.library.core.splines.SplinePoint;
import net.lintford.library.core.storage.FileUtils;

/** Stores a {@link TrackDefinition} file as a raceable track (with splines etc.)  */
public class Track {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private TrackDefinition mTrackDefinition;

	private Spline mTrackSpline;
	private boolean mIsTrackBuilt;
	private boolean mIsDirty;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public boolean isDirty() {
		return mIsDirty;
	}

	public TrackDefinition trackDefinition() {
		return mTrackDefinition;
	}

	public boolean isTrackLoaded() {
		return mTrackDefinition != null;
	}

	public Spline trackSpline() {
		return mTrackSpline;
	}

	public float getTrackDistance() {
		return mTrackSpline.totalSplineLength();
	}

	public boolean isTrackDefinitionUpToDate() {
		if (mTrackDefinition == null)
			return false;

		if (mTrackDefinition.numControlPoints() != mTrackSpline.numberSplineControlPoints())
			return false;

		return !mIsDirty;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public Track() {

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void saveToFile(String filename) {
		if (isTrackDefinitionUpToDate() == false)
			updateTrackDefinition();

		try (Writer writer = new FileWriter(filename)) {
			Gson gson = new GsonBuilder().create();
			gson.toJson(mTrackDefinition, writer);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		return;
	}

	public void loadTrackDefinitionFromDefinition(TrackDefinition trackDefinition) {
		mTrackDefinition = trackDefinition;
		buildTrackFromDefinition();
	}

	public void loadTrackDefinitionFromFile(String filename) {
		final var lGson = new GsonBuilder().create();

		String lTrackRawFileContents = null;
		TrackDefinition lTrackDefinition = null;

		try {
			lTrackRawFileContents = FileUtils.loadString(filename);
			lTrackDefinition = lGson.fromJson(lTrackRawFileContents, TrackDefinition.class);

		} catch (JsonSyntaxException ex) {
			Debug.debugManager().logger().printException(getClass().getSimpleName(), ex);
		}

		if (lTrackDefinition == null) {
			Debug.debugManager().logger().e(getClass().getSimpleName(), "There was an error reading the track information (" + filename + ")");
			return;
		}

		mTrackDefinition = lTrackDefinition;
		buildTrackFromDefinition();
	}

	private void buildTrackFromDefinition() {
		if (isTrackLoaded() == false)
			return;

		if (mTrackDefinition.isTrackDefinitionValid() == false)
			return;

		if (mIsTrackBuilt)
			return;

		final var lNumPoints = mTrackDefinition.numControlPoints();
		final var lPoints = new SplinePoint[lNumPoints];

		final var lControlPointsX = mTrackDefinition.copyOfControlPointsX();
		final var lControlPointsY = mTrackDefinition.copyOfControlPointsY();

		for (int i = 0; i < lNumPoints; i++) {
			final float lX = (float) lControlPointsX[i];
			final float lY = (float) lControlPointsY[i];

			lPoints[i] = new SplinePoint(lX, lY);
		}

		mTrackSpline = new Spline(lPoints);
		mTrackSpline.isLooped(true);

		mIsTrackBuilt = true;
	}

	public void onTrackChanged() {
		mTrackSpline.recalculate();
		mIsDirty = true;
	}

	private void updateTrackDefinition() {
		final var lSplinePoints = mTrackSpline.points();
		final var lNumControlPoints = lSplinePoints.size();

		final var lControlPointsX = new float[lNumControlPoints];
		final var lControlPointsY = new float[lNumControlPoints];

		for (int i = 0; i < lNumControlPoints; i++) {
			final var lControlPoint = lSplinePoints.get(i);
			lControlPointsX[i] = lControlPoint.x;
			lControlPointsY[i] = lControlPoint.y;
		}

		mTrackDefinition = new TrackDefinition(lControlPointsX, lControlPointsY);
		mIsDirty = false;
	}

}
