package lintfordpickle.ld51.screens;

import lintfordpickle.ld51.ConstantsGame;
import lintfordpickle.ld51.data.tracks.GameFileHeader;
import lintfordpickle.ld51.services.TrackListService;
import net.lintford.library.screenmanager.MenuEntry;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.entries.MenuDropDownEntry;
import net.lintford.library.screenmanager.layouts.ListLayout;

public class EditorTrackSelectionScreen extends MenuScreen {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final String TITLE = "Editor Track Selection";

	private static final int BUTTON_LOAD_ID = 0;
	private static final int BUTTON_CREATE_NEW_ID = 1;
	private static final int BUTTON_BACK_ID = 2;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private MenuDropDownEntry<GameFileHeader> mTrackFilenameEntries;

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public EditorTrackSelectionScreen(ScreenManager screenManager) {
		super(screenManager, TITLE);

		final var lListLayout = new ListLayout(this);

		mTrackFilenameEntries = new MenuDropDownEntry<GameFileHeader>(screenManager, lListLayout, "Track Filename");
		populateDropDownListWithTrackFilenames(mTrackFilenameEntries);

		final var lCreateNewTrack = new MenuEntry(screenManager, lListLayout, "Create New");
		lCreateNewTrack.registerClickListener(this, BUTTON_CREATE_NEW_ID);

		final var lLoadTrack = new MenuEntry(screenManager, lListLayout, "Load");
		lLoadTrack.registerClickListener(this, BUTTON_LOAD_ID);

		final var lBackButton = new MenuEntry(screenManager, lListLayout, "Back");
		lBackButton.registerClickListener(this, BUTTON_BACK_ID);

		lListLayout.addMenuEntry(lCreateNewTrack);

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
		case BUTTON_CREATE_NEW_ID:
			screenManager().addScreen(new EditorScreen(screenManager()));
			break;

		case BUTTON_LOAD_ID:
			if (mTrackFilenameEntries.selectedItem() != null) {
				final var lGameFileHeader = mTrackFilenameEntries.selectedItem().value;
				screenManager().addScreen(new EditorScreen(screenManager(), lGameFileHeader));
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
			final var lNewEntry = pEntry.new MenuEnumEntryItem(lWorldFile.getName(), lNewTrackHeader);
			pEntry.addItem(lNewEntry);
		}
	}
}
