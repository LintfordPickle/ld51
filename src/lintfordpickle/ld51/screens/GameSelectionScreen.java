package lintfordpickle.ld51.screens;

import lintfordpickle.ld51.ConstantsGame;
import lintfordpickle.ld51.data.tracks.GameFileHeader;
import lintfordpickle.ld51.services.TrackListService;
import net.lintford.library.screenmanager.MenuEntry;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.entries.MenuDropDownEntry;
import net.lintford.library.screenmanager.layouts.ListLayout;
import net.lintford.library.screenmanager.screens.LoadingScreen;

public class GameSelectionScreen extends MenuScreen {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final String TITLE = "Track Selection";

	private static final int BUTTON_LOAD_ID = 0;
	private static final int BUTTON_BACK_ID = 2;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private MenuDropDownEntry<GameFileHeader> mTrackFilenameEntries;

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public GameSelectionScreen(ScreenManager screenManager) {
		super(screenManager, TITLE);

		final var lListLayout = new ListLayout(this);

		mTrackFilenameEntries = new MenuDropDownEntry<GameFileHeader>(screenManager, lListLayout, "Track");
		populateDropDownListWithTrackFilenames(mTrackFilenameEntries);

		final var lLoadTrack = new MenuEntry(screenManager, lListLayout, "Play");
		lLoadTrack.registerClickListener(this, BUTTON_LOAD_ID);

		final var lBackButton = new MenuEntry(screenManager, lListLayout, "Back");
		lBackButton.registerClickListener(this, BUTTON_BACK_ID);

		lListLayout.addMenuEntry(MenuEntry.menuSeparator());
		lListLayout.addMenuEntry(mTrackFilenameEntries);
		lListLayout.addMenuEntry(lLoadTrack);

		lListLayout.addMenuEntry(MenuEntry.menuSeparator());
		lListLayout.addMenuEntry(lBackButton);

		addLayout(lListLayout);

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	@Override
	protected void handleOnClick() {
		switch (mClickAction.consume()) {
		case BUTTON_LOAD_ID:
			if (mTrackFilenameEntries.selectedItem() != null) {
				final var lGameFileHeader = mTrackFilenameEntries.selectedItem().value;
				screenManager().createLoadingScreen(new LoadingScreen(screenManager(), true, new GameScreen(screenManager(), lGameFileHeader, false)));
			}

			break;

		case BUTTON_BACK_ID:
			exitScreen();
			break;
		}
	}

	private void populateDropDownListWithTrackFilenames(MenuDropDownEntry<GameFileHeader> pEntry) {
		final var lListOfWorlds = TrackListService.getListWithTrackFilesSortedModified(ConstantsGame.TrackFolder);

		final int lWorldCount = lListOfWorlds.size();
		for (int i = 0; i < lWorldCount; i++) {
			final var lWorldFile = lListOfWorlds.get(i);
			final var lFilename = lWorldFile.getName();
			final var lTrackName = lFilename.substring(0, lFilename.lastIndexOf('.'));

			final var lNewTrackHeader = new GameFileHeader(lTrackName, ConstantsGame.TrackFolder);
			final var lNewEntry = pEntry.new MenuEnumEntryItem(lTrackName, lNewTrackHeader);
			pEntry.addItem(lNewEntry);
		}
	}
}
