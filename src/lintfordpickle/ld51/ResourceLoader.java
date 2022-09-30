package lintfordpickle.ld51;

import net.lintford.library.GameResourceLoader;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.options.DisplayManager;

public class ResourceLoader extends GameResourceLoader {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	// ---------------------------------------------
	// Constructors
	// ---------------------------------------------

	public ResourceLoader(ResourceManager resourceManager, DisplayManager displayManager) {
		super(resourceManager, displayManager);

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	protected void resourcesToLoadInBackground() {
		Debug.debugManager().logger().i(getClass().getSimpleName(), "Loading game assets into group: " + ConstantsGame.GAME_RESOURCE_GROUP_ID);
		mResourceManager.addProtectedEntityGroupUid(ConstantsGame.GAME_RESOURCE_GROUP_ID);

		currentStatusMessage("loading resources");

	}

}
