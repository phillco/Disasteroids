/*
 * DISASTEROIDS
 * GameMode.java
 */

package disasteroids;

import java.awt.Graphics;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Game modes affect how the player progresses through the game.
 * @author Phillip Cohen
 * @since February 28, 2008
 */
public interface GameMode
{
    public void act();
    
    public void draw(Graphics g);
    
    public void flatten( DataOutputStream stream ) throws IOException;

}
