package lintfordpickle.ld51.screens;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import lintfordpickle.ld51.controllers.CameraShipChaseController;
import lintfordpickle.ld51.controllers.ShipController;
import lintfordpickle.ld51.controllers.TrackController;
import lintfordpickle.ld51.data.ships.ShipManager;
import lintfordpickle.ld51.data.tracks.GameFileHeader;
import lintfordpickle.ld51.renderers.ShipRenderer;
import lintfordpickle.ld51.renderers.TrackRenderer;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.graphics.rendertarget.RenderTarget;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.screens.BaseGameScreen;

public class GameScreen extends BaseGameScreen {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	// Data
	private GameFileHeader mGameFileHeader;
	private ShipManager mShipManager;

	// Controllers
	private ShipController mShipController;
	private TrackController mTrackController;
	private CameraShipChaseController mCameraChaseController;

	// Renderers
	private ShipRenderer mShipRenderer;
	private TrackRenderer mTrackRenderer;

	private RenderTarget mGameCanvasRT;

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

		mShipManager = new ShipManager();
		mShipManager.createPlayerShip();

		mShipManager.createAiShips(ShipController.DEFAULT_NUMBER_OPPONENTS);

		final var lCore = screenManager().core();
		final var lControllerManager = lCore.controllerManager();

		createControllers(lControllerManager);
		initializeControllers(lCore);
	}

	@Override
	public void loadResources(ResourceManager resourceManager) {
		super.loadResources(resourceManager);

		mGameCanvasRT = mRendererManager.createRenderTarget("Game Canvas", 150, 100, 1f, GL11.GL_NEAREST, true);

		createRenderers(screenManager().core());

	}

	@Override
	public void unloadResources() {
		super.unloadResources();

		mRendererManager.unloadRenderTarget(mGameCanvasRT);
	}

	@Override
	public void handleInput(LintfordCore core) {
		super.handleInput(core);

		if (core.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_O)) {

		}

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
		GL11.glClearColor(0.06f, 0.18f, 0.11f, 1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		super.draw(core);
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void createControllers(ControllerManager controllerManager) {
		final var lShip = mShipManager.playerShip();

		mCameraChaseController = new CameraShipChaseController(controllerManager, mGameCamera, lShip, entityGroupUid());
		mTrackController = new TrackController(controllerManager, mGameFileHeader, entityGroupUid());
		mShipController = new ShipController(controllerManager, mShipManager, entityGroupUid());
	}

	public void initializeControllers(LintfordCore core) {
		mCameraChaseController.initialize(core);
		mTrackController.initialize(core);
		mShipController.initialize(core);
	}

	public void createRenderers(LintfordCore core) {
		mTrackRenderer = new TrackRenderer(mRendererManager, entityGroupUid());
		mTrackRenderer.initialize(core);

		mShipRenderer = new ShipRenderer(mRendererManager, entityGroupUid());
		mShipRenderer.initialize(core);
	}
}
