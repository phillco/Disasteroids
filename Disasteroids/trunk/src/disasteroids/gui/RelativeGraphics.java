/*
 * DISASTEROIDS
 * RelativeGraphics.java
 */
package disasteroids.gui;

import disasteroids.*;

/**
 * Utility class for drawing graphics around the localPlayer.
 * @author Phillip Cohen
 * @since Jan 5, 2008
 */
public abstract class RelativeGraphics
{
    public static int translateX( double x )
    {
        return (int) Math.round( ( x - AsteroidsFrame.frame().localPlayer().getX() + AsteroidsFrame.frame().getWidth() / 2 + 4 * Game.getInstance().GAME_WIDTH ) % Game.getInstance().GAME_WIDTH );
    }

    public static int translateY( double y )
    {
        return (int) Math.round( ( y - AsteroidsFrame.frame().localPlayer().getY() + AsteroidsFrame.frame().getHeight() / 2 + 4 * Game.getInstance().GAME_HEIGHT ) % Game.getInstance().GAME_HEIGHT );
    }
}
