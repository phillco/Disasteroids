/*
 * DISASTEROIDS
 * RelativeGraphics.java
 */
package disasteroids.gui;

import disasteroids.Game;
import disasteroids.Main;

/**
 * Utility class for drawing graphics around the localPlayer.
 * @author Phillip Cohen
 * @since January 5, 2008
 */
public abstract class RelativeGraphics
{
    public static int translateX( double x )
    {
        try
        {
            return (int) Math.round( ( x - Local.getLocalPlayer().getX() + MainWindow.frame().getWidth() / 2 + 4 * Game.getInstance().GAME_WIDTH ) % Game.getInstance().GAME_WIDTH ) + MainWindow.frame().getRumbleX();

        }
        catch ( NullPointerException nullPointerException )
        {
            Main.warning( "Null Pointer Exception, translateX" );
            return -1;
        }

    }

    public static int translateY( double y )
    {
        try
        {
            return (int) Math.round( ( y - Local.getLocalPlayer().getY() + MainWindow.frame().getHeight() / 2 + 4 * Game.getInstance().GAME_HEIGHT ) % Game.getInstance().GAME_HEIGHT ) + MainWindow.frame().getRumbleY();

        }
        catch ( NullPointerException nullPointerException )
        {
            Main.warning( "Null Pointer Exception, translateY" );
            return -1;
        }
    }

    /**
     * Calculates and returns the location directly opposite the first player
     * @return the x coordinate of the location directly opposite the first player
     */
    public static int oppositeX()
    {
        try
        {
            return (int) ( ( Game.getInstance().getObjectManager().getPlayers().peek().getX() + Game.getInstance().GAME_WIDTH / 2 ) ) % Game.getInstance().GAME_WIDTH;

        }
        catch ( Exception exception )
        {
            Main.warning( exception.getClass().getName() + " at oppositeX" );
            return -1;
        }

    }

    /**
     * Calculates and returns the location directly opposite the first player
     * @return the y coordinate of the location directly opposite the first player
     */
    public static int oppositeY()
    {
        try
        {
            return (int) ( Game.getInstance().getObjectManager().getPlayers().peek().getY() + Game.getInstance().GAME_HEIGHT / 2 ) % Game.getInstance().GAME_HEIGHT;
        }
        catch ( Exception exception )
        {
            Main.warning( exception.getClass().getName() + " at oppositeY" );
            return -1;
        }
    }
}
