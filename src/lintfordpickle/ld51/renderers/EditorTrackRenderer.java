package lintfordpickle.ld51.renderers;

import org.lwjgl.opengl.GL11;

import lintfordpickle.ld51.controllers.EditorTrackController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.graphics.ColorConstants;
import net.lintford.library.core.graphics.sprites.spritesheet.SpriteSheetDefinition;
import net.lintford.library.core.graphics.textures.CoreTextureNames;
import net.lintford.library.renderers.RendererManager;

public class EditorTrackRenderer extends TrackRenderer {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private SpriteSheetDefinition mCoreSpritesheet;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public EditorTrackController trackController() {
		return (EditorTrackController) mTrackController;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public EditorTrackRenderer(RendererManager rendererManager, int entityGroupID) {
		super(rendererManager, entityGroupID);
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public boolean isInitialized() {
		return false;
	}

	@Override
	public void initialize(LintfordCore pCore) {
		mTrackController = (EditorTrackController) pCore.controllerManager().getControllerByNameRequired(EditorTrackController.CONTROLLER_NAME, entityGroupID());

	}

	@Override
	public void loadResources(ResourceManager resourceManager) {
		super.loadResources(resourceManager);

		mCoreSpritesheet = resourceManager.spriteSheetManager().coreSpritesheet();
	}

	@Override
	public void unloadResources() {
		super.unloadResources();

	}

	@Override
	public void update(LintfordCore core) {
		super.update(core);

	}

	@Override
	public void draw(LintfordCore core) {
		if (trackController().trackChangedInEditor()) {
			loadTrackMesh(trackController().currentTrack());
			trackController().trackChangedInEditor(false);
		}

		super.draw(core);

		drawTrackBasic(core);

		drawDebugPixel(core);

		drawDebugControlPoints(core);

		drawDebugInfo(core);
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void drawTrackBasic(LintfordCore core) {
		final var lLineBatch = rendererManager().uiLineBatch();
		final var lCurrentTrack = mTrackController.currentTrack();

		lLineBatch.lineType(GL11.GL_LINE_STRIP);
		lLineBatch.begin(core.gameCamera());
		final var lTrackSpline = lCurrentTrack.trackSpline();
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

		final var lTrack = mTrackController.currentTrack();
		if (lTrack != null) {
			final var lSplinePoints = lTrack.trackSpline().points();
			final int lNumPoints = lSplinePoints.size();
			for (int i = 0; i < lNumPoints; i++) {
				final var lPoint = lSplinePoints.get(i);

				final var lIsControlPointSelected = i == ((EditorTrackController) mTrackController).selectedNodeIndex();
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

		final var lSelectedNodeIndex = ((EditorTrackController) mTrackController).selectedNodeIndex();
		final var lTrack = mTrackController.currentTrack();

		final var lFontUnit = rendererManager().uiTextFont();
		float lPositionX = lHudBoundingBox.left() + 5.f;
		float lPositionY = lHudBoundingBox.top() + 5.f;
		lFontUnit.begin(core.HUD());
		lFontUnit.drawText("Number Controls: " + lTrack.trackSpline().numberSplineControlPoints(), lPositionX, lPositionY += lFontUnit.fontHeight() + 2, -0.01f, 1.f);
		lFontUnit.drawText("Length: " + lTrack.getTrackDistance(), lPositionX, lPositionY += lFontUnit.fontHeight() + 2, -0.01f, 1.f);
		lFontUnit.drawText("Selected Index: " + lSelectedNodeIndex, lPositionX, lPositionY += lFontUnit.fontHeight() + 2, -0.01f, 1.f);
		lFontUnit.drawText("Track UpToDate: " + lTrack.isDirty(), lPositionX, lPositionY += lFontUnit.fontHeight() + 2, -0.01f, 1.f);
		lFontUnit.end();
	}

}
