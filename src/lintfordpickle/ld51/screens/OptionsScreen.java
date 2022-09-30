package lintfordpickle.ld51.screens;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.screenmanager.MenuEntry;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.layouts.ListLayout;
import net.lintford.library.screenmanager.screens.AudioOptionsScreen;

public class OptionsScreen extends MenuScreen {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final String SCREEN_TITLE = "Options";

	private static final int BUTTON_AUDIO = 10;
	private static final int BUTTON_BACK = 11;

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public OptionsScreen(ScreenManager pScreenManager) {
		super(pScreenManager, SCREEN_TITLE);

		final var lLayout = new ListLayout(this);

		//---
		final var lAudioSettingsEntry = new MenuEntry(mScreenManager, lLayout, "Audio");
		lAudioSettingsEntry.registerClickListener(this, BUTTON_AUDIO);

		final var lBackEntry = new MenuEntry(mScreenManager, lLayout, "Back");
		lBackEntry.registerClickListener(this, BUTTON_BACK);

		lLayout.addMenuEntry(lAudioSettingsEntry);
		lLayout.addMenuEntry(lBackEntry);

		mLayouts.add(lLayout);

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

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

	@Override
	protected void handleOnClick() {
		switch (mClickAction.consume()) {
		case BUTTON_AUDIO:
			screenManager().addScreen(new AudioOptionsScreen(mScreenManager));
			break;

		case BUTTON_BACK:
			exitScreen();
			break;
		}
	}

}
