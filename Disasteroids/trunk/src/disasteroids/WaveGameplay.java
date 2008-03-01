/*
 * DISASTEROIDS
 * WaveGameplay.java
 */
package disasteroids;

import disasteroids.gui.AsteroidsFrame;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import javax.swing.JOptionPane;

/**
 * A game mode where players fend off waves of asteroids.
 * @author Phillip Cohen
 */
public class WaveGameplay implements GameMode
{
    private int currentWave;

    private int wavePoints;

    public WaveGameplay()
    {
        currentWave = 5;
        wavePoints = getWavePoints( currentWave );
    }

    public void act()
    {
        // Is the wave over?
        if ( wavePoints <= 0 && Game.getInstance().asteroidManager().size() <= 0 )
        {
            Running.log( "Wave " + currentWave + " completed!", 300 );
            currentWave += 1;
            wavePoints = getWavePoints( currentWave );
        }
        int x = RandomGenerator.get().nextBoolean() ? -1999 : 1999;
        int y = RandomGenerator.get().nextBoolean() ? -1999 : 1999;
        /*
        // Choose a spawn spot.
        switch ( RandomGenerator.get().nextInt( 3 ) )
        {
        case 0:
        x = -250;
        y = 300;
        break;
        case 1:
        x = 600;
        y = -100;
        break;
        case 2:
        x = -900;
        y = 600;
        break;
        case 3:
        x = 50;
        y = -400;
        break;
        }
         */
        // Spawn an asteroid.
        if ( wavePoints >= 50 && RandomGenerator.get().nextInt( 20 ) == 0 )
        {
            wavePoints -= 50;

            // Make it a bonus asteroid.
            if ( RandomGenerator.get().nextInt( 10 ) == 0 )
            {
                Game.getInstance().asteroidManager().add(
                        new BonusAsteroid( x, y, RandomGenerator.nextMidpointDouble() * x * -.002, RandomGenerator.nextMidpointDouble() * y * -.002,
                                           RandomGenerator.get().nextInt( 60 ) + 40, 15 ), true );
            }
            else
            {
                Game.getInstance().asteroidManager().add(
                        new Asteroid( x, y, RandomGenerator.nextMidpointDouble() * x * -.002, RandomGenerator.nextMidpointDouble() * y * -.002,
                                      RandomGenerator.get().nextInt( 70 ) + 10, 15 ), true );

            }
        }

        // Spawn an alien.
        if ( wavePoints >= 100 && RandomGenerator.get().nextInt( 60 ) == 0 )
        {
            wavePoints -= 100;
            Alien a = new Alien( x, y, RandomGenerator.nextMidpointDouble() * x * -.002, RandomGenerator.nextMidpointDouble() * y * -.002 );
            Game.getInstance().gameObjects.add( a );
            Game.getInstance().shootingObjects.add( a );
        }

    }

    public void draw( Graphics g )
    {
        Graphics2D g2d = (Graphics2D) g;
        String text = "";
        int x = AsteroidsFrame.frame().getPanel().getWidth() - 10;
        int y = AsteroidsFrame.frame().getPanel().getHeight() - 25;

        // Draw the counter.
        g2d.setColor( Color.lightGray );
        g2d.setFont( new Font( "Tahoma", Font.BOLD, 16 ) );
        text = "" + currentWave;
        x -= (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth();
        g2d.drawString( text, x, y );

        // Draw the "wave" string.
        g2d.setFont( new Font( "Tahoma", Font.ITALIC, 12 ) );
        text = "wave";
        x -= (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() + 8;
        g2d.drawString( text, x, y );

        // Draw the progress box.
        x = AsteroidsFrame.frame().getPanel().getWidth() - 110;
        y += 10;
        g.setColor( Color.darkGray );
        g.drawRect( x, y, 100, 10 );
        int width = (int) ( 100 * ( 1 - (double) wavePoints / getWavePoints( currentWave ) ) );
        g.setColor( Color.lightGray );
        g.fillRect( x, y, width, 10 );
    }

    int getWavePoints( int wave )
    {
        return wave * 300;
    }

    public void flatten( DataOutputStream stream ) throws IOException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void optionsKey()
    {
        try
        {
            int newWave = Integer.parseInt( JOptionPane.showInputDialog( null, "Enter the wave to start.", currentWave ) );
            Game.getInstance().asteroidManager().clear();
            Game.getInstance().gameObjects.clear();
            Game.getInstance().shootingObjects = new LinkedList<ShootingObject>( Game.getInstance().players );
            currentWave = newWave;
            wavePoints = getWavePoints( currentWave );
        }
        catch ( NumberFormatException e )
        {
            // Do nothing with incorrect or cancelled input.
            Running.log( "Invalid warp command.", 800 );
        }
    }
}
