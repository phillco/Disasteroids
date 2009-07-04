/*
 * DISASTEROIDS
 * Level.java
 */
package disasteroids.game.levels;

import java.awt.Graphics;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A level that the game is played on.
 */
public interface Level
{
    public void act();

    public void drawHUD( Graphics g );

    public String getName();

    public void flatten( DataOutputStream stream ) throws IOException;

    public void optionsKey();
}
