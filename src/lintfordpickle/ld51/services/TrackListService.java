package lintfordpickle.ld51.services;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import lintfordpickle.ld51.data.tracks.GameFileHeader;

/** loads a list of game worlds to be played */
public class TrackListService {

	public static final FilenameFilter filter = new FilenameFilter() {
		@Override
		public boolean accept(File f, String name) {
			return name.endsWith(GameFileHeader.GAMEWORLD_FILE_EXTENSION);
		}
	};

	public static List<String> getListWithTrackFilenames(String pTracksDirectory) {
		final var lDirectory = new File(pTracksDirectory);
		final var lTrackList = lDirectory.list(filter);

		return Arrays.asList(lTrackList);
	}

	public static List<File> getListWithTrackFilesSortedModified(String pTracksDirectory) {
		final var lDirectory = new File(pTracksDirectory);
		final var lTrackList = lDirectory.listFiles(filter);

		// Sort files based on date modified (easier for testing if nothing else)
		Arrays.sort(lTrackList, new Comparator<File>() {
			public int compare(File f1, File f2) {
				return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
			}
		});

		return Arrays.asList(lTrackList);
	}

}
