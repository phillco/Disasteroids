/*
 * DISASTEROIDS
 * EmptyLevel.java
 */
package disasteroids.game.levels;

import java.awt.Graphics;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * An empty level.
 */
public class EmptyLevel implements Level
{

	public EmptyLevel()
	{
	}

	public void act()
	{
	}

	public void drawHUD( Graphics g )
	{
	}

	public String getName()
	{
		return "Empty level";
	}

	public void flatten( DataOutputStream stream ) throws IOException
	{
	}

	public EmptyLevel( DataInputStream stream ) throws IOException
	{
	}

	public void optionsKey()
	{
	}
}
