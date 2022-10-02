package lintfordpickle.ld51.renderers;

import org.lwjgl.opengl.GL11;

import lintfordpickle.ld51.controllers.ShipController;
import lintfordpickle.ld51.data.ships.Ship;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.graphics.linebatch.LineBatch;
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

	protected LineBatch mColliderLineBatch;

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
		mColliderLineBatch = new LineBatch();
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
		mColliderLineBatch.loadResources(resourceManager);
	}

	@Override
	public void unloadResources() {
		super.unloadResources();

		mTextureBatch.unloadResources();
		mColliderLineBatch.unloadResources();
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

		drawDebugTrackCollisionLines(core);
	}

	private void drawDebugTrackCollisionLines(LintfordCore core) {
		final var lineSegmentInner = mShipController.innerWallSegment;
		final var lineSegmentOuter = mShipController.outerWallSegment;

		if (lineSegmentInner != null) {
			mColliderLineBatch.lineType(GL11.GL_LINES);
			mColliderLineBatch.lineWidth(2);
			mColliderLineBatch.begin(core.gameCamera());
			{

				float nx = -(lineSegmentInner.ey - lineSegmentInner.sy);
				float ny = (lineSegmentInner.ex - lineSegmentInner.sx);
				float d = (float) Math.sqrt(nx * nx + ny * ny);
				nx /= d;
				ny /= d;

				final float r = lineSegmentInner.radius;
				mColliderLineBatch.draw(lineSegmentInner.sx + nx * r, lineSegmentInner.sy + ny * r, -0.01f, 1.f, 0.f, 0.f, 1.f);
				mColliderLineBatch.draw(lineSegmentInner.ex + nx * r, lineSegmentInner.ey + ny * r, -0.01f, 1.f, 0.f, 0.f, 1.f);

				mColliderLineBatch.draw(lineSegmentInner.sx - nx * r, lineSegmentInner.sy - ny * r, -0.01f, 1.f, 0.f, 0.f, 1.f);
				mColliderLineBatch.draw(lineSegmentInner.ex - nx * r, lineSegmentInner.ey - ny * r, -0.01f, 1.f, 0.f, 0.f, 1.f);
			}

			{
				float nx = -(lineSegmentOuter.ey - lineSegmentOuter.sy);
				float ny = (lineSegmentOuter.ex - lineSegmentOuter.sx);
				float d = (float) Math.sqrt(nx * nx + ny * ny);
				nx /= d;
				ny /= d;

				final float r = lineSegmentInner.radius;

				mColliderLineBatch.draw(lineSegmentOuter.sx + nx * r, lineSegmentOuter.sy + ny * r, -0.01f, 1.f, 0.f, 0.f, 1.f);
				mColliderLineBatch.draw(lineSegmentOuter.ex + nx * r, lineSegmentOuter.ey + ny * r, -0.01f, 1.f, 0.f, 0.f, 1.f);

				mColliderLineBatch.draw(lineSegmentOuter.sx - nx * r, lineSegmentOuter.sy - ny * r, -0.01f, 1.f, 0.f, 0.f, 1.f);
				mColliderLineBatch.draw(lineSegmentOuter.ex - nx * r, lineSegmentOuter.ey - ny * r, -0.01f, 1.f, 0.f, 0.f, 1.f);
			}
			mColliderLineBatch.end();
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

			mTextureBatch.draw(lTexture, lSourceX, lSourceY, lSourceW, lSourceH, ship.x(), ship.y(), lDestW, lDestH, -0.01f, ship.headingAngle, 0f, 0f, lScale, 1f, 1f, 1f, 1f);
		}

		mTextureBatch.end();

		// DEBUG DRAWERS

		GL11.glPointSize(3.f);

		{
			final var lShipVelocityPosX = ship.x + ship.v.x * 25.f;
			final var lShipVelocityPosY = ship.y + ship.v.y * 25.f;

			Debug.debugManager().drawers().drawLineImmediate(core.gameCamera(), ship.x, ship.y, lShipVelocityPosX, lShipVelocityPosY, -0.01f, 3.0f, 2.4f, 0.8f);
		}

		final var lShipHeading = ship.headingAngle;
		final var lShipHeadingPosX = ship.x + (float) Math.cos(lShipHeading) * 20.f;
		final var lShipHeadingPosY = ship.y + (float) Math.sin(lShipHeading) * 20.f;

		Debug.debugManager().drawers().drawLineImmediate(core.gameCamera(), ship.x, ship.y, lShipHeadingPosX, lShipHeadingPosY, -0.01f, 1.0f, 0.4f, 0.8f);

		final var lShipSteering = ship.headingAngle + ship.steeringAngle;
		final var lShipSteeringPosX = ship.x + (float) Math.cos(lShipSteering) * 15.f;
		final var lShipSteeringPosY = ship.y + (float) Math.sin(lShipSteering) * 15.f;

		Debug.debugManager().drawers().drawLineImmediate(core.gameCamera(), ship.x, ship.y, lShipSteeringPosX, lShipSteeringPosY, -0.01f, 0.4f, 0.9f, 0.8f);

		// Draw ship radius
		Debug.debugManager().drawers().drawCircleImmediate(core.gameCamera(), ship.x, ship.y, ship.radius());

		// Draw position on track
		{
			final var lLoResAngle = ship.loResTrackAngle;
			final var lLoResPointHeadingX = ship.pointOnLoResTrackX + (float) Math.cos(lLoResAngle) * 20.f;
			final var lLoResPointHeadingY = ship.pointOnLoResTrackY + (float) Math.sin(lLoResAngle) * 20.f;

			Debug.debugManager().drawers().drawLineImmediate(core.gameCamera(), ship.pointOnLoResTrackX, ship.pointOnLoResTrackY, lLoResPointHeadingX, lLoResPointHeadingY, -0.01f, 0.9f, 0.9f, 0.2f);
		}

		drawShipDebugInfo(core, ship);
	}

	private void drawShipDebugInfo(LintfordCore core, Ship ship) {
		if (ship.isPlayerControlled) {
			final var lFontUnit = rendererManager().uiTextFont();
			final var lBoundingBox = core.HUD().boundingRectangle();

			float yPos = lBoundingBox.top() + 5.f;
			final float lDist = (ship.shipProgress.distanceIntoRace / 10.f);
			final var lDistanceTravelledInM = String.format(java.util.Locale.US, "%.2f", lDist);

			lFontUnit.begin(core.HUD());
			lFontUnit.drawText("current node: " + ship.shipProgress.currentNodeUid, lBoundingBox.left() + 5.f, yPos += 25f, -0.01f, 1.f);
			lFontUnit.drawText("distance: " + lDistanceTravelledInM + "m", lBoundingBox.left() + 5.f, yPos += 25f, -0.01f, 1.f);
			lFontUnit.drawText("current lap: " + ship.shipProgress.currentLapNumber, lBoundingBox.left() + 5.f, yPos += 25f, -0.01f, 1.f);
			lFontUnit.end();
		}
	}
}
