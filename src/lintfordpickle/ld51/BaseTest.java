package lintfordpickle.ld51;

import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.glClearColor;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import net.lintford.library.GameInfo;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.graphics.ColorConstants;
import net.lintford.library.core.graphics.batching.SpriteBatch;
import net.lintford.library.core.graphics.batching.TextureBatchPC;
import net.lintford.library.core.graphics.batching.TextureBatchPCT;
import net.lintford.library.core.graphics.batching.TextureBatchPT;
import net.lintford.library.core.graphics.fonts.BitmapFontManager;
import net.lintford.library.core.graphics.fonts.FontUnit;
import net.lintford.library.core.graphics.polybatch.IndexedPolyBatchPCT;
import net.lintford.library.core.graphics.polybatch.PolyBatchPC;
import net.lintford.library.core.graphics.sprites.spritesheet.SpriteSheetDefinition;
import net.lintford.library.core.graphics.textures.CoreTextureNames;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.core.maths.Vector2f;

public class BaseTest extends LintfordCore {

	// ---------------------------------------------
	// Entry Point
	// ---------------------------------------------

	private static final String APP_NAME = "LD51";
	private static final String WINDOW_NAME = "LD51 - Tachyon-R II";

	public static void main(String[] args) {

		final var lGameInfo = new GameInfo() {
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

		final var lClient = new BaseTest(lGameInfo, args);
		lClient.createWindow();
	}

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private int mEntityGroupUid = 10;
	private TextureBatchPC mTextureBatchPC;
	private TextureBatchPT mTextureBatchPT;
	private TextureBatchPCT mTextureBatchPCT;

	private final List<Vector2f> mPolyVertices = new ArrayList<>();
	private PolyBatchPC mPolyBatchPC;
	private IndexedPolyBatchPCT mIndexedPolyBatchPCT;

	private SpriteBatch mSpriteBatch;
	private SpriteSheetDefinition mCoreSpritesheet;

	private Texture mTextureTest00;
	private Texture mTextureTest01;
	private Texture mTextureTest02;
	private Texture mTextureTest03;

	private FontUnit mConsoleFont;

	private int mDrawMode;
	private final int mNumDrawModes = 8;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public BaseTest(GameInfo pGameInfo, String[] pArgs) {
		super(pGameInfo, pArgs, false);

		mIsFixedTimeStep = false;
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
	}

	@Override
	protected void onLoadResources() {
		super.onLoadResources();

		mTextureBatchPC = new TextureBatchPC();
		mTextureBatchPC.loadResources(mResourceManager);

		mTextureBatchPT = new TextureBatchPT();
		mTextureBatchPT.loadResources(mResourceManager);

		mTextureBatchPCT = new TextureBatchPCT();
		mTextureBatchPCT.loadResources(mResourceManager);

		mSpriteBatch = new SpriteBatch();
		mSpriteBatch.loadResources(mResourceManager);

		mPolyBatchPC = new PolyBatchPC();
		mPolyBatchPC.loadResources(mResourceManager);

		mIndexedPolyBatchPCT = new IndexedPolyBatchPCT();
		mIndexedPolyBatchPCT.loadResources(mResourceManager);

		mCoreSpritesheet = mResourceManager.spriteSheetManager().coreSpritesheet();

		mConsoleFont = mResourceManager.fontManager().getFontUnit(BitmapFontManager.SYSTEM_FONT_CONSOLE_NAME);

		mTextureTest00 = mResourceManager.textureManager().loadTexture("TEXTURE_TEST_00", "res/textures/textureTest00.png", mEntityGroupUid);
		mTextureTest01 = mResourceManager.textureManager().loadTexture("TEXTURE_TEST_01", "res/textures/textureTest01.png", mEntityGroupUid);
		mTextureTest02 = mResourceManager.textureManager().loadTexture("TEXTURE_TEST_02", "res/textures/textureTest02.png", mEntityGroupUid);
		mTextureTest03 = mResourceManager.textureManager().loadTexture("TEXTURE_TEST_03", "res/textures/textureTest03.png", mEntityGroupUid);

		mGameCamera = setNewGameCamera(mGameCamera);
	}

	@Override
	protected void onUnloadResources() {
		super.onUnloadResources();

		mTextureBatchPC.unloadResources();
		mTextureBatchPT.unloadResources();
		mTextureBatchPCT.unloadResources();

		mPolyBatchPC.unloadResources();
		mIndexedPolyBatchPCT.unloadResources();

		mSpriteBatch.unloadResources();
	}

	@Override
	protected void onHandleInput() {
		super.onHandleInput();

		if (mInputState.keyboard().isKeyDownTimed(GLFW.GLFW_KEY_O)) {
			mDrawMode--;
			if (mDrawMode < 0)
				mDrawMode = mNumDrawModes - 1;
		}

		if (mInputState.keyboard().isKeyDownTimed(GLFW.GLFW_KEY_P)) {
			mDrawMode++;
			if (mDrawMode >= mNumDrawModes)
				mDrawMode = 0;
		}
	}

	@Override
	protected void onUpdate() {
		super.onUpdate();
	}

	@Override
	protected void onDraw() {
		GL11.glClearColor(0.3f, 0.2f, 0.3f, 1.f);

		super.onDraw();

		final var lHudBounds = mHUD.boundingRectangle();
		mConsoleFont._countDebugStats(false);
		mConsoleFont.begin(mHUD);

		switch (mDrawMode) {
		case 1:
			mConsoleFont.drawText("1. TextureBatchPC", lHudBounds.left() + 5.f, lHudBounds.top() + 5.f, -0.01f, 1.f);
			mHUD.update(this);
			mTextureBatchPC.begin(mHUD);
			mTextureBatchPC.draw(-318, -238, 316, 236, -0.01f, ColorConstants.BLUE);
			mTextureBatchPC.draw(0, 0, 318, 238, -0.01f, ColorConstants.BLUE);
			mTextureBatchPC.end();
			break;

		case 2:
			mConsoleFont.drawText("2. TextureBatchPT", lHudBounds.left() + 5.f, lHudBounds.top() + 5.f, -0.01f, 1.f);

			mTextureBatchPT.begin(mHUD);
			mTextureBatchPT.draw(mTextureTest00, 0, 0, 64, 64, 0, 0, 64, 64, -0.01f);
			mTextureBatchPT.end();
			break;

		case 3:
			mConsoleFont.drawText("3. TextureBatchPCT", lHudBounds.left() + 5.f, lHudBounds.top() + 5.f, -0.01f, 1.f);

			mTextureBatchPCT.begin(mGameCamera);
			mTextureBatchPCT.draw(mTextureTest00, 0, 0, 64, 64, -256, -256, 256, 256, -0.01f, ColorConstants.WHITE);
			mTextureBatchPCT.draw(mTextureTest01, 0, 0, 64, 64, 16, -256, 256, 256, -0.01f, ColorConstants.WHITE);
			mTextureBatchPCT.draw(mTextureTest02, 0, 0, 64, 64, -256, 16, 256, 256, -0.01f, ColorConstants.WHITE);
			mTextureBatchPCT.draw(mTextureTest03, 0, 0, 64, 64, 16, 16, 256 * 0.6f, 256 * .6f, -0.01f, ColorConstants.WHITE);
			mTextureBatchPCT.end();
			break;

		case 4:
			mConsoleFont.drawText("4. SpriteBatch", lHudBounds.left() + 5.f, lHudBounds.top() + 5.f, -0.01f, 1.f);
			mSpriteBatch.begin(mHUD);
			mSpriteBatch.draw(mCoreSpritesheet, CoreTextureNames.TEXTURE_AUTOSCROLL, 0, 0, 32, 32, -0.01f, ColorConstants.WHITE);
			mSpriteBatch.end();
			break;

		case 5:
			mPolyVertices.clear();
			mPolyVertices.add(new Vector2f(0, 0));
			mPolyVertices.add(new Vector2f(100, 0));
			mPolyVertices.add(new Vector2f(100, 100));
			mPolyVertices.add(new Vector2f(0, 100));
			mPolyVertices.add(new Vector2f(-50, 50));

			mConsoleFont.drawText("5. PolyBatch", lHudBounds.left() + 5.f, lHudBounds.top() + 5.f, -0.01f, 1.f);

			mPolyBatchPC.lineMode(GL11.GL_LINES);
			mPolyBatchPC.begin(mHUD);
			mPolyBatchPC.drawVertices(mPolyVertices, 5, -0.01f, true, 1.f, 0.f, 0.f);
			mPolyBatchPC.end();
			break;

		case 6:
			mConsoleFont.drawText("6. TextureBatchPC (Rotation)", lHudBounds.left() + 5.f, lHudBounds.top() + 5.f, -0.01f, 1.f);
			mHUD.update(this);

			final float lXTranslation = (float) Math.cos(mGameTime.totalTimeMilli() * .001f) * 100.f;
			final float lRotAmount = (float) mGameTime.totalTimeMilli() * .001f;

			mTextureBatchPC.begin(mHUD);
			mTextureBatchPC.drawAroundCenter(lXTranslation, 0, 256, 256, -0.01f, lRotAmount, -128, -128, 1.f, ColorConstants.WHITE);
			mTextureBatchPC.end();
			break;

		case 7:
			mConsoleFont.drawText("7. Camera Transforms", lHudBounds.left() + 5.f, lHudBounds.top() + 5.f, -0.01f, 1.f);

			final float lCXTranslation = (float) Math.cos(mGameTime.totalTimeMilli() * .001f) * 100.f;
			final float lCZoomAmount = (float) Math.sin(mGameTime.totalTimeMilli() * .001f) * 0.1f + 1f;

			mGameCamera.setPosition(lCXTranslation, 0);
			mGameCamera.setZoomFactor(lCZoomAmount);

			mTextureBatchPC.begin(mGameCamera);
			mTextureBatchPC.drawAroundCenter(-128, -128, 256, 256, -0.01f, 0, -128, -128, 1.f, ColorConstants.WHITE);
			mTextureBatchPC.end();
			break;
		}

		mConsoleFont._countDebugStats(false);
		mConsoleFont.end();
		mConsoleFont._countDebugStats(true);
	}
}
