package lintfordpickle.ld51.screens;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import lintfordpickle.ld51.controllers.CameraMovementController;
import lintfordpickle.ld51.data.tracks.GameFileHeader;
import lintfordpickle.ld51.data.tracks.Track;
import lintfordpickle.ld51.data.tracks.TrackDefinition;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.graphics.ColorConstants;
import net.lintford.library.core.graphics.textures.CoreTextureNames;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.screens.BaseGameScreen;

public class EditorScreen extends BaseGameScreen {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	// Data
	private GameFileHeader mGameFileHeader; // [CannotBeNull]
	private Track mCurrentTrack; // [CannotBeNull]
	private int mSelectedControlIndex;

	private float mMouseX;
	private float mMouseY;

	// Controllers
	private CameraMovementController mCameraMovementController;

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public EditorScreen(ScreenManager screenManager) {
		super(screenManager);

		mGameFileHeader = new GameFileHeader();

		final var lDefautTrackDefinition = createDefaultTrackDefinition();
		mCurrentTrack = new Track();
		mCurrentTrack.loadTrackDefinitionFromDefinition(lDefautTrackDefinition);
	}

	public EditorScreen(ScreenManager screenManager, GameFileHeader gamefileHeader) {
		super(screenManager);

		mGameFileHeader = gamefileHeader;
		mCurrentTrack = new Track();
		mCurrentTrack.loadTrackDefinitionFromFile(mGameFileHeader.trackFilename());
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize() {
		super.initialize();

		final var lCore = screenManager().core();
		final var lControllerManager = lCore.controllerManager();
		createControllers(lControllerManager);
		initializeControllers(lCore);
	}

	@Override
	public void handleInput(LintfordCore core) {
		super.handleInput(core);

		if (core.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_ESCAPE)) {
			screenManager().addScreen(new EditorPauseScreen(screenManager(), mGameFileHeader, mCurrentTrack));
			return;
		}

		// ----

		if (core.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_O)) {
			mSelectedControlIndex++;
			if (mSelectedControlIndex > mCurrentTrack.trackSpline().numberSplineControlPoints() - 1)
				mSelectedControlIndex = 0;
		}

		if (core.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_P)) {
			mSelectedControlIndex--;
			if (mSelectedControlIndex < 0)
				mSelectedControlIndex = mCurrentTrack.trackSpline().numberSplineControlPoints() - 1;
		}

		final var lCtrlModKeyDown = core.input().keyboard().isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL) || core.input().keyboard().isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL);
		// final var lAltModKeyDown = core.input().keyboard().isKeyDown(GLFW.GLFW_KEY_LEFT_ALT) || core.input().keyboard().isKeyDown(GLFW.GLFW_KEY_RIGHT_ALT);

		if (core.input().mouse().isMouseLeftButtonDown()) {
			mMouseX = core.gameCamera().getMouseWorldSpaceX();
			mMouseY = core.gameCamera().getMouseWorldSpaceY();

			if (lCtrlModKeyDown) {
				// Move selected control point
				final var lControlPoint = mCurrentTrack.trackSpline().points().get(mSelectedControlIndex);
				lControlPoint.x = mMouseX;
				lControlPoint.y = mMouseY;

				mCurrentTrack.onTrackChanged();

			} else if (lCtrlModKeyDown) {

			} else { // Control point selection
				final var lSplinePoints = mCurrentTrack.trackSpline().points();
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
	}

	@Override
	public void update(LintfordCore core, boolean otherScreenHasFocus, boolean coveredByOtherScreen) {
		super.update(core, otherScreenHasFocus, coveredByOtherScreen);
	}

	@Override
	public void draw(LintfordCore core) {
		super.draw(core);

		GL11.glClearColor(0.3f, 0.06f, 0.07f, 1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		// Draw origin
		drawDebugPixel(core);

		drawDebugControlPoints(core);

		drawDebugInfo(core);

		final var lLineBatch = rendererManager().uiLineBatch();

		lLineBatch.lineType(GL11.GL_LINE_STRIP);
		lLineBatch.begin(mGameCamera);
		final var lTrackSpline = mCurrentTrack.trackSpline();
		final var lNumTrackPoints = lTrackSpline.numberSplineControlPoints();
		final var lStepSize = 0.05f;
		for (float t = 0; t < lNumTrackPoints; t += lStepSize) {
			final var lPoint = lTrackSpline.getPointOnSpline(t);
			lLineBatch.draw(lPoint.x, lPoint.y, -0.01f, .6f, .6f, .6f, 1.f);
		}

		lLineBatch.end();
	}

	private void drawDebugPixel(LintfordCore core) {
		final var lSpriteBatch = rendererManager().uiSpriteBatch();
		final var lPixelSize = 1.f;

		lSpriteBatch.begin(core.gameCamera());
		lSpriteBatch.draw(mCoreSpritesheet, CoreTextureNames.TEXTURE_BLUE, 0, 0, lPixelSize, lPixelSize, -0.01f, ColorConstants.WHITE);
		lSpriteBatch.end();
	}

	private void drawDebugControlPoints(LintfordCore core) {
		final var lFontUnit = rendererManager().uiTextFont();
		final var lSpriteBatch = rendererManager().uiSpriteBatch();
		final var lPixelSize = 1.f;

		lFontUnit.begin(core.gameCamera());
		lSpriteBatch.begin(core.gameCamera());

		if (mCurrentTrack != null) {
			final var lSplinePoints = mCurrentTrack.trackSpline().points();
			final int lNumPoints = lSplinePoints.size();
			for (int i = 0; i < lNumPoints; i++) {
				final var lPoint = lSplinePoints.get(i);

				final var lIsControlPointSelected = i == mSelectedControlIndex;
				final var lControlPointColor = lIsControlPointSelected ? ColorConstants.RED : ColorConstants.WHITE;
				lSpriteBatch.draw(mCoreSpritesheet, CoreTextureNames.TEXTURE_WHITE, lPoint.x, lPoint.y, lPixelSize, lPixelSize, -0.01f, lControlPointColor);
				lFontUnit.drawText(String.valueOf(i), lPoint.x + 1, lPoint.y + 1, -0.01f, 0.15f);
			}
		}

		lSpriteBatch.end();
		lFontUnit.end();
	}

	private void drawDebugInfo(LintfordCore core) {
		final var lHudBoundingBox = core.HUD().boundingRectangle();

		final var lFontUnit = rendererManager().uiTextFont();
		float lPositionX = lHudBoundingBox.left() + 5.f;
		float lPositionY = lHudBoundingBox.top() + 5.f;
		lFontUnit.begin(core.HUD());
		lFontUnit.drawText("Number Controls: " + mCurrentTrack.trackSpline().numberSplineControlPoints(), lPositionX, lPositionY += lFontUnit.fontHeight() + 2, -0.01f, 1.f);
		lFontUnit.drawText("Length: " + mCurrentTrack.getTrackDistance(), lPositionX, lPositionY += lFontUnit.fontHeight() + 2, -0.01f, 1.f);
		lFontUnit.drawText("Selected Index: " + mSelectedControlIndex, lPositionX, lPositionY += lFontUnit.fontHeight() + 2, -0.01f, 1.f);
		lFontUnit.drawText("Track UpToDate: " + mCurrentTrack.isDirty(), lPositionX, lPositionY += lFontUnit.fontHeight() + 2, -0.01f, 1.f);
		lFontUnit.end();
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

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

	public void createControllers(ControllerManager controllerManager) {
		final var lIsEditorMode = true;
		mCameraMovementController = new CameraMovementController(controllerManager, mGameCamera, lIsEditorMode, entityGroupUid());
	}

	public void initializeControllers(LintfordCore core) {
		mCameraMovementController.initialize(core);
	}
}
