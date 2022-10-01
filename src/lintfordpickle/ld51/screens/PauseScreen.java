package lintfordpickle.ld51.screens;

import lintfordpickle.ld51.data.tracks.GameFileHeader;
import net.lintford.library.screenmanager.MenuEntry;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.layouts.ListLayout;
import net.lintford.library.screenmanager.screens.LoadingScreen;

public class PauseScreen extends MenuScreen {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final String TITLE = "Paused";

	private static final int SCREEN_BUTTON_CONTINUE = 10;
	private static final int SCREEN_BUTTON_RESTART = 11;
	private static final int SCREEN_BUTTON_EXIT = 12;

	private GameFileHeader mGameFileHeader;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public PauseScreen(ScreenManager screenManager, GameFileHeader gameFileHeader) {
		super(screenManager, TITLE);

		mGameFileHeader = gameFileHeader;

		final var lLayout = new ListLayout(this);

		//---
		final var lPlayEntry = new MenuEntry(mScreenManager, lLayout, "Continue");
		lPlayEntry.registerClickListener(this, SCREEN_BUTTON_CONTINUE);

		final var lOptionsEntry = new MenuEntry(mScreenManager, lLayout, "Restart");
		lOptionsEntry.registerClickListener(this, SCREEN_BUTTON_RESTART);

		final var lCreditsEntry = new MenuEntry(mScreenManager, lLayout, "Exit");
		lCreditsEntry.registerClickListener(this, SCREEN_BUTTON_EXIT);

		lLayout.addMenuEntry(lPlayEntry);
		lLayout.addMenuEntry(lOptionsEntry);
		lLayout.addMenuEntry(lCreditsEntry);

		mLayouts.add(lLayout);

		mIsPopup = true;
		mShowBackgroundScreens = true;
	}

	@Override
	protected void handleOnClick() {
		switch (mClickAction.consume()) {
		case SCREEN_BUTTON_CONTINUE:
			exitScreen();
			return;

		case SCREEN_BUTTON_RESTART:
			screenManager().createLoadingScreen(new LoadingScreen(screenManager(), true, new GameScreen(screenManager(), mGameFileHeader, false)));
			break;

		case SCREEN_BUTTON_EXIT:
			screenManager().createLoadingScreen(new LoadingScreen(screenManager(), false, new MenuBackgroundScreen(mScreenManager), new MainMenu(screenManager())));
			break;

		}
	}
}
