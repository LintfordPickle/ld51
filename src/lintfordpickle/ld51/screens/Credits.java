package lintfordpickle.ld51.screens;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;

public class Credits extends MenuScreen {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final String SCREEN_TITLE = "Credits";

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public Credits(ScreenManager pScreenManager) {
		super(pScreenManager, SCREEN_TITLE);

		mESCBackEnabled = true;
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	protected void handleOnClick() {

	}

	@Override
	public void update(LintfordCore core, boolean otherScreenHasFocus, boolean coveredByOtherScreen) {
		super.update(core, otherScreenHasFocus, coveredByOtherScreen);
	}

	@Override
	public void draw(LintfordCore core) {
		super.draw(core);
	}

}
