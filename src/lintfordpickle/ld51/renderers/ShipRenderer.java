package lintfordpickle.ld51.renderers;

import org.lwjgl.opengl.GL11;

import lintfordpickle.ld51.controllers.ShipController;
import lintfordpickle.ld51.data.ships.Ship;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.core.graphics.textures.texturebatch.SubPixelTextureBatch;
import net.lintford.library.renderers.BaseRenderer;
import net.lintford.library.renderers.RendererManager;

public class ShipRenderer extends BaseRenderer {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String RENDERER_NAME = "Ship Renderer";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	protected ShipController mShipController;
	private SubPixelTextureBatch mTextureBatch;

	protected Texture mShipTexturePlayer;
	protected Texture mShipTextureEnemy;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	@Override
	public boolean isInitialized() {
		return mShipController != null;

	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public ShipRenderer(RendererManager rendererManager, int entityGroupID) {
		super(rendererManager, RENDERER_NAME, entityGroupID);

		mTextureBatch = new SubPixelTextureBatch();

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore core) {
		mShipController = (ShipController) core.controllerManager().getControllerByNameRequired(ShipController.CONTROLLER_NAME, entityGroupID());

	}

	@Override
	public void loadResources(ResourceManager resourceManager) {
		super.loadResources(resourceManager);

		mShipTexturePlayer = resourceManager.textureManager().loadTexture("TEXTURE_VEHICLE_01", "res/textures/textureShip.png", entityGroupID());
		mShipTextureEnemy = resourceManager.textureManager().loadTexture("TEXTURE_VEHICLE_02", "res/textures/textureShip.png", entityGroupID());

		mTextureBatch.loadResources(resourceManager);
	}

	@Override
	public void unloadResources() {
		super.unloadResources();

		mTextureBatch.unloadResources();
	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

	}

	@Override
	public void draw(LintfordCore core) {
		final var lPlayerShip = mShipController.shipManager().playerShip();
		drawShip(core, lPlayerShip);

		final var lListOfOpponents = mShipController.shipManager().ships();
		final int lNumOfOpponents = lListOfOpponents.size();

		for (int i = 0; i < lNumOfOpponents; i++) {
			final var lOpponentShip = lListOfOpponents.get(i);
			drawShip(core, lOpponentShip);
		}
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void drawShip(LintfordCore core, Ship ship) {
		if (ship == null)
			return;

		final float lScale = 1.f;

		mTextureBatch.begin(core.gameCamera());

		var lTexture = mShipTextureEnemy;
		if (ship.isPlayerControlled)
			lTexture = mShipTexturePlayer;

		{// MainBody
			final float lSourceX = 0.f;
			final float lSourceY = 0.f;
			final float lSourceW = 27.f;
			final float lSourceH = 18.f;

			final float lDestW = lSourceW;
			final float lDestH = lSourceH;

			mTextureBatch.draw(lTexture, lSourceX, lSourceY, lSourceW, lSourceH, ship.worldPositionX(), ship.worldPositionY(), lDestW, lDestH, -0.01f, ship.heading, 0f, 0f, lScale, 1f, 1f, 1f, 1f);
		}

		mTextureBatch.end();

		// DEBUG DRAWERS

		GL11.glPointSize(3.f);
		Debug.debugManager().drawers().drawPointImmediate(core.gameCamera(), ship.rearWheelPosition.x, ship.rearWheelPosition.y);
		Debug.debugManager().drawers().drawPointImmediate(core.gameCamera(), ship.frontWheelPosition.x, ship.frontWheelPosition.y);

		// rear wheels 
		final var lRearWheelPosition = ship.rearWheelPosition;

		final var lShipHeading = ship.heading;
		final var lShipHeadingPosX = lRearWheelPosition.x + (float) Math.cos(lShipHeading) * 10.f;
		final var lShipHeadingPosY = lRearWheelPosition.y + (float) Math.sin(lShipHeading) * 10.f;

		final var lShipRearHeading = ship.heading + ship.steerFrontAngle;
		final var lShipRearHeadingPosX = lRearWheelPosition.x + (float) Math.cos(lShipRearHeading) * 10.f;
		final var lShipRearHeadingPosY = lRearWheelPosition.y + (float) Math.sin(lShipRearHeading) * 10.f;

		Debug.debugManager().drawers().drawLineImmediate(core.gameCamera(), lRearWheelPosition.x, lRearWheelPosition.y, lShipHeadingPosX, lShipHeadingPosY);
		Debug.debugManager().drawers().drawLineImmediate(core.gameCamera(), lRearWheelPosition.x, lRearWheelPosition.y, lShipRearHeadingPosX, lShipRearHeadingPosY);

		// front wheels
		final var lFrontWheelPosition = ship.frontWheelPosition;

		final var lShipFrontHeading = ship.heading + ship.steerRearAngle;
		final var lShipFrontHeadingPosX = lFrontWheelPosition.x + (float) Math.cos(lShipFrontHeading) * 10.f;
		final var lShipFrontHeadingPosY = lFrontWheelPosition.y + (float) Math.sin(lShipFrontHeading) * 10.f;

		Debug.debugManager().drawers().drawLineImmediate(core.gameCamera(), lFrontWheelPosition.x, lFrontWheelPosition.y, lShipFrontHeadingPosX, lShipFrontHeadingPosY);

	}

}
