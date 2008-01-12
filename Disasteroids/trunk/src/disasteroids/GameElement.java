/*
 * DISASTEROIDS
 * GameElement.java
 */
package disasteroids;

import java.awt.Graphics;
import java.io.Serializable;

/**
 * The basic interface for everything that thinks and draws.
 * @author Phillip Cohen
 * @since December 21, 2007
 */
public interface GameElement extends Serializable
{
    public void act();

    public void draw( Graphics g );
}
