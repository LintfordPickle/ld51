package lintfordpickle.ld51.screens;

import net.lintford.library.screenmanager.MenuEntry;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.layouts.ListLayout;

public class MainMenu extends MenuScreen {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final String TITLE = "Main Menu";

	private static final int SCREEN_BUTTON_PLAY = 10;
	private static final int SCREEN_BUTTON_OPTIONS = 11;
	private static final int SCREEN_BUTTON_EDITOR = 13;
	private static final int SCREEN_BUTTON_CREDITS = 14;
	private static final int SCREEN_BUTTON_EXIT = 15;

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public MainMenu(ScreenManager pScreenManager) {
		super(pScreenManager, TITLE);

		final var lLayout = new ListLayout(this);

		//---
		final var lPlayEntry = new MenuEntry(mScreenManager, lLayout, "Play");
		lPlayEntry.registerClickListener(this, SCREEN_BUTTON_PLAY);

		final var lEditorEntry = new MenuEntry(mScreenManager, lLayout, "Editor");
		lEditorEntry.registerClickListener(this, SCREEN_BUTTON_EDITOR);

		final var lOptionsEntry = new MenuEntry(mScreenManager, lLayout, "Options");
		lOptionsEntry.registerClickListener(this, SCREEN_BUTTON_OPTIONS);

		final var lCreditsEntry = new MenuEntry(mScreenManager, lLayout, "Credits");
		lCreditsEntry.registerClickListener(this, SCREEN_BUTTON_CREDITS);

		final var lExitEntry = new MenuEntry(mScreenManager, lLayout, "Exit");
		lExitEntry.registerClickListener(this, SCREEN_BUTTON_EXIT);

		lLayout.addMenuEntry(lPlayEntry);
		lLayout.addMenuEntry(lEditorEntry);
		lLayout.addMenuEntry(MenuEntry.menuSeparator());
		lLayout.addMenuEntry(lOptionsEntry);
		lLayout.addMenuEntry(lCreditsEntry);
		lLayout.addMenuEntry(MenuEntry.menuSeparator());
		lLayout.addMenuEntry(lExitEntry);

		mLayouts.add(lLayout);

		mIsPopup = false;
		mShowBackgroundScreens = true;
		mESCBackEnabled = false;
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	protected void handleOnClick() {
		switch (mClickAction.consume()) {
		case SCREEN_BUTTON_PLAY:
			mScreenManager.createLoadingScreen(new GameScreen(mScreenManager));
			break;

		case SCREEN_BUTTON_EDITOR:
			screenManager().addScreen(new EditorTrackSelectionScreen(mScreenManager));
			break;

		case SCREEN_BUTTON_OPTIONS:
			screenManager().addScreen(new OptionsScreen(mScreenManager));
			break;

		case SCREEN_BUTTON_CREDITS:
			screenManager().addScreen(new Credits(mScreenManager));
			break;

		case SCREEN_BUTTON_EXIT:
			screenManager().exitGame();
			break;
		}
	}
}
