package lintfordpickle.ld51.screens;

import org.lwjgl.glfw.GLFW;

import lintfordpickle.ld51.controllers.CameraMovementController;
import lintfordpickle.ld51.controllers.TrackController;
import lintfordpickle.ld51.data.tracks.GameFileHeader;
import lintfordpickle.ld51.renderers.TrackRenderer;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.graphics.ColorConstants;
import net.lintford.library.core.graphics.textures.CoreTextureNames;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.screens.BaseGameScreen;

public class GameScreen extends BaseGameScreen {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	// Data
	private GameFileHeader mGameFileHeader;

	// Controllers
	private TrackController mTrackController;
	private CameraMovementController mCameraMovementController;

	// Renderers
	private TrackRenderer mTrackRenderer;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public GameFileHeader gameFileHeader() {
		return mGameFileHeader;
	}

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public GameScreen(ScreenManager screenManager, GameFileHeader gamefileHeader, boolean showHelp) {
		super(screenManager);

		mGameFileHeader = gamefileHeader;
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
	public void loadResources(ResourceManager resourceManager) {
		super.loadResources(resourceManager);

		createRenderers(screenManager().core());

	}

	@Override
	public void handleInput(LintfordCore core) {
		super.handleInput(core);

		if (core.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_ESCAPE)) {
			screenManager().addScreen(new PauseScreen(screenManager(), mGameFileHeader));
			return;
		}
	}

	@Override
	public void update(LintfordCore core, boolean otherScreenHasFocus, boolean coveredByOtherScreen) {
		super.update(core, otherScreenHasFocus, coveredByOtherScreen);
	}

	@Override
	public void draw(LintfordCore core) {
		super.draw(core);

		drawDebugControlPoints(core);
	}

	private void drawDebugControlPoints(LintfordCore core) {
		if (mTrackController == null)
			return;

		if (mTrackController.currentTrack() == null)
			return;

		final var lSpriteBatch = rendererManager().uiSpriteBatch();
		final var lPixelSize = 1.f;

		lSpriteBatch.begin(core.gameCamera());

		final var lTrack = mTrackController.currentTrack();
		if (lTrack != null) {
			final var lSplinePoints = lTrack.trackSpline().points();
			final int lNumPoints = lSplinePoints.size();
			for (int i = 0; i < lNumPoints; i++) {
				final var lPoint = lSplinePoints.get(i);

				lSpriteBatch.draw(mCoreSpritesheet, CoreTextureNames.TEXTURE_WHITE, lPoint.x, lPoint.y, lPixelSize, lPixelSize, -0.01f, ColorConstants.WHITE);
			}
		}

		lSpriteBatch.end();
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void createControllers(ControllerManager controllerManager) {
		final var lIsEditorMode = true;
		mCameraMovementController = new CameraMovementController(controllerManager, mGameCamera, lIsEditorMode, entityGroupUid());
		mTrackController = new TrackController(controllerManager, mGameFileHeader, entityGroupUid());
	}

	public void initializeControllers(LintfordCore core) {
		mCameraMovementController.initialize(core);
		mTrackController.initialize(core);
	}

	public void createRenderers(LintfordCore core) {
		mTrackRenderer = new TrackRenderer(mRendererManager, entityGroupUid());
		mTrackRenderer.initialize(core);
	}
}
