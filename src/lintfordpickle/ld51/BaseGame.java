package lintfordpickle.ld51;

import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.glClearColor;

import org.lwjgl.opengl.GL11;

import lintfordpickle.ld51.screens.MainMenu;
import lintfordpickle.ld51.screens.MenuBackgroundScreen;
import net.lintford.library.GameInfo;
import net.lintford.library.GameResourceLoader;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.camera.Camera;
import net.lintford.library.core.debug.GLDebug;
import net.lintford.library.core.maths.RandomNumbers;
import net.lintford.library.screenmanager.ScreenManager;

public class BaseGame extends LintfordCore {

	// ---------------------------------------------
	// Entry Point
	// ---------------------------------------------

	private static final String APP_NAME = "LD51";
	private static final String WINDOW_NAME = "LD51 - Tachyon-R II";

	public static void main(String[] args) {
		GameInfo lGameInfo = new GameInfo() {
			@Override
			public String applicationName() {
				return APP_NAME;
			}

			@Override
			public String windowTitle() {
				return WINDOW_NAME;
			}

			@Override
			public int minimumWindowWidth() {
				return ConstantsGame.GAME_CANVAS_WIDTH;
			}

			@Override
			public int minimumWindowHeight() {
				return ConstantsGame.GAME_CANVAS_HEIGHT;
			}

			@Override
			public int baseGameResolutionWidth() {
				return ConstantsGame.GAME_CANVAS_WIDTH;
			}

			@Override
			public int baseGameResolutionHeight() {
				return ConstantsGame.GAME_CANVAS_HEIGHT;
			}

			@Override
			public boolean stretchGameResolution() {
				return true;
			}

			@Override
			public boolean windowResizeable() {
				return true;
			}
		};

		// ExcavationClient def constructor will automatically create a window and load the previous
		// settings (if they exist).
		BaseGame lClient = new BaseGame(lGameInfo, args);
		lClient.createWindow();
	}

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	protected int mEntityGroupID;

	private GameResourceLoader mGameResourceLoader;
	private ScreenManager mScreenManager;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public ScreenManager screenManager() {
		return mScreenManager;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public BaseGame(GameInfo pGameInfo, String[] pArgs) {
		super(pGameInfo, pArgs, false);

		mEntityGroupID = RandomNumbers.RANDOM.nextInt();
		mIsFixedTimeStep = false;

		mScreenManager = new ScreenManager(this);
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	protected void showStartUpLogo(long pWindowHandle) {
		glClearColor(0f, 0f, 0f, 1f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		glfwSwapBuffers(pWindowHandle);
	}

	@Override
	protected void onInitializeApp() {
		super.onInitializeApp();

		mScreenManager.addScreen(new MenuBackgroundScreen(mScreenManager));
		mScreenManager.addScreen(new MainMenu(mScreenManager));
		mScreenManager.initialize();
	}

	@Override
	protected void onLoadResources() {
		super.onLoadResources();

		mGameResourceLoader = new ResourceLoader(mResourceManager, config().display());
		mGameResourceLoader.loadResources(mResourceManager);
		mGameResourceLoader.setMinimumTimeToShowLogosMs(ConstantsGame.IS_DEBUG_MODE ? 0 : 2000);
		mGameResourceLoader.loadResourcesInBackground(this);

		GLDebug.checkGLErrorsException();

		mGameCamera = new Camera(mMasterConfig.display());
		mScreenManager.loadResources(mResourceManager);
	}

	@Override
	protected void onUnloadResources() {
		super.onUnloadResources();

		mScreenManager.unloadResources();
	}

	@Override
	protected void onHandleInput() {
		super.onHandleInput();

		gameCamera().handleInput(this);
		mScreenManager.handleInput(this);
	}

	@Override
	protected void onUpdate() {
		super.onUpdate();

		mScreenManager.update(this);
	}

	@Override
	protected void onDraw() {
		super.onDraw();

		mScreenManager.draw(this);
	}

}
