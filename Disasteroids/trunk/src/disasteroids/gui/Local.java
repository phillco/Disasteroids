/*
 * DISASTEROIDS
 * Local.java
 */
package disasteroids.gui;

import disasteroids.Ship;

/**
 * Quick access class for local objects.
 * @author Phillip Cohen
 * @since January 15, 2008
 */
public class Local
{
    /**
     * A boolean that's toggled every step. Useful for flashing objects.
     * @since April 11, 2008
     */
    static boolean globalFlash = true;

    public static boolean loading = false;

    /**
     * Returns if commonly used things (like the AsteroidsFrame and Background) are null, and thus are loading.
     * 
     * @return  if common graphics classes are null!
     * @since April 10, 2008
     */
    public static boolean isStuffNull()
    {
        return ( loading || MainWindow.frame() == null || MainWindow.frame().localPlayer() == null || MainWindow.frame().getPanel().getStarBackground() == null );
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
            return MainWindow.frame().localPlayer();
    }

    /**
     * Returns a boolean that's toggled every step. Useful for flashing objects.
     * 
     * @return  a global boolean, toggled at every step.
     * @since April 11, 2008
     */
    public static boolean getGlobalFlash()
    {
        return globalFlash;
    }
}
