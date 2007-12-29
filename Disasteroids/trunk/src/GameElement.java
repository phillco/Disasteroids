/*
 * DISASTEROIDS
 * GameElement.java
 */

import java.awt.Graphics;
import java.io.Serializable;

/**
 *
 * @author Phillip Cohen
 * @since Dec 21, 2007
 */
public interface GameElement extends Serializable
{
    public void act();

    public void draw( Graphics g );
}
