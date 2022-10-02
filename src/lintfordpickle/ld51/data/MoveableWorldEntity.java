package lintfordpickle.ld51.data;

import net.lintford.library.core.entity.CircleEntity;
import net.lintford.library.core.maths.Vector2f;

public class MoveableWorldEntity extends CircleEntity {

	private static final long serialVersionUID = 1876248764285701266L;

	// --------------------------------------
	// Variables
	// --------------------------------------

	public final Vector2f v = new Vector2f();
	public float mass = 1.f;

}
