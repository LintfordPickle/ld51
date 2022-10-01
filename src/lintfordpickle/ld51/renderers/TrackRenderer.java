package lintfordpickle.ld51.renderers;

import java.nio.FloatBuffer;

import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import lintfordpickle.ld51.controllers.TrackController;
import lintfordpickle.ld51.data.tracks.Track;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.graphics.shaders.ShaderSubPixel;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.core.maths.Matrix4f;
import net.lintford.library.renderers.BaseRenderer;
import net.lintford.library.renderers.RendererManager;

public class TrackRenderer extends BaseRenderer {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	// The number of bytes an element has (all elements are floats here)
	protected static final int elementBytes = 4;

	// Elements per parameter
	protected static final int positionElementCount = 4;
	protected static final int colorElementCount = 4;
	protected static final int textureElementCount = 2;

	// Bytes per parameter
	protected static final int positionBytesCount = positionElementCount * elementBytes;
	protected static final int colorBytesCount = colorElementCount * elementBytes;
	protected static final int textureBytesCount = textureElementCount * elementBytes;

	// Byte offsets per parameter
	protected static final int positionByteOffset = 0;
	protected static final int colorByteOffset = positionByteOffset + positionBytesCount;
	protected static final int textureByteOffset = colorByteOffset + colorBytesCount;

	// The amount of elements that a vertex has
	protected static final int elementCount = positionElementCount + colorElementCount + textureElementCount;

	// The size of a vertex in bytes (sizeOf())
	protected static final int stride = positionBytesCount + colorBytesCount + textureBytesCount;

	public static final String RENDERER_NAME = "Track Renderer";

	protected static final String VERT_FILENAME = "/res/shaders/shader_basic_pct.vert";
	protected static final String FRAG_FILENAME = "res/shaders/shaderTrack.frag";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private FloatBuffer mTrackBuffer;

	protected int mVaoId = -1;
	protected int mVboId = -1;
	protected int mVertexCount = 0;

	protected ShaderSubPixel mShader;
	protected Matrix4f mModelMatrix;

	protected boolean mIsTrackGenerated;
	protected Texture mTrackTexture;
	protected Texture mTrackPropsTexture;
	protected Texture mTrackGrassTexture;

	protected TrackController mTrackController;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrackRenderer(RendererManager rendererManager, int entityGroupID) {
		super(rendererManager, RENDERER_NAME, entityGroupID);

		mShader = new ShaderSubPixel("TrackShader", VERT_FILENAME, FRAG_FILENAME);

		mModelMatrix = new Matrix4f();
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public boolean isInitialized() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void initialize(LintfordCore pCore) {
		mTrackController = (TrackController) pCore.controllerManager().getControllerByNameRequired(TrackController.CONTROLLER_NAME, entityGroupID());

	}

	@Override
	public void loadResources(ResourceManager resourceManager) {
		super.loadResources(resourceManager);

		final var lTrack = mTrackController.currentTrack();

		if (mVaoId == -1)
			mVaoId = GL30.glGenVertexArrays();

		if (mVboId == -1)
			mVboId = GL15.glGenBuffers();

		mShader.loadResources(resourceManager);

		mTrackTexture = resourceManager.textureManager().loadTexture("TEXTURE_TRACK", "res/textures/textureTrack.png", GL11.GL_LINEAR, entityGroupID());
		mTrackPropsTexture = resourceManager.textureManager().loadTexture("TEXTURE_TRACK_PROPS", "res/textures/textureTrackProps.png", GL11.GL_LINEAR, entityGroupID());
		mTrackGrassTexture = resourceManager.textureManager().loadTexture("TEXTURE_TRACK_GRASS", "res/textures/textureTrackGrass.png", GL11.GL_LINEAR, entityGroupID());

		loadTrackMesh(lTrack);
	}

	@Override
	public void unloadResources() {
		super.unloadResources();

		mShader.unloadResources();
		mTrackTexture = null;

		if (mVaoId > -1)
			GL30.glDeleteVertexArrays(mVaoId);

		if (mVboId > -1)
			GL15.glDeleteBuffers(mVboId);

	}

	@Override
	public void update(LintfordCore core) {
		super.update(core);

	}

	@Override
	public void draw(LintfordCore core) {
		if (!mTrackController.isInitialized())
			return;

		final var lTrack = mTrackController.currentTrack();
		if (lTrack == null)
			return;

		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, mTrackTexture.getTextureID());

		GL30.glBindVertexArray(mVaoId);

		mShader.projectionMatrix(core.gameCamera().projection());
		mShader.viewMatrix(core.gameCamera().view());
		mModelMatrix.setIdentity();
		mModelMatrix.translate(0, 0f, -6f);
		mShader.modelMatrix(mModelMatrix);

		GL11.glLineWidth(1);
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		mShader.bind();

		GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, mVertexCount);

		mShader.unbind();

		GL30.glBindVertexArray(0);

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void loadTrackMesh(Track track) {
		if (track == null)
			return;

		buildTrackMesh(track);

		GL30.glBindVertexArray(mVaoId);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, mVboId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, mTrackBuffer, GL15.GL_STATIC_DRAW);

		GL20.glVertexAttribPointer(0, positionElementCount, GL11.GL_FLOAT, false, stride, positionByteOffset);
		GL20.glVertexAttribPointer(1, colorElementCount, GL11.GL_FLOAT, false, stride, colorByteOffset);
		GL20.glVertexAttribPointer(2, textureElementCount, GL11.GL_FLOAT, false, stride, textureByteOffset);

		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);

		GL30.glBindVertexArray(0);

		mIsTrackGenerated = true;
	}

	public void buildTrackMesh(Track track) {
		final var lInnerVertices = mTrackController.innerTrackVertices();
		final var lOuterVertices = mTrackController.outerTrackVertices();

		final int lNumSplinePoints = lInnerVertices.length;
		mTrackBuffer = BufferUtils.createFloatBuffer(lNumSplinePoints * 4 * stride);

		float lDistanceTravelled = 0.f;
		float lLengthOfSegment = 0.f;

		float lCurX = 0.f;
		float lCurY = 0.f;
		float lPrevX = 0.f;
		float lPrevY = 0.f;

		for (int i = 0; i < lNumSplinePoints; i++) {

			lCurX = lInnerVertices[i].x;
			lCurY = lInnerVertices[i].y;

			lLengthOfSegment = Vector2f.distance(lCurX, lCurY, lPrevX, lPrevY) / 1024.f;
			lDistanceTravelled += lLengthOfSegment;

			final float lInnerPointX = lInnerVertices[i].x;
			final float lInnerPointY = lInnerVertices[i].y;
			final float lOuterPointX = lOuterVertices[i].x;
			final float lOuterPointY = lOuterVertices[i].y;

			addVertToBuffer(lInnerPointX, lInnerPointY, 0, 0.f, lDistanceTravelled);
			addVertToBuffer(lOuterPointX, lOuterPointY, 0, 1.f, lDistanceTravelled);

			lPrevX = lCurX;
			lPrevY = lCurY;
		}

		mTrackBuffer.flip();
	}

	private void addVertToBuffer(float x, float y, float z, float u, float v) {
		mTrackBuffer.put(x);
		mTrackBuffer.put(y);
		mTrackBuffer.put(z);
		mTrackBuffer.put(1f);

		mTrackBuffer.put(1f);
		mTrackBuffer.put(1f);
		mTrackBuffer.put(1f);
		mTrackBuffer.put(1f);

		mTrackBuffer.put(u);
		mTrackBuffer.put(v);

		mVertexCount++;
	}

}
