package lintfordpickle.ld51.screens;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.screens.BaseGameScreen;

public class GameScreen extends BaseGameScreen {

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public GameScreen(ScreenManager screenManager) {
		this(screenManager, true, 1);
	}

	public GameScreen(ScreenManager screenManager, boolean showHelp, int levelNumber) {
		super(screenManager);
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void handleInput(LintfordCore core) {
		super.handleInput(core);

		if (core.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_ESCAPE)) {
			screenManager().addScreen(new PauseScreen(screenManager()));
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

		GL11.glClearColor(0.3f, 0.06f, 0.07f, 1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		final var lCoreSpritesheet = mCoreSpritesheet;
		final var lSpriteBatch = rendererManager().uiSpriteBatch();

		lSpriteBatch.begin(core.gameCamera());

		lSpriteBatch.end();
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

}
