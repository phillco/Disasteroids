/*
 * DISASTEROIDS
 * Local.java
 */
package disasteroids.gui;

import disasteroids.Game;
import disasteroids.gameobjects.*;
import disasteroids.sound.Sound;

/**
 * Manages the local (on this computer) objects.
 */
public class Local
{

    /**
     * ID of the player that's at this computer.
     */
    private static long localPlayerID = -1;

    /**
     * Whether the game is loading.
     */
    private static boolean loading = false;

    public static void init( long localID )
    {
        if ( localPlayerID == -1 )
            localPlayerID = localID;
        Sound.updateMusic();
    }


    /**
     * Returns if commonly used things (like the AsteroidsFrame and Background) are null, and thus are loading.
     * 
     * @return  if common graphics classes are null!
     * @since April 10, 2008
     */
    public static boolean isStuffNull()
    {
        return ( loading || MainWindow.frame() == null || localPlayerID == -1 || MainWindow.frame().getPanel().getStarBackground() == null );
    }

    /**
     * Returns the star background.
     * 
     * @return  the star background, as seen in the frame's panel
     * @since April 10, 2008
     */
    public static Background getStarBackground()
    {
        if ( isStuffNull() )
            return null;
        else
            return MainWindow.frame().getPanel().getStarBackground();
    }

    public static Ship getLocalPlayer()
    {
        if ( isStuffNull() )
            return null;
        else
            return ( Ship ) Game.getInstance().getObjectManager().getObject( localPlayerID );
    }

    public static void loadGame()
    {
        loading = true;
        localPlayerID = Game.loadFromFile();
        loading = false;
    }
}
