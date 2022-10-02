package lintfordpickle.ld51.controllers;

import java.util.ArrayList;
import java.util.List;

import lintfordpickle.ld51.ConstantsGame;
import lintfordpickle.ld51.data.tracks.GameFileHeader;
import lintfordpickle.ld51.data.tracks.Track;
import lintfordpickle.ld51.data.tracks.TrackDefinition;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.maths.Vector2f;
import net.lintford.library.core.splines.Spline;
import net.lintford.library.core.splines.SplinePoint;

public class TrackController extends BaseController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "Track Controller";

	protected float mSegmentWidth = ConstantsGame.TRACK_SEG_REG_WIDTH;
	protected float mTrackScale = 1.f;

	protected Vector2f[] mInnerVertices;
	protected Vector2f[] mOuterVertices;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------{

	protected GameFileHeader mGameFileHeader;
	protected Track mTrack;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public Vector2f[] innerTrackVertices() {
		return mInnerVertices;
	}

	public Vector2f[] outerTrackVertices() {
		return mOuterVertices;
	}

	@Override
	public boolean isInitialized() {
		return mTrack != null;
	}

	public Track currentTrack() {
		return mTrack;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrackController(ControllerManager controllerManager, GameFileHeader gameFileHeader, int entityGroupID) {
		super(controllerManager, CONTROLLER_NAME, entityGroupID);

		mGameFileHeader = gameFileHeader;
		mTrack = new Track();

		if (mGameFileHeader.isValid())
			mTrack.loadTrackDefinitionFromFile(mGameFileHeader.trackFilename());
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore core) {
		buildHiResolutionTrack();
	}

	@Override
	public void unload() {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(LintfordCore core) {
		super.update(core);

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private Spline getHiResSpline(Spline pSpline) {
		List<SplinePoint> lNewSplinePoints = new ArrayList<>();
		final var offset = pSpline.isLooped() ? 0f : 3f;
		for (float t = 0; t < pSpline.points().size() - offset;) {

			final var lPoint = pSpline.getPointOnSpline(t);
			SplinePoint lNewSplinePoint = new SplinePoint(lPoint.x, lPoint.y);

			lNewSplinePoints.add(lNewSplinePoint);

			float lSegmentLength = pSpline.calculateSegmentLength((int) t);
			float lStepSize = 3.f / (lSegmentLength / 5.f);

			t += lStepSize;
		}

		SplinePoint[] lSplinePoints = new SplinePoint[lNewSplinePoints.size()];
		lNewSplinePoints.toArray(lSplinePoints);

		return new Spline(lSplinePoints);
	}

	public TrackDefinition createDefaultTrackDefinition() {
		final int lDefautlNumControlPoints = 20;

		final var lControlPointsX = new float[lDefautlNumControlPoints];
		final var lControlPointsY = new float[lDefautlNumControlPoints];

		final var lOriginX = 0.f;
		final var lOriginY = 0.f;

		final var lRadius = 25.f;

		for (int i = 0; i < lDefautlNumControlPoints; i++) {
			final float lNormalizedI = (float) i / lDefautlNumControlPoints;
			lControlPointsX[i] = lOriginX + lRadius * (float) Math.cos(lNormalizedI * Math.PI * 2.f);
			lControlPointsY[i] = lOriginY + lRadius * (float) Math.sin(lNormalizedI * Math.PI * 2.f);
		}

		final var lNewTrackDefinition = new TrackDefinition(lControlPointsX, lControlPointsY);
		return lNewTrackDefinition;
	}

	protected void buildHiResolutionTrack() {
		final var lHiResSpline = getHiResSpline(mTrack.trackSpline());

		mTrack.hiResTrackSpline(lHiResSpline);
		buildTrackCollisionVertices(lHiResSpline);
	}

	private void buildTrackCollisionVertices(Spline hiResolutionSpline) {
		final var lNumSplinePoints = hiResolutionSpline.points().size();

		Vector2f lTempVector = new Vector2f();
		SplinePoint tempDriveDirection = new SplinePoint();
		SplinePoint tempSideDirection = new SplinePoint();

		mInnerVertices = new Vector2f[lNumSplinePoints + 1];
		mOuterVertices = new Vector2f[lNumSplinePoints + 1];

		final float lScaledSegWidth = mSegmentWidth * mTrackScale;

		for (int i = 0; i < lNumSplinePoints; i++) {
			int nextIndex = i + 1;
			if (nextIndex > lNumSplinePoints - 1)
				nextIndex = 0;

			tempDriveDirection.x = hiResolutionSpline.points().get(nextIndex).x - hiResolutionSpline.points().get(i).x;
			tempDriveDirection.y = hiResolutionSpline.points().get(nextIndex).y - hiResolutionSpline.points().get(i).y;

			tempSideDirection.x = tempDriveDirection.y;
			tempSideDirection.y = -tempDriveDirection.x;

			lTempVector.set(tempSideDirection.x, tempSideDirection.y);
			lTempVector.nor();

			final var lOuterPoint = new Vector2f();
			final var lInnerPoint = new Vector2f();

			lOuterPoint.x = (hiResolutionSpline.points().get(i).x + lTempVector.x * lScaledSegWidth / 2);
			lOuterPoint.y = (hiResolutionSpline.points().get(i).y + lTempVector.y * lScaledSegWidth / 2);

			lInnerPoint.x = (hiResolutionSpline.points().get(i).x - lTempVector.x * lScaledSegWidth / 2);
			lInnerPoint.y = (hiResolutionSpline.points().get(i).y - lTempVector.y * lScaledSegWidth / 2);

			mInnerVertices[i] = lInnerPoint;
			mOuterVertices[i] = lOuterPoint;
		}

		// close loop
		mInnerVertices[lNumSplinePoints] = mInnerVertices[0];
		mOuterVertices[lNumSplinePoints] = mOuterVertices[0];
	}
}
