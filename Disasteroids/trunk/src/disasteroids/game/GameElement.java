/*
 * DISASTEROIDS
 * GameElement.java
 */
package disasteroids.game;

import java.awt.Graphics;

/**
 * The basic interface for everything that thinks and draws.
 * @author Phillip Cohen
 * @since December 21, 2007
 */
public interface GameElement
{
	public void act();

	public void draw( Graphics g );
}
