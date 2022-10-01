package lintfordpickle.ld51;

import net.lintford.library.core.entity.BaseEntity;

public class ConstantsGame {

	// ---------------------------------------------
	// Setup
	// ---------------------------------------------

	public static final String FOOTER_TEXT = "A Game by LintfordPickle for LD#51 2022";

	public static final String APPLICATION_NAME = "LD51";
	public static final String WINDOW_TITLE = "LD51";

	public static final float ASPECT_RATIO = 21.f / 9.f;

	public static final int TRACK_SEG_REG_WIDTH = 90;

	public static final int GAME_CANVAS_WIDTH = (int) 380;
	public static final int GAME_CANVAS_HEIGHT = (int) 200;

	public static final int GAME_RESOURCE_GROUP_ID = BaseEntity.getEntityNumber();

	public static final String TrackFolder = "res/tracks/";

	// ---------------------------------------------
	// Debug
	// ---------------------------------------------

	public static final boolean IS_DEBUG_MODE = true;
	public static final boolean CAMERA_DEBUG_MODE = true;

	// ---------------------------------------------
	// Game Related
	// ---------------------------------------------

	public static final float METER_TO_PIXEL = 32.f;
	public static final float PIXEL_TO_METER = 1.f / METER_TO_PIXEL;

}
