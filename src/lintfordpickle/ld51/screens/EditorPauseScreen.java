package lintfordpickle.ld51.screens;

import lintfordpickle.ld51.ConstantsGame;
import lintfordpickle.ld51.data.tracks.GameFileHeader;
import lintfordpickle.ld51.data.tracks.Track;
import net.lintford.library.screenmanager.MenuEntry;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.entries.MenuInputEntry;
import net.lintford.library.screenmanager.layouts.ListLayout;
import net.lintford.library.screenmanager.screens.LoadingScreen;

public class EditorPauseScreen extends MenuScreen {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final String TITLE = "Paused";

	private static final int SCREEN_BUTTON_CONTINUE = 10;
	private static final int SCREEN_BUTTON_SAVE = 11;
	private static final int SCREEN_BUTTON_EXIT = 12;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private Track mTrack;
	private GameFileHeader mGameFileHeader;
	private MenuInputEntry mFilenameEntry;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public EditorPauseScreen(ScreenManager screenManager, GameFileHeader gamefileHeader, Track currentTrack) {
		super(screenManager, TITLE);

		mTrack = currentTrack;
		mGameFileHeader = gamefileHeader;

		final var lLayout = new ListLayout(this);

		//---
		final var lContinue = new MenuEntry(mScreenManager, lLayout, "Continue");
		lContinue.registerClickListener(this, SCREEN_BUTTON_CONTINUE);

		mFilenameEntry = new MenuInputEntry(mScreenManager, lLayout);
		mFilenameEntry.label("Filename");
		mFilenameEntry.scaleTextToWidth(false);

		final var lSaveEntry = new MenuEntry(mScreenManager, lLayout, "Save");
		lSaveEntry.registerClickListener(this, SCREEN_BUTTON_SAVE);

		final var lMainMenuEntry = new MenuEntry(mScreenManager, lLayout, "Main Menu");
		lMainMenuEntry.registerClickListener(this, SCREEN_BUTTON_EXIT);

		lLayout.addMenuEntry(lContinue);
		lLayout.addMenuEntry(MenuEntry.menuSeparator());
		lLayout.addMenuEntry(mFilenameEntry);
		lLayout.addMenuEntry(lSaveEntry);
		lLayout.addMenuEntry(MenuEntry.menuSeparator());
		lLayout.addMenuEntry(lMainMenuEntry);

		mLayouts.add(lLayout);

		mIsPopup = true;
		mShowBackgroundScreens = true;
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	@Override
	protected void handleOnClick() {
		switch (mClickAction.consume()) {
		case SCREEN_BUTTON_CONTINUE:
			exitScreen();
			return;

		case SCREEN_BUTTON_SAVE:
			saveTrack();
			break;

		case SCREEN_BUTTON_EXIT:
			screenManager().createLoadingScreen(new LoadingScreen(screenManager(), false, new MenuBackgroundScreen(mScreenManager), new MainMenu(screenManager())));
			break;

		}
	}

	private boolean saveTrack() {
		final var lFilename = mFilenameEntry.inputString();
		if (lFilename == null || lFilename.length() == 0) {
			screenManager().toastManager().addMessage("Error Saving", "You must enter a file name", 2000);
			return false;
		}

		mGameFileHeader.trackName(lFilename);
		mGameFileHeader.trackDirectory(ConstantsGame.TrackFolder);

		if (mGameFileHeader.isValid() == false) {
			screenManager().toastManager().addMessage("Error Saving", "GamefileHeader is not valid", 2000);
			return false;
		}

		mTrack.saveToFile(mGameFileHeader.trackFilename());

		screenManager().toastManager().addMessage("Track Saved", "Track saved", 2000);
		return true;
	}

}