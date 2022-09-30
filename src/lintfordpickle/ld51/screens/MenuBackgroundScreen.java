package lintfordpickle.ld51.screens;

import org.lwjgl.opengl.GL11;

import lintfordpickle.ld51.ConstantsGame;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.screenmanager.Screen;
import net.lintford.library.screenmanager.ScreenManager;

public class MenuBackgroundScreen extends Screen {

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public MenuBackgroundScreen(ScreenManager screenManager) {
		super(screenManager);

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void draw(LintfordCore core) {
		super.draw(core);

		GL11.glClearColor(0.06f, 0.18f, 0.11f, 1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		final var lHudBoundingBox = core.HUD().boundingRectangle();

		final var lFontUnit = rendererManager().uiTextBoldFont();
		lFontUnit.begin(core.HUD());
		lFontUnit.drawText(ConstantsGame.FOOTER_TEXT, lHudBoundingBox.left() + 5.f, lHudBoundingBox.bottom() - 5.f - lFontUnit.fontHeight(), -0.01f, 1.f);
		lFontUnit.end();
	}
}
