package lintfordpickle.ld51.controllers;

import org.lwjgl.glfw.GLFW;

import lintfordpickle.ld51.data.tracks.GameFileHeader;
import lintfordpickle.ld51.data.tracks.Track;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.maths.Vector2f;

public class EditorTrackController extends TrackController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------{

	private int mSelectedControlIndex;
	private float mMouseX;
	private float mMouseY;

	private boolean mTrackChangedInEditor;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public boolean trackChangedInEditor() {
		return mTrackChangedInEditor;
	}

	public void trackChangedInEditor(boolean newValue) {
		mTrackChangedInEditor = newValue;
	}

	public Vector2f[] innerTrackVertices() {
		return mInnerVertices;
	}

	public Vector2f[] outerTrackVertices() {
		return mOuterVertices;
	}

	public int selectedNodeIndex() {
		return mSelectedControlIndex;
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

	public EditorTrackController(ControllerManager controllerManager, GameFileHeader gameFileHeader, int entityGroupID) {
		super(controllerManager, gameFileHeader, entityGroupID);

		if (mTrack.isTrackLoaded() == false) {
			final var lNewTrackDefinition = createDefaultTrackDefinition();
			mTrack.loadTrackDefinitionFromDefinition(lNewTrackDefinition);
		}

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore core) {
		super.initialize(core);
	}

	@Override
	public void unload() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean handleInput(LintfordCore core) {

		// ----

		if (core.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_O)) {
			mSelectedControlIndex++;
			if (mSelectedControlIndex > mTrack.trackSpline().numberSplineControlPoints() - 1)
				mSelectedControlIndex = 0;
		}

		if (core.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_P)) {
			mSelectedControlIndex--;
			if (mSelectedControlIndex < 0)
				mSelectedControlIndex = currentTrack().trackSpline().numberSplineControlPoints() - 1;
		}

		final var lCtrlModKeyDown = core.input().keyboard().isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL) || core.input().keyboard().isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL);
		// final var lAltModKeyDown = core.input().keyboard().isKeyDown(GLFW.GLFW_KEY_LEFT_ALT) || core.input().keyboard().isKeyDown(GLFW.GLFW_KEY_RIGHT_ALT);

		if (core.input().mouse().isMouseLeftButtonDown()) {
			mMouseX = core.gameCamera().getMouseWorldSpaceX();
			mMouseY = core.gameCamera().getMouseWorldSpaceY();

			if (lCtrlModKeyDown) {
				// Move selected control point
				final var lControlPoint = currentTrack().trackSpline().points().get(mSelectedControlIndex);
				lControlPoint.x = mMouseX;
				lControlPoint.y = mMouseY;

				currentTrack().onTrackChanged();
				buildHiResolutionTrack();
				mTrackChangedInEditor = true;

			} else if (lCtrlModKeyDown) {

			} else { // Control point selection
				final var lSplinePoints = currentTrack().trackSpline().points();
				final int lNumPoints = lSplinePoints.size();
				for (int i = 0; i < lNumPoints; i++) {
					final var lPoint = lSplinePoints.get(i);

					var dist = Vector2f.distance(mMouseX, mMouseY, lPoint.x, lPoint.y);
					if (dist < 4.0f) {
						mSelectedControlIndex = i;
						break;
					}
				}
			}
		}

		return super.handleInput(core);
	}

	@Override
	public void update(LintfordCore core) {
		super.update(core);

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

}
