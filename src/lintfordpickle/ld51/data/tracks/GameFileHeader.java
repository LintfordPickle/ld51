package lintfordpickle.ld51.data.tracks;

public class GameFileHeader {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String FILE_SEPERATOR = System.getProperty("file.separator");

	public static final String GAMEWORLD_FILE_EXTENSION = ".track";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private boolean mIsValid;
	private String mTrackName;
	private String mTrackDirectory;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public String trackName() {
		return mTrackName;
	}

	public void trackName(String trackName) {
		mTrackName = trackName;
		validateHeader();
	}

	public String trackFilename() {
		return mTrackDirectory + mTrackName + GAMEWORLD_FILE_EXTENSION;
	}

	public String trackDirectory() {
		return mTrackDirectory;
	}

	public void trackDirectory(String directoryName) {
		mTrackDirectory = directoryName;
		validateHeader();
	}

	public boolean isValid() {
		return mIsValid;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameFileHeader() {
	}

	public GameFileHeader(String trackName, String worldDirectoryName) {
		mTrackName = trackName;
		mTrackDirectory = worldDirectoryName;

		validateHeader();
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void validateHeader() {
		// TODO: Validate the directory + filename
		mIsValid = mTrackName != null && mTrackName != null;
	}
}
