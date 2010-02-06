/*
 * DISASTEROIDS | Game
 * Cooperative.java
 */
package disasteroids.game.modes;

/**
 * Game mode where players work together to destroy enemies.
 */
public class Cooperative extends GameMode
{
	public Cooperative()
	{
		super( FLAG_PLAYERSEARNPOINTS );
	}

	@Override
	public String getName()
	{
		return "Cooperative";
	}
}
