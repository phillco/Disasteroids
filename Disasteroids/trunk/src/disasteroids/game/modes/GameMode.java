/*
 * DISASTEROIDS | Game
 * GameMode.java
 */
package disasteroids.game.modes;

import java.awt.Graphics;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 
 * @author Phillip Cohen
 */
public abstract class GameMode
{
	// Game mode flags.
	public final static int FLAG_FRIENDLYFIRE = 1 << 0; // 1

	public final static int FLAG_PLAYERSEARNPOINTS = 1 << 1; // 2

	public final static int FLAG_PLAYERSEARNKILLS = 1 << 2; // 4

	private int flags;

	public GameMode( int flags )
	{
		this.flags = flags;
	}

	public void act()
	{
	}

	public void drawHUD( Graphics g )
	{
	}

	public void flatten( DataOutputStream stream ) throws IOException
	{
	}

	public int getFlags()
	{
		return flags;
	}

	public boolean hasFlag( int flag )
	{
		return ( flags & flag ) != 0;
	}

	public void optionsKey()
	{
	}

	public abstract String getName();
}
