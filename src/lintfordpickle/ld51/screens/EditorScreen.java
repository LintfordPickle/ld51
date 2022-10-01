package lintfordpickle.ld51.screens;

import org.lwjgl.glfw.GLFW;

import lintfordpickle.ld51.controllers.CameraMovementController;
import lintfordpickle.ld51.controllers.EditorTrackController;
import lintfordpickle.ld51.data.tracks.GameFileHeader;
import lintfordpickle.ld51.renderers.EditorTrackRenderer;
import net.lintford.library.controllers.camera.CameraZoomController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.screens.BaseGameScreen;

public class EditorScreen extends BaseGameScreen {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	// Data
	private GameFileHeader mGameFileHeader; // [CannotBeNull]

	// Controllers
	private EditorTrackController mTrackController;
	private CameraZoomController mCameraZoomController;
	private CameraMovementController mCameraMovementController;

	// Renderers
	private EditorTrackRenderer mTrackRenderer;

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public EditorScreen(ScreenManager screenManager) {
		super(screenManager);

		mGameFileHeader = new GameFileHeader();
	}

	public EditorScreen(ScreenManager screenManager, GameFileHeader gamefileHeader) {
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

		createRenderers(lCore);
	}

	@Override
	public void handleInput(LintfordCore core) {
		super.handleInput(core);

		if (core.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_ESCAPE)) {
			screenManager().addScreen(new EditorPauseScreen(screenManager(), mGameFileHeader, mTrackController.currentTrack()));
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

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void createControllers(ControllerManager controllerManager) {
		final var lIsEditorMode = true;
		mCameraMovementController = new CameraMovementController(controllerManager, mGameCamera, lIsEditorMode, entityGroupUid());
		mCameraZoomController = new CameraZoomController(controllerManager, mGameCamera, entityGroupUid());

		mTrackController = new EditorTrackController(controllerManager, mGameFileHeader, entityGroupUid());

	}

	public void initializeControllers(LintfordCore core) {
		mCameraMovementController.initialize(core);
		mCameraZoomController.initialize(core);
		mTrackController.initialize(core);
	}

	private void createRenderers(LintfordCore core) {
		mTrackRenderer = new EditorTrackRenderer(mRendererManager, entityGroupUid());
		mTrackRenderer.initialize(core);
	}
}
