/*
 * DISASTEROIDS
 * GameElement.java
 */

import java.awt.Graphics;

/**
 *
 * @author Phillip Cohen
 * @since Dec 21, 2007
 */
public interface GameElement
{
    public void act();

    public void draw( Graphics g );
}
