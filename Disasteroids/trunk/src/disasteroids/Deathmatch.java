/*
 * DISASTEROIDS
 * WaveGameplay.java
 */
package disasteroids;

import disasteroids.gui.AsteroidsFrame;
import disasteroids.gui.RelativeGraphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.swing.JOptionPane;

/**
 * A game mode where players fend off waves of asteroids.
 * @author Phillip Cohen
 */
public class Deathmatch implements GameMode
{

    public Deathmatch()
    {
    }

    public void act()
    {
    }

    public void draw( Graphics g )
    {
        
    }

    int getWavePoints( int wave )
    {
        return 0;
    }

    public void flatten( DataOutputStream stream ) throws IOException
    {

    }

    public Deathmatch( DataInputStream stream ) throws IOException
    {

    }

    public void optionsKey()
    {

    }
}
